package mk.ukim.finki.dnick.hosting.model.entity

import jakarta.persistence.*

@Entity
@Table(name = "image_log")
class ImageLog(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "data", nullable = false)
    var data: String,

    @OneToOne
    @JoinColumn(name = "image_id", nullable = false)
    var image: ImageTag,
) : BaseEntity<Int?>() {

    override fun extractId() = id
}