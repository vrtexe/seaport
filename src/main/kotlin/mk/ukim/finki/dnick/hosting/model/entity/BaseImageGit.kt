package mk.ukim.finki.dnick.hosting.model.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "base_image_git")
class BaseImageGit(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "build_tool", nullable = false, length = 64)
    var buildTool: String,

    @Column(name = "version", nullable = false, length = 64)
    var version: String,

    @Column(name = "value", nullable = false, length = Integer.MAX_VALUE)
    var value: String,

    @JsonBackReference
//    @JsonManagedReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "base_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var base: BaseImage,

    @OneToOne
    @JsonManagedReference
    @JoinColumn(name = "ref_id", nullable = false)
    var ref: BaseImageRef
) : BaseEntity<Int?>() {

    override fun extractId() = id
}

