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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "namespace_uid", nullable = false)
    var namespace: Namespace,

    @OneToMany(mappedBy = "image")
    var tags: MutableSet<ImageTag> = mutableSetOf()
) : BaseEntity<Int?>() {

    override fun extractId() = id
}