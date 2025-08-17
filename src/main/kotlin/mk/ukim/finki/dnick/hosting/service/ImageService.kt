package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.builder.cleanupImageTag
import mk.ukim.finki.dnick.hosting.generated.model.ImageCreateRequest
import mk.ukim.finki.dnick.hosting.generated.model.ImageUpdateRequest
import mk.ukim.finki.dnick.hosting.image.ImageExeParams
import mk.ukim.finki.dnick.hosting.image.ImageGitParams
import mk.ukim.finki.dnick.hosting.image.ImageParamsTyped

import mk.ukim.finki.dnick.hosting.model.dto.ImageBuildRequestDto
import mk.ukim.finki.dnick.hosting.model.entity.*
import mk.ukim.finki.dnick.hosting.repository.*
import mk.ukim.finki.dnick.hosting.socket.SocketSessionCache
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.*

@Service
class ImageService(
    private val imageBuilderService: ImageBuilderService,
    private val socketSessionCache: SocketSessionCache,
    private val imageTagRepository: ImageTagRepository,
    private val baseImageExeRepository: BaseImageExeRepository,
    private val baseImageGitRepository: BaseImageGitRepository,
    private val namespaceService: NamespaceService,
    private val imageRepository: ImageRepository,
    private val imageLogRepository: ImageLogRepository,
    private val podRepository: PodRepository,
) {

    @Transactional
    fun createImage(image: ImageCreateRequest): Image {
        val namespace = namespaceService.resolveUserNamespace()
        imageRepository.findBy(image.name, namespace.name)?.let {
            throw ResponseStatusException(BAD_REQUEST, "An image with name ${image.name} already exists")
        }
        return imageRepository.save(
            Image(
                name = image.name.cleanupImageTag(),
                namespace = namespace
            )
        )
    }

    @Transactional(readOnly = true)
    fun getImageLog(imageId: Int): ImageLog? {
        return imageLogRepository.findByImageId(imageId)
    }

    @Transactional
    fun getImages(pageable: Pageable): Page<Image> {
        val namespace = namespaceService.resolveUserNamespace()
        return imageRepository.findAllByNamespace(namespace.name, pageable)
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    fun getImage(id: Int): Image {
        return imageRepository.findByIdOrNull(id)
            ?: throw ResponseStatusException(NOT_FOUND, "Could not find image with id $id")
    }

    @Transactional
    fun editImage(id: Int, update: ImageUpdateRequest): Image {
        val image = getImage(id)
        val updatedName = update.name.cleanupImageTag()
        if (image.name == updatedName) {
            return image
        }
        val namespace = namespaceService.resolveUserNamespace()
        imageRepository.findBy(updatedName, namespace.name)?.let {
            if (it.id != id)
                throw ResponseStatusException(BAD_REQUEST, "An image with name: `${updatedName}` already exists.")
        }

        image.name = updatedName

        return imageRepository.save(image)
    }

    @Transactional
    fun deleteImage(id: Int) {
        val image = getImage(id)
        if (podRepository.findByActiveImageIdIn(image.tags.map { it.id }.filterNotNull()).isNotEmpty()) {
            throw ResponseStatusException(BAD_REQUEST, "The application is referenced by a deployment.")
        }
        imageRepository.delete(image)
    }

    fun buildImage(dto: ImageBuildRequestDto) {
        val image = getImage(dto.imageId)
        val socket = dto.socket?.let { socketSessionCache.get(it) }

        val tag = saveImage(dto.request, image)

        sendText("Building ${dto.request.data.name}:${dto.request.data.version}", socket)
        buildImage(tag, dto.request)
    }

    private fun saveImage(imageParams: ImageParamsTyped, image: Image): ImageTag {
        return saveImageTag(imageParams, image)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveImage(imageParams: ImageParamsTyped, hash: UUID = UUID.randomUUID()): ImageTag {
        val image = imageRepository.save(
            Image(
                namespace = namespaceService.resolveUserNamespace(),
                name = imageParams.data.name.cleanupImageTag(),
            )
        )

        return saveImageTag(imageParams, image, hash)
    }

    private fun saveImageTag(imageParams: ImageParamsTyped, image: Image, hash: UUID = UUID.randomUUID()): ImageTag {
        return imageTagRepository.saveAndFlush(
            ImageTag(
                version = imageParams.data.version.cleanupImageTag(),
                hash = hash,
                arguments = imageParams.buildArgs,
                image = image,
                base = findBaseImageRef(imageParams),
                status = ImageStatus.initialized
            )
        )
    }

    private fun updateStatus(status: ImageStatus, imageTag: ImageTag): ImageTag {
        imageTag.status = status
        return imageTagRepository.save(imageTag)
    }

    private fun buildImage(tag: ImageTag, image: ImageParamsTyped) {
        when (image) {
            is ImageGitParams -> imageBuilderService.createImage(image, tag)
            is ImageExeParams -> imageBuilderService.createImage(image, tag)
        }
    }

    private fun findBaseImageRef(image: ImageParamsTyped): BaseImageRef {
        return when (image) {
            is ImageExeParams -> baseImageExeRepository.findBy(image.base.language, image.base.version)?.ref
            is ImageGitParams -> baseImageGitRepository.findBy(
                image.base.language,
                image.base.version,
                image.buildTool,
                image.version
            )?.ref

            else -> null
        } ?: throw ResponseStatusException(NOT_FOUND, "Base image not found")
    }

    private fun sendText(text: String, socket: WebSocketSession?) {
        // language=json
        socket?.sendMessage(
            TextMessage(
                """
                    {
                        "type": "STRING",
                        "data": "$text"
                    }
                    """.trimIndent()
            )
        )
    }

}