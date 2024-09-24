package mk.ukim.finki.dnick.hosting.model.entity

import mk.ukim.finki.dnick.hosting.model.domain.Application as DomainApplication
import mk.ukim.finki.dnick.hosting.model.domain.Deployment as DomainDeployment
import mk.ukim.finki.dnick.hosting.model.domain.Environment as DomainEnvironment
import mk.ukim.finki.dnick.hosting.model.domain.ExternalService as DomainExternalService
import mk.ukim.finki.dnick.hosting.model.domain.Image as DomainImage
import mk.ukim.finki.dnick.hosting.model.domain.Ingress as DomainIngress
import mk.ukim.finki.dnick.hosting.model.domain.IngressRule as DomainIngressRule
import mk.ukim.finki.dnick.hosting.model.domain.Namespace as DomainNamespace
import mk.ukim.finki.dnick.hosting.model.domain.Pod as DomainPod
import mk.ukim.finki.dnick.hosting.model.domain.Service as DomainService
import mk.ukim.finki.dnick.hosting.model.domain.ServicePort as DomainServicePort

fun Application.toDomain() = DomainApplication(
    id = this.id!!,
    name = this.name,
    namespace = this.namespace.name,
    deployments = this.deployments.map { it.toDomain() }.toSet(),
    ingresses = this.ingresses.map { it.toDomain() }.toSet(),
    services = this.services.map { it.toDomain() }.toSet(),
    images = this.namespace.images.map { it.toDomain() }.toSet()
)

fun Service.toDomain() = DomainService(
    id = this.id!!,
    name = this.name,
    ports = this.ports.map { it.toDomain() }.toSet(),
    deployment = this.deployment.name
)

fun Ingress.toDomain() = DomainIngress(
    id = this.id!!,
    name = this.name,
    rules = this.rules.map { it.toDomain() }.toSet()
)

fun IngressRule.toDomain() = DomainIngressRule(
    id = this.id!!,
    path = this.path,
    servicePort = this.servicePort.toDomain()
)

fun ServicePort.toDomain() = DomainServicePort(
    id = this.id!!,
    name = this.name,
    port = this.port,
    targetPort = this.pod.port,
    serviceName = this.service.name
)

fun Deployment.toDomain() = DomainDeployment(
    id = this.id!!,
    name = this.name,
    namespace = this.application.namespace.name,
    pods = this.pods.map { it.toDomain() }.toSet()
)

fun Pod.toDomain() = DomainPod(
    id = this.id!!,
    name = this.name,
    port = this.port,
    workdir = this.workdir,
    image = this.activeImage.toDomain(),
    environment = this.environment.toDomain(),
)

fun Environment.toDomain() = DomainEnvironment(
    id = this.id!!,
    name = this.name,
    content = this.values.map { Pair(it.name, it.value) }.toMap()
)

fun Image.toDomain() = DomainImage(
    id = this.id!!,
    name = this.name,
    version = this.version,
    hash = this.hash,
    baseRef = this.base.id!!,
    arguments = this.arguments,
)

fun Namespace.toDomain() = DomainNamespace(
    id = this.id,
    name = this.name,
    applications = this.applications.map { it.toDomain() }.toSet(),
    externalServices = this.externalService.map { it.toDomain() }.toSet()
)

fun ExternalService.toDomain() = DomainExternalService(
    id = this.id!!,
    name = this.name,
    content = this.config
)

