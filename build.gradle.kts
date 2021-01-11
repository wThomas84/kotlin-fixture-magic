plugins {
    val kotlinVersion = "1.4.21"
    id("java")
    kotlin("jvm")  version(kotlinVersion)
//    id("org.jetbrains.dokka") version (kotlinVersion)
    id("org.kordamp.gradle.kotlin-project") version "0.40.0"
    id("org.kordamp.gradle.bintray") version "0.40.0"
}

group = "net.datenstrudel"
version = "0.1.0"


val jdkVersion = "11"

if (!project.hasProperty("bintrayUsername"))    extra.apply{set ("bintrayUsername", "**undefined**")}
if (!project.hasProperty("bintrayApiKey"))      extra.apply{set ("bintrayApiKey", "**undefined**")}
if (!project.hasProperty("sonatypeUsername"))   extra.apply{set ("sonatypeUsername", "**undefined**")}
if (!project.hasProperty("sonatypePassword"))   extra.apply{set ("sonatypePassword", "**undefined**")}
if (!project.hasProperty("release"))            extra.apply{set ("release", "false")}


repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

license{
    this.ignoreFailures = true
}

config {
    release = ( rootProject.findProperty("release") == "true" )

    info {
        name          = "kotlin-fixture-magic"
        description   = "Allows creation of random instances of (many) arbitrary classes in Kotlin. JVM only"
        vendor        = "datenstrudel"
        inceptionYear = "2021"
        tags          = listOf("kotlin", "testing", "reflection", "fixtures", "instantiation")

        links {
            website      = "https://github.com/wThomas84/kotlin-fixture-magic"
            issueTracker = "https://github.com/wThomas84/kotlin-fixture-magic/issues"
            scm          = "https://github.com/wThomas84/kotlin-fixture-magic.git"
        }

        credentials {
            sonatype {
                val sonatypeUsername: String by extra
                val sonatypePassword: String by extra
                username = sonatypeUsername
                password = sonatypePassword
            }
        }

        people {
            person {
                id    = "datenstrudel"
                name  = "Thomas Wendzinski"
                roles = listOf("developer")
            }
        }
    }

    licensing {
        licenses {
            license {
                id = "Apache-2.0"
            }
        }
    }

    bintray {
        userOrg      = "datenstrudel"
        name         = rootProject.name
        publish      = config.release
        credentials {
            val bintrayUsername: String by extra
            val bintrayApiKey: String by extra
            username = bintrayUsername
            password = bintrayApiKey
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-allopen")
    implementation("org.jetbrains.kotlin:kotlin-noarg")

    implementation("org.reflections:reflections:0.9.12")

    api("org.slf4j:slf4j-api:1.7.25")

    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testImplementation("org.assertj:assertj-core:3.6.1")

    testImplementation("org.slf4j:slf4j-simple:1.7.25")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

//configure<JavaPluginConvention> {
//    sourceCompatibility = JavaVersion.VERSION_11
//    targetCompatibility = JavaVersion.VERSION_11
//}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = jdkVersion
    targetCompatibility = jdkVersion
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = jdkVersion
    }
}
