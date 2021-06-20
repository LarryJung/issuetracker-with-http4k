package com.larry.issuetracker.clients

import org.http4k.security.AccessToken

data class GithubUserInfo(val username: String, val email: String)

class GithubClient {
    fun getUserInfo(accessToken: AccessToken): GithubUserInfo = GithubUserInfo("username1", "email@gmail.com")
}