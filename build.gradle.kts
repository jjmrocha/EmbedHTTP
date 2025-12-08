import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    id("java")
    `java-library`
    jacoco
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

var junitVersion = "6.0.1"
var assertjVersion = "3.27.6"
var mockitoVersion = "5.20.0"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    mockitoAgent("org.mockito:mockito-core:$mockitoVersion") { isTransitive = false }
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

    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required = false
        csv.required = true
    }

    dependsOn(tasks.test) // tests are required to run before generating the report
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
