package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import trik.testsys.webapp.backoffice.data.entity.Token
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX

/**
 * @author Roman Shishkin
 * @since %CURRENT_VERSION%
 */
@Entity
@Table(name = "${TABLE_PREFIX}token",  uniqueConstraints = [
    UniqueConstraint(name = "${TABLE_PREFIX}uc_token_type_value", columnNames = ["type", "value"])
])
class RegToken() : Token(Type.REGISTRATION) {

    @OneToOne(mappedBy = "adminRegToken", cascade = [CascadeType.PERSIST], orphanRemoval = true)
    var viewer: User? = null
}