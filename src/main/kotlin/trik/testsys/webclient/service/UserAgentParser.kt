package trik.testsys.webclient.service

import ua_parser.Client
import java.nio.charset.Charset

interface UserAgentParser {

    fun getClientInfo(userAgent: String): Client

    fun getCharset(userAgent: String): Charset
}