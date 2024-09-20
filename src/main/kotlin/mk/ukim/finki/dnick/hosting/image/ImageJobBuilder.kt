package mk.ukim.finki.dnick.hosting.image

import io.kubernetes.client.openapi.models.*
import mk.ukim.finki.dnick.hosting.builder.cleanupK8sName
import org.springframework.stereotype.Component

@Component
class ImageJobBuilder(private val properties: ImageBuilderProperties) {

    private companion object {
        const val API_VERSION = "batch/v1"
        const val KIND = "Job"

        const val DOCKER_FILE = "--dockerfile=/workdir/ContainerFile"
        const val DESTINATION = "--destination=%s/%s:%s"
        const val INSECURE = "--insecure"
        const val BUILD_ARG = "--build-arg=%s=%s"

        const val WorkdirVolumeName = "workdir"
        val WorkdirVolumeMount = V1VolumeMount()
            .name(WorkdirVolumeName)
            .mountPath("/workdir")
    }


    fun newImageBuilder(image: ImageJobProperties): V1Job {
        return V1Job()
            .apiVersion(API_VERSION)
            .kind(KIND)
            .metadata(
                V1ObjectMeta()
                    .name("${properties.jobPrefix}-${image.key}".cleanupK8sName())
                    .namespace(properties.namespace)
            )
            .spec(
                V1JobSpec()
                    .ttlSecondsAfterFinished(5)
                    .template(
                        V1PodTemplateSpec()
                            .metadata(
                                V1ObjectMeta()
                                    .name(properties.podName.cleanupK8sName())
                                    .labels(createLabels(image.key))
                            )
                            .spec(
                                V1PodSpec()
                                    .restartPolicy("Never")
                                    .initContainers(listOf(fileLoaderContainer(image)))
                                    .containers(listOf(builderContainer(image)))
                                    .volumes(
                                        listOf(
                                            V1Volume().name(WorkdirVolumeName).emptyDir(V1EmptyDirVolumeSource())
                                        )
                                    )
                            )
                    )
            )
    }

    private fun createLabels(imageName: String): Map<String, String> {
        return mapOf(
            properties.labels.app.toPair(),
            properties.labels.image.copy(value = imageName).toPair()
        )
    }

    private fun fileLoaderContainer(image: ImageJobProperties) =
        V1Container()
            .name("load-container-file")
            .image("busybox:1.36.1-musl")
            .command(
                listOf(
                    "sh", "-c",
                    "echo -e \"${'$'}CONTAINER_FILE\" > /workdir/ContainerFile"
                )
            )
            .env(
                listOf(
                    V1EnvVar().name("CONTAINER_FILE").value(image.content)

                )
            )
            .volumeMounts(listOf(WorkdirVolumeMount))


    private fun builderContainer(image: ImageJobProperties) =
        V1Container()
            .name("kaniko")
            .image("gcr.io/kaniko-project/executor:v1.23.2")
            .args(
                listOf(
                    DOCKER_FILE,
                    DESTINATION.format(properties.registry, image.name, image.version),
                    INSECURE,
                    *image.buildArguments.map {
                        BUILD_ARG.format(it.key, it.value)
                    }.toTypedArray()
                )
            )
            .volumeMounts(listOf(WorkdirVolumeMount))

}