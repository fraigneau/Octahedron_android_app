package com.octahedron.ui.lang

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import com.octahedron.data.AppLanguage

@Composable
fun ProvideLocalizedResources(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    val base = LocalContext.current

    val localizedCtx = remember(language) {
        val cfg = Configuration()
        val locales = if (language.tag.isEmpty())
            LocaleList.getDefault()
        else
            LocaleList.forLanguageTags(language.tag)
        cfg.setLocales(locales)
        if (locales.size() > 0) cfg.setLayoutDirection(locales[0])

        base.createConfigurationContext(cfg)
    }

    CompositionLocalProvider(LocalContext provides localizedCtx) {
        content()
    }
}