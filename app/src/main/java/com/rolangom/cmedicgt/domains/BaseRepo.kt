package com.rolangom.cmedicgt.domains

import kotlinx.coroutines.flow.Flow

enum class SortBy {
    ASC,
    DESC
}

data class PaginateConfig(
    val start: Int,
    val end: Int,
)


interface Filterable {
    fun getStringFilters(startAt: Int = 1): String
    fun getValues(): Array<out Any?>
}

interface BaseRepo<T, F: Filterable> {
    fun get(id: String): Flow<T>
    fun list(sort: Pair<String, SortBy>, paginate: PaginateConfig, filters: F): Flow<List<T>>
    suspend fun post(item: T): T
    suspend fun update(id: String, item: T): T
    suspend fun delete(id: String): T
}