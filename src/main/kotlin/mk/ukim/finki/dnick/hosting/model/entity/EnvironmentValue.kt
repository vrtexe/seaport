package mk.ukim.finki.dnick.hosting.model.entity

import jakarta.persistence.*

@Entity
@Table(name = "environment_value")
class EnvironmentValue(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "value", nullable = false)
    var value: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    var environment: Environment
) : BaseEntity<Int?>() {


    override fun extractId() = id

}