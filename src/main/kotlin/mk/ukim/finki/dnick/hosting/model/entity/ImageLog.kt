package mk.ukim.finki.dnick.hosting.model.entity

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.Instant
import java.util.*

@Entity
@Table(name = "image_log")
class ImageLog(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "data", nullable = false, length = 64)
    var data: String,

    @OneToOne
    @JoinColumn(name = "image_id", nullable = false)
    var image: ImageTag,
) : BaseEntity<Int?>() {

    override fun extractId() = id
}