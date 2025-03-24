package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.ImageLog
import mk.ukim.finki.dnick.hosting.model.entity.ImageTag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ImageLogRepository : JpaRepository<ImageLog, Int> {

    fun findByImageId(imageId: Int): ImageLog?
}