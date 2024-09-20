package mk.ukim.finki.dnick.hosting.model.entity

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type

@Entity
@Table(name = "external_service")
class ExternalService(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "name", nullable = false, length = 64)
    var name: String,

    @Column(name = "port", nullable = false)
    var port: Int,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "namespace_uid", nullable = false)
    var namespace: Namespace,

    @Column(name = "config", columnDefinition = "hstore not null")
    @Type(PostgreSQLHStoreType::class)
    var config: MutableMap<String, String> = mutableMapOf()
) : BaseEntity<Int?>() {

    override fun extractId() = id
}