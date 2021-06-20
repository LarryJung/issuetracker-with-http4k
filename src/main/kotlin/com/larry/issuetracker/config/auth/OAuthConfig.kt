package com.larry.issuetracker.config.auth

import com.larry.issuetracker.clients.GithubClient
import com.larry.issuetracker.config.global.Global
import com.larry.issuetracker.domain.Email
import com.larry.issuetracker.domain.User
import com.larry.issuetracker.domain.UserRepository
import com.larry.issuetracker.domain.Username
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.cookie.cookie
import org.http4k.security.AccessToken
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
    private val userRepository: UserRepository,
    private val githubClient: GithubClient,
    private val jwt: JWT
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler = oauthProvider.authFilter.then {
        val authToken = oAuthPersistence.retrieveToken(it) ?: throw RuntimeException("no access token")
        val githubUser = githubClient.getUserInfo(authToken)
        val jwtToken = jwt.generate(Username(githubUser.username), Email(githubUser.email))
        if (userRepository.getUserByEmail(Email(githubUser.email)) == null) {
            userRepository.insert(
                User(
                    email = Email(githubUser.email),
                    token = jwtToken,
                    username = Username(githubUser.username)
                )
            )
        }
        next(it.with(Global.Keys.jwtToken of jwtToken))
    }
}
