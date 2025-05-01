import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    id("java")
    `java-library`
    id("com.vanniktech.maven.publish") version "0.31.0"
}

group = project.properties["GROUP"].toString()
version = project.properties["VERSION_NAME"].toString()

java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}

repositories {
    mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("org.mockito:mockito-core:5.17.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    mockitoAgent("org.mockito:mockito-core:5.17.0") { isTransitive = false }
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to version
            )
        )
    }
}

tasks.test {
    jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}")

    useJUnitPlatform()
    testLogging {
        events(
            FAILED,
            STANDARD_ERROR,
            SKIPPED,
            PASSED
        )
        exceptionFormat = FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.DEFAULT)
    signAllPublications()
}
