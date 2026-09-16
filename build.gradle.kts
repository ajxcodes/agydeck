plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

allprojects {
    group = "com.ajxcodes.agydeck"
    version = "0.1.0-alpha"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
    }

    dependencies {
        add("detektPlugins", rootProject.libs.detekt.formatting)
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    kover(project(":domain"))
    kover(project(":application"))
    kover(project(":infrastructure"))
    kover(project(":presentation"))
}

kover {
    reports {
        verify {
            rule {
                minBound(80)
            }
        }
    }
}
