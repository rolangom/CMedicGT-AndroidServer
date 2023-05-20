package com.rolangom.cmedicgt.domains.patients

import com.rolangom.cmedicgt.domains.visits.VisitsRepo
import kotlinx.coroutines.flow.Flow

interface PatientRepo {
    fun getPatients(): Flow<List<Patient>>
    fun getPatients(page: Int, pageSize: Int): Flow<List<Patient>>
    fun getPatient(id: String): Flow<Patient>
    suspend fun createPatient(patient: Patient)
    suspend fun updatePatient(patient: Patient)
    suspend fun deletePatient(id: String)

    fun buildVisitsRepo(id: String): VisitsRepo
}