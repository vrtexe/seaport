package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.Image
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ImageRepository : JpaRepository<Image, Int> {

    @Query(
        """
        select i from Image i
        where i.namespace.name = :namespace
        """
    )
    fun findAllByNamespace(@Param("namespace") namespace: String, pageable: Pageable): Page<Image>

    @Query(
        """
        select i from Image i
        where i.name = :name and i.namespace.name = :namespace
        """
    )
    fun findBy(
        @Param("name") name: String,
        @Param("namespace") namespace: String
    ): Image?
}