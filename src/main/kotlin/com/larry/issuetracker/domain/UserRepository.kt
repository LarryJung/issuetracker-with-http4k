package com.larry.issuetracker.domain

interface UserRepository {
    fun getUserByEmail(email: Email): User?
    fun getUserByName(username: Username): User?
    fun insert(user: User): User
}

class InMemoryUserRepository : UserRepository {
    private val users: MutableList<User> = mutableListOf()

    override fun getUserByEmail(email: Email): User? {
        return users.find { it.email == email }
    }

    override fun getUserByName(username: Username): User? {
        return users.find { it.username == username }
    }

    override fun insert(user: User): User =
        user.copy(id = users.size + 1).also { users.add(it) }

}