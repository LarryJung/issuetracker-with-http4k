package com.larry.issuetracker.view

import com.larry.issuetracker.auth.AuthUser
import org.http4k.template.ViewModel

data class Index(
    val description: String,
    val isLogin: Boolean,
    val authUser: AuthUser?
) : ViewModel {
    override fun template() = super.template() + ".html"
}
