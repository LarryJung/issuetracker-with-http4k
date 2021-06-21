package com.larry.issuetracker.config.view

import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.lens.BiDiBodyLens
import org.http4k.template.ThymeleafTemplates
import org.http4k.template.ViewModel
import org.http4k.template.viewModel

val renderer = ThymeleafTemplates().CachingClasspath()
val view: BiDiBodyLens<ViewModel> = Body.viewModel(renderer, ContentType.TEXT_HTML).toLens()