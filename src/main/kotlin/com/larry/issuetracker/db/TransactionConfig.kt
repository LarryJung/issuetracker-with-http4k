package com.larry.issuetracker.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

interface TransactionManager<Repository> {
    fun <T> tx(block: Repository.() -> T): T
}

typealias CommonTxManager = TransactionManager<CommonRepository>

class CommonTxManagerImpl(
    private val database: Database,
    private val repository: CommonRepository
) : CommonTxManager {
    override fun <T> tx(block: CommonRepository.() -> T): T =
        transaction(database) {
            addLogger(StdOutSqlLogger)
            block(repository)
        }
}

class CommonRepository
