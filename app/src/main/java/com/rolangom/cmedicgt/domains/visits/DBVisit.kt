package com.rolangom.cmedicgt.domains.visits

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
    var reasons: String? = null
    var summary: String? = null
    var growthStatus: String? = null
    var weight: Int? = null
    var height: Int? = null
    var temp: Int? = null
    var results: String? = null
    var diagnosis: String? = null
    var treatment: String? = null
    var nonPathologicalBg: String? = null
    var pathologicalBg: String? = null
    var isAllergic: Boolean = false
    var allergicTo: String? = null
    var vaccination: String? = null
    var surgeries: String? = null
    val patient: RealmResults<DBPatient> by backlinks(DBPatient::visits)

    companion object {
        fun fromPlain(visit: Visit): DBVisit = DBVisit().apply {
            _id = ObjectId(visit.id)
            date = visit.date.toRealmInstant()
            reasons = visit.reasons
            summary = visit.summary
            growthStatus = visit.growthStatus
            weight = visit.weight
            height = visit.height
            temp = visit.temp
            results = visit.results
            diagnosis = visit.diagnosis
            nonPathologicalBg = visit.nonPathologicalBg
            pathologicalBg = visit.pathologicalBg
            isAllergic = visit.isAllergic
            allergicTo = visit.allergicTo
            vaccination = visit.vaccination
            surgeries = visit.surgeries
        }
    }

    fun toPlain() = Visit(
        id = this._id.toHexString(),
        patient = this.patient.get(0).toPlain(),
        date = this.date.toInstant(),
        reasons = this.reasons,
        summary = this.summary,
        growthStatus = this.growthStatus,
        weight = this.weight,
        height = this.height,
        temp = this.temp,
        results = this.results,
        diagnosis = this.diagnosis,
        treatment = this.treatment,
        nonPathologicalBg = this.nonPathologicalBg,
        pathologicalBg = this.pathologicalBg,
        isAllergic = this.isAllergic,
        allergicTo = this.allergicTo,
        vaccination = this.vaccination,
        surgeries = this.surgeries
    )
}