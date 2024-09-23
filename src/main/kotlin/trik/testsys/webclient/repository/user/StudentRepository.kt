package trik.testsys.webclient.repository.user

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.core.utils.marker.TrikRepository
import trik.testsys.webclient.entity.user.impl.Student

/**
 * @author Roman Shishkin
 * @since 1.0.0
 */
@Repository
interface StudentRepository : UserRepository<Student>, TrikRepository {

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
                "WHERE REGEXP_LIKE(wu.username, :prefixRegex) and students.group_id = :groupId " +
                "ORDER BY number DESC LIMIT 1"
    )
    fun findMaxNumberWithSameNamePrefix(
        @Param("prefixRegex") prefixRegex: String,
        @Param("groupId") groupId: Long
    ): Long?
}