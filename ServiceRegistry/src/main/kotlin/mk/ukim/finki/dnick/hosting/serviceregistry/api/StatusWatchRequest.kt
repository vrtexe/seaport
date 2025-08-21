package mk.ukim.finki.dnick.hosting.serviceregistry.api

data class StatusWatchRequest(
    val namespace: String,
    val selector: String
)