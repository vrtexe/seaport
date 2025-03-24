package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.generated.model.ImageCreateRequest
import mk.ukim.finki.dnick.hosting.generated.model.ImageUpdateRequest
import mk.ukim.finki.dnick.hosting.image.ImageExeParams
import mk.ukim.finki.dnick.hosting.image.ImageGitParams
import mk.ukim.finki.dnick.hosting.image.ImageParamsTyped
import mk.ukim.finki.dnick.hosting.infra.config.UserProperties
import mk.ukim.finki.dnick.hosting.model.dto.ImageBuildRequestDto
import mk.ukim.finki.dnick.hosting.model.entity.*
import mk.ukim.finki.dnick.hosting.repository.*
import mk.ukim.finki.dnick.hosting.socket.SocketSessionCache
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.BAD_REQUEST
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
    private val userProperties: UserProperties,
    private val imageLogRepository: ImageLogRepository,
) {

    @Transactional
    fun createImage(image: ImageCreateRequest): Image {
        imageRepository.findBy(image.name, userProperties.namespace)?.let {
            throw ResponseStatusException(BAD_REQUEST, "An image with name ${image.name} already exists")
        }
        return imageRepository.save(
            Image(
                name = image.name,
                namespace = namespaceService.resolveUserNamespace()
            )
        )
    }

    @Transactional(readOnly = true)
    fun getImageLog(imageId: Int): ImageLog? {
        return imageLogRepository.findByImageId(imageId)
    }

    @Transactional(readOnly = true)
    fun getImages(pageable: Pageable): Page<Image> {
        return imageRepository.findAllByNamespace(userProperties.namespace, pageable)
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    fun getImage(id: Int): Image {
        return imageRepository.findByIdOrNull(id)
            ?: throw ResponseStatusException(NOT_FOUND, "Could not find image with id $id")
    }

    @Transactional
    fun editImage(id: Int, update: ImageUpdateRequest): Image {
        val image = getImage(id)
        if (image.name == update.name) {
            return image
        }

        imageRepository.findBy(update.name, userProperties.namespace)?.let {
            if (it.id != id)
                throw ResponseStatusException(BAD_REQUEST, "An image with name: `${update.name}` already exists.")
        }

        image.name = update.name

        return imageRepository.save(image)
    }

    @Transactional
    fun deleteImage(id: Int) {
        val image = getImage(id)
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
                name = imageParams.data.name,
            )
        )

        return saveImageTag(imageParams, image, hash)
    }

    private fun saveImageTag(imageParams: ImageParamsTyped, image: Image, hash: UUID = UUID.randomUUID()): ImageTag {
        return imageTagRepository.saveAndFlush(
            ImageTag(
                version = imageParams.data.version,
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