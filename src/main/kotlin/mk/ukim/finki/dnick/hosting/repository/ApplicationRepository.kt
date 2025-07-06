package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.Application
import mk.ukim.finki.dnick.hosting.model.entity.Image
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : JpaRepository<Application, Int> {

    @Query(
        """
        select a from Application a
        where a.namespace.name = :namespace
        """
    )
    fun findAllByNamespace(@Param("namespace") namespace: String, pageable: Pageable): Page<Application>



    fun findByNameContainingIgnoreCase(name: String): Application?
}