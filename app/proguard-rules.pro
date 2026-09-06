# Wifite Security Proguard Rules

# Keep Kotlin class metadata
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all Kotlin classes
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }

# Keep cryptography classes
-keep class javax.crypto.** { *; }
-keep class org.bouncycastle.** { *; }
-keepclassmembers class org.bouncycastle.** { *; }

# Keep Android classes
-keep class android.** { *; }
-keep class androidx.** { *; }
-keepclassmembers class androidx.** { *; }

# Keep Timber logging
-keep class timber.** { *; }
-keepclassmembers class timber.** { *; }

# Keep Wifite Security classes
-keep class com.wifite.security.** { *; }
-keepclassmembers class com.wifite.security.** { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove logging in release builds
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
}
