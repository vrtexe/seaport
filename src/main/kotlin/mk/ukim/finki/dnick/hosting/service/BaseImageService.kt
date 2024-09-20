package mk.ukim.finki.dnick.hosting.service

import mk.ukim.finki.dnick.hosting.model.domain.BaseImageArg
import mk.ukim.finki.dnick.hosting.model.entity.BaseImageType
import mk.ukim.finki.dnick.hosting.model.entity.toDomain
import mk.ukim.finki.dnick.hosting.repository.BaseImageExeRepository
import mk.ukim.finki.dnick.hosting.repository.BaseImageGitRepository
import mk.ukim.finki.dnick.hosting.repository.BaseImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BaseImageService(
    private val baseImageRepository: BaseImageRepository,
    private val baseImageExeRepository: BaseImageExeRepository,
    private val baseImageGitRepository: BaseImageGitRepository
) {

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
    fun findArguments(request: BaseImageRequest): List<BaseImageArg> {
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

}