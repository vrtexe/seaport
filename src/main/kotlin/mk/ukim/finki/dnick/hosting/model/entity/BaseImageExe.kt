package mk.ukim.finki.dnick.hosting.model.entity

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "base_image_exe")
class BaseImageExe(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "value", nullable = false, length = Integer.MAX_VALUE)
    var value: String,

    @Column(name = "file_type", nullable = false)
    val fileType: String,

    @JsonManagedReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "base_id", nullable = false)
    @ManyToOne(optional = false)
    var base: BaseImage,

    @OneToOne
    @JsonManagedReference
    @JoinColumn(name = "ref_id", nullable = false)
    var ref: BaseImageRef
) : BaseEntity<Int?>() {

    override fun extractId() = id
}