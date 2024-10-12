package trik.testsys.webclient.view.impl

import trik.testsys.webclient.entity.user.impl.Admin
import trik.testsys.webclient.util.atTimeZone
import trik.testsys.webclient.util.format


data class AdminViewerView(
    val id: Long?,
    val name: String,
    val creationDate: String?,
    val additionalInfo: String
) {

    companion object {

        fun Admin.toViewerView(timeZoneId: String?) = AdminViewerView(
            id = this.id,
            name = this.name,
            creationDate = this.creationDate?.atTimeZone(timeZoneId)?.format(),
            additionalInfo = this.additionalInfo
        )
    }
}