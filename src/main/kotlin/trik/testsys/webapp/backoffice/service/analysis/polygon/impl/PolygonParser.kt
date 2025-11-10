package trik.testsys.webapp.backoffice.service.analysis.polygon.impl

import org.w3c.dom.Element
import trik.testsys.webapp.backoffice.service.analysis.polygon.BinaryCondition
import trik.testsys.webapp.backoffice.service.analysis.polygon.BinaryConditionKind
import trik.testsys.webapp.backoffice.service.analysis.polygon.BinaryExpression
import trik.testsys.webapp.backoffice.service.analysis.polygon.BinaryExpressionKind
import trik.testsys.webapp.backoffice.service.analysis.polygon.Condition
import trik.testsys.webapp.backoffice.service.analysis.polygon.ConditionGlue
import trik.testsys.webapp.backoffice.service.analysis.polygon.ConstExpression
import trik.testsys.webapp.backoffice.service.analysis.polygon.DropTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.DroppedCondition
import trik.testsys.webapp.backoffice.service.analysis.polygon.EventConstraint
import trik.testsys.webapp.backoffice.service.analysis.polygon.FailTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.InitConstraint
import trik.testsys.webapp.backoffice.service.analysis.polygon.InsideCondition
import trik.testsys.webapp.backoffice.service.analysis.polygon.LogTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.MessageTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.NotCondition
import trik.testsys.webapp.backoffice.service.analysis.polygon.ObjectPoints
import trik.testsys.webapp.backoffice.service.analysis.polygon.ObjectStateExpression
import trik.testsys.webapp.backoffice.service.analysis.polygon.PlainConstraint
import trik.testsys.webapp.backoffice.service.analysis.polygon.Polygon
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonAtomicTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonCondition
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonConstraint
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonConstraints
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonElement
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonExpression
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.PolygonWorld
import trik.testsys.webapp.backoffice.service.analysis.polygon.Region
import trik.testsys.webapp.backoffice.service.analysis.polygon.SetUpTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.SettedUpCondition
import trik.testsys.webapp.backoffice.service.analysis.polygon.SetterTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.SuccessTrigger
import trik.testsys.webapp.backoffice.service.analysis.polygon.TimeLimitConstraint
import trik.testsys.webapp.backoffice.service.analysis.polygon.TimerCondition
import trik.testsys.webapp.backoffice.service.analysis.polygon.TrueCondition
import trik.testsys.webapp.backoffice.service.analysis.polygon.TypeOfExpression
import trik.testsys.webapp.backoffice.service.analysis.polygon.UnaryExpression
import trik.testsys.webapp.backoffice.service.analysis.polygon.UnaryExpressionKind
import trik.testsys.webapp.backoffice.service.analysis.polygon.VariableValueExpression
import java.util.IdentityHashMap
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.typeOf

/**
 * @author Viktor Karasev
 * @since %CURRENT_VERSION%
 */
class PolygonParser {


    //region Type safe xml
    private data class XmlElement(
        val path: String,
        val tagName: String,
        val attributes: Map<String, String>,
        val children: List<XmlElement>
    )

    private fun Element.headerToString(): String {
        val result = StringBuilder()
        result.append("<${tagName}")
        val attributesCount = this.attributes.length
        for (i in 0 until attributesCount) {
            val attribute = this.attributes.item(i)
            result.append(" ${attribute.nodeName}=${attribute.nodeValue}")
        }
        result.append(">")
        return result.toString()
    }

    private fun Element.toTypeSafeElement(currentPath: String = ""): XmlElement {
        val path = if (currentPath.isEmpty()) {
            "/${this.headerToString()}"
        } else {
            "$currentPath/${this.headerToString()}"
        }

        val tagName = this.tagName

        val attributes = hashMapOf<String, String>()
        val attributesCount = this.attributes.length
        for (i in 0 until attributesCount) {
            val attribute = this.attributes.item(i)
            attributes[attribute.nodeName] = attribute.nodeValue
        }

        val children = mutableListOf<XmlElement>()

        val childrenCount = this.childNodes.length
        for (i in 0 until childrenCount) {
            val child = this.childNodes.item(i)
            if (child is Element) {
                children.add(child.toTypeSafeElement(path))
            }
        }

        return XmlElement(path, tagName, attributes, children)
    }
    //endregion

