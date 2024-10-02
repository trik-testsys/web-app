package trik.testsys.core.utils.l10n.localizer

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import trik.testsys.core.utils.l10n.L10nCode
import java.io.File

/**
 * Default implementation of localizer.
 *
 * @author Roman Shishkin
 * @since 2.0.0
 */
@Service
class DefaultLocalizer(@Value("\${path.l10n}") l10nDirPath: String) : Localizer {

    private var l10nDir: File = File(l10nDirPath)

    private lateinit var l10nMap: Map<String, String>

    init {
        val map = mutableMapOf<String, String>()

        if (l10nDir.exists() && l10nDir.isDirectory) {
            l10nDir.listFiles()?.map {
                val objectMapper = ObjectMapper()
                val jsonNode = objectMapper.readTree(it.reader())

                jsonNode.fields().forEach { field -> map[field.key] = field.value.asText() }
            }
        }

        l10nMap = map.toSortedMap()
    }

    override fun localize(l10nCode: L10nCode): String {
        val code = l10nCode.code
        val l10n = l10nMap[code] ?: return code

        return l10n
    }
}