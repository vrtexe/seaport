package mk.ukim.finki.dnick.hosting.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.NaturalId
import java.util.*

@Entity
@Table(name = "app_user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @NaturalId
    @Column(name = "uid", nullable = false)
    var uid: UUID,

//    @OneToMany(mappedBy = "user")
//    var namespaces: MutableSet<Namespace> = mutableSetOf()
) : BaseEntity<Int?>() {
    override fun extractId() = id
}