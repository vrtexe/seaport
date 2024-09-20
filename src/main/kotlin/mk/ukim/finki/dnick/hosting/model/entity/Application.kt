package mk.ukim.finki.dnick.hosting.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "application")
class Application(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "name", nullable = false, length = 64)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "namespace_uid", nullable = false)
    var namespace: Namespace,

    @OneToMany(mappedBy = "application")
    var deployments: MutableSet<Deployment> = mutableSetOf(),

    @OneToMany(mappedBy = "application")
    var ingresses: MutableSet<Ingress> = mutableSetOf(),

    @OneToMany(mappedBy = "application")
    var services: MutableSet<Service> = mutableSetOf(),
) : BaseEntity<Int?>() {

    override fun extractId(): Int? = id
}

