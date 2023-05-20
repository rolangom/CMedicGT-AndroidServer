package com.rolangom.cmedicgt.domains.visits

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant

typealias PatientId = String

val localVisits = mutableMapOf<PatientId, MutableList<Visit>>(
    "1" to mutableListOf(
        Visit("1", date = Instant.now(), reasons = "Dolor de estomago"),
        Visit("2",  date = Instant.now(), reasons = "Dolor de barriga"),
    ),
    "2" to mutableListOf(
        Visit("1", date = Instant.now(), reasons = "Dolor de no se que"),
        Visit("2", date = Instant.now(), reasons = "Dolor de pies"),
    )
)

class InMemoryVisitsRepo(patientId: PatientId) : VisitsRepo {
    private val visits: MutableList<Visit>

    init {

        visits = localVisits[patientId] ?: throw NoSuchElementException("No patient or visits found")
    }
    override fun getVisits(): Flow<List<Visit>> = flowOf(visits)

    override fun getVisit(id: String): Flow<Visit> = flowOf(
        visits.first { it.id == id }
    )

    override suspend fun createVisit(visit: Visit) {
        visits.add(visit)
    }

    private fun getIndexOf(id: String): Int {
        return visits.indexOfFirst { it.id == id }
    }

    override suspend fun updateVisit(visit: Visit) {
        val index = getIndexOf(visit.id)
        if (index >= 0)
            visits[index] = visit
    }

    override suspend fun deleteVisit(id: String) {
        val index = getIndexOf(id)
        if (index >= 0)
            visits.removeAt(index)
    }

    override suspend fun deleteVisit(visit: Visit) {
        visits.remove(visit)
    }

}