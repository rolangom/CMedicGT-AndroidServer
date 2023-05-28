package com.rolangom.cmedicgt.domains.patients

import java.time.Instant

data class Patient(
    val id: String? = null,
    val nationalId: String? = null,
    val firstName: String,
    val lastName: String? = null,
    val birthDate: Instant? = null,
    val growthStatus: String? = null,
    val scholarLevel: String? = null,
    val accompaniedBy: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val insuranceCompany: String? = null,
    val doctorId: String? = null,
    val doctorName: String? = null,
    val reasons: String? = null,
    val summary: String? = null,
    val gender: String? = null,
    val createdAt: Instant = Instant.now(),
    val modifiedAt: Instant = Instant.now(),
)
