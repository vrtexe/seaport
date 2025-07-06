package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun findByUid(uid: UUID): User?
}