package trik.testsys.webclient.service

import java.io.File

/**
 * Interface for managing files.
 * @author Roman Shishkin
 * @since 2.0.0
 */
interface FileManager {

    fun getTaskFiles(taskName: String): Collection<File>

    fun getSolutionFile(solutionName: String): File
}