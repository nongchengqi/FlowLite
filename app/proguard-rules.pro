# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontoptimize
-optimizationpasses 10
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-dontwarn

#不跳过非公共的库的类成员
-dontskipnonpubliclibraryclassmembers

#混淆时采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes InnerClasses,LineNumberTable

#把混淆类中的方法名也混淆了
-useuniqueclassmembernames

#优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification

#将文件来源重命名为“SourceFile”字符串
-renamesourcefileattribute SourceFile
#保留行号
-keepattributes SourceFile,LineNumberTable
#保持泛型
-keepattributes Signature

#保持所有实现 Serializable 接口的类成员
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.view.View
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.app.Fragment
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends androidx.**

-keep class android.net.http.SslError
-keep class android.webkit.**{*;}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class **.R$* {
    *;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#Fragment不需要在AndroidManifest.xml中注册，需要额外保护下
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment
