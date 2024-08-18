package trik.testsys.core.view

import trik.testsys.core.entity.Entity

interface View<T : Entity> {

    val id: Long?

    fun toEntity(): T
}