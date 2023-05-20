package com.rolangom.cmedicgt.domains.visits

import com.rolangom.cmedicgt.domains.patients.Patient
import java.time.Instant

enum class GrowthStatus {
    APPROPIATE,
    INAPPROPIATE,
}

data class Visit(
    val id: String,
    @Transient
    val patient: Patient? = null,
    val date: Instant,
    val reasons: String? = null,
    val summary: String? = null,
    val growthStatus: String? = null,
    val weight: Int? = null,
    val height: Int? = null,
    val temp: Int? = null,
    val results: String? = null,
    val diagnosis: String? = null,
    val treatment: String? = null,
    val nonPathologicalBg: String? = null,
    val pathologicalBg: String? = null,
    val isAllergic: Boolean = false,
    val allergicTo: String? = null,
    val vaccination: String? = null,
    val surgeries: String? = null
)
