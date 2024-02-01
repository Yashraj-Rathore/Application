// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false

}
buildscript {
    repositories {
        google()
        mavenCentral()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4") // Android Gradle Plugin
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0") // Kotlin Gradle Plugin
        classpath("com.google.gms:google-services:4.4.0") // Google Services plugin
        // ... any other classpath dependencies
    }
}
