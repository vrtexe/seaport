package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.ImageTag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ImageTagRepository : JpaRepository<ImageTag, Int> {

    fun findByHash(uid: UUID): ImageTag?
}