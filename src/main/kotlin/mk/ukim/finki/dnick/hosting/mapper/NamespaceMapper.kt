package mk.ukim.finki.dnick.hosting.mapper

import mk.ukim.finki.dnick.hosting.generated.model.Namespace
import mk.ukim.finki.dnick.hosting.model.domain.BaseNamespace

fun BaseNamespace.toResponse() = Namespace(
    uid = this.id.toString(),
    name = this.name
)