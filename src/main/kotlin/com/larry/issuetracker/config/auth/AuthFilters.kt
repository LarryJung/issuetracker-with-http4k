package com.larry.issuetracker.config.auth

import com.larry.issuetracker.config.global.Global
import com.larry.issuetracker.domain.*
import org.http4k.core.Filter
import org.http4k.core.cookie.cookie
import org.http4k.core.with

data class AuthUser(
    val username: Username,
    val email: Email
)

class EntryChecker(
    private val jwt: JWT
) {
    operator fun invoke(needAuth: Boolean): Filter =
        Filter { next ->
            { request ->
                val jwtToken: Token? = request.cookie(JWT.COOKIE_NAME)?.value?.let { Token(it) }

                if (jwtToken == null) {
                    if (needAuth) {
                        throw RuntimeException("need auth. no token")
                    }
                } else {
                    val claims = jwt.parse(jwtToken)
                    val username = Username(claims.get("username", String::class.java))
                    val email = Email(claims.get("email", String::class.java))
                    request.with(Global.Keys.authUser of AuthUser(username, email))
                }
                next(request)
            }
        }
}
