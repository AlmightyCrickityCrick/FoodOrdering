package com.pr

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.pr.plugins.*
import io.ktor.client.*

var restaurantList = HashMap<Int, Restaurant>()
val client = HttpClient()

fun main() {
    embeddedServer(Netty, port = 8088) {
        configureSerialization()
        configureRouting()
    }.start(wait = true)
}
