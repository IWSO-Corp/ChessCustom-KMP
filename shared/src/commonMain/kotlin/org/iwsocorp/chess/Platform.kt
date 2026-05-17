package org.iwsocorp.chess

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform