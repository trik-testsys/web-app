package trik.testsys.core.view.user

import trik.testsys.core.entity.user.UserEntity
import trik.testsys.core.view.AbstractView

abstract class AbstractUserView<E : UserEntity>(entity: E) : AbstractView<E>(entity) {

    val name: String = entity.name

    val accessToken = entity.accessToken
}