    //region Locations
    private val locations = IdentityHashMap<PolygonElement, String>()

    private fun <T: PolygonElement> recordLocation(e: XmlElement, value: T): T {
        locations[value] = e.path
        return value
    }
    //endregion

    //region Helpers
    class PolygonParsingException(
        message: String
    ): Exception(message)

    private fun throwInvalidElement(e: XmlElement, message: String): Nothing {
        throw PolygonParsingException("Error while parsing ${e.path}: $message")
    }

    private fun assertChildrenCount(e: XmlElement, expected: Int) {
        val actual = e.children.size
        if (actual != expected) {
            throwInvalidElement(e,
                "${e.tagName} tag must have exactly $expected child tag(s), actual: $actual"
            )
        }
    }

    private fun XmlElement.getOptionalChild(tagName: String): XmlElement? {
        return this.children.find { it.tagName == tagName }
    }

    private fun XmlElement.getRequiredChild(tagName: String): XmlElement {
        return this.getOptionalChild(tagName) ?: throwInvalidElement(
            this, "Missed required child tag: $tagName"
        )
    }

    private inline fun <reified T> XmlElement.getRequiredAttribute(attributeName: String): T {
        return this.getOptionalAttribute<T>(attributeName) ?: throwInvalidElement(
            this, "Missed required attribute: $attributeName"
        )
    }

    private inline fun <reified T> XmlElement.getOptionalAttribute(attributeName: String): T? {
        val value = this.attributes[attributeName] ?: return null
        val expectedType = typeOf<T>()

        return when (expectedType) {
            typeOf<String>() -> value
            typeOf<Int>() -> {
                try {
                    value.toInt()
                } catch (_: NumberFormatException) {
                    throwInvalidElement(this, "expected $attributeName is valid int, actual: $value")
                }
            }
            typeOf<Double>() -> {
                try {
                    value.toDouble()
                } catch (_: NumberFormatException) {
                    throwInvalidElement(this, "expected $attributeName is valid double, actual: $value")
                }
            }
            typeOf<Boolean>() -> {
                when(value) {
                    "true" -> true
                    "false" -> false
                    else -> throwInvalidElement(this,
                        "expected $attributeName is true or false, actual: $value"
                    )
                }
            }
            else -> {
                throw Exception("Unsupported attribute type: ${T::class.java.typeName}")
            }
        } as T
    }
    //endregion

    //region Expressions
    private fun parseBool(e: XmlElement): ConstExpression.BooleanExpression {
        val value = e.getRequiredAttribute<Boolean>("value")
        return recordLocation(e,  ConstExpression.BooleanExpression(value))
    }

    private fun parseInt(e: XmlElement): ConstExpression.IntExpression {
        val value = e.getRequiredAttribute<Int>("value")
        return recordLocation(e,  ConstExpression.IntExpression(value))
    }

    private fun parseString(e: XmlElement): ConstExpression.StringExpression {
        val value = e.getRequiredAttribute<String>("value")
        return recordLocation(e,  ConstExpression.StringExpression(value))
    }

    private fun parseDouble(e: XmlElement): ConstExpression.DoubleExpression {
        val value = e.getRequiredAttribute<Double>("value")
        return recordLocation(e,  ConstExpression.DoubleExpression(value))
    }

    private fun parseVariableValue(e: XmlElement): VariableValueExpression {
        val value = e.getRequiredAttribute<String>("name")
        return recordLocation(e, VariableValueExpression(value))
    }

    private fun parseTypeOf(e: XmlElement): TypeOfExpression {
        val value = e.getRequiredAttribute<String>("objectId")
        return recordLocation(e, TypeOfExpression(value))
    }

    private fun parseObjectState(e: XmlElement): ObjectStateExpression {
        val value = e.getRequiredAttribute<String>("object")
        return recordLocation(e, ObjectStateExpression(value))
    }

    private fun parseBinaryExpression(e: XmlElement): BinaryExpression {
        assertChildrenCount(e, 2)

        val operation = e.tagName

        val left = parseExpression(e.children[0])
        val right = parseExpression(e.children[1])

        val kind =
            when(operation) {
                "mul" -> BinaryExpressionKind.Mul
                "sum" -> BinaryExpressionKind.Sum
                "difference" -> BinaryExpressionKind.Diff
                "min" -> BinaryExpressionKind.Min
                "max" -> BinaryExpressionKind.Max
                "distance" -> BinaryExpressionKind.Distance
                else -> throwInvalidElement(e, "Unexpected operation: $operation")
            }

        return recordLocation(e, BinaryExpression(left, right, kind))
    }

