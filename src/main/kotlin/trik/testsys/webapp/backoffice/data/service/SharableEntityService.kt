package trik.testsys.webapp.backoffice.data.service

import trik.testsys.webapp.backoffice.data.entity.Sharable
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.service.EntityService

interface SharableEntityService<E> : EntityService<E>
        where E : AbstractEntity,
              E : Sharable {

    fun a()

}