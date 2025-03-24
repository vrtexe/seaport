package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.ImageLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageLogRepository : JpaRepository<ImageLog, Int> {

    fun findByImageId(imageId: Int): ImageLog?
}