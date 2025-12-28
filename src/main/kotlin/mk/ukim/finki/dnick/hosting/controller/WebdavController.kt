package mk.ukim.finki.dnick.hosting.controller

import com.github.sardine.DavResource
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import mk.ukim.finki.dnick.hosting.generated.model.Pagination
import mk.ukim.finki.dnick.hosting.generated.model.ResponseMetadata
import mk.ukim.finki.dnick.hosting.generated.model.StorageResponse
import mk.ukim.finki.dnick.hosting.generated.model.StoredResource
import mk.ukim.finki.dnick.hosting.webdav.WebDavClient
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.ZoneId
import java.util.Comparator.*
import java.util.function.Function
import javax.swing.SortOrder
import kotlin.math.ceil


const val WEBDAV_API_V2_PREFIX = "/api/v2/storage"
const val WEBDAV_API_V2_RESOURCE_PREFIX = "$WEBDAV_API_V2_PREFIX/resource"

@RestController
@RequestMapping(WEBDAV_API_V2_PREFIX)
class WebdavController(private val webDavClient: WebDavClient) {


    data class StorageQueryParams(
        val name: String? = null
    )

    @GetMapping
    fun getStoredResourcesCatchAll(
        @Valid params: StorageQueryParams,
        @Valid
        @PageableDefault(page = 0, size = 20)
        page: Pageable = Pageable.unpaged()
    ): StorageResponse {
        val webdavResult = webDavClient.fetchAll()

        val result = paginateCollection(
            webdavResult.map { it.toStoredResource() }
                .filter {
                    params.name?.let { p -> (it.path?.contains(p) ?: true) || it.name.contains(p) } ?: true
                }
                .toMutableList(),
            page,
            createComparator(StoredResource::contentLength, SortOrder.DESCENDING)
        );

        return StorageResponse(
            data = result.collection.toList(),
            metadata = ResponseMetadata(
                pagination = result.pagination
            )
        )
    }

    @GetMapping("/resource/**", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getResource(@RequestParam path: String?, request: HttpServletRequest): ResponseEntity<Resource> {
        val resourcePath = path?.removePrefix(WEBDAV_API_V2_RESOURCE_PREFIX)
            ?: request.requestURI.removePrefix(WEBDAV_API_V2_RESOURCE_PREFIX)

        val fileInfo = webDavClient.fetch(resourcePath) ?: throw Exception("File not found at path: $resourcePath")
        val fileData = webDavClient.getContent(resourcePath) ?: throw Exception("File not found at path: $resourcePath")
        val resource = InputStreamResource(fileData)

        return ResponseEntity.ok()
            .header(CONTENT_DISPOSITION, "attachment; filename=${fileInfo.name}")
            .body(resource)
    }

    @DeleteMapping("/resource/**", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun deleteResource(@RequestParam path: String?, request: HttpServletRequest): ResponseEntity<Void> {
        val resourcePath = path?.removePrefix(WEBDAV_API_V2_RESOURCE_PREFIX)
            ?: request.requestURI.removePrefix(WEBDAV_API_V2_RESOURCE_PREFIX)

        webDavClient.delete(resourcePath)

        return ResponseEntity.noContent().build()
    }

    fun <T> paginateCollection(
        collection: MutableCollection<T>,
        pageable: Pageable,
        comparator: Comparator<T>?
    ): PaginatedCollection<T> {
        if (pageable.isUnpaged) {
            return PaginatedCollection(
                collection,
                Pagination(
                    page = 1,
                    propertySize = collection.size,
                    totalElements = collection.size,
                    totalPages = 1
                )
            )
        }

        val page: Int = pageable.pageNumber + 1
        val size: Int = pageable.pageSize
        val totalElements = collection.size
        val totalPages = ceil(totalElements.toDouble() / size).toInt()

        val stream = if (comparator != null) collection.stream().sorted(comparator) else collection.stream()
        val paginatedCollection: MutableCollection<T> = stream
            .skip((page - 1).toLong() * size)
            .limit(size.toLong())
            .toList()

        val pagination = Pagination(page, size, totalElements, totalPages)

        return PaginatedCollection(paginatedCollection, pagination)
    }

    data class PaginatedCollection<T>(
        val collection: MutableCollection<T>,
        val pagination: Pagination
    )
//    private fun getStoredResourcesInternal(webdavPath: String, page: Pageable): StorageResponse {

//        return StorageResponse(
//            current = webdavResult?.firstOrNull()?.let {
//                if (it.name == "dav") null
//                else it.toStoredResource()
//            },
//            contents = webDavClient.fetch(webdavPath)?.drop(1)?.filter { it != null }?.map {
//                it!!.toStoredResource()
//            } ?: listOf()
//        )
//    }

    fun <T, U : Comparable<U>> createComparator(keyExtractor: Function<in T, out U>, order: SortOrder): Comparator<T> {
        val comparator = comparing(keyExtractor, nullsLast(naturalOrder()))
        return if (order === SortOrder.DESCENDING) comparator.reversed() else comparator
    }

    fun DavResource.toStoredResource() = StoredResource(
        name = this.name,
        displayName = this.displayName ?: this.name,
        href = "${WEBDAV_API_V2_RESOURCE_PREFIX}${this.href.toString().removePrefix("/dav")}",
        directory = this.isDirectory,
        path = this.path,
        etag = this.etag,
        created = this.creation?.toInstant()?.atZone(ZoneId.systemDefault())?.toOffsetDateTime(),
        modified = this.modified?.toInstant()?.atZone(ZoneId.systemDefault())?.toOffsetDateTime(),
        contentType = this.contentType,
        contentLength = this.contentLength
    )

    data class WebDavResponse(
        var current: WebDavResource?,
        val contents: List<WebDavResource>
    )

    data class WebDavResource(
        val name: String,
        val displayName: String?,
        val href: String,
        val directory: Boolean,
        val etag: String?,
        val created: Instant?,
        val modified: Instant?,
        val contentType: String,
        val contentLength: Long
    )

}