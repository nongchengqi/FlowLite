package com.evan.flowlite.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.rippleClickable(
    radius: Dp = 24.dp,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
) = composed {
    val rippleIndication = rememberRipple(radius = radius)
    val source = remember { MutableInteractionSource() }
    clickable(source, rippleIndication, enabled, onClickLabel, role, onClick)
}