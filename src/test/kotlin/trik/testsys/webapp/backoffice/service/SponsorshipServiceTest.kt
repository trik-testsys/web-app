package trik.testsys.webapp.backoffice.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class SponsorshipServiceTest {

    @Test
    fun `returns empty list when directory does not exist`() {
        val service = SponsorshipService("/nonexistent/path/")
        assertTrue(service.getImageNames().isEmpty())
    }

    @Test
    fun `returns empty list when directory is empty`(@TempDir dir: Path) {
        val service = SponsorshipService(dir.toString())
        assertTrue(service.getImageNames().isEmpty())
    }

    @Test
    fun `returns image filenames sorted`(@TempDir dir: Path) {
        dir.resolve("b_logo.png").toFile().createNewFile()
        dir.resolve("a_logo.svg").toFile().createNewFile()
        dir.resolve("readme.txt").toFile().createNewFile()

        val service = SponsorshipService(dir.toString())
        assertEquals(listOf("a_logo.svg", "b_logo.png"), service.getImageNames())
    }

    @Test
    fun `returns only image files by extension`(@TempDir dir: Path) {
        listOf("a.png", "b.jpg", "c.jpeg", "d.svg", "e.gif", "f.webp", "g.txt", "h.pdf")
            .forEach { dir.resolve(it).toFile().createNewFile() }

        val service = SponsorshipService(dir.toString())
        assertEquals(listOf("a.png", "b.jpg", "c.jpeg", "d.svg", "e.gif", "f.webp"), service.getImageNames())
    }

    @Test
    fun `returns image files with uppercase extensions`(@TempDir dir: Path) {
        dir.resolve("logo.PNG").toFile().createNewFile()
        dir.resolve("banner.JPG").toFile().createNewFile()
        dir.resolve("icon.SVG").toFile().createNewFile()

        val service = SponsorshipService(dir.toString())
        assertEquals(listOf("banner.JPG", "icon.SVG", "logo.PNG"), service.getImageNames())
    }
}
