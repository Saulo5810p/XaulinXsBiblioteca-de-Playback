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

# ===== Regras específicas do RetroPlayer (necessárias com minifyEnabled=true) =====
# Sem essas regras, R8 pode remover ou renomear classes usadas via reflection
# pelo Room, Kotlin Coroutines ou pela serialização interna do Compose.

# Room: entidades e DAO usados via reflection para gerar as queries SQL
-keep class com.example.database.** { *; }
-keepclassmembers class com.example.database.** { *; }

# Modelos de domínio (Track, Playlist, PlaybackState) — usados em Compose
# State/Saveable e em comparações de igualdade estrutural (data class)
-keep class com.example.model.** { *; }

# Kotlin Coroutines / Flow — evita quebra de metadata de corrotinas suspensas
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    <fields>;
}

# Serviço de reprodução e widgets — componentes do sistema Android instanciados
# via reflection pelo próprio SO (Service, AppWidgetProvider), nunca ofuscar
-keep class com.example.playback.PlaybackService { *; }
-keep class com.example.widgets.** { *; }

# AGSL RenderEffect / shaders customizados usam reflection para os uniforms
-keep class com.example.ui.effects.CinematicShader { *; }

# Mantém atributos necessários para stack traces legíveis em crash reports
-keepattributes *Annotation*, Signature, Exceptions, InnerClasses, EnclosingMethod

