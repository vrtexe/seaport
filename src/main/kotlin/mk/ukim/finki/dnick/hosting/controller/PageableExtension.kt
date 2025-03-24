package mk.ukim.finki.dnick.hosting.controller

import mk.ukim.finki.dnick.hosting.generated.model.Pagination
import mk.ukim.finki.dnick.hosting.model.entity.Image
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

//class PageableMapper {
//}

fun <T> Page<T>.toPagination() = Pagination(
    page = this.number + 1,
    propertySize = this.size,
    totalPages = this.totalPages,
    totalElements = this.totalElements.toInt()
)