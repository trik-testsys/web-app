package trik.testsys.webclient.repositories

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

import trik.testsys.webclient.entities.Student
import trik.testsys.webclient.entities.WebUser

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Repository
interface StudentRepository : CrudRepository<Student, String> {

    fun findByWebUser(webUser: WebUser): Student?

    /**
     * Returns max number of student with same username prefix.
     *
     * For example, if username prefix is "student" and there are students with usernames "student_1", "student_2", "student_3"
     * then this method will return 3.
     * @author Roman Shishkin
     * @since 1.1.0
     */
    @Query(
        nativeQuery = true,
        value = "SELECT CAST(SUBSTRING_INDEX(wu.username, '_', -1) AS UNSIGNED) as number " +
                "FROM students JOIN web_users wu on wu.id = students.web_user_id " +
                "WHERE REGEXP_LIKE(wu.username, :prefixRegex) " +
                "ORDER BY number DESC LIMIT 1"
    )
    fun findMaxNumberWithSameNamePrefix(@Param("prefixRegex") prefixRegex: String): Long?

    fun findById(id: Long): Student?
}