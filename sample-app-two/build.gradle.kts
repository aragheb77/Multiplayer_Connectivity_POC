plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.ragheb.sampletwo"
    compileSdk = 35
    defaultConfig { applicationId = "com.ragheb.localpoc.consumer2"; minSdk = 24; targetSdk = 35; versionCode = 1; versionName = "0.1-poc" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation(project(":connectivity")); implementation("androidx.appcompat:appcompat:1.7.1") }
