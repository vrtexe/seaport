package mk.ukim.finki.dnick.hosting.registry

import org.springframework.util.MimeType


enum class RegistryMediaType(val value: String) {
    APPLICATION_DOCKER_MANIFEST_V2_JSON("application/vnd.docker.distribution.manifest.v2+json"),
    APPLICATION_DOCKER_MANIFEST_LIST_V2_JSON("application/vnd.docker.distribution.manifest.list.v2+json"),
    APPLICATION_OCI_IMAGE_INDEX_V1_JSON("application/vnd.oci.image.index.v1+json"),
    APPLICATION_OCI_IMAGE_MANIFEST_V1_JSON("application/vnd.oci.image.manifest.v1+json");

    companion object {
        @JvmStatic
        val APPLICATION_DOCKER_MANIFEST_V2_JSON_VALUE = APPLICATION_DOCKER_MANIFEST_V2_JSON.value

        @JvmStatic
        val APPLICATION_DOCKER_MANIFEST_LIST_V2_JSON_VALUE = APPLICATION_DOCKER_MANIFEST_LIST_V2_JSON.value

        @JvmStatic
        val APPLICATION_OCI_IMAGE_INDEX_V1_JSON_VALUE = APPLICATION_OCI_IMAGE_INDEX_V1_JSON.value

        @JvmStatic
        val APPLICATION_OCI_IMAGE_MANIFEST_V1_JSON_VALUE = APPLICATION_OCI_IMAGE_MANIFEST_V1_JSON.value

        @JvmStatic
        fun mimeTypes(): Array<MimeType> {
            return RegistryMediaType.entries.map { MimeType.valueOf(it.value) }.toTypedArray()
        }
    }


}