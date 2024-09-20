package mk.ukim.finki.dnick.hosting.model.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "namespace")
class Namespace(
    @Id
    @Column(name = "uid", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, length = 64)
    var name: String,

    @OneToMany(mappedBy = "namespace")
    var applications: MutableSet<Application> = mutableSetOf(),

    @OneToMany(mappedBy = "namespace")
    var externalService: MutableSet<ExternalService> = mutableSetOf(),

    @OneToMany(mappedBy = "namespace")
    var images: MutableSet<Image> = mutableSetOf()
) : BaseEntity<UUID?>() {

    override fun extractId() = id
}