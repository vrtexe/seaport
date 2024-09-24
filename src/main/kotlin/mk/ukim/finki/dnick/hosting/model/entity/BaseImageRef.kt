package mk.ukim.finki.dnick.hosting.model.entity

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
@Table(name = "base_image_ref")
class BaseImageRef(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @OneToOne(mappedBy = "ref", optional = true)
    var baseImageExe: BaseImageExe?,

    @OneToOne(mappedBy = "ref", optional = true)
    var baseImageGit: BaseImageGit?,

    @JsonManagedReference
    @OneToMany(mappedBy = "baseImageRef", fetch = FetchType.LAZY)
    var arguments: MutableSet<BaseImageArg> = mutableSetOf()
) : BaseEntity<Int?>() {

    val type: BaseImageType
        get() =
            if (this.baseImageGit != null) BaseImageType.GIT
            else if (this.baseImageExe != null) BaseImageType.EXE
            else throw UnsupportedOperationException()


    override fun extractId() = id
}

enum class BaseImageType {
    GIT, EXE
}
