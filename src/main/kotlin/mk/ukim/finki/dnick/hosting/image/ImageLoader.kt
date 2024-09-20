package mk.ukim.finki.dnick.hosting.image

import mk.ukim.finki.dnick.hosting.image.ImageCache.Image
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.nio.charset.Charset


@Component
class ImageLoader(images: ImageCache) {

    init {
        images.update(loadImage())
    }

    companion object {
        fun loadImage(): Map<String, Image> {
            ClassPathResource("templates/java/exe/curl/Dockerfile")
            ClassPathResource("templates/java/git/maven/Dockerfile")
            ClassPathResource("templates/java/git/gradle/Dockerfile")
            return mapOf(
                Pair(
                    "java-exe", Image(
                        name = "Java Executable",
                        value = ClassPathResource("templates/java/exe/curl/Dockerfile").getContentAsString(Charset.defaultCharset()),
                        args = listOf()
                    )
                ),
                Pair(
                    "java-git-maven", Image(
                        name = "Java Maven Git",
                        value = ClassPathResource("templates/java/git/maven/Dockerfile").getContentAsString(Charset.defaultCharset()),
                        args = listOf()
                    )
                ),
                Pair(
                    "java-git-gradle", Image(
                        name = "Gradle Git",
                        value = ClassPathResource("templates/java/git/gradle/Dockerfile").getContentAsString(Charset.defaultCharset()),
                        args = listOf()
                    )
                )

            )
        }
    }

}