package com.larry.issuetracker

import com.larry.issuetracker.auth.EntryChecker
import com.larry.issuetracker.auth.IssueJwtFilter
import com.larry.issuetracker.auth.JWT
import com.larry.issuetracker.auth.oauthProvider
import com.larry.issuetracker.clients.GithubClient
import com.larry.issuetracker.config.view.view
import com.larry.issuetracker.db.*
import com.larry.issuetracker.domain.*
import com.larry.issuetracker.global.*
import com.larry.issuetracker.view.Index
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.invalidateCookie
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

fun makeRoutesWithDependencies(
    jwt: JWT,
    txManager: CommonTxManager,
    githubClient: GithubClient
): RoutingHttpHandler {
    return routes(
        // ===== API =====
        "/api/v1" bind routes(
            "/ping" bind GET to {
                Response(OK).body("OK")
            },
            "/users" bind POST to {
                okWith(txManager.tx {
                    insertUser(
                        User(
                            email = Email("email@${UUID.randomUUID()}.net"),
                            username = Username("username${Random(10).nextInt()}")
                        )
                    )
                })
            },
            "/users" bind GET to {
                okWith(txManager.tx { findAllUser() })
            },
            "/me" bind GET to EntryChecker(jwt)(true).then {
                val authUser = Global.Keys.authUser(it)
                okWith(txManager.tx { getUser(authUser!!.username) })
            },
            "/label/{labelId}" bind GET to {
                okWith(LabelDto(it.path("labelId")!!, it.query("color")!!))
            },
            "/labels" bind GET to {
                okWith(listOf(LabelDto("bug", "red"), LabelDto("bug", "red")))
            }
        ),
        // ===== VIEW =====
        "/" bind GET to EntryChecker(jwt)(false).then {
            val authUser = Global.Keys.authUser(it)
            Response(OK).with(view of Index("Hello there!", authUser != null, authUser))
        },
        "/logout" bind GET to EntryChecker(jwt)(true).then {
            Response(FOUND)
                .invalidateCookie(JWT.COOKIE_NAME)
                .header("Location", "http://localhost:9000")
        },
        "/oauth" bind routes(
            "/" bind GET to IssueJwtFilter(txManager, githubClient, jwt).then { request ->
                Response(FOUND).cookie(
                    with(Clock.systemDefaultZone()) {
                        Cookie(
                            JWT.COOKIE_NAME,
                            Global.Keys.jwtToken(request).value,
                            expires = LocalDateTime.ofInstant(this.instant().plus(Duration.ofHours(3)), this.zone),
                            path = "/"
                        )
                    }
                ).header("Location", "http://localhost:9000")
            },
            "/callback" bind GET to oauthProvider.callback
        )
    )
}

fun main() {
    val server = startApp(local)
    server.block()
}


fun startApp(config: AppConfig): Http4kServer {
    // ==== dependencies start
    val jwt = with(config.jwtConfig) { JWT(secret, algorithm, issuer, expirationMillis) }
    val db = with(config.db) { createDb(url, driver) }
    val txManager = CommonTxManagerImpl(db, CommonRepository())
    val githubClient = GithubClient()
    // ==== dependencies end

    val printingApp: HttpHandler =
        DebuggingFilters.PrintRequestAndResponse()
            .then(ServerFilters.InitialiseRequestContext(Global.contexts))
            .then(makeRoutesWithDependencies(jwt, txManager, githubClient))

    val server = printingApp.asServer(SunHttp(9000)).start()
    println("Server started on " + server.port())
    return server
}