    private fun parseUnaryExpression(e: XmlElement): UnaryExpression {
        assertChildrenCount(e, 1)

        val operation = e.tagName

        val expr = parseExpression(e.children[0])

        val kind =
            when(operation) {
                "minus" -> UnaryExpressionKind.Minus
                "abs" -> UnaryExpressionKind.Abs
                "boundingRect" -> UnaryExpressionKind.BoundingRect
                else -> throwInvalidElement(e, "Unexpected operation: $operation")
            }

        return recordLocation(e, UnaryExpression(expr, kind))
    }

    private fun parseExpression(e: XmlElement): PolygonExpression {
        val tag = e.tagName

        return recordLocation(e,  when(tag) {
            "bool" -> parseBool(e)
            "int" -> parseInt(e)
            "string" -> parseString(e)
            "double" -> parseDouble(e)
            "variableValue" -> parseVariableValue(e)
            "typeOf" -> parseTypeOf(e)
            "objectState" -> parseObjectState(e)
            in listOf("minus", "abs", "boundingRect") -> parseUnaryExpression(e)
            in listOf("mul", "sum", "difference", "min", "max", "distance") -> parseBinaryExpression(e)
            else -> throwInvalidElement(e, "unknown value")
        })
    }
    //endregion

    //region Triggers
    private fun parseFailTrigger(e: XmlElement): FailTrigger {
        val value = e.getRequiredAttribute<String>("message")
        return recordLocation(e, FailTrigger(value))
    }

    private fun parseSuccessTrigger(e: XmlElement): SuccessTrigger {
        val value = e.getOptionalAttribute<Boolean>("deferred") ?: false
        return recordLocation(e, SuccessTrigger(value))
    }

    private fun parseSetterTrigger(e: XmlElement): SetterTrigger {
        assertChildrenCount(e, 1)
        val name = e.getRequiredAttribute<String>("name")
        val expr = parseExpression(e.children[0])
        return recordLocation(e, SetterTrigger(name, expr))
    }

    private fun parseSetUpTrigger(e: XmlElement): SetUpTrigger {
        val identifier = e.getRequiredAttribute<String>("id")
        return recordLocation(e, SetUpTrigger(identifier))
    }

    private fun parseDropTrigger(e: XmlElement): DropTrigger {
        val identifier = e.getRequiredAttribute<String>("id")
        return recordLocation(e, DropTrigger(identifier))
    }

    private fun parseReplacements(e: XmlElement): List<Pair<String, PolygonExpression>> {
        return e.children.map {
            if (it.tagName != "replace") {
                throwInvalidElement(it, "invalid tag, expected <replace>")
            }

            val variable = it.getRequiredAttribute<String>("var")

            assertChildrenCount(it, 1)
            val replacement = parseExpression(it.children[0])
            variable to replacement
        }
    }

    private fun parseMessageTrigger(e: XmlElement): MessageTrigger {
        val text = e.getRequiredAttribute<String>("text")
        val replacements = parseReplacements(e)
        return recordLocation(e, MessageTrigger(text, replacements))
    }

    private fun parseLogTrigger(e: XmlElement): LogTrigger {
        val text = e.getRequiredAttribute<String>("text")
        val replacements = parseReplacements(e)
        return recordLocation(e, LogTrigger(text, replacements))
    }

    private fun parseAtomicTrigger(e: XmlElement): PolygonAtomicTrigger {
        val tagName = e.tagName

        return recordLocation(e,  when (tagName) {
            "fail" -> parseFailTrigger(e)
            "message" -> parseMessageTrigger(e)
            "success" -> parseSuccessTrigger(e)
            "setter" -> parseSetterTrigger(e)
            "setUp" -> parseSetUpTrigger(e)
            "drop" -> parseDropTrigger(e)
            "log" -> parseLogTrigger(e)
            "setState" -> throw Exception("TODO: Element not described in document")
            else -> throwInvalidElement(e, "unknown trigger")
        })
    }

