package com.rolangom.cmedicgt.domains.doctors


data class Doctor(
    val id: String,
    val prefix: String,
    val firstName: String,
    val lastName: String,
    val speciality: String,
    val phoneNumber: String? = null,
    val email: String? = null
)