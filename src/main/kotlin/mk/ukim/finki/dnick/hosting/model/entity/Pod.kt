package mk.ukim.finki.dnick.hosting.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "pod")
class Pod(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "name", nullable = false, length = 64)
    var name: String,

    @Column(name = "port", nullable = false)
    var port: Int,

    @Column(name = "workdir", nullable = false)
    var workdir: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "active_image_id", nullable = false)
    var activeImage: Image,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "deployment_id", nullable = false)
    var deployment: Deployment,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    var environment: Environment
) : BaseEntity<Int?>() {

    override fun extractId() = id
}