    private fun parseSingleTrigger(e: XmlElement): PolygonAtomicTrigger {
        assertChildrenCount(e, 1)
        return recordLocation(e,  parseAtomicTrigger(e.children[0]))
    }

    private fun parseMultipleTriggers(e: XmlElement): List<PolygonAtomicTrigger> {
        return e.children.map { parseAtomicTrigger(it) }
    }

    private fun parseTrigger(e: XmlElement): PolygonTrigger {
        val tagName = e.tagName

        val atomicTriggers =  when (tagName) {
            "trigger" -> listOf(parseSingleTrigger(e))
            "triggers" -> parseMultipleTriggers(e)
            else -> throwInvalidElement(e, "unknown trigger")
        }

        return recordLocation(e, PolygonTrigger(atomicTriggers))
    }
    //endregion

    //region Conditions
    private fun parseCondition(e: XmlElement): Condition {
        val tagName = e.tagName

        return recordLocation(e,  when (tagName) {
            "true" -> TrueCondition
            "not" -> parseNot(e)
            "inside" -> parseInside(e)
            "settedUp" -> parseSettedUp(e)
            "dropped" -> parseDropped(e)
            "timer" -> parseTimer(e)
            "conditions" -> parseMultipleConditions(e)
            "using" -> throw Exception("TODO: Element not described in document")
            in listOf("equals", "notEqual", "greater", "notGreater", "less", "notLess") -> parseBinaryCondition(e)
            else -> throwInvalidElement(e, "Unexpected tag")
        })
    }

    private fun parseNot(e: XmlElement): NotCondition {
        assertChildrenCount(e, 1)
        val condition = parseCondition(e.children[0])
        return recordLocation(e, NotCondition(condition))
    }

    private fun parseBinaryCondition(e: XmlElement): BinaryCondition {
        assertChildrenCount(e, 2)

        val tagName = e.tagName

        val left = parseExpression(e.children[0])
        val right = parseExpression(e.children[1])

        val kind =
            when (tagName) {
                "equals" -> BinaryConditionKind.Equals
                "notEqual" -> BinaryConditionKind.NotEqual
                "greater" -> BinaryConditionKind.Greater
                "notGreater" -> BinaryConditionKind.NotGreater
                "less" -> BinaryConditionKind.Less
                "notLess" -> BinaryConditionKind.NotLess
                else -> throwInvalidElement(e, "Unexpected comparison: $tagName")
            }

        return recordLocation(e, BinaryCondition(left, right, kind))
    }

    private fun parseInside(e: XmlElement): InsideCondition {
        val objectId = e.getRequiredAttribute<String>("objectId")
        val regionId = e.getRequiredAttribute<String>("regionId")
        val stringPoints = e.getOptionalAttribute<String>("objectPoint")

        val point =
            when (stringPoints) {
                "center" -> ObjectPoints.Center
                "any" -> ObjectPoints.Any
                "all" -> ObjectPoints.All
                null -> ObjectPoints.Center
                else -> throwInvalidElement(e, "Expected objetPoint=center|all|any, actual is $stringPoints")
            }

        return recordLocation(e, InsideCondition(objectId, regionId, point))
    }

    private fun parseSettedUp(e: XmlElement): SettedUpCondition {
        val identifier = e.getRequiredAttribute<String>("id")
        return recordLocation(e, SettedUpCondition(identifier))
    }

    private fun parseDropped(e: XmlElement): DroppedCondition {
        val identifier = e.getRequiredAttribute<String>("id")
        return recordLocation(e, DroppedCondition(identifier))
    }

    private fun parseTimer(e: XmlElement): TimerCondition {
        val timeout = e.getRequiredAttribute<Int>("timeout")
        val forceDrop = e.getOptionalAttribute<Boolean>("forceDropOnTimeout") ?: true
        return recordLocation(e, TimerCondition(timeout, forceDrop))
    }

    private fun parseSingleCondition(e: XmlElement): PolygonCondition {
        assertChildrenCount(e, 1)
        val condition = parseCondition(e.children[0])
        return recordLocation(e, PolygonCondition(ConditionGlue.GlueAnd, listOf(condition)))
    }

