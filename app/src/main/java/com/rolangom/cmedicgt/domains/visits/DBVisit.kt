package com.rolangom.cmedicgt.domains.visits

import com.rolangom.cmedicgt.domains.Filterable
import com.rolangom.cmedicgt.domains.patients.DBPatient
import com.rolangom.cmedicgt.domains.toInstant
import com.rolangom.cmedicgt.domains.toRealmInstant
import io.realm.kotlin.ext.backlinks
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId


class DBVisit: RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var date: RealmInstant = RealmInstant.MIN
    var owner_id: String = ""
    var patient_id: ObjectId = ObjectId()
    var reasons: String? = null
    var weight: Int? = null
    var height: Int? = null
    var temp: Int? = null
    var headCircunference: Int? = null
    var bloodPressure: Int? = null
    var results: String? = null
    var diagnosis: String? = null
    var treatment: String? = null
    var nonPathologicalBg: String? = null
    var pathologicalBg: String? = null
    var actualMedicines: String? = null
    var isAllergic: Boolean = false
    var allergicTo: String? = null
    var vaccination: String? = null
    var surgeries: String? = null
    val patient: RealmResults<DBPatient> by backlinks(DBPatient::visits)

    companion object {
        fun fromPlain(visit: Visit): DBVisit = DBVisit().apply {
            _id = if (visit.id.isNullOrEmpty()) ObjectId() else ObjectId(visit.id)
            patient_id = ObjectId(visit.patientId)
            date = visit.date.toRealmInstant()
            owner_id = visit.doctorId ?: ""
            reasons = visit.reasons
            weight = visit.weight
            height = visit.height
            temp = visit.temp
            headCircunference = visit.headCircunference
            bloodPressure = visit.bloodPressure
            results = visit.results
            diagnosis = visit.diagnosis
            treatment = visit.treatment
            nonPathologicalBg = visit.nonPathologicalBg
            pathologicalBg = visit.pathologicalBg
            actualMedicines = visit.actualMedicines
            isAllergic = visit.isAllergic
            allergicTo = visit.allergicTo
            vaccination = visit.vaccination
            surgeries = visit.surgeries
        }
    }

    fun toPlain() = Visit(
        id = this._id.toHexString(),
//        patient = this.patient[0].toPlain(),
        patientId = this.patient_id.toHexString(),
        doctorId = this.owner_id,
        date = this.date.toInstant(),
        reasons = this.reasons,
        weight = this.weight,
        height = this.height,
        temp = this.temp,
        headCircunference = this.headCircunference,
        bloodPressure = this.bloodPressure,
        results = this.results,
        diagnosis = this.diagnosis,
        treatment = this.treatment,
        nonPathologicalBg = this.nonPathologicalBg,
        pathologicalBg = this.pathologicalBg,
        actualMedicines = this.actualMedicines,
        isAllergic = this.isAllergic,
        allergicTo = this.allergicTo,
        vaccination = this.vaccination,
        surgeries = this.surgeries
    )

    fun update(visit: DBVisit) {
        date = visit.date
        owner_id = visit.owner_id
        reasons = visit.reasons
        weight = visit.weight
        height = visit.height
        patient_id = visit.patient_id
        temp = visit.temp
        headCircunference = visit.headCircunference
        bloodPressure = visit.bloodPressure
        results = visit.results
        diagnosis = visit.diagnosis
        treatment = visit.treatment
        nonPathologicalBg = visit.nonPathologicalBg
        pathologicalBg = visit.pathologicalBg
        actualMedicines = visit.actualMedicines
        isAllergic = visit.isAllergic
        allergicTo = visit.allergicTo
        vaccination = visit.vaccination
        surgeries = visit.surgeries
    }
}


data class FilterableVisit(
    val ids: List<String>? = null,
    val patientId: String? = null,
): Filterable {
    override fun getStringFilters(startAt: Int): String {
        // TODO abstract this in a common class
        val filterList = mutableListOf<String>()
        var index = startAt
        if (!ids.isNullOrEmpty()) {
            val idsPlaceholders = List(ids.size) { i -> "$${i + index}" }.joinToString(",")
            filterList.add("_id IN { $idsPlaceholders }")
            index += ids.size
        }
        if (!patientId.isNullOrEmpty())
            filterList.add("patient_id == $${index}")
        return filterList.joinToString(" AND ")
    }

    override fun getValues(): Array<out Any?> {
        val objIds = ids?.map { ObjectId(it) }?.toTypedArray().orEmpty()
        return listOfNotNull(*objIds, patientId).toTypedArray()
    }
}