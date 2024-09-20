package mk.ukim.finki.dnick.hosting.model.domain

import java.util.UUID

data class Namespace(
    val id: UUID = UUID.randomUUID(),
    val name: String,

    val applications: Set<Application>,
    val externalServices: Set<ExternalService>,
)

data class ExternalService(
    val id: Int,
    val name: String,
    val content: Map<String, String>,
)

data class Environment(
    val id: Int,
    val name: String,
    val content: Map<String, String>,
)
