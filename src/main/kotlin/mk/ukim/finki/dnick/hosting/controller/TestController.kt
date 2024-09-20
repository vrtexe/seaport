package mk.ukim.finki.dnick.hosting.controller

import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*
import mk.ukim.finki.dnick.hosting.image.*
import mk.ukim.finki.dnick.hosting.model.domain.Application
import mk.ukim.finki.dnick.hosting.model.domain.Namespace
import mk.ukim.finki.dnick.hosting.model.entity.toDomain
import mk.ukim.finki.dnick.hosting.repository.NamespaceRepository
import mk.ukim.finki.dnick.hosting.service.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = ["*"])
class TestController(
    private val client: ApiClient,
    private val coreV1Api: CoreV1Api,
    private val namespaceRepository: NamespaceRepository,
    private val applicationPersistenceService: ApplicationPersistenceService,
    private val imageBuilderService: ImageBuilderService,
    private val pipelineService: PipelineService,
) {


    @PostMapping("test-image-build")
    fun test123(@RequestPart("file") file: MultipartFile) {
        imageBuilderService.createImage(
            ImageExeParams(
                data = ImageData("ngtest", "1.0.0"),
                file = file,
                base = ImageBaseParams("nginx", "1.27.1"),
                buildArgs = mapOf(),
                namespace = "test-me",
                uid = UUID.randomUUID().toString()
            )
        )
    }

    @GetMapping("test/app")
    fun createTestApp(): Application? {
        return applicationPersistenceService.create()
    }

    @PostMapping("test-upd")
    fun buildTest(

        @RequestPart("files") files: List<MultipartFile>
    ) {
        for (file in files) {
            println(file.originalFilename)
        }
    }

    @PostMapping("/image/exe/create")
    fun buildImageFromExecutable(
        @RequestParam socket: String?,
        @RequestParam request: ImagesExeRequest,
        @RequestParam body: ApplicationRequest,
        @RequestPart files: List<MultipartFile>
    ) {
        val dto = files
            .map {
                request.images[it.originalFilename]?.toParams(it, request)
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
            .let { body.toDto(it) }

        pipelineService.buildAndDeploy(dto)
    }

    @PostMapping("/image/git/create")
    fun buildImageFromExecutable(@RequestBody body: ApplicationDto) {
        pipelineService.buildAndDeploy(body)
    }

    fun ImageExeRequest.toParams(file: MultipartFile, request: ImagesExeRequest) = ImageExeParams(
        namespace = request.namespace,
        file = file,
        base = this.base,
        uid = this.uid,
        data = this.data,
        buildArgs = this.buildArgs
    )

    @GetMapping("test/namespace")
    fun getNamespace(): Namespace? {
        return namespaceRepository.findAll().first()?.toDomain()
    }

    @GetMapping("test/namespaces")
    fun getNamespaces(): List<Namespace> {
        return namespaceRepository.findAll().map { it.toDomain() }
    }

    fun cleanupBuildArguments(params: Map<String, String>): Map<String, String> {
        return params
            .filter { it.key == it.key.uppercase() }
            .toMap()
    }

    @GetMapping("namespace")
    fun getNamespaceStr(): String {
        return namespaceRepository.findAll().get(0).toString()
    }

    @GetMapping("pods")
    fun listPods(): List<String> {
        return fetchPods()
    }

    fun fetchPods(): List<String> {

//        coreV1Api.listNamespacedService("").labelSelector("")
//client.
//        appsV1Api.create
//        appsV1Api.createNamespacedDeployment("project-namespace", V1Deployment()
//            .metadata(V1ObjectMeta().name("asd"))
//        )
//CustomObjectsApi

//        DynamicKubernetesApi().create()
//DynamicKubernetesObject()
//        NetworkingV1Api
//        V1IngressRules

        val ingres = V1Ingress()
            .metadata(
                V1ObjectMeta()
                    .name("app-test-ingress")
                    .annotations(
                        mapOf(
                            Pair("nginx.ingress.kubernetes.io/rewrite-target", "/$2")
                        )
                    )
            )
            .spec(
                V1IngressSpec().rules(
                    listOf(
                        V1IngressRule().http(
                            V1HTTPIngressRuleValue().paths(
                                listOf(
                                    V1HTTPIngressPath()
                                        .pathType("Prefix")
                                        .path("/app1(/|$)(.*)")
                                        .backend(
                                            V1IngressBackend().service(
                                                V1IngressServiceBackend()
                                                    .name("application-source-service")
                                                    .port(
                                                        V1ServiceBackendPort().number(8080)
                                                    )
                                            )
                                        )

                                )
                            )
                        )
                    )
                )
            )

//        appsV1Api.listDeploymentForAllNamespaces()
//            .execute().items.forEach {
//
//            }


        println(ingres.toJson())
//        networkingV1Api.createNamespacedIngress("default", ingres).execute()
//        V1IngressClass()
//            .spec(V1IngressClassSpec().parameters(V1IngressClassParametersReference()))
        return coreV1Api.listPodForAllNamespaces().execute().items
            .map { it.metadata }
            .map { it.name };
    }

}

