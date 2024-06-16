package trik.testsys.core.entity.user

import trik.testsys.core.entity.AbstractEntity
import javax.persistence.*
import kotlin.properties.Delegates

@MappedSuperclass
abstract class AbstractUser : User, AbstractEntity() {

    @Column(
        nullable = false, unique = true, length = NAME_MAX_LEN,
        columnDefinition = "VARCHAR($NAME_MAX_LEN) DEFAULT '$DEFAULT_NAME'"
    ) override var name: String = DEFAULT_NAME

    @Column(
        nullable = false, length = ACCESS_TOKEN_MAX_LEN,
        columnDefinition = "VARCHAR($ACCESS_TOKEN_MAX_LEN) DEFAULT '$DEFAULT_ACCESS_TOKEN'"
    ) override var accessToken = DEFAULT_ACCESS_TOKEN

    companion object {

        private const val DEFAULT_NAME = ""
        private const val DEFAULT_ACCESS_TOKEN = ""

        private const val NAME_MAX_LEN = 128
        private const val ACCESS_TOKEN_MAX_LEN = 256

        val System = object: AbstractUser() {

            override var name = "System User"

            override var accessToken = "oisjgoisjropgivsop;ipjgvm;fskldgjsldkvmdslkl;snbkdfnbl;knskldfnbls;knfb"
        }
    }
}