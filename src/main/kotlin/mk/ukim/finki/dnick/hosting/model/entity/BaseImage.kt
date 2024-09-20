package mk.ukim.finki.dnick.hosting.model.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
@Table(name = "base_image")
class BaseImage(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "language", nullable = false, length = 64)
    var language: String,

    @Column(name = "version", length = 64)
    var version: String? = null,

    @JsonBackReference
    @OneToMany(mappedBy = "base")
    var baseImagesExe: MutableSet<BaseImageExe> = mutableSetOf(),

    @JsonManagedReference
    @OneToMany(mappedBy = "base")
    var baseImagesGit: MutableSet<BaseImageGit> = mutableSetOf()
) : BaseEntity<Int?>() {

    override fun extractId() = id
}
