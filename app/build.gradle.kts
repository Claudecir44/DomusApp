plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
}

import java.util.Properties
        import java.io.FileInputStream

        android {
            namespace = "com.cjstudio.condominio_sociedade_morro_grande"
            compileSdk = 34

            defaultConfig {
                applicationId = "com.cjstudio.condominio_sociedade_morro_grande"
                minSdk = 24
                targetSdk = 34
                versionCode = 1
                versionName = "1.0"

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                val localProperties = Properties()
                val localPropertiesFile = rootProject.file("local.properties")
                if (localPropertiesFile.exists()) {
                    FileInputStream(localPropertiesFile).use { localProperties.load(it) }
                }

                buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""}\"")
                buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL") ?: ""}\"")
            }

            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }

            kotlinOptions {
                jvmTarget = "1.8"
            }

            buildFeatures {
                buildConfig = true
                viewBinding = true
            }

            flavorDimensions += "version"

            productFlavors {
                create("admin") {
                    dimension = "version"
                    applicationIdSuffix = ".admin"
                    versionNameSuffix = "-admin"
                    resValue("string", "app_name", "Admin")
                    buildConfigField("String", "PERFIL", "\"admin\"")
                }
                create("morador") {
                    dimension = "version"
                    applicationIdSuffix = ".morador"
                    versionNameSuffix = "-morador"
                    resValue("string", "app_name", "Morador")
                    buildConfigField("String", "PERFIL", "\"morador\"")
                }
            }

            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    excludes += "META-INF/DEPENDENCIES"
                }
            }
        }

dependencies {
    // Android Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    // Lifecycle & Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.2.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.2.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.2.0")
    implementation("io.github.jan-tennert.supabase:functions-kt:2.2.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.2.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.ktor:ktor-client-okhttp:2.3.5")

    // JSON
    implementation("org.json:json:20231013")
    implementation("com.google.code.gson:gson:2.10.1")

    // Biometric
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}