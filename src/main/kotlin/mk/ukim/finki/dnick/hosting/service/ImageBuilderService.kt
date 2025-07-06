package mk.ukim.finki.dnick.hosting.service

import io.github.oshai.kotlinlogging.KotlinLogging
import mk.ukim.finki.dnick.hosting.image.*
import mk.ukim.finki.dnick.hosting.model.entity.BaseImageType
import mk.ukim.finki.dnick.hosting.model.entity.ImageTag
import mk.ukim.finki.dnick.hosting.repository.BaseImageExeRepository
import mk.ukim.finki.dnick.hosting.repository.BaseImageGitRepository
import mk.ukim.finki.dnick.hosting.webdav.WebdavService
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

private val log = KotlinLogging.logger {}

enum class InternalArgument(val value: String) {
    URL("URL"),
    HASH("HASH");

    companion object {
        val allowedGitValues = listOf(URL)
        val allowedExeValues = listOf<InternalArgument>()

        val valuesByType = mapOf(
            BaseImageType.GIT to entries
                .filter { !allowedGitValues.contains(it) }
                .map { it.value }
                .toList(),
            BaseImageType.EXE to entries
                .filter { !allowedExeValues.contains(it) }
                .map { it.value }
                .toList()
        )
        val values = entries.map { it.value }.toList()
    }
}

@Service
class ImageBuilderService(
    private val baseImageExeRepository: BaseImageExeRepository,
    private val baseImageGitRepository: BaseImageGitRepository,
    private val webdavService: WebdavService,
    private val imageBuildQueue: ImageBuildQueue
) {

    fun createImage(image: ImageExeParams, tag: ImageTag) {
        log.info { "Creating image ${image.data.name}" }

        val baseImage = baseImageExeRepository.findBy(image.base.language, image.base.version)
            ?: throw ResponseStatusException(NOT_FOUND, "Image not found")

        log.info { "Found base image (id: ${baseImage.id}, ref:${baseImage.ref.id})" }

        val fileUrl = webdavService.upload(
            "${image.data.name}-${image.data.version}.${baseImage.fileType}",
            "${image.namespace}/${image.data.name}",
            image.file
        )

        imageBuildQueue.queueImageBuild(
            tag,
            ImageJobProperties(
                name = image.data.name,
                version = image.data.version,
                content = baseImage.value,
                imageUid = tag.hash,
                buildArguments = mapOf(
                    *image.buildArgs.entries.map { it.key to it.value }.toTypedArray(),
                    InternalArgument.URL.value to fileUrl,
                    InternalArgument.HASH.value to UUID.randomUUID().toString(),
                )
            )
        )
    }

    fun createImage(image: ImageGitParams, tag: ImageTag) {
        val baseImage = baseImageGitRepository.findBy(
            image.base.language,
            image.base.version,
            image.buildTool,
            image.version
        ) ?: throw ResponseStatusException(NOT_FOUND, "Image not found")

        imageBuildQueue.queueImageBuild(
            tag,
            ImageJobProperties(
                name = image.data.name,
                version = image.data.version,
                content = baseImage.value,
                imageUid = tag.hash,
                buildArguments = mapOf(
                    *image.buildArgs.entries.map { it.key to it.value }.toTypedArray(),
                    InternalArgument.HASH.value to UUID.randomUUID().toString(),
                )
            )
        )
    }

}