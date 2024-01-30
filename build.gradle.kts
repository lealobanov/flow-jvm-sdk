import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// configuration variables
val defaultGroupId = "com.nftco"
val defaultVersion = "0.7.4-SNAPSHOT"

// other variables

fun getProp(name: String, defaultValue: String? = null): String? {
    return project.findProperty("flow.$name")?.toString()?.trim()?.ifBlank { null }
        ?: project.findProperty(name)?.toString()?.trim()?.ifBlank { null }
        ?: defaultValue
}

group = getProp("groupId", defaultGroupId)!!
version = when {
    getProp("version") !in setOf("unspecified", null) -> { getProp("version")!! }
    getProp("snapshotDate") != null -> { "${defaultVersion.replace("-SNAPSHOT", "")}.${getProp("snapshotDate")!!}-SNAPSHOT" }
    else -> { defaultVersion }
}

plugins {
    id("org.jetbrains.dokka") version "1.9.10"
    kotlin("jvm") version "1.9.22"
    idea
    jacoco
    signing
    `java-library`
    `java-test-fixtures`
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-1"
    id("org.jmailen.kotlinter") version "4.2.0"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.10")

    api("org.onflow:flow:0.21")

    api("com.github.TrustedDataFramework:java-rlp:1.1.20")

    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    api("org.bouncycastle:bcpkix-jdk18on:1.76")

    api(platform("com.fasterxml.jackson:jackson-bom:2.16.1"))
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")

    testApi("org.junit.jupiter:junit-jupiter:5.10.1")
    testApi("org.assertj:assertj-core:3.25.1")

    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.apply {
        jvmTarget = JavaVersion.VERSION_18.toString()
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.apply {
        jvmTarget = JavaVersion.VERSION_18.toString()
        freeCompilerArgs = listOf("-Xjvm-default=all", "-opt-in=com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview")
        allWarningsAsErrors = false
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

tasks {

    test {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showStackTraces = true
            showCauses = true
        }
        finalizedBy("jacocoTestReport")
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            html.required = true
            xml.required = true
            csv.required = false
        }
    }

    jacoco {
        toolVersion = "0.8.11"
    }

    kotlinter {
        reporters = arrayOf("checkstyle", "plain", "html")
    }

    val documentationJar by creating(Jar::class) {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.get().outputs)
    }

    val sourcesJar by creating(Jar::class) {
        dependsOn(classes)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource + sourceSets["testFixtures"].allSource)
    }

    artifacts {
        add("archives", documentationJar)
        add("archives", sourcesJar)
    }

    nexusPublishing {
        repositories {
            sonatype {
                if (getProp("sonatype.nexusUrl") != null) {
                    nexusUrl.set(uri(getProp("sonatype.nexusUrl")!!))
                }
                if (getProp("sonatype.snapshotRepositoryUrl") != null) {
                    snapshotRepositoryUrl.set(uri(getProp("sonatype.snapshotRepositoryUrl")!!))
                }
                if (getProp("sonatype.username") != null) {
                    username.set(getProp("sonatype.username")!!)
                }
                if (getProp("sonatype.password") != null) {
                    password.set(getProp("sonatype.password")!!)
                }
            }
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(project.components["java"])
                artifact(documentationJar)
                artifact(sourcesJar)

                pom {
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    name.set(project.name)
                    url.set("https://onflow.org")
                    description.set("The Flow Blockchain JVM SDK")
                    scm {
                        url.set("https://github.com/onflow/flow")
                        connection.set("scm:git:git@github.com/onflow/flow-jvm-sdk.git")
                        developerConnection.set("scm:git:git@github.com/onflow/flow-jvm-sdk.git")
                    }
                    developers {
                        developer {
                            name.set("Flow Developers")
                            url.set("https://onflow.org")
                        }
                    }
                }
            }
        }
    }

    signing {
        if (getProp("signing.key") != null) {
            useInMemoryPgpKeys(getProp("signing.key"), getProp("signing.password"))
        } else {
            useGpgCmd()
        }
        sign(publishing.publications)
    }
}
