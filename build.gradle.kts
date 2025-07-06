import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("java")
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.hibernate.orm") version "6.5.2.Final"
    id("org.openapi.generator") version "7.11.0"

//    id("org.graalvm.buildtools.native") version "0.10.2"
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.spring") version "2.0.10"
    kotlin("plugin.jpa") version "2.0.10"

}

group = "mk.ukim.finki.dnick"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

sourceSets {
    main {
        kotlin {
            srcDir("${layout.buildDirectory.get()}/generated/src/main/kotlin")
        }
    }
}

repositories {
    mavenCentral()
}

configurations.all {
    exclude("org.slf4j", "slf4j-simple")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.liquibase:liquibase-core")
    implementation("io.kubernetes:client-java:24.0.0")
    implementation("io.kubernetes:client-java-extended:24.0.0")
    implementation("com.github.lookfirst:sardine:5.12")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.8.2")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")
    implementation("io.swagger.parser.v3:swagger-parser:2.1.25")
    implementation("org.springframework.boot:spring-boot-starter-security")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")

    implementation("io.github.oshai:kotlin-logging-jvm:5.1.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

//    implementation("io.kubernetes:client-java-spring-integration:20.0.1")
//    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-fabric8-all:3.1.1")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

hibernate {
    enhancement {
        enableAssociationManagement.set(true)
    }
}

tasks.register<GenerateTask>("generateApi") {
    generatorName.set("kotlin-spring")
    inputSpec.set("$rootDir/src/main/resources/api.yaml")
    outputDir.set("${rootProject.layout.buildDirectory.get()}/generated")
    apiPackage.set("mk.ukim.finki.dnick.hosting.generated.api")
    modelPackage.set("mk.ukim.finki.dnick.hosting.generated.model")
    generateAliasAsModel.set(true)
    supportingFilesConstrainedTo.set(listOf())

    configOptions.apply {
        put("useSpringBoot3", "true")
        put("skipDefaultInterface", "true")
        put("useTags", "true")
        put("requestMappingMode", "api_interface")
        put("interfaceOnly", "true")
        put("exceptionHandler", "false")
    }

    typeMappings.apply {
        put("object+pageable", "Pageable")
        put("object+sort", "Sort")
    }

    schemaMappings.apply {
        put("Pageable","org.springframework.data.domain.Pageable")
        put("Sort", "org.springframework.data.domain.Sort")
    }

    configOptions.put("dateLibrary", "java8")
}