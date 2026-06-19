// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.0")
        // Plugin do Google Services (necessário para Firebase)
        classpath("com.google.gms:google-services:4.4.2")
    }
}

// Nota: não coloque repositories aqui fora do buildscript,
// pois agora usamos o settings.gradle.kts para gerenciar repositórios.

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}