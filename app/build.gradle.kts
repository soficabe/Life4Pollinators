import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.example.life4pollinators"
    compileSdk = 35

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }

    val key = localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""
    val url = localProperties.getProperty("SUPABASE_URL") ?: ""
    val googleClientId = localProperties.getProperty("GOOGLE_CLIENT_ID") ?: ""

    defaultConfig {
        applicationId = "com.example.life4pollinators"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String","SUPABASE_ANON_KEY","\"$key\"")
        buildConfigField("String","SUPABASE_URL","\"$url\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        disable += "NullSafeMutableLiveData"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //dipendenza per libreria icone material estese
    implementation(libs.androidx.material.icons.extended)

    //dipendenze per navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.ktor.serialization.kotlinx.json)

    //dipendenze per viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    //dipendenza per datastore
    implementation(libs.androidx.datastore.preferences)

    //dipendenza per dependency-injection con koin
    implementation(libs.koin.androidx.compose)

    //dipendenze per supabase
    implementation(platform(libs.bom))
    implementation(libs.postgrest.kt)
    implementation(libs.auth.kt)
    implementation(libs.compose.auth)
    implementation(libs.compose.auth.ui)
    implementation(libs.storage.kt)
    implementation(libs.ktor.client.android)

    //dipendenze per google signin
    implementation (libs.play.services.auth)
    implementation (libs.androidx.credentials)
    implementation(libs.googleid)

    //dipendenza per coil (per usare AsyncImage)
    implementation(libs.coil.compose)

    //dipendenza material3
    implementation(libs.material3)

    //dipendenze per OSM (OpenStreetMap)
    implementation(libs.osmdroid.android)

    //dipendenze per Google Play Services Location
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
}