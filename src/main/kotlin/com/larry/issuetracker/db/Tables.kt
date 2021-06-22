package com.larry.issuetracker.db

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun createDb(url: String, driver: String): Database {
    val database = Database.connect(url, driver = driver)
    transaction(database) {
        SchemaUtils.create(Users)
        SchemaUtils.create(Issues)
        SchemaUtils.create(Assignments)
        SchemaUtils.create(Labels)
        SchemaUtils.create(LabelsIssues)
        SchemaUtils.create(Comments)
    }
    return database
}

object Users : IntIdTable("users") {
    val username = varchar("name", 50).index()
    val email = varchar("email", 255).nullable()
    val image = varchar("image", 255).nullable()
}

object Issues : IntIdTable("issues") {
    val title = varchar("title", 255)
    val text = text("text")
    val writerId = reference("writer_id", Users, ReferenceOption.CASCADE).index()
    val createdAt = datetime("created_at")
}

object Assignments : IntIdTable("assignments") {
    val assigneeId = reference("assignee_id", Users, ReferenceOption.CASCADE).index()
    val issueId = reference("issue_id", Issues, ReferenceOption.CASCADE).index()
}

object LabelsIssues: IntIdTable("labels_issues") {
    val labelId = reference("label_id", Labels, ReferenceOption.CASCADE).index()
    val issueId = reference("issue_id", Issues, ReferenceOption.CASCADE).index()
}

object Labels : IntIdTable("labels") {
    val name = varchar("name", 20)
}

object Comments : IntIdTable("comments") {
    val createdAt = datetime("createdAt")
    val updatedAt = datetime("updatedAt")
    val body = text("body")
    val authorId = reference("author_id", Users, ReferenceOption.CASCADE)
    val issueId = reference("issue_id", Issues, ReferenceOption.CASCADE)
}
