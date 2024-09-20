package mk.ukim.finki.dnick.hosting.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kubernetes.client.openapi.apis.CoreV1Api
import mk.ukim.finki.dnick.hosting.image.ImageExeParams
import mk.ukim.finki.dnick.hosting.image.ImageGitParams
import mk.ukim.finki.dnick.hosting.image.ImageJobManager
import mk.ukim.finki.dnick.hosting.image.ImageJobProperties
import mk.ukim.finki.dnick.hosting.repository.BaseImageExeRepository
import mk.ukim.finki.dnick.hosting.repository.BaseImageGitRepository
import mk.ukim.finki.dnick.hosting.webdav.WebdavService
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class ImageBuilderService(
    private val imageJobManager: ImageJobManager,
    private val coreV1Api: CoreV1Api,
    private val baseImageExeRepository: BaseImageExeRepository,
    private val baseImageGitRepository: BaseImageGitRepository,
    private val webdavService: WebdavService
) {

    companion object {
        const val URL_ARG = "URL"
        const val HASH_ARG = "HASH"
    }

    fun createImage(image: ImageExeParams) {
        log.info { "Creating image ${image.data.name}" }

        val baseImage = baseImageExeRepository.findBy(image.base.language, image.base.version)
            ?: throw ResponseStatusException(NOT_FOUND, "Image not found")

        log.info { "Found base image (id: ${baseImage.id}, ref:${baseImage.ref.id})" }

        val fileUrl = webdavService.upload(
            "${image.data.name}-${image.data.version}.${baseImage.fileType}",
            "${image.namespace}/${image.data.name}",
            image.file
        )

        imageJobManager.buildImage(
            ImageJobProperties(
                name = image.data.name,
                version = image.data.version,
                content = baseImage.value,
                buildArguments = mapOf(
                    *image.buildArgs.entries.map { it.key to it.value }.toTypedArray(),
                    URL_ARG to fileUrl,
                    HASH_ARG to UUID.randomUUID().toString(),
                )
            )
        )
    }

    fun createImage(image: ImageGitParams) {
        val baseImage = baseImageGitRepository.findBy(
            image.base.language,
            image.base.version,
            image.buildTool,
            image.version
        ) ?: throw ResponseStatusException(NOT_FOUND, "Image not found")

        imageJobManager.buildImage(
            ImageJobProperties(
                name = image.data.name,
                version = image.data.version,
                content = baseImage.value,
                buildArguments = mapOf(
                    *image.buildArgs.entries.map { it.key to it.value }.toTypedArray(),
                    HASH_ARG to UUID.randomUUID().toString(),
                )
            )
        )
    }


}