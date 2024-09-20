package mk.ukim.finki.dnick.hosting.builder

const val APP_LABEL = "app"
const val RESOURCE_NAME = "name"
const val CONTAINER_SUFFIX = "container"
const val DEPLOYMENT_SUFFIX = "deployment"

const val INTERNAL_REGISTRY = "internal.io"


enum class ApiVersion(val value: String) {
    CORE_V1("v1"),
    APPS_V1("apps/v1"),
    NETWORKING_V1("networking.k8s.io/v1");

    override fun toString(): String {
        return this.value
    }
}

enum class Kind(val value: String) {
    NAMESPACE("Namespace"),
    CONFIG_MAP("ConfigMap"),
    SERVICE("Service"),
    INGRESS("Ingress"),
    POD("Pod");

    override fun toString(): String {
        return this.value
    }
}