package trik.testsys.webapp.backoffice.data.entity

import trik.testsys.webapp.backoffice.data.entity.impl.UserGroup

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
interface Sharable {

    val userGroups: MutableSet<UserGroup>
}