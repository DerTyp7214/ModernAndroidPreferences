import io.gitlab.arturbosch.detekt.Detekt
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    alias(libs.plugins.detekt)
    alias(libs.plugins.binarycompatibilityvalidator)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.testlogger)
    `maven-publish`
    signing
}

// Versions
val libraryVersion = "2.3.1"
val libraryGroup = "de.maxr1998"
val libraryName = "modernandroidpreferences"
val prettyLibraryName = "ModernAndroidPreferences"

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$projectDir/detekt.yml")
    autoCorrect = true
}

android {
    namespace = "de.Maxr1998.modernpreferences"
    compileSdk = 31
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            lint {
                disable.add("MissingTranslation")
                disable.add("ExtraTranslation")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_15
        targetCompatibility = JavaVersion.VERSION_15
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_15.toString()
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += listOf("-module-name", libraryName)
    }
    lint {
        abortOnError = false
        sarifReport = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)

    // UI
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime)
    implementation(Dependencies.Core.appCompat)
    implementation(Dependencies.Core.coreKtx)
    implementation(Dependencies.UI.constraintLayout)
    implementation(Dependencies.UI.recyclerView)
    implementation(Dependencies.UI.googleMaterial)

    // Testing
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk)

    // Detekt formatting
    detektPlugins(libs.detekt.formatting)
}

tasks {
    withType<Detekt> {
        reports {
            html.required.set(true)
            xml.required.set(false)
            txt.required.set(true)
            sarif.required.set(true)
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

var ossrhUsername: String? = null
var ossrhPassword: String? = null
var githubToken: String? = null
val propFile = project.file("key.properties")
if (propFile.exists()) {
    val props = Properties()
    props.load(FileInputStream(propFile))
    ext["signing.gnupg.keyName"] = props["signingKeyName"] as? String
    ossrhUsername = props["ossrhUsername"] as? String
    ossrhPassword = props["ossrhPassword"] as? String
    githubToken = props["githubToken"] as? String
}

// Maven publishing config
publishing {
    publications {
        register("production", MavenPublication::class) {
            groupId = libraryGroup
            artifactId = libraryName
            version = libraryVersion
            artifact("$buildDir/outputs/aar/library-release.aar")
            artifact(sourcesJar.get())

            pom {
                name.set(prettyLibraryName)
                description.set("Android Preferences defined through Kotlin DSL, shown in a RecyclerView")
                url.set("https://github.com/Maxr1998/ModernAndroidPreferences")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("Maxr1998")
                        name.set("Max Rumpf")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/Maxr1998/ModernAndroidPreferences.git")
                    developerConnection.set("scm:git:ssh://github.com/Maxr1998/ModernAndroidPreferences.git")
                    url.set("https://github.com/Maxr1998/ModernAndroidPreferences/tree/master")
                }
                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")
                    configurations.implementation.get().allDependencies.forEach {
                        val dependencyNode = dependenciesNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", it.group)
                        dependencyNode.appendNode("artifactId", it.name)
                        dependencyNode.appendNode("version", it.version)
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
        maven {
            name = "GitHubPackages"
            setUrl("https://maven.pkg.github.com/Maxr1998/ModernAndroidPreferences")
            credentials {
                username = "Maxr1998"
                password = githubToken
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}