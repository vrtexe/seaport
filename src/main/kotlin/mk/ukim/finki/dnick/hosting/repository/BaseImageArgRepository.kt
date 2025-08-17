package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.BaseImageArg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BaseImageArgRepository : JpaRepository<BaseImageArg, Int> {
}