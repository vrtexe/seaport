package mk.ukim.finki.dnick.hosting.repository

import mk.ukim.finki.dnick.hosting.model.entity.IngressRule
import mk.ukim.finki.dnick.hosting.model.entity.Pod
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IngressRuleRepository : JpaRepository<IngressRule, Int> {
}