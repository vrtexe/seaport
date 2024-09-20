package mk.ukim.finki.dnick.hosting.image

import org.springframework.stereotype.Component

@Component
class ImageCache(private var images: Map<String, Image> = mapOf()) {

    data class Image(val name: String, val args: List<ImageArg>, val value: String)
    data class ImageArg(val name: String, val type: String)

    fun get(image: String): Image? {
        return images.get(image)
    }

    fun update(images: Map<String, Image>) {
        this.images = images;
    }
}