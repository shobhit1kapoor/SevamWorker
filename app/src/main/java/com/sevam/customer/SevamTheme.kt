package com.sevam.customer

import androidx.compose.runtime.Composable
import com.sevam.core.ui.SevamTheme as CoreSevamTheme

@Composable
fun SevamTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    CoreSevamTheme(darkTheme = darkTheme, content = content)
}
