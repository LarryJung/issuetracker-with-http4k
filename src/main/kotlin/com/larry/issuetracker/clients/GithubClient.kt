package com.larry.issuetracker.clients

import com.larry.issuetracker.domain.Token
import org.http4k.client.ApacheClient
import org.http4k.core.*
import org.http4k.filter.ClientFilters.SetBaseUriFrom
import com.larry.issuetracker.global.MyJackson.auto
import org.http4k.filter.DebuggingFilters

data class GithubUserInfo(val name: String, val email: String?, val avatarUrl: String)

val githubUserInfoLens = Body.auto<GithubUserInfo>().toLens()

class GithubClient {
    private val client: HttpHandler =
        DebuggingFilters.PrintRequestAndResponse()
            .then(SetBaseUriFrom(Uri.of("https://api.github.com")))
            .then { next: HttpHandler ->
                { request ->
                    next(request.header("Accept", "application/vnd.github.v3+json"))
                }
            }
            .then(ApacheClient())

    fun getUserInfo(accessToken: Token): GithubUserInfo {
        return githubUserInfoLens(
            client(
                Request(Method.GET, "/user").header(
                    "Authorization",
                    "Bearer ${accessToken.value}"
                )
            )
        )
    }

}