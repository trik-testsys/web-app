package trik.testsys.webclient.entity.impl

import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
import trik.testsys.core.utils.enums.Enum
import trik.testsys.core.utils.enums.converter.AbstractEnumConverter
import trik.testsys.webclient.entity.AbstractNotedEntity
import trik.testsys.webclient.entity.user.impl.Developer
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.*


@Entity
@Table(name = "${TABLE_PREFIX}_CONTEST")
class Contest(
    name: String,

    @Column(nullable = false, unique = false, updatable = true)
    var startDate: LocalDateTime,

    @Column(nullable = false, unique = false, updatable = true)
    var endDate: LocalDateTime,

    @Column(nullable = false, unique = false, updatable = true)
    var duration: LocalTime,
) : AbstractNotedEntity(name) {

    @ManyToOne
    @JoinColumn(
        nullable = false, unique = false, updatable = true,
        name = "developer_id", referencedColumnName = "id"
    )
    lateinit var developer: Developer

    @Column(nullable = false, unique = false, updatable = true)
    var visibility: Visibility = Visibility.PRIVATE

    fun switchVisibility() {
        visibility = visibility.opposite()
    }

    fun isPublic() = visibility == Visibility.PUBLIC

    enum class Visibility(override val dbkey: String) : Enum {

        PUBLIC("PLC"),
        PRIVATE("PRV");

        fun opposite() = when (this) {
            PUBLIC -> PRIVATE
            PRIVATE -> PUBLIC
        }

        @Converter(autoApply = true)
        class VisibilityConverter : AbstractEnumConverter<Visibility>()
    }
}