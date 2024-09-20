package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.Namespace
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NamespaceRepository : JpaRepository<Namespace, UUID> {

    fun findByName(name: String): Namespace?
}