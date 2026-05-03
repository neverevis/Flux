package com.socialapp.flux.model

data class Post(
    var autor: String = "",
    var texto: String = "",
    var imagem: String = "",
    var cidade: String = "",
    var timestamp: Long = 0L
)
