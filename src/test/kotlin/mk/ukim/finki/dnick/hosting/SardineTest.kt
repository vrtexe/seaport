package mk.ukim.finki.dnick.hosting

import com.github.sardine.Sardine
import com.github.sardine.SardineFactory
import com.github.sardine.impl.SardineException
//import mk.ukim.finki.dnick.hosting.controller.WebDavClient
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.*

class SardineTest {
    val WEBDAV_URL: String = "http://localhost/webdav/dav"

    @Test
    fun testMe() {
        val sardine = SardineFactory.begin()

        val path = "app/test"

        createMissingDirectories(path, sardine)
//        sardine.put("${WebDavClient.WEBDAV_URL}/$path/file.txt", "File Contents\nTest me".toByteArray())
    }

    fun createMissingDirectories(path: String, sardineClient: Sardine) {
        val paths = Stack<String>()
        paths.push(path)

        while (true) {
            val innerPath = paths.pop()
            val lastSection = innerPath.lastIndexOf('/')

            if (!existsPath(innerPath, sardineClient)) {
                paths.push(innerPath)

                if (lastSection < 0) {
                    createAllDirectories(paths.reversed(), sardineClient)
                    return
                }

                paths.push(innerPath.substring(0, lastSection))
                continue
            } else {
                createAllDirectories(paths.reversed(), sardineClient)
                return
            }
        }
    }

    fun createAllDirectories(paths: List<String>, sardineClient: Sardine) {
        paths.forEach {
            sardineClient.createDirectory("$WEBDAV_URL/$it")
        }
    }

    fun existsPath(path: String, sardineClient: Sardine): Boolean {
        try {
            sardineClient.list("$WEBDAV_URL/$path")
            return true;
        } catch (e: SardineException) {
            if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
                return false
            }

            throw e;
        }
    }
}