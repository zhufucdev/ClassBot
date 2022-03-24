package com.zhufucdev.api

import com.zhufucdev.Plugin
import com.zhufucdev.Query
import com.zhufucdev.data.Database
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

object HttpServer {
    const val PORT = 9237
    const val HOST = "0.0.0.0"

    private lateinit var engine: ApplicationEngine

    fun init() {
        engine = embeddedServer(Netty, port = PORT, host = HOST) {
            module()
            routing {
                listRoute()
            }
        }.start()

        Plugin.logger.info("API server running at http://$HOST:$PORT")
    }

    private fun Route.listRoute() {
        route("/classroom") {
            get {
                val classes = Database.classes().map { Classroom.from(it) }
                call.respond(classes)
            }

            get("{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(
                    message = "Missing classroom id",
                    status = HttpStatusCode.BadRequest
                )

                try {
                    val group = id.toLongOrNull() ?: return@get call.respond(
                        message = "Invalid classroom id",
                        status = HttpStatusCode.BadRequest
                    )
                    val students = Database.classmates(group).map { Student.from(it, group) }

                    call.respond(students)
                } catch (e: Query.NoSuchGroupException) {
                    call.respond(
                        message = "No such group",
                        status = HttpStatusCode.NotFound
                    )
                }
            }
        }
    }

    private fun Application.module() {
        install(ContentNegotiation) {
            json()
        }
    }
}