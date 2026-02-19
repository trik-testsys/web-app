package trik.testsys.webapp.backoffice.data.entity

import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
interface Sharable {

    val userGroups: MutableSet<UserGroup>
}