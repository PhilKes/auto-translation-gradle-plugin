// package io.github.philkes.android.auto.translation
//
// import com.google.auth.ApiKeyCredentials
// import com.google.cloud.translate.Translate
// import com.google.cloud.translate.TranslateOptions
// import io.github.philkes.auto.translation.plugin.task.AutoTranslateTask
// import io.github.philkes.auto.translation.plugin.task.toIsoLocale
// import org.junit.jupiter.api.Test
// import java.util.Locale
// import kotlin.test.assertEquals
// import kotlin.text.split
//
// class AutoTranslateTaskTest {
//
////    @Test
////    fun maskPlaceholders(){
////        val text = "This is %1\$d Test to check if %20\$d works"
////
////        val maskedTextStream = AutoTranslateTask.maskPlaceholders(text)
////
////        assertEquals("This is <1/> Test to check if <20/> works", maskedTextStream)
////    }
////
////    @Test
////    fun maskPlaceholdersNoMasking(){
////        val text = "This works"
////
////        val maskedTextStream = AutoTranslateTask.maskPlaceholders(text)
////
////        assertEquals("This works", maskedTextStream)
////    }
////
////    @Test
////    fun unmaskPlaceholders(){
////        val text = "This is <1/> Test to check if <20/> works"
////
////        val maskedTextStream = AutoTranslateTask.unmaskPlaceholders(text)
////
////        assertEquals("This is %1\$d Test to check if %20\$d works", maskedTextStream)
////    }
////
////    @Test
////    fun unmaskPlaceholdersNoMasking(){
////        val text = "This works"
////
////        val maskedTextStream = AutoTranslateTask.unmaskPlaceholders(text)
////
////        assertEquals("This works", maskedTextStream)
////    }
//
//    @Test
//    fun test(){
//       val x= "zh-rCN"
//        val parts = x.split('-', '_')
//        val language = parts.getOrElse(0) {""}
//        val country = parts.getOrElse(1) {""}.replace("r", "")
//        val variant = parts.getOrElse(2) {""}.replace("r", "")
//        val locale = Locale(language, country, variant)
//        val t=  Locale.getAvailableLocales().find {
//            it.language == locale.language && it.country == locale.country && it.variant ==
// locale.variant
//        }
//        println(t)
//
//        val toIsoLocale = "pt_BR_#Latn".toIsoLocale()
//
//        val toIsoLocale1 = "zh-rTW".toIsoLocale()
//        println(toIsoLocale1)
//        println(toIsoLocale)
//    }
//
//    @Test
//    fun testGoogle(){
//        val service = TranslateOptions.newBuilder()
//            .setCredentials(ApiKeyCredentials.create("AIzaSyCGO_64PkKqh1mcwzlxmjl7iZoQpAqbZx4"))
//            .build().service
////        val listSupportedLanguages = service.listSupportedLanguages()
////        println(listSupportedLanguages)
//
//        val translate = service.translate(
//            "Hello",
//            Translate.TranslateOption.sourceLanguage("en-US".toIsoLocale().toString()),
//            Translate.TranslateOption.targetLanguage("zh-rTW".toIsoLocale().toString()),
//        )
//        println(translate)
//    }
// }
