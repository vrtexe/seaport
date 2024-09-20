package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.BaseImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BaseImageRepository : JpaRepository<BaseImage, Int> {

    @Query("select distinct language from base_image", nativeQuery = true)
    fun findAllLanguages(): List<String>

    @Query("select distinct version from base_image where language = :language", nativeQuery = true)
    fun findLanguageVersions(@Param("language") language: String): List<String>


    @Query(
        value = """
            select b
            from BaseImage b
            where b.language = :language and
                 (b.version = null or b.version = :version)
        """
    )
    fun findBy(
        @Param("language") language: String,
        @Param("version") version: String?
    ): BaseImage?

}