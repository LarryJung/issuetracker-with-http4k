package com.larry.issuetracker

import com.larry.issuetracker.clients.GithubClient
import com.larry.issuetracker.config.AppConfig
import com.larry.issuetracker.config.auth.*
import com.larry.issuetracker.config.global.Global
import com.larry.issuetracker.config.local
import com.larry.issuetracker.config.view.view
import com.larry.issuetracker.domain.InMemoryUserRepository
import com.larry.issuetracker.domain.UserRepository
import com.larry.issuetracker.handlers.api.LabelDto
import com.larry.issuetracker.handlers.api.okWith
import com.larry.issuetracker.view.Index
import org.http4k.core.*
import org.http4k.core.Method.GET
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

fun makeRoutesWithDependencies(
    jwt: JWT,
    userRepository: UserRepository,
    githubClient: GithubClient
): RoutingHttpHandler {
    return routes(
        // ===== API =====
        "/api/v1" bind routes(
            "/ping" bind GET to {
                Response(OK).body("OK")
            },
            "/me" bind GET to EntryChecker(jwt)(true).then {
                val authUser = Global.Keys.authUser(it)
                okWith(authUser)
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
            "/" bind GET to IssueJwtFilter(userRepository, githubClient, jwt).then { request ->
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
    val userRepository = InMemoryUserRepository()
    val githubClient = GithubClient()
    // ==== dependencies end

    val printingApp: HttpHandler =
        DebuggingFilters.PrintRequestAndResponse()
            .then(ServerFilters.InitialiseRequestContext(Global.contexts))
            .then(makeRoutesWithDependencies(jwt, userRepository, githubClient))

    val server = printingApp.asServer(SunHttp(9000)).start()
    println("Server started on " + server.port())
    return server
}

/*
TODO
1. 로그인, 인증 보완
  - [O] 쿠키로 로그인 여부를 컨텍스트에서 가져올 수 있다.
  - [O] path 별로 인증필터를 추가한다.
  - [ ] github api 연동한다.
  - [ ] 쿠키가 만료되면 재발행한다. (refresh token 구현)

2. 테이블 설계
  - user, issue, label, milestone, assignee, comment

3. Api 설계
  - issue 생성
  - issue detail 조회
  - issue update api
  - issue 목록 조회 with 검색필터
  - 페이징 추가
  - comment api

4. api 데이터 기반 화면 개발

5. util 설계
  - request에서 path나 param 빼오는 부분을 wrapping 해보기
 */