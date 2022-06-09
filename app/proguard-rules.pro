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

#PROGUARD
# Retain generated class which implement Unbinder.
#-keep public class * implements butterknife.Unbinder { public <init>(**, android.view.View); }

# Prevent obfuscation of types which use ButterKnife annotations since the simple name
# is used to reflectively look up the generated ViewBinding.
#-keep class butterknife.*
#-keepclasseswithmembernames class * { @butterknife.* <methods>; }
#-keepclasseswithmembernames class * { @butterknife.* <fields>; }

#KOTLIN
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

-keep class com.spin.id.api.** {
  public protected private *;
}

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

#-keep class com.spin.id.api.request {
#  public protected private *;
#}


-keep class kotlin.Metadata

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

#GLIDE
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#PARCELER
# Parceler library
-keep interface org.parceler.Parcel
-keep @org.parceler.Parcel class * { *; }
-keep class **$$Parcelable { *; }

#CRASHLYTICS
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

#CHUCK
-keep class com.readystatesoftware.chuck.internal.data.HttpTransaction { *; }
-keep class android.support.v7.widget.SearchView { *; }

#ONE SIGNAL
#-dontwarn com.onesignal.**

# These 2 methods are called with reflection.
-keep class com.google.android.gms.common.api.GoogleApiClient {
    void connect();
    void disconnect();
}


#-keep class com.onesignal.ActivityLifecycleListenerCompat** {*;}


# Observer backcall methods are called with reflection
#-keep class com.onesignal.OSSubscriptionState {
#    void changed(com.onesignal.OSPermissionState);
#}
#
#-keep class com.onesignal.OSPermissionChangedInternalObserver {
#    void changed(com.onesignal.OSPermissionState);
#}
#
#-keep class com.onesignal.OSSubscriptionChangedInternalObserver {
#    void changed(com.onesignal.OSSubscriptionState);
#}
#
#-keep class ** implements com.onesignal.OSPermissionObserver {
#    void onOSPermissionChanged(com.onesignal.OSPermissionStateChanges);
#}
#
#-keep class ** implements com.onesignal.OSSubscriptionObserver {
#    void onOSSubscriptionChanged(com.onesignal.OSSubscriptionStateChanges);
#}
#
#-keep class com.onesignal.shortcutbadger.impl.AdwHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.ApexHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.AsusHomeLauncher { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.DefaultBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.EverythingMeHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.HuaweiHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.LGHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.NewHtcHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.NovaHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.OPPOHomeBader { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.SamsungHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.SonyHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.VivoHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.XiaomiHomeBadger { <init>(...); }
#-keep class com.onesignal.shortcutbadger.impl.ZukHomeBadger { <init>(...); }


-dontwarn com.amazon.**

# Proguard ends up removing this class even if it is used in AndroidManifest.xml so force keeping it.
#-keep public class com.onesignal.ADMMessageHandler {*;}
#
#-keep class com.onesignal.JobIntentService$* {*;}
#
#-keep class com.onesignal.OneSignalUnityProxy {*;}

#EVENTBUS
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#Androidx & Android
-keep class androidx.appcompat.widget.** { *; }
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }

-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }

-optimizations !code/simplification/variable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.support.v4.app.Fragment

#GOOGLE MAP
# The Maps Android API uses custom parcelables.
# Use this rule (which is slightly broader than the standard recommended one)
# to avoid obfuscating them.
#-keepclassmembers class * implements android.os.Parcelable {
#    static *** CREATOR;
#}
# The Maps Android API uses serialization.
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    static final java.io.ObjectStreamField[] serialPersistentFields;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}

#Support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }


# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

##ENTITY
#-keep class id.qasir.mitra.preference.entity.** { *; }

#Log
# This will strip `Log.v`, `Log.d`, and `Log.i` statements and will leave `Log.w` and `Log.e` statements intact.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

#Timber
-assumenosideeffects class timber.log.Timber* {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
