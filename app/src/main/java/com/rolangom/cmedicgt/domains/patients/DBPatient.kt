package com.rolangom.cmedicgt.domains.patients

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
    var firstName: String = ""
    @Index
    var lastName: String? = ""
    var birthDate: RealmInstant? = RealmInstant.MIN
    var address: String? = ""
    var arsId: String? = ""
    var doctorName: String? = ""
    var scholarLevel: String? = null
//    var gender: GenderEnum = GenderEnum()
    var gender: String? = null
    var createdAt: RealmInstant = RealmInstant.now()
    var modifiedAt: RealmInstant = RealmInstant.now()
    var visits: RealmList<DBVisit> = realmListOf()

    companion object {
        fun fromPlain(patient: Patient): DBPatient = DBPatient().apply {
            _id = ObjectId(patient.id)
            nationalId = patient.nationalId
            firstName = patient.firstName
            lastName = patient.lastName
            birthDate = patient.birthDate?.toRealmInstant()
            address = patient.address
            arsId = patient.insuranceCompany
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
        scholarLevel = this.scholarLevel,
        address = this.address,
        insuranceCompany = this.arsId,
        doctorName = this.doctorName,
        gender = this.gender,
        createdAt = this.createdAt.toInstant(),
        modifiedAt = this.modifiedAt.toInstant(),
    )
}
