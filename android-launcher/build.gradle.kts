plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.jpastore.box"
    compileSdk = 35
    buildFeatures { buildConfig = true }
    defaultConfig { applicationId = "com.jpastore.box"; minSdk = 23; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
    signingConfigs {
        create("cloudRelease") {
            val keyPath = System.getenv("JPA_KEYSTORE_PATH")
            if (!keyPath.isNullOrBlank()) {
                storeFile = file(keyPath); storePassword = System.getenv("JPA_STORE_PASSWORD")
                keyAlias = System.getenv("JPA_KEY_ALIAS"); keyPassword = System.getenv("JPA_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        debug { applicationIdSuffix = ".debug"; versionNameSuffix = "-TESTE"; resValue("string", "app_name", "JPA BOX • TESTE") }
        release { signingConfig = signingConfigs.getByName("cloudRelease"); resValue("string", "app_name", "JPA BOX OS EXTREME") }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { testImplementation("junit:junit:4.13.2") }
