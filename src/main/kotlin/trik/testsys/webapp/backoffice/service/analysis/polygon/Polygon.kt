package trik.testsys.webapp.backoffice.service.analysis.polygon

sealed interface PolygonElement

//region World
// TODO: Add missed components
data class PolygonWorld(
    val regions: List<Region>
): PolygonElement

data class Region(
    val identifier: String
): PolygonElement
//endregion

//region Constraints
data class PolygonConstraints(
    val constraints: List<PolygonConstraint>
): PolygonElement

sealed interface PolygonConstraint: PolygonElement

data class TimeLimitConstraint(
    val milliseconds: Int
): PolygonConstraint

data class InitConstraint(
    val triggers: List<PolygonAtomicTrigger>,
): PolygonConstraint

data class PlainConstraint(
    val checkOnce: Boolean,
    val failMessage: String,
    val condition: PolygonCondition
): PolygonConstraint

data class EventConstraint(
    val dropsOnFire: Boolean,
    val identifier: String?,
    val settedUpInitially: Boolean,
    val condition: PolygonCondition,
    val trigger: PolygonTrigger,
): PolygonConstraint
//endregion

//region Conditions
sealed interface Condition: PolygonElement
sealed interface AtomicCondition: Condition

sealed interface ConditionGlue {
    object GlueAnd: ConditionGlue
    object GlueOr: ConditionGlue
}

data class PolygonCondition(
    val glue: ConditionGlue,
    val conditions: List<Condition>
): Condition

sealed interface BinaryConditionKind {
    object Equals: BinaryConditionKind
    object NotEqual: BinaryConditionKind
    object Greater: BinaryConditionKind
    object NotGreater: BinaryConditionKind
    object Less: BinaryConditionKind
    object NotLess: BinaryConditionKind
}

data class BinaryCondition(
    val left: PolygonExpression,
    val right: PolygonExpression,
    val kind: BinaryConditionKind
): AtomicCondition

object TrueCondition: AtomicCondition

sealed interface ObjectPoints {
    object Center: ObjectPoints
    object Any: ObjectPoints
    object All: ObjectPoints
}

data class InsideCondition(
    val objectId: String,
    val regionId: String,
    val points: ObjectPoints
): AtomicCondition

data class SettedUpCondition(
    val eventId: String
): AtomicCondition

data class DroppedCondition(
    val eventId: String
): AtomicCondition

data class TimerCondition(
    val timeout: Int,
    val forceDropOnTimeout: Boolean
): AtomicCondition

data class NotCondition(
    val condition: Condition
): Condition

//endregion

//region Triggers
data class PolygonTrigger(
    val atomicTriggers: List<PolygonAtomicTrigger>
): PolygonElement

sealed interface PolygonAtomicTrigger: PolygonElement

data class FailTrigger(
    val failMessage: String
): PolygonAtomicTrigger

data class SuccessTrigger(
    val deferred: Boolean
): PolygonAtomicTrigger

data class SetterTrigger(
    val accessString: String,
    val value: PolygonExpression
): PolygonAtomicTrigger

data class SetUpTrigger(
    val eventId: String
): PolygonAtomicTrigger

data class DropTrigger(
    val eventId: String
): PolygonAtomicTrigger

data class MessageTrigger(
    val message: String,
    val replacements: List<Pair<String, PolygonExpression>>
): PolygonAtomicTrigger

data class LogTrigger(
    val message: String,
    val replacements: List<Pair<String, PolygonExpression>>
): PolygonAtomicTrigger
//endregion

//region Expressions
sealed interface PolygonExpression: PolygonElement

data class VariableValueExpression(
    val accessString: String
): PolygonExpression

data class ObjectStateExpression(
    val accessString: String
): PolygonExpression

data class TypeOfExpression(
    val accessString: String
): PolygonExpression

sealed interface ConstExpression: PolygonExpression {
    data class IntExpression(val value: Int): ConstExpression
    data class DoubleExpression(val value: Double): ConstExpression
    data class BooleanExpression(val value: Boolean): ConstExpression
    data class StringExpression(val value: String): ConstExpression
}

sealed interface UnaryExpressionKind {
    object Minus: UnaryExpressionKind
    object Abs: UnaryExpressionKind
    object BoundingRect: UnaryExpressionKind
}

data class UnaryExpression(
    val expr: PolygonExpression,
    val kind: UnaryExpressionKind
): PolygonExpression

sealed interface BinaryExpressionKind {
    object Sum: BinaryExpressionKind
    object Diff: BinaryExpressionKind
    object Min: BinaryExpressionKind
    object Max: BinaryExpressionKind
    object Mul: BinaryExpressionKind
    object Distance: BinaryExpressionKind
}

data class BinaryExpression(
    val left: PolygonExpression,
    val right: PolygonExpression,
    val kind: BinaryExpressionKind
): PolygonExpression
//endregion

// TODO: Add missed components
data class Polygon(
    val world: PolygonWorld,
    val constraints: PolygonConstraints
)