package mk.ukim.finki.dnick.hosting.model.entity

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type
import java.util.*

@Entity
@Table(name = "image")
class Image(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "name", nullable = false, length = 64)
    var name: String,

    @Column(name = "version", nullable = false, length = 64)
    var version: String,

    @Column(name = "hash", nullable = false)
    var hash: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "namespace_uid", nullable = false)
    var namespace: Namespace,

    @ManyToOne
    @JoinColumn(name = "base_ref_id", nullable = false)
    var base: BaseImageRef,

    @Type(PostgreSQLHStoreType::class)
    @Column(name = "arguments")
    var arguments: Map<String, String> = mutableMapOf()
) : BaseEntity<Int?>() {

    override fun extractId() = id
}