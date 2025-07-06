package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.Deployment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DeploymentRepository : JpaRepository<Deployment, Int> {

    @Query(
        """
        select d from Deployment d
        left join fetch d.application a
        left join fetch a.namespace n
        left join fetch d.pods p
        left join fetch p.activeImage it
        left join fetch it.image
        left join fetch p.servicePorts sp
        left join fetch sp.service s
        left join fetch sp.ingressRules ir
        left join fetch ir.ingress
        left join fetch p.environment
        where n.name = :namespace
        """
    )
    fun findAllByNamespace(@Param("namespace") namespace: String, pageable: Pageable): Page<Deployment>

    fun findByName(name: String): Deployment?
    fun findByUid(uid: UUID): Deployment?
}