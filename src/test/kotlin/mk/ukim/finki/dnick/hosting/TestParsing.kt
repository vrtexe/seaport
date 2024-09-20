package mk.ukim.finki.dnick.hosting

import org.junit.jupiter.api.Test
import org.springframework.boot.json.GsonJsonParser

class TestParsing {

    val data = """
    {
      "images": {
        "option": "Language",
        "options": [
          {
            "id": "java",
            "name": "Java",
            "option": "Version",
            "options": [
              {
                "id": "21",
                "name": "21",
                "options": [
                  {
                    "id": "exe",
                    "name": "executable",
                    "options": [
                      {
                        "id": "default",
                        "name": "Default",
                        "image": "FROM curlimages/curl:8.8.0 as downloader\n\nARG HASH\nARG URL\n\nWORKDIR /download\n\nENV HASH=${'$'}{HASH}\nRUN curl ${'$'}URL > app.jar\n\n\nFROM eclipse-temurin:21-alpine as jrebuilder\n\nWORKDIR build\n\nRUN jlink \\\n     --add-modules ALL-MODULE-PATH \\\n     --strip-debug \\\n     --no-man-pages \\\n     --no-header-files \\\n     --output jre\n\n\nFROM alpine:3.20.2\n\nCOPY --from=jrebuilder /build/jre jre\nCOPY --from=downloader  /download/app.jar .\n\nENV JAVA_ARGS ''\nENV JAR_ARGS ''\n\nENV JAVA_HOME /jre\nENV PATH ${'$'}JAVA_HOME/bin:${'$'}PATH\n\nCMD java ${'$'}JAVA_ARGS -jar ${'$'}JAR_ARGS app.jar\n"
                      }
                    ]
                  },
                  {
                    "id": "git",
                    "name": "git",
                    "options": [
                      {
                        "id": "maven",
                        "name": "Maven",
                        "image": "FROM alpine/git:2.45.2 as src\n\nWORKDIR /data\n\nARG HASH\nARG URL\n\nENV HASH=${'$'}{HASH}\nRUN git clone ${'$'}URL src\n\n\nFROM maven:3.9.8-eclipse-temurin-21-alpine as builder\n\nARG JAR_PATH\nARG BASE_DIRECTORY=\".\"\nARG BUILD_ARGS=''\n\nWORKDIR /data\n\nCOPY --from=src /data/src src\n\nWORKDIR /data/src/${'$'}BASE_DIRECTORY\n\nRUN mvn ${'$'}BUILD_ARGS clean install\nRUN mv ${'$'}JAR_PATH /data/app.jar\n\n\nFROM eclipse-temurin:21-alpine as jrebuilder\n\nWORKDIR build\n\nRUN jlink \\\n     --add-modules ALL-MODULE-PATH \\\n     --strip-debug \\\n     --no-man-pages \\\n     --no-header-files \\\n     --output jre\n\n\nFROM alpine:3.20.2\n\nCOPY --from=jrebuilder /build/jre jre\nCOPY --from=builder  /data/app.jar app.jar\n\nENV JAVA_ARGS ''\nENV JAR_ARGS ''\n\nENV JAVA_HOME /jre\nENV PATH ${'$'}JAVA_HOME/bin:${'$'}PATH\n\nCMD java ${'$'}JAVA_ARGS -jar ${'$'}JAR_ARGS app.jar"
                      },
                      {
                        "id": "gradle",
                        "name": "Gradle",
                        "image": "FROM alpine/git:2.45.2 as src\n\nWORKDIR /data\n\nARG HASH\nARG URL\n\nENV HASH=${'$'}{HASH}\nRUN git clone ${'$'}URL src\n\n\nFROM gradle:8.9.0-jdk21-alpine as builder\n\nARG JAR_PATH\nARG BASE_DIRECTORY=\".\"\nARG BUILD_ARGS=''\n\nWORKDIR /data\n\nCOPY --from=src /data/src src\n\nWORKDIR /data/src/${'$'}BASE_DIRECTORY\n\nRUN gradle ${'$'}BUILD_ARGS clean build\nRUN mv ${'$'}JAR_PATH /data/app.jar\n\n\nFROM eclipse-temurin:21-alpine as jrebuilder\n\nWORKDIR build\n\nRUN jlink \\\n     --add-modules ALL-MODULE-PATH \\\n     --strip-debug \\\n     --no-man-pages \\\n     --no-header-files \\\n     --output jre\n\n\nFROM alpine:3.20.2\n\nCOPY --from=jrebuilder /build/jre jre\nCOPY --from=builder  /data/app.jar app.jar\n\nENV JAVA_ARGS ''\nENV JAR_ARGS ''\n\nENV JAVA_HOME /jre\nENV PATH ${'$'}JAVA_HOME/bin:${'$'}PATH\n\nCMD java ${'$'}JAVA_ARGS -jar ${'$'}JAR_ARGS app.jar"
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    }""".trimIndent()

//    data class ImageFilter(
//        val option: String
//    )

    @Test
    fun test() {
        val res = GsonJsonParser().parseMap(data)

        println(res)
    }
}