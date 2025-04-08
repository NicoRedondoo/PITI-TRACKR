package com.example.pititrackr

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    var unlocked: Boolean = false
)
