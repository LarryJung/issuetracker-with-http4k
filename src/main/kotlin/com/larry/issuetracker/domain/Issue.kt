package com.larry.issuetracker.domain

import com.larry.issuetracker.db.CommonRepository
import com.larry.issuetracker.db.Issues
import com.larry.issuetracker.db.Users
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.joda.time.DateTime

data class Issue(
    val id: Int,
    val title: String,
    val text: String,
    val writerId: Int,
    val createdAt: DateTime
)

// query
fun CommonRepository.getIssueById(id: Int): Issue? =
    Issues.select { Issues.id eq id }.firstOrNull()?.let {
        Issue(
            it[Issues.id].value,
            it[Issues.title],
            it[Issues.text],
            it[Issues.writerId].value,
            it[Issues.createdAt]
        )
    }

fun CommonRepository.insertIssue(issue: Issue): Issue =
    Issues.insertAndGetId {
        it[title] = issue.title
        it[text] = issue.text
        it[writerId] = EntityID(issue.writerId, Users)
        it[createdAt] = issue.createdAt
    }.let { issue.copy(id = it.value) }