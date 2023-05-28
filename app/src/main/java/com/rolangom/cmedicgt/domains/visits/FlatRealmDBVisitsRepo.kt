package com.rolangom.cmedicgt.domains.visits

import com.rolangom.cmedicgt.domains.BaseRepo
import com.rolangom.cmedicgt.domains.PaginateConfig
import com.rolangom.cmedicgt.domains.SortBy
import com.rolangom.cmedicgt.domains.fromSortBy
import com.rolangom.cmedicgt.domains.patients.DBPatient
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import org.mongodb.kbson.ObjectId

class FlatRealmDBVisitsRepo(
    private val realm: Realm,
    private val app: App,
): BaseRepo<Visit, FilterableVisit> {

    private val currentUser get() = app.currentUser!!
    
    override fun get(id: String): Flow<Visit> {
        return realm.query<DBVisit>("_id == $0", ObjectId(id))
            .asFlow()
            .map { it.list.first().toPlain() }
    }

    override fun list(
        sort: Pair<String, SortBy>,
        paginate: PaginateConfig,
        filters: FilterableVisit
    ): Flow<List<Visit>> {
        val queryString = "owner_id == $0".let {
            val extraPlaceholders = filters.getStringFilters(1)
            if (extraPlaceholders.isNotEmpty()) "$it AND $extraPlaceholders" else it
        }
        val params = listOf(currentUser.id, *filters.getValues()).toTypedArray()
        return realm.query<DBVisit>(queryString, *params)
            .sort(sort.first, fromSortBy(sort.second))
            .asFlow()
            .drop(paginate.start)
            .take(paginate.end - paginate.start)
            .map { value -> value.list.toList().map { it.toPlain() } }
    }

    override suspend fun delete(id: String): Visit {
        return realm.query<DBVisit>("_id == $0", ObjectId(id))
            .first()
            .find()
            ?.let { visit ->
                val patient = realm.query<DBPatient>("_id == $0", visit.patient_id).first().find()
                realm.write {
                    findLatest(visit)?.let { this.delete(it) }
                    patient?.let {
                        findLatest(it)?.visits?.remove(visit)
                    }
                }
                visit.toPlain()
            }!!
    }

    override suspend fun update(id: String, item: Visit): Visit {
        return realm.query<DBVisit>("_id == $0", ObjectId(id))
            .first()
            .find()
            ?.let {
                val updated = DBVisit.fromPlain(item)
                realm.write {
                    findLatest(it)?.update(updated)
                }
                updated.toPlain()
            }!!
    }

    override suspend fun post(item: Visit): Visit {
        val visit = DBVisit.fromPlain(item).apply {
            owner_id = currentUser.id
        }
        return realm.query<DBPatient>("_id == $0", ObjectId(item.patientId)).asFlow().map {
            val patient = it.list.first()
            realm.write {
                val managedVisit = copyToRealm(visit)
                findLatest(patient)?.visits?.add(managedVisit)
                managedVisit.toPlain()
            }
        }.first()
    }

}