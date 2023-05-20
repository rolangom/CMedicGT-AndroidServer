package com.rolangom.cmedicgt.domains.visits

import kotlinx.coroutines.flow.Flow

interface VisitsRepo {
    fun getVisits(): Flow<List<Visit>>
    fun getVisit(id: String): Flow<Visit>
    suspend fun createVisit(visit: Visit)
    suspend fun updateVisit(visit: Visit)
    suspend fun deleteVisit(id: String)
    suspend fun deleteVisit(visit: Visit)
}
