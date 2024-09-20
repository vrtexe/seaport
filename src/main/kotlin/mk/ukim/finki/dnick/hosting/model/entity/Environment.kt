package mk.ukim.finki.dnick.hosting.model.entity

import jakarta.persistence.*

@Entity
@Table(name = "environment")
class Environment(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    @OneToMany(mappedBy = "environment")
    var values: MutableSet<EnvironmentValue> = mutableSetOf()
) : BaseEntity<Int?>() {

    override fun extractId() = id
}
