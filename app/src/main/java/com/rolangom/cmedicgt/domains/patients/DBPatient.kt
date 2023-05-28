package com.rolangom.cmedicgt.domains.patients

import com.rolangom.cmedicgt.domains.Filterable
import com.rolangom.cmedicgt.domains.visits.DBVisit
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import com.rolangom.cmedicgt.domains.toInstant
import com.rolangom.cmedicgt.domains.toRealmInstant
import io.realm.kotlin.types.RealmInstant


//open class GenderEnum(
//    private var enumDescription: String = Gender.MALE.name
//): RealmObject {
//    var value: Gender
//        get() { return Gender.valueOf(enumDescription) }
//        set(newMyEnum) { enumDescription = newMyEnum.name }
//}

class DBPatient: RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var nationalId: String? = ""
    @Index
    var firstName: String   = ""
    @Index
    var lastName: String? = ""
    var birthDate: RealmInstant? = RealmInstant.MIN
    var growthStatus: String? = null
    var address: String? = ""
    var phoneNumber: String? = null
    var email: String? = null
    var insuranceCompany: String? = ""
    var owner_id: String = ""
    var doctorName: String? = ""
    var reasons: String? = null
    var summary: String? = null
    var scholarLevel: String? = null
    var accompaniedBy: String? = null
//    var gender: GenderEnum = GenderEnum()
    var gender: String? = null
    var createdAt: RealmInstant = RealmInstant.now()
    var modifiedAt: RealmInstant = RealmInstant.now()
    var visits: RealmList<DBVisit> = realmListOf()

    companion object {
        fun fromPlain(patient: Patient): DBPatient = DBPatient().apply {
            _id = if (patient.id != null) ObjectId(patient.id) else ObjectId()
            nationalId = patient.nationalId
            firstName = patient.firstName
            lastName = patient.lastName
            birthDate = patient.birthDate?.toRealmInstant()
            growthStatus = patient.growthStatus
            accompaniedBy = patient.accompaniedBy
            reasons = patient.reasons
            summary = patient.summary
            address = patient.address
            phoneNumber = patient.phoneNumber
            email = patient.email
            insuranceCompany = patient.insuranceCompany
            owner_id = patient.doctorId ?: ""
            doctorName = patient.doctorName
            scholarLevel = patient.scholarLevel
            gender = patient.gender
            createdAt = patient.createdAt.toRealmInstant()
            modifiedAt = patient.modifiedAt.toRealmInstant()
        }
    }

    fun toPlain(): Patient = Patient(
        id = this._id.toHexString(),
        nationalId = this.nationalId,
        firstName = this.firstName,
        lastName = this.lastName,
        birthDate = this.birthDate?.toInstant(),
        growthStatus = this.growthStatus,
        scholarLevel = this.scholarLevel,
        accompaniedBy = this.accompaniedBy,
        address = this.address,
        phoneNumber = this.phoneNumber,
        email = this.email,
        insuranceCompany = this.insuranceCompany,
        doctorId = this.owner_id,
        doctorName = this.doctorName,
        reasons = this.reasons,
        summary = this.summary,
        gender = this.gender,
        createdAt = this.createdAt.toInstant(),
        modifiedAt = this.modifiedAt.toInstant(),
    )

    fun update(patient: DBPatient) {
        nationalId = patient.nationalId
        firstName = patient.firstName
        lastName = patient.lastName
        birthDate = patient.birthDate
        growthStatus = patient.growthStatus
        scholarLevel = patient.scholarLevel
        accompaniedBy = patient.accompaniedBy
        address = patient.address
        phoneNumber = patient.phoneNumber
        email = patient.email
        insuranceCompany = patient.insuranceCompany
        owner_id = patient.owner_id
        doctorName = patient.doctorName
        reasons = patient.reasons
        summary = patient.summary
        gender = patient.gender
        createdAt = patient.createdAt
        modifiedAt = RealmInstant.now()
    }
}


data class FilterablePatient(
    val ids: List<String>? = null,
    val firstName: String? = null,
    val lastName: String? = null,
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
        if (!firstName.isNullOrEmpty())
            filterList.add("firstName CONTAINS[c] $${index++}")
        if (!lastName.isNullOrEmpty())
            filterList.add("lastName CONTAINS[c] $${index}")
        return filterList.joinToString(" AND ")
    }

    override fun getValues(): Array<out Any?> {
        val objIds = ids?.map { ObjectId(it) }?.toTypedArray().orEmpty()
        return listOfNotNull(
            *objIds,
            firstName,
            lastName
        ).toTypedArray()
    }
}