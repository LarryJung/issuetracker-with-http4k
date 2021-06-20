package com.larry.issuetracker.handlers.api

import org.http4k.core.Request


class FindMeHandler {
    operator fun invoke(request: Request): String {
        return request.toMessage()
    }
}

val findMeHandler = FindMeHandler()