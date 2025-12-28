package mk.ukim.finki.dnick.hosting.webdav

import com.github.sardine.DavResource
import com.github.sardine.Sardine
import com.github.sardine.impl.SardineException
import io.github.oshai.kotlinlogging.KotlinLogging
import liquibase.resource.InputStreamList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.*

private val log = KotlinLogging.logger {}

@Component
class WebDavClient(
    private val webDavProperties: WebDavProperties,
    private val sardineClient: Sardine
) {

    private val baseUrl get() = webDavProperties.baseUrl
    private val k8sUrl get() = webDavProperties.k8sUrl


    fun fetchAll(): Set<DavResource> {
        val queue = ArrayDeque(sardineClient.list(baseUrl))
        val passed = queue.map { s -> s.href }.toMutableSet()

        val result = mutableSetOf<DavResource>()
        while (queue.isNotEmpty()) {
            val resource = queue.pop()

            if (resource.isDirectory) {
                sardineClient.list("$baseUrl/${resource.path.removePrefix("/dav")}")
                    .forEach { if (passed.add(it.href)) queue.push(it) }
            } else {
                resource.let { result.add(it) }
            }
        }

        return result
    }

    fun fetch(path: String): DavResource? {
        return sardineClient.list("$baseUrl/${path}").firstOrNull()
    }

    fun getContent(path: String): InputStream? {
        return sardineClient.get("$baseUrl/${path}")
    }

    fun delete(path: String) {
        sardineClient.delete("$baseUrl/${path}")
    }

    fun upload(name: String, path: String, file: MultipartFile): String {
        log.info { "Uploading file $name at: $path" }

        val fileUrl = "$baseUrl/$path/$name"
        createMissingDirectories(path)
        sardineClient.put(fileUrl, file.inputStream)
        log.info { "Uploaded file $name at: $path with internal url: $k8sUrl/$path/$name and external url: $baseUrl/$path/$name" }

        return "$k8sUrl/$path/$name"
    }

    fun createMissingDirectories(path: String) {
        val paths = Stack<String>()
        paths.push(path)

        while (true) {
            val innerPath = paths.pop()
            val lastSection = innerPath.lastIndexOf('/')

            if (!existsPath(innerPath)) {
                paths.push(innerPath)

                if (lastSection < 0) {
                    createAllDirectories(paths.reversed())
                    return
                }

                paths.push(innerPath.substring(0, lastSection))
                continue
            } else {
                createAllDirectories(paths.reversed())
                return
            }
        }
    }

    fun createAllDirectories(paths: List<String>) {
        paths.forEach {
            log.info { "Creating directory $it" }
            sardineClient.createDirectory("$baseUrl/$it")
        }
    }

    fun existsPath(path: String): Boolean {
        try {
            sardineClient.list("$baseUrl/$path")
            return true;
        } catch (e: SardineException) {
            if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
                return false
            }

            throw e;
        }
    }

}