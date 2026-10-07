# Keep JNI native entry points
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class com.commonmsm.engine.** { *; }
-keep class com.commonmsm.data.models.** { *; }
