// build.gradle.kts (ROOT)
plugins {
    // Versi ini harus cocok dengan versi Android Studio kamu,
    // 8.2.0 aman untuk rata-rata studio baru.
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}