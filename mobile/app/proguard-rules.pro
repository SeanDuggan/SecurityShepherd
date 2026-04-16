# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ===================================
# REVERSE ENGINEERING LESSONS/CHALLENGES
# Keep these unobfuscated for educational purposes
# ===================================

# Reverse Engineering Lesson - meant to be reverse engineered
-keep class com.owasp.app.ui.lessons.** { *; }

# Reverse Engineering Challenges 1-3 - meant to be reverse engineered
-keep class com.owasp.app.ui.challenges.** { *; }

# ===================================
# OBFUSCATE EVERYTHING ELSE
# These should be protected from reverse engineering
# ===================================

# Keep names for ViewBinding and ViewModels to avoid runtime issues
-keep class com.owasp.app.databinding.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep MainActivity and navigation infrastructure
-keep class com.owasp.app.MainActivity { *; }
-keep class com.owasp.app.LandingActivity { *; }
-keep class com.owasp.app.Preferences { *; }

# Keep Fragment classes but obfuscate their internals
-keepnames class * extends androidx.fragment.app.Fragment

# Keep these challenge/lesson packages OBFUSCATED (not in RE category)
# Insecure Data Storage - should be obfuscated
-keepnames class com.owasp.app.ui.lessons.insecuredata.** 
-keepnames class com.owasp.app.ui.challenges.insecuredata1.**
-keepnames class com.owasp.app.ui.challenges.insecuredata2.**
-keepnames class com.owasp.app.ui.challenges.insecuredata3.**

# Poor Authentication - should be obfuscated
-keepnames class com.owasp.app.ui.lessons.poorauth.**
-keepnames class com.owasp.app.ui.challenges.poorauth.**

# Supply Chain Security - should be obfuscated
-keepnames class com.owasp.app.ui.lessons.supplychain.**
-keepnames class com.owasp.app.ui.challenges.supplychain.**

# Insecure Communication - should be obfuscated
-keepnames class com.owasp.app.ui.lessons.insecurecomm.**
-keepnames class com.owasp.app.ui.challenges.insecurecomm.**

# Cryptography - should be obfuscated
-keepnames class com.owasp.app.ui.lessons.crypto.**
-keepnames class com.owasp.app.ui.challenges.crypto.**

# Security Misconfiguration - should be obfuscated
-keepnames class com.owasp.app.ui.lessons.securitymisconfig.**
-keepnames class com.owasp.app.ui.challenges.securitymisconfig.**

# Input Validation - should be obfuscated
-keepnames class com.owasp.app.ui.lessons.inputvalidation.**
-keepnames class com.owasp.app.ui.challenges.inputvalidation.**

# Insecure Authorization - should be obfuscated
-keepnames class com.owasp.app.ui.lessons.insecureauthorization.**

# Home fragment
-keepnames class com.owasp.app.ui.home.**

# Aggressive obfuscation settings
-optimizationpasses 5
-overloadaggressively
-repackageclasses ''
-allowaccessmodification

# Obfuscate string constants (except in RE packages)
-adaptclassstrings

# Remove logging for non-RE packages
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep source file and line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# AndroidX and Material Components
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class com.google.android.material.** { *; }

# Navigation component
-keep class androidx.navigation.** { *; }

# Prevent stripping of enum classes
-keepclassmembers enum * { *; }