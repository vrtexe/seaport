package mk.ukim.finki.dnick.hosting.model.entity

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "base_image_ref")
class BaseImageRef(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

//    @Column(insertable=false, updatable=false)
    @OneToOne(mappedBy = "ref", optional = true)
    var baseImageExe: BaseImageExe?,

//    @Column(insertable=false, updatable=false)
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
