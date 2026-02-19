package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import trik.testsys.webapp.backoffice.data.entity.Token
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX

/**
 * @author Roman Shishkin
 * @since 3.12.0
 */
@Entity
@Table(name = "${TABLE_PREFIX}token",  uniqueConstraints = [
    UniqueConstraint(name = "${TABLE_PREFIX}uc_token_type_value", columnNames = ["type", "value"])
])
class StudentGroupToken() : Token(Type.STUDENT_GROUP) {

    @OneToOne(mappedBy = "studentGroupToken")
    var studentGroup: StudentGroup? = null
}