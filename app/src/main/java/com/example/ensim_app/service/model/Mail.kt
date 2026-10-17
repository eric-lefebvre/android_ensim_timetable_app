package com.example.ensim_app.service.model

import com.google.firebase.Timestamp

data class Mail(
    val id: String = "",
    val fromResourceId: Int = 0,
    val fromDisplayName: String = "",
    val fromUid: String = "",
    val toResourceId: Int = 0,
    val toDisplayName: String = "",
    val subject: String = "",
    val body: String = "",
    val sentAt: Timestamp? = null,
    val read: Boolean = false,
)
