package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.BaseImage
import mk.ukim.finki.dnick.hosting.model.entity.BaseImageType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
        """
        select bi from BaseImage bi
        where bi.baseImagesExe is not null
    """
    )
    fun findBy(
        @Param("type") type: BaseImageType?,
        @Param("language") language: String,
        @Param("buildTool") buildTool: String
    ): List<String>


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

    @Query(
        """
        select b from BaseImage b
        join fetch b.baseImagesGit bg
        join fetch b.baseImagesExe be
        join fetch be.ref
        join fetch bg.ref
        where (:language is null or b.language ilike %:language%) and 
              (:buildTool is null or bg.buildTool ilike %:buildTool%)
              """
    )
    fun findAllBy(

        @Param("language") language: String?,
        @Param("buildTool") buildTool: String?,
        pageable: Pageable
    ): Page<BaseImage>

}