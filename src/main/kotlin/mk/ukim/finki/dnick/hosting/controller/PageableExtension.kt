package mk.ukim.finki.dnick.hosting.controller

import mk.ukim.finki.dnick.hosting.generated.model.Pagination
import org.springframework.data.domain.Page

fun <T> Page<T>.toPagination() = Pagination(
    page = this.number + 1,
    propertySize = this.size,
    totalPages = this.totalPages,
    totalElements = this.totalElements.toInt()
)