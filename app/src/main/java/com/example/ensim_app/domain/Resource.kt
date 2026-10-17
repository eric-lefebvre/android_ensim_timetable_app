package com.example.ensim_app.domain

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    object Loading : Resource<Nothing>()
    object Error : Resource<Nothing>()
}
