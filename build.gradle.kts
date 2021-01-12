import java.util.Date

plugins {
    val kotlinVersion = "1.4.21"
    id("java")
    kotlin("jvm")  version(kotlinVersion)
    id("org.jetbrains.dokka") version "1.4.20"
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.5"
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
    jcenter()
    mavenCentral()
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

tasks {
    dokkaHtml.configure {
        outputDirectory.set(File("$buildDir/javadoc"))
    }
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
    dependsOn(tasks.dokkaHtml)
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

val githubUrl = "https://github.com/wThomas84/kotlin-fixture-magic"
val pomDescription = "Allows creation of random instances of (many) arbitrary classes in Kotlin. JVM only"

publishing {
    publications {
        create<MavenPublication>("central") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = rootProject.version.toString()
            // This is the main artifact
            from(components["java"])
            artifact(dokkaJar)
            artifact(sourcesJar)

            pom {
                name.set("kotlin-fixture-magic")
                description.set(pomDescription)
                url.set(githubUrl)

                licenses{
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("datenstrudel")
                        name.set("Thomas Wendzinski")
                        email.set("twx1@gmx.de")
                    }
                }
                scm {
                    connection.set("git@github.com:wThomas84/kotlin-fixture-magic.git")
                    developerConnection.set("git@github.com:wThomas84/kotlin-fixture-magic.git")
                    url.set(project.findProperty("githubUrl").toString())
                }
            }
        }
    }
}

bintray {
    user = project.findProperty("bintrayUsername").toString()
    key = project.findProperty("bintrayApiKey").toString()
    publish = ( rootProject.findProperty("release") == "true" )

    setPublications("central")

    pkg.apply {
        repo = "maven"
        name = project.name
        githubRepo = "wThomas84/kotlin-fixture-magic"
        vcsUrl = "$githubUrl.git"
        setLabels("kotlin", "testing", "reflection", "fixtures", "instantiation")
        setLicenses("Apache-2.0")
        desc = description
        websiteUrl = githubUrl
        issueTrackerUrl = "$githubUrl/issues"
        githubReleaseNotesFile = "$rootDir/README.md"

        version.apply {
            name = rootProject.version.toString()
            desc = description
            released = Date().toString()
//            vcsTag = rootProject.version.toString()
            gpg.sign = publish
            githubReleaseNotesFile = "README.md"
        }
    }
}

