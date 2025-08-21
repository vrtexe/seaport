package mk.ukim.finki.dnick.hosting.serviceregistry.api

const val APP_LABEL = "app"


fun selectorOf(data: Pair<String, String>): String {
    return data.toSelector()
}

fun Pair<String, String>.toSelector() = "${this.first}=${this.second}"