package com.truenorth.citizenshiptest

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform