package trik.testsys.webclient.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import trik.testsys.webclient.entity.impl.Label

@Repository
interface LabelRepository : CrudRepository<Label, Long> {

    fun findLabelByName(name: String): Label?
}