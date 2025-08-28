# Keep your app’s core classes
-keep class com.example.paymentgatewayui.** { *; }

# Keep Compose UI
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep resource references (R.java)
-keep class **.R$* { *; }