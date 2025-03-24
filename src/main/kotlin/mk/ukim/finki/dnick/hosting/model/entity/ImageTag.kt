package mk.ukim.finki.dnick.hosting.model.entity

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.Instant
import java.util.*

@Entity
@Table(name = "image_tag")
class ImageTag(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "version", nullable = false, length = 64)
    var version: String,

    @Column(name = "hash", nullable = false)
    var hash: UUID = UUID.randomUUID(),

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ImageStatus,

    @Type(PostgreSQLHStoreType::class)
    @Column(name = "arguments")
    var arguments: Map<String, String> = mutableMapOf(),

    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    var image: Image,

    @ManyToOne
    @JoinColumn(name = "base_ref_id", nullable = false)
    var base: BaseImageRef,

    @OneToOne(mappedBy = "image")
    var log: ImageLog? = null
) : BaseEntity<Int?>() {

    override fun extractId() = id
}

enum class ImageStatus {
    initialized, started, completed, failed
}