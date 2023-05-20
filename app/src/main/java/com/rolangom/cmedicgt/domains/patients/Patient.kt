package com.rolangom.cmedicgt.domains.patients

import java.time.Instant

enum class ScholarLevel {
    NONE,
    BASIC,
    SECONDARY,
    TECHINCIAN,
    COLLEGE,
}

enum class Gender {
    FEMALE,
    MALE,
}


data class ARS(
    val id: String,
    val title: String
)

data class Patient(
    val id: String,
    val nationalId: String? = null,
//    val doctorId: String? = null,
    val firstName: String,
    val lastName: String? = null,
    val birthDate: Instant? = null,
    val scholarLevel: String? = null,
    val address: String? = null,
    val insuranceCompany: String? = null,
    val doctorName: String? = null,
    val gender: String? = null, // Gender,
    val createdAt: Instant = Instant.now(),
    val modifiedAt: Instant = Instant.now(),
)
