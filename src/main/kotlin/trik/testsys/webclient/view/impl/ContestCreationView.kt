package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.impl.Contest
import trik.testsys.webclient.entity.user.impl.Developer

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
data class ContestCreationView(
    val name: String,
    val additionalInfo: String,
    val note: String
) {

    fun toEntity(developer: Developer) = Contest(
        name
    ).also {
        it.additionalInfo = additionalInfo
        it.note = note
        it.developer = developer
    }

    companion object {

        fun empty() = ContestCreationView("", "", "")
    }
}