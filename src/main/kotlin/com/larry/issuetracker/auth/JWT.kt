package com.larry.issuetracker.auth

import com.larry.issuetracker.domain.Email
import com.larry.issuetracker.domain.Token
import com.larry.issuetracker.domain.Username
import io.jsonwebtoken.*
import java.util.*
import javax.crypto.spec.SecretKeySpec
import kotlin.RuntimeException

class JWT(
    secret: String,
    algorithm: String?,
    private val issuer: String,
    private val expirationMillis: Long
) {
    companion object {
        const val COOKIE_NAME = "issuetracker_jwt"
    }

    private val signingKey: SecretKeySpec = SecretKeySpec(
        secret.toByteArray(),
        algorithm ?: SignatureAlgorithm.HS256.jcaName
    )

    fun generate(subject: String, claims: Map<String, String>) =
        Token(
            Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .setClaims(claims)
                .setExpiration(Date(System.currentTimeMillis() + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact()
        )

    fun parse(token: Token): Claims = try {
        Jwts.parser()
            .setSigningKey(signingKey)
            .parseClaimsJws(token.value)
            .body
            .also {
                if (!(it.contains("username") && it.contains("email"))) {
                    throw RuntimeException("no required claims")
                }
            }
    } catch (e: Exception) {
        when (e) {
            is ExpiredJwtException,
            is UnsupportedJwtException,
            is MalformedJwtException,
            is SignatureException,
            is IllegalArgumentException -> throw RuntimeException("token parse validation error")
            else -> throw RuntimeException("token parse error")
        }
    }
}
