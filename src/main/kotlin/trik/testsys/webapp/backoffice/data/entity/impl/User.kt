package trik.testsys.webapp.backoffice.data.entity.impl

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.Converter
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Transient
import trik.testsys.webapp.core.data.entity.AbstractEntity
import trik.testsys.webapp.core.data.entity.AbstractEntity.Companion.TABLE_PREFIX
import trik.testsys.webapp.core.utils.enums.PersistableEnum
import trik.testsys.webapp.core.utils.enums.converter.AbstractPersistableEnumConverter
import java.time.Instant

@Entity
@Table(name = "${TABLE_PREFIX}user")
class User() : AbstractEntity() {

    @Column(name = "name", nullable = false)
    var name: String? = null

    @OneToOne(fetch = FetchType.EAGER, optional = false, orphanRemoval = true)
    @JoinColumn(name = "access_token_id", nullable = false, unique = true)
    var accessToken: AccessToken? = null

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null

    @Transient
    var hasLoggedIn = lastLoginAt != null

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "${TABLE_PREFIX}privilege", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "${TABLE_PREFIX}privilege")
    @Enumerated(EnumType.STRING)
    val privileges: MutableSet<Privilege> = mutableSetOf()

    @OneToMany(mappedBy = "owner")
    var ownedGroups: MutableSet<UserGroup> = mutableSetOf()

    @ManyToMany(mappedBy = "members")
    var memberedGroups: MutableSet<UserGroup> = mutableSetOf()

    @OneToMany(mappedBy = "owner")
    var ownedStudentGroups: MutableSet<StudentGroup> = mutableSetOf()

    @ManyToMany(mappedBy = "members")
    var memberedStudentGroups: MutableSet<StudentGroup> = mutableSetOf()

    @OneToMany(mappedBy = "viewer")
    var managedAdmins: MutableSet<User> = mutableSetOf()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_id")
    var viewer: User? = null

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_reg_token_id", unique = true)
    var adminRegToken: RegToken? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_user_id")
    var superUser: User? = null

    @OneToMany(mappedBy = "superUser")
    var createdUsers: MutableSet<User> = mutableSetOf()

    @OneToMany(mappedBy = "developer", orphanRemoval = true)
    var contests: MutableSet<Contest> = mutableSetOf()

    @Suppress("unused")
    enum class Privilege(override val dbKey: String) : PersistableEnum {

        ADMIN("ADM"),
        DEVELOPER("DEV"),
        JUDGE("JDG"),
        STUDENT("ST"),
        SUPER_USER("SU"),
        VIEWER("VWR"),
        GROUP_ADMIN("GA");

        companion object {

            @Converter(autoApply = true)
            class JpaConverter : AbstractPersistableEnumConverter<Privilege>()
        }
    }

    companion object {

        const val ACCESS_TOKEN = "accessToken"
        const val PRIVILEGES = "privileges"
    }
}