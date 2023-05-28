package com.rolangom.cmedicgt.domains.patients

import com.rolangom.cmedicgt.domains.visits.InMemoryVisitsRepo
import com.rolangom.cmedicgt.domains.visits.VisitsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryPatientRepo : PatientRepo {
    private val patients = mutableListOf<Patient>(
//        Patient(id = "1", firstName = "Rolando", lastName = "Gomez", birthDate = Instant.parse("1990-05-19T04:00:00Z")),
        Patient(id = "2", firstName = "Valeria", lastName = "Aleman"),
        Patient(id = "3", firstName = "Christy", lastName = "Gomez"),
    )

    override fun getPatients(): Flow<List<Patient>> = flowOf(patients)

    override fun getPatients(page: Int, pageSize: Int): Flow<List<Patient>> {
        val startIndex = page * pageSize
        val endIndex = startIndex + pageSize
        return flowOf(
            patients.subList(startIndex, endIndex)
        )
    }

    override fun getPatient(id: String): Flow<Patient> = flowOf(
        patients.first { it.id == id }
    )

    override suspend fun createPatient(patient: Patient) {
        patients.add(patient)
    }

    private fun atIndex(id: String): Int = patients.indexOfFirst { it.id === id }

    override suspend fun updatePatient(patient: Patient) {
        val patientIndex = atIndex(patient.id!!)
        if (patientIndex >= 0)
            patients[patientIndex] = patient
    }

    override suspend fun deletePatient(id: String) {
        val patientIndex = atIndex(id)
        if (patientIndex >= 0)
            patients.removeAt(patientIndex)
    }

    override fun buildVisitsRepo(id: String): VisitsRepo {
        return InMemoryVisitsRepo(id)
    }

}