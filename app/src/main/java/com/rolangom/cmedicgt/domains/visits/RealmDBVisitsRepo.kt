package com.rolangom.cmedicgt.domains.visits

import com.rolangom.cmedicgt.domains.BaseRepo
import com.rolangom.cmedicgt.domains.PaginateConfig
import com.rolangom.cmedicgt.domains.SortBy
import com.rolangom.cmedicgt.domains.fromSortBy
import com.rolangom.cmedicgt.domains.patients.DBPatient
import com.rolangom.cmedicgt.domains.patients.Patient
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import org.mongodb.kbson.ObjectId

class RealmDBVisitsRepo(patient: Patient, private val realm: Realm, private val app: App): BaseRepo<Visit, FilterableVisit> {
    private val dbPatient = DBPatient.fromPlain(patient)

    override fun get(id: String): Flow<Visit> {
        return dbPatient.visits.query("_id == $0", ObjectId(id)).asFlow()
            .map { it.list.first().toPlain() }
    }

    override fun list(
        sort: Pair<String, SortBy>,
        paginate: PaginateConfig,
        filters: FilterableVisit
    ): Flow<List<Visit>> {
        return dbPatient.visits.query()
            .sort(sort.first, fromSortBy(sort.second))
            .asFlow()
            .drop(paginate.start)
            .take(paginate.end - paginate.start)
            .map { it.list.map { it.toPlain() } }
    }

    override suspend fun delete(id: String): Visit {
        return dbPatient.visits.query("_id == $0", ObjectId(id))
            .first()
            .find()
            ?.let {
                realm.write {
                    findLatest(it)?.let { delete(it) }
                    dbPatient.visits.remove(it)
                    findLatest(dbPatient)?.update(dbPatient)
                }
                it.toPlain()
            }!!

    }

    override suspend fun update(id: String, item: Visit): Visit {
        return dbPatient.visits.query("_id == $0", ObjectId(id))
            .first()
            .find()
            ?.let {
                val updatedItem = DBVisit.fromPlain(item)
                realm.write {
                    findLatest(it)?.update(updatedItem)
                }
                updatedItem.toPlain()
            }!!
    }

    override suspend fun post(item: Visit): Visit = realm.write {
        val created = copyToRealm(DBVisit.fromPlain(item).apply {
            owner_id = app.currentUser!!.id
            patient_id = dbPatient._id
        })
        dbPatient.visits.add(created)
        findLatest(dbPatient)?.update(dbPatient)
        created.toPlain()
    }

}