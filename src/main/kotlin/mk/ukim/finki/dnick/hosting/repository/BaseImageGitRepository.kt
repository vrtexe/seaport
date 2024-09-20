package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.BaseImageGit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BaseImageGitRepository : JpaRepository<BaseImageGit, Int> {

    @Query(
        value = """
            select i
            from BaseImageGit i
            join i.base b
            where b.language = :language and
                  i.buildTool = :build_tool and
                  i.version = :build_tool_version and
                 (b.version = null or b.version = :version)
        """
    )
    fun findBy(
        @Param("language") language: String,
        @Param("version") version: String?,
        @Param("build_tool") buildTool: String,
        @Param("build_tool_version") buildToolVersion: String
    ): BaseImageGit?

    @Query("select distinct build_tool from base_image_git where  base_id = :base_image_id", nativeQuery = true)
    fun findAllBuildTools(@Param("base_image_id") baseImageId: Int): List<String>

    @Query("select distinct version from base_image_git where build_tool = :build_tool", nativeQuery = true)
    fun findBuildToolVersions(@Param("build_tool") buildTool: String): List<String>

}