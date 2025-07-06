package mk.ukim.finki.dnick.hosting.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.NaturalId
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*

@Entity
@Table(name = "deployment")
class Deployment(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @NaturalId
    @Column(name = "uid", nullable = false)
    var uid: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, length = 64)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    var state: DeploymentState,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "application_id", nullable = false)
    var application: Application,


    @OneToMany(mappedBy = "deployment")
    var pods: MutableSet<Pod> = mutableSetOf(),
) : BaseEntity<Int?>() {


    override fun extractId() = id
}

enum class DeploymentState {
    initial, started, stopped, failed
}

