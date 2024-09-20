package mk.ukim.finki.dnick.hosting.image

import com.google.gson.reflect.TypeToken.getParameterized
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.BatchV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Job
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.util.Watch
import io.kubernetes.client.util.Watch.Response
import org.springframework.stereotype.Component
import java.util.*

private val log = KotlinLogging.logger {}

@Component
class ImageJobManager(
    private val batchV1Api: BatchV1Api,
    private var coreV1Api: CoreV1Api,
    private var client: ApiClient,
    private val imageJobBuilder: ImageJobBuilder,
    private val properties: ImageBuilderProperties,
) {

    fun buildImageV5(jobProperties: ImageJobProperties): V1Job {
        return buildImage(jobProperties)
    }

    fun buildImageV4(): V1Job {
        val imageName = "nginx-test"
        val imageTag = "1.0.0"

        return buildImage(
            ImageJobProperties(
                name = imageName,
                version = imageTag,
                content = """
                FROM curlimages/curl:8.8.0 as downloader
            
                ARG HASH
                ARG URL
            
                WORKDIR /download
            
                ENV HASH=${'$'}{HASH}
                ENV URL=${'$'}{URL}
                RUN echo ${'$'}{URL}
                RUN echo ${'$'}{HASH}
                RUN curl "${'$'}{URL}" > app.zip
            
                RUN mkdir app
                RUN unzip app.zip -d app
            
                RUN if [[ "${'$'}(ls -l app | tail -n +2 | wc -l | xargs)" = 1 && "${'$'}(ls -ld app/*/ | wc -l | xargs)" = 1 ]]; \
                    then                       \
                        folder=${'$'}(ls app);      \
                        mv app/${'$'}folder/* app/; \
                        rm -r app/${'$'}folder;     \
                    fi;
            
                FROM nginx:1.27.1-alpine
            
                ARG PORT=80
            
                RUN echo -e "\
                server { \\\\n\
                    listen ${'$'}PORT;\\\\n\
                    root /var/www;\\\\n\
                    index index.html index.htm;\\\\n\
                \\\\n\
                    location / {\\\\n\
                      try_files \${'$'}uri.html \${'$'}uri \${'$'}uri/ index.html;\\\\n\
                    }\\\\n\
                }" > /etc/nginx/conf.d/default.conf
                COPY --from=downloader /download/app /var/www
            
                CMD ["nginx", "-g", "daemon off;"]""".trimIndent(),
                buildArguments = mapOf(
                    "HASH" to "me",
                    "URL" to "http://webdav-service/dav/app.zip"
                )
            )
        )
    }


    fun buildImageV3(): V1Job {
        val imageName = "nginx"
        val imageTag = "test1"

        return buildImage(
            ImageJobProperties(
                name = imageName,
                version = imageTag,
                content = """
                        FROM nginx:latest
                        ENV HASH='${UUID.randomUUID()}'
                        EXPOSE 80 
                        EXPOSE 443 
                        CMD ["sleep", "infinity"]
                        """.trimIndent(),
                buildArguments = mapOf(Pair("HASH", "2"))
            )
        )
    }


    fun buildImage(properties: ImageJobProperties): V1Job {
        log.info { "Building image: ${properties.name}:${properties.version}" }

        return imageJobBuilder.newImageBuilder(properties).let {
            startJob(it)
            waitForJob(properties.key)
            it
        }

    }

    fun startJob(job: V1Job) {
        log.info { "Creating image builder job" }
        batchV1Api.createNamespacedJob(job.metadata.namespace, job).execute()
        log.info { "Created image builder job" }
    }

    fun waitForJob(image: String): Boolean {
        log.info { "Waiting for image build: $image" }
        return Watch.createWatch<V1Pod>(
            client,
            coreV1Api.listPodForAllNamespaces().watch(true)
                .labelSelector(selectorOf(image))
                .buildCall(null),
            getParameterized(Response::class.java, V1Pod::class.java).type
        ).use {
            isPodCompleted(it)
        }
    }

    fun isPodCompleted(w: Watch<V1Pod>): Boolean {
        for (pod in w) {
            val podStatus = pod.`object`.getTerminatedStatus()
            if (podStatus != null) {
                if (podStatus == CompletedReason || podStatus == ErrorReason) {
                    log.info { "Image build finished with status: $podStatus" }
                }

                return podStatus == CompletedReason || podStatus == ErrorReason
            }
        }

        return false
    }


    fun selectorOf(imageValue: String): String {
        return "${properties.labels.app.toSelector()},${properties.labels.image.copy(value = imageValue).toSelector()}"
    }


}