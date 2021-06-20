package com.larry.issuetracker.config.global

import com.larry.issuetracker.config.auth.AuthUser
import com.larry.issuetracker.domain.Token
import org.http4k.core.RequestContexts
import org.http4k.lens.RequestContextKey

object Global {
    val contexts = RequestContexts()

    object Keys {
        val jwtToken = RequestContextKey.required<Token>(contexts)
        val authUser = RequestContextKey.optional<AuthUser>(contexts)
    }
}
