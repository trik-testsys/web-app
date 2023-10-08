package trik.testsys.webclient.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.GradientPaint
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.random.Random

/**
 * @since 1.1.0
 * @author Roman Shishkin
 */
@Component
class AvatarGenerator @Autowired constructor(
    @Value("\${app.avatar.path}")
    val avatarPath: String
) {

    fun generatePrimitiveAvatar(userId: Long) {
        val bufferedImage = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
        val graphics = bufferedImage.createGraphics()

        val gradientPaint = GradientPaint(0f, 0f, Color.BLUE, WIDTH.toFloat(), HEIGHT.toFloat(), Color.CYAN)
        graphics.paint = gradientPaint
        graphics.fillRect(0, 0, WIDTH, HEIGHT)

        for (x in 0 until WIDTH) {
            for (y in 0 until HEIGHT) {
                val randomColor = Color(
                    (Math.random() * 256).toInt(),
                    (Math.random() * 256).toInt(),
                    (Math.random() * 256).toInt()
                )
                bufferedImage.setRGB(x, y, randomColor.rgb)
            }
        }

        try {
            val outputFile = File("$avatarPath/$userId.png")
            ImageIO.write(bufferedImage, "png", outputFile)
            println("Image saved as ${outputFile.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            graphics.dispose()
        }

    }

    fun generateShapedAvatar(userId: Long) {
        val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)

        // Get the image's graphics context
        val g2d = image.createGraphics()

        // Create a gradient background
        val gradientPaint = GradientPaint(
            0f, 0f, Color.CYAN,
            WIDTH.toFloat(), HEIGHT.toFloat(), Color.BLUE
        )
        g2d.paint = gradientPaint
        g2d.fillRect(0, 0, WIDTH, HEIGHT)

        // Draw random shapes on the background
        val numShapes = 50
        for (i in 0 until numShapes) {
            val shapeType = Random.nextInt(3) // 0: Rectangle, 1: Ellipse, 2: Line

            val shapeColor = Color(
                Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)
            )
            g2d.color = shapeColor

            val x1 = Random.nextInt(WIDTH)
            val y1 = Random.nextInt(HEIGHT)
            val x2 = Random.nextInt(WIDTH)
            val y2 = Random.nextInt(HEIGHT)

            when (shapeType) {
                0 -> g2d.fillRect(x1, y1, x2, y2) // Rectangle
                1 -> g2d.fillOval(x1, y1, x2, y2) // Ellipse
                2 -> g2d.drawLine(x1, y1, x2, y2) // Line
            }
        }

        val outputFile = File("$avatarPath/$userId.png")
        ImageIO.write(image, "png", outputFile)

        println("Beautiful image saved to ${outputFile.absolutePath}")

        g2d.dispose()
    }

    enum class UserType {
        DEVELOPER,
        VIEWER,
        ADMIN,
        STUDENT
    }

    companion object {
        private const val WIDTH = 20
        private const val HEIGHT = 20
    }
}