package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.BaseImageExe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BaseImageExeRepository : JpaRepository<BaseImageExe, Int> {


    @Query(
        value = """
            select i
            from BaseImageExe i
            join i.base b
            where b.language = :language and
                (b.version = null or b.version = :version)"""
    )
    fun findBy(
        @Param("language") language: String,
        @Param("version") version: String?
    ): BaseImageExe?

}