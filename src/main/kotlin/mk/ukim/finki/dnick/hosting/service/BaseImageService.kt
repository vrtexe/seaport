package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.controller.BaseImageCriteria
import mk.ukim.finki.dnick.hosting.generated.model.BaseImageArgument
import mk.ukim.finki.dnick.hosting.generated.model.BaseImageRequestExe
import mk.ukim.finki.dnick.hosting.generated.model.BaseImageRequestGit
import mk.ukim.finki.dnick.hosting.generated.model.BaseImageType.EXE
import mk.ukim.finki.dnick.hosting.generated.model.BaseImageType.GIT
import mk.ukim.finki.dnick.hosting.generated.model.BaseImageUpdateRequest
import mk.ukim.finki.dnick.hosting.model.domain.BaseImageArg
import mk.ukim.finki.dnick.hosting.model.entity.*
import mk.ukim.finki.dnick.hosting.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import mk.ukim.finki.dnick.hosting.generated.model.BaseImageRequest as BaseImageRequestDto
import mk.ukim.finki.dnick.hosting.model.entity.BaseImageArg as BaseImageArgEntity

@Service
class BaseImageService(
    private val baseImageRepository: BaseImageRepository,
    private val baseImageExeRepository: BaseImageExeRepository,
    private val baseImageGitRepository: BaseImageGitRepository,
    private val baseImageRefRepository: BaseImageRefRepository,
    private val baseImageArgRepository: BaseImageArgRepository
) {

    @Transactional(readOnly = true)
    fun findBaseImagesBy(criteria: BaseImageCriteria, pageable: Pageable): Page<BaseImage> {
        return baseImageRepository.findAllBy(
            criteria.language,
            criteria.buildTool,
            pageable
        )
    }

    @Transactional
    fun createBaseImage(data: BaseImageRequestDto) {
        val baseImage = findOrCreateBaseImage(data)

        when (data.type) {
            GIT -> baseImage.baseImagesGit.add(createGitBaseImage(data.toContext(), baseImage))
            EXE -> baseImage.baseImagesExe.add(createExeBaseImage(data.toContext(), baseImage))
        }
    }

    @Transactional
    fun updateBaseImage(id: Int, data: BaseImageUpdateRequest) {
        val baseImageRef =
            baseImageRefRepository.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val newType = BaseImageType.valueOf(data.type.value)
        if (baseImageRef.type !== newType) {
            val baseImage = when (baseImageRef.type) {
                BaseImageType.GIT -> baseImageRef.baseImageGit?.base
                BaseImageType.EXE -> baseImageRef.baseImageExe?.base
            }
            when (baseImageRef.type) {
                BaseImageType.GIT -> baseImageRef.baseImageGit?.let { baseImageGitRepository.delete(it) }
                BaseImageType.EXE -> baseImageRef.baseImageExe?.let { baseImageExeRepository.delete(it) }
            }

            when (data.type) {
                GIT -> baseImage?.baseImagesGit?.add(createGitBaseImage(data.toContext(), baseImage, baseImageRef))
                EXE -> baseImage?.baseImagesExe?.add(createExeBaseImage(data.toContext(), baseImage, baseImageRef))
            }
            baseImage?.let { baseImageRepository.save(it) }
        } else {
            baseImageArgRepository.deleteAll(baseImageRef.arguments)
            baseImageRef.arguments.clear()
            baseImageRef.arguments.addAll(createBaseImageArguments(data.toContext(), baseImageRef))

            when (baseImageRef.type) {
                BaseImageType.GIT -> {
                    baseImageRef.baseImageGit?.also {
                        data.git?.let { u ->
                            it.buildTool = u.buildTool
                            it.version = u.buildToolVersion
                        }
                        it.value = data.value
                    }?.let { baseImageGitRepository.save(it) }
                }

                BaseImageType.EXE -> {
                    baseImageRef.baseImageExe?.also {
                        data.exe?.let { u ->
                            it.fileType = u.fileType
                        }
                        it.value = data.value
                    }?.let { baseImageExeRepository.save(it) }
                }
            }
            baseImageRefRepository.save(baseImageRef)
        }
    }

    @Transactional
    fun deleteBaseImage(id: Int) {
        baseImageRefRepository.findByIdOrNull(id)?.let { it ->
            when (it.type) {
                BaseImageType.GIT -> it.baseImageGit?.let {
                    baseImageGitRepository.delete(it)
                    if (it.base.baseImagesExe.size + it.base.baseImagesGit.size <= 1) {
                        baseImageRepository.delete(it.base)
                    }
                }

                BaseImageType.EXE -> it.baseImageExe?.let { baseImageExeRepository.delete(it) }
            }
            baseImageArgRepository.deleteAll(it.arguments)
            baseImageRefRepository.delete(it)
        }


    }

    private fun findOrCreateBaseImage(data: BaseImageRequestDto): BaseImage {
        return data.baseImageId?.let { baseImageRepository.findByIdOrNull(it) }
            ?: data.baseImage?.let {
                baseImageRepository.save(
                    BaseImage(
                        language = it.language,
                        version = it.version
                    )
                )
            } ?: throw throw ResponseStatusException(HttpStatus.BAD_REQUEST)
    }

    private fun createExeBaseImage(
        data: ImageContext,
        baseImage: BaseImage,
        providedRef: BaseImageRef? = null
    ): BaseImageExe {
        val ref = providedRef ?: BaseImageRef(baseImageExe = null, baseImageGit = null)
        val baseImageExe = BaseImageExe(
            value = data.value,
            base = baseImage,
            ref = ref,
            fileType = data.exe!!.fileType
        )

        ref.baseImageGit = null
        ref.baseImageExe = baseImageExe

        baseImageRefRepository.save(ref)
        baseImageExeRepository.save(baseImageExe)

        if (ref.arguments.isNotEmpty()) baseImageArgRepository.deleteAll(ref.arguments)

        ref.arguments.clear()
        ref.arguments.addAll(createBaseImageArguments(data, ref))

        return baseImageExe
    }


    private fun createGitBaseImage(
        data: ImageContext,
        baseImage: BaseImage,
        providedRef: BaseImageRef? = null
    ): BaseImageGit {
        val ref = providedRef ?: BaseImageRef(baseImageExe = null, baseImageGit = null)
        val baseImageGit = data.git?.let {
            BaseImageGit(
                buildTool = it.buildTool,
                version = it.buildToolVersion,
                value = data.value,
                base = baseImage,
                ref = ref
            )
        } ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        ref.baseImageExe = null
        ref.baseImageGit = baseImageGit

        baseImageRefRepository.save(ref)
        baseImageGitRepository.save(baseImageGit)

        if (ref.arguments.isNotEmpty()) baseImageArgRepository.deleteAll(ref.arguments)
        ref.arguments.clear()
        ref.arguments.addAll(createBaseImageArguments(data, ref))

        return baseImageGit
    }

    fun createBaseImageArguments(
        data: ImageContext,
        ref: BaseImageRef
    ): MutableList<BaseImageArgEntity> {
        return baseImageArgRepository.saveAll(
            data.arguments.map {
                BaseImageArgEntity(
                    name = it.name,
                    description = it.description,
                    stage = BaseImageArgStage.valueOf(it.stage.value.lowercase()),
                    type = BaseImageArgType.valueOf(it.type.value.lowercase()),
                    baseImageRef = ref
                )
            }
        )
    }

    @Transactional(readOnly = true)
    fun findBaseImages(ids: Set<Int>): MutableList<BaseImageRef> {
        return baseImageRefRepository.findAllById(ids)
    }

    @Transactional(readOnly = true)
    fun findLanguages(): List<String> {
        return baseImageRepository.findAllLanguages()
    }

    @Transactional(readOnly = true)
    fun findLanguageVersions(language: String): List<String> {
        return baseImageRepository.findLanguageVersions(language)
    }

    @Transactional(readOnly = true)
    fun findImageBuildTools(language: String, version: String): List<String> {
        return baseImageRepository.findBy(language, version)?.toDomain()
            ?.let { baseImageGitRepository.findAllBuildTools(it.id) }
            ?: listOf()
    }

    @Transactional(readOnly = true)
    fun findBuildToolVersions(buildTool: String): List<String> {
        return baseImageGitRepository.findBuildToolVersions(buildTool)
    }

    @Transactional(readOnly = true)
    fun findArguments(request: BaseImageArgumentRequest): List<BaseImageArg> {
        return when (request.type) {
            BaseImageType.EXE -> baseImageExeRepository.findBy(request.language, request.languageVersion)?.ref
            BaseImageType.GIT -> request.buildTool?.let { buildTool ->
                baseImageGitRepository.findBy(
                    request.language, request.languageVersion,
                    buildTool.name, buildTool.version
                )?.ref
            }
        }?.arguments?.map { it.toDomain() } ?: listOf()
    }


    data class ImageContext(
        val value: String,
        val git: BaseImageRequestGit?,
        val exe: BaseImageRequestExe?,
        val arguments: List<BaseImageArgument>
    )

    fun BaseImageUpdateRequest.toContext() = ImageContext(
        value = this.value,
        git = this.git,
        exe = this.exe,
        arguments = this.arguments
    )

    fun BaseImageRequestDto.toContext() = ImageContext(
        value = this.value,
        git = this.git,
        exe = this.exe,
        arguments = this.arguments
    )
}