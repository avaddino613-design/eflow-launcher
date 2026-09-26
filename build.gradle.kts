plugins {
    // AGP 8.13.x is the current stable line as of this writing, and matches
    // what GitHub-hosted runners ship Android SDK platforms/build-tools for.
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    // No Room, no KSP, no kapt: this app now uses plain JSON file storage
    // (see data/LibraryStore.kt), so there's no annotation processor at
    // all - and therefore nothing that can drift out of version-lockstep
    // with the Kotlin compiler, which is what broke the last two builds.
}
