package com.larry.issuetracker.handlers.api

import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto

// marker interface
interface ResDto

data class CommonResponse<T>(val body: T) : ResDto

fun <T> T.toResDto() = CommonResponse(this)

private fun ResDto.toModifier(): (Response) -> Response = Body.auto<ResDto>().toLens() of this

fun <T> okWith(dto: T) =  Response(Status.OK).with(dto.toResDto().toModifier())

// TODO color는 클래스로 분리
data class LabelDto(val name: String, val color: String)