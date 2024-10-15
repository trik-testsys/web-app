package trik.testsys.webclient.service.impl

import org.springframework.stereotype.Service
import trik.testsys.webclient.service.UserAgentParser
import ua_parser.Client
import ua_parser.Parser
import java.nio.charset.Charset

@Service
class UserAgentParserImpl : UserAgentParser {

    private val parser = Parser()

    override fun getClientInfo(userAgent: String): Client = parser.parse(userAgent)

    override fun getCharset(userAgent: String): Charset {
        try {
            val clientInfo = getClientInfo(userAgent)
            val osFamily = OsFamily.getOsFamily(clientInfo.os.family)
            val charset = osFamily.getCharsetByVersion(clientInfo.os.major)

            return charset
        } catch (e: Exception) {
            return UTF_8
        }
    }

    sealed interface OsFamily {

        val name: String

        fun getCharsetByVersion(version: String?): Charset = UTF_8

        object WINDOWS : OsFamily {

            override val name = "Windows NT"

            override fun getCharsetByVersion(version: String?): Charset {
                version ?: super.getCharsetByVersion(version)
                val charset = charsetsByVersion[version] ?: WINDOWS_1251

                return charset
            }

            private val charsetsByVersion = mapOf(
                "5.1" to WINDOWS_1251,
                "6.0" to WINDOWS_1251,
                "6.1" to WINDOWS_1251,
                "6.2" to WINDOWS_1251,
                "6.3" to WINDOWS_1251,
                "10.0" to UTF_8
            )
        }

        object MacOs : OsFamily {

            override val name = "Mac OS X"
        }

        object Linux : OsFamily {

            override val name = "Linux"
        }

        object Android : OsFamily {

            override val name = "Android"
        }

        object IOS : OsFamily {

            override val name = "iPhone OS"
        }

        object IpadOS : OsFamily {

            override val name = "iPad; CPU OS"
        }

        object Unknown : OsFamily {

            override val name = "Unknown"
        }

        companion object {

            val osFamiliesByName = mapOf(
                WINDOWS.name to WINDOWS,
                MacOs.name to MacOs,
                Linux.name to Linux,
                Android.name to Android,
                IOS.name to IOS,
                IpadOS.name to IpadOS,
                Unknown.name to Unknown
            )

            fun getOsFamily(osFamily: String?): OsFamily = osFamiliesByName[osFamily] ?: Unknown
        }
    }

    companion object {

        val WINDOWS_1251 = Charset.forName("windows-1251")
        val WINDOWS_1252 = Charset.forName("windows-1252")

        val UTF_8 = Charsets.UTF_8
    }
}