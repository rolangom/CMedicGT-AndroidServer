package com.rolangom.cmedicgt.domains.visits

import com.rolangom.cmedicgt.domains.patients.Patient
import java.time.Instant

data class Visit(
    val id: String? = null,
    @Transient
    val patient: Patient? = null,
    val doctorId: String? = null,
    val patientId: PatientId,
    val date: Instant,
    val reasons: String? = null,
    val weight: String? = null,
    val height: String? = null,
    val temp: String? = null,
    val headCircunference: String? = null,
    val bloodPressure: String? = null,
    val results: String? = null,
    val diagnosis: String? = null,
    val treatment: String? = null,
    val nonPathologicalBg: String? = null,
    val pathologicalBg: String? = null,
    val actualMedicines: String? = null,
    val isAllergic: Boolean = false,
    val allergicTo: String? = null,
    val vaccination: String? = null,
    val surgeries: String? = null
)
