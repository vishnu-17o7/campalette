# Keep enough metadata for Play Console crash deobfuscation.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Prepare-for-release: drop verbose log calls from the shipped binary.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
