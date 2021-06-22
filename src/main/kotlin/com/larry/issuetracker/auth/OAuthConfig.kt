package com.larry.issuetracker.auth

import com.larry.issuetracker.clients.GithubClient
import com.larry.issuetracker.db.CommonTxManager
import com.larry.issuetracker.domain.*
import com.larry.issuetracker.global.Global
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.security.InsecureCookieBasedOAuthPersistence
import org.http4k.security.OAuthProvider
import org.http4k.security.gitHub

const val githubClientId = "76473224cf9a80ad8e7c"
const val githubClientSecret = "2cbd1c6296952846bd61f346b1a44abf4134c5ad"

// this is a test implementation of the OAuthPersistence interface, which should be
// implemented by application developers
private val oAuthPersistence = InsecureCookieBasedOAuthPersistence("Github")

val oauthProvider = OAuthProvider.gitHub(
    JavaHttpClient(),
    Credentials(githubClientId, githubClientSecret),
    Uri.of("http://localhost:9000/oauth/callback"),
    oAuthPersistence
)

class IssueJwtFilter(
    private val txManager: CommonTxManager,
    private val githubClient: GithubClient,
    private val jwt: JWT
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler = oauthProvider.authFilter.then { request ->
        val authToken = oAuthPersistence.retrieveToken(request) ?: throw RuntimeException("no access token")
        val githubUser = githubClient.getUserInfo(Token(authToken.value))
        val jwtToken =
            jwt.generate(githubUser.name, mapOf("username" to githubUser.name, "email" to (githubUser.email ?: "")))

        if (txManager.tx { getUser(Username(githubUser.name)) } == null) {
            txManager.tx {
                insertUser(User(
                    email = githubUser.email?.let { Email(it) } ?: Email(""),
                    username = Username(githubUser.name),
                    image = Image(githubUser.avatarUrl)
                ))
            }
        }
        next(request.with(Global.Keys.jwtToken of jwtToken))
    }
}