    private fun parseMultipleConditions(e: XmlElement): PolygonCondition {
        val conditions =  e.children.map { parseCondition(it) }
        val stringGlue = e.getRequiredAttribute<String>("glue")

        val glue =
            when(stringGlue) {
                "and" -> ConditionGlue.GlueAnd
                "or" -> ConditionGlue.GlueOr
                else -> throwInvalidElement(e, "Expected glue=and|or, actual is $stringGlue")
            }

        return recordLocation(e, PolygonCondition(glue, conditions))
    }

    private fun parsePolygonCondition(e: XmlElement): PolygonCondition {
        val tagName = e.tagName

        return recordLocation(e,  when (tagName) {
            "condition" -> parseSingleCondition(e)
            "conditions" -> parseMultipleConditions(e)
            else -> throwInvalidElement(e, "unknown condition")
        })
    }
    //endregion

    //region Constraints
    private fun parseTimeLimit(e: XmlElement): TimeLimitConstraint {
        val value = e.getRequiredAttribute<Int>("value")
        return recordLocation(e, TimeLimitConstraint(value))
    }

    private fun parseInitConstraint(e: XmlElement): InitConstraint {
        val triggers = parseMultipleTriggers(e)
        return recordLocation(e, InitConstraint(triggers))
    }

    private fun parsePlainConstraint(e: XmlElement): PlainConstraint {
        assertChildrenCount(e, 1)
        val failMessage = e.getRequiredAttribute<String>("failMessage")
        val checkOnce = e.getOptionalAttribute<Boolean>("checkOnce") ?: false

        val child = e.children[0]
        val condition =
            when(child.tagName) {
                in listOf("condition", "conditions") -> parsePolygonCondition(child)
                else -> parseSingleCondition(e)
            }
        return recordLocation(e, PlainConstraint(checkOnce, failMessage, condition))
    }

    private fun parseEventConstraint(e: XmlElement): EventConstraint {
        assertChildrenCount(e, 2)
        val identifier = e.getOptionalAttribute<String>("id")
        val dropsOnFire = e.getOptionalAttribute<Boolean>("dropsOnFire") ?: true
        val settedUpInitially = e.getOptionalAttribute<Boolean>("settedUpInitially") ?: false

        val triggerChild =
            e.getOptionalChild("trigger")
                ?: e.getOptionalChild("triggers")
                ?: throwInvalidElement(e, "Event tag must have \"trigger\" or \"triggers\" child tag.")

        val conditionChild =
            e.getOptionalChild("condition")
                ?: e.getOptionalChild("conditions")
                ?: throwInvalidElement(e, "Event tag must have \"condition\" or \"conditions\" child tag.")

        val trigger = parseTrigger(triggerChild)
        val condition = parsePolygonCondition(conditionChild)

        return recordLocation(e, EventConstraint(
            dropsOnFire,
            identifier,
            settedUpInitially,
            condition,
            trigger
        )
        )
    }

    private fun parseConstraint(e: XmlElement): PolygonConstraint {
        return recordLocation(e,  when(e.tagName) {
            "event" -> parseEventConstraint(e)
            "constraint" -> parsePlainConstraint(e)
            "timelimit" -> parseTimeLimit(e)
            in listOf("init", "initialization") -> parseInitConstraint(e)
            else -> throwInvalidElement(e, "Unexpected tag: ${e.tagName}")
        })
    }

    private fun parseConstraints(e: XmlElement): PolygonConstraints {
        val constraints = e.children.map { parseConstraint(it) }
        return recordLocation(e, PolygonConstraints(constraints))
    }
    //endregion

    //region World
    private fun parseRegion(e: XmlElement): Region {
        val identifier = e.getRequiredAttribute<String>("id")
        return recordLocation(e, Region(identifier))
    }

    private fun parseWorld(e: XmlElement): PolygonWorld {
        val regions = e.children
            .filter { it.tagName == "region" }
            .map { parseRegion(it) }
        return recordLocation(e, PolygonWorld(regions))
    }
    //endregion

    private fun parsePolygon(e: XmlElement): Polygon {
        val world = parseWorld(e.getRequiredChild("world"))
        val constraints = parseConstraints(e.getRequiredChild("constraints"))
        return Polygon(world, constraints)
    }

    fun parse(text: String): Polygon {
        locations.clear()
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = builder.parse(text.byteInputStream())
        val xmlElement = document.documentElement.toTypeSafeElement()
        return parsePolygon(xmlElement)
    }

    fun getLocations(): IdentityHashMap<PolygonElement, String> {
        return locations
    }
}