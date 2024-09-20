package mk.ukim.finki.dnick.hosting.model.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(name = "base_image_arg")
class BaseImageArg(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "name", nullable = false, length = 64)
    var name: String,

    @Column(name = "description", nullable = false)
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    var stage: BaseImageArgStage,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: BaseImageArgType = BaseImageArgType.string,

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_image_ref_id", nullable = false)
    var baseImageRef: BaseImageRef
) : BaseEntity<Int?>() {

    override fun extractId() = id
}

enum class BaseImageArgType {
    string, file
}

enum class BaseImageArgStage {
    runtime, build
}