package trik.testsys.webclient.model.impl

import trik.testsys.webclient.controller.impl.ViewerController
import trik.testsys.webclient.entity.Label
import trik.testsys.webclient.model.TrikModel
import trik.testsys.webclient.util.exception.TrikException

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
class ViewerModel private constructor(
    val accessToken: String,
    val username: String?,
    val correctNames: Set<String>?,
    val incorrectNames: Set<String>?,
    val labels: Set<Label>?,
    val table: List<ViewerController.TableRow>?
) : TrikModel {

    class Builder internal constructor() {

        private var accessToken: String? = null
        private var username: String? = null
        private var correctNames: Set<String>? = null
        private var incorrectNames: Set<String>? = null
        private var labels: Set<Label>? = null
        private var table: List<ViewerController.TableRow>? = null

        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }

        fun username(username: String) = apply { this.username = username }

        fun correctNames(correctNames: Set<String>) = apply { this.correctNames = correctNames }

        fun incorrectNames(incorrectNames: Set<String>) = apply { this.incorrectNames = incorrectNames }

        fun labels(labels: Set<Label>) = apply { this.labels = labels }

        fun table(table: List<ViewerController.TableRow>) = apply { this.table = table }

        fun build() = ViewerModel(
            accessToken ?: throw TrikException(String.format(PARAMETER_ERROR, ViewerModel::accessToken.name)),
            username,
            correctNames,
            incorrectNames,
            labels,
            table
        )
    }

    override fun asMap(): Map<String, Any?> {
        val argsMap = mutableMapOf<String, Any?>()

        argsMap[this::accessToken.name] = accessToken
        argsMap[this::username.name] = username
        argsMap[this::correctNames.name] = correctNames
        argsMap[this::incorrectNames.name] = incorrectNames
        argsMap[this::labels.name] = labels
        argsMap[this::table.name] = table

        return argsMap
    }

    companion object {
        private const val PARAMETER_ERROR = "Parameter '%s' must be specified."
    }
}