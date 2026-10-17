package com.example.ensim_app.ui.screen.messagerie

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import com.example.ensim_app.ui.theme.Gold
import com.example.ensim_app.ui.theme.Navy800
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue

@Composable
internal fun mailFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = OffWhite,
    unfocusedTextColor = OffWhite,
    focusedBorderColor = Gold,
    unfocusedBorderColor = SlateBlue,
    cursorColor = Gold,
    focusedContainerColor = Navy800,
    unfocusedContainerColor = Navy800,
)
