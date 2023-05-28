package com.rolangom.cmedicgt.domains.patients

import com.rolangom.cmedicgt.domains.BaseRepo
import com.rolangom.cmedicgt.domains.BaseRepoWithChildren
import com.rolangom.cmedicgt.domains.PaginateConfig
import com.rolangom.cmedicgt.domains.SortBy
import com.rolangom.cmedicgt.domains.fromSortBy
import com.rolangom.cmedicgt.domains.visits.FilterableVisit
import com.rolangom.cmedicgt.domains.visits.RealmDBVisitsRepo
import com.rolangom.cmedicgt.domains.visits.Visit
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import org.mongodb.kbson.ObjectId

class RealmDBPatientRepo(private val realm: Realm, private val app: App):
    BaseRepoWithChildren<Patient, FilterablePatient, Visit, FilterableVisit> {

    private val currentUser get() = app.currentUser!!

    private fun getDBItem(id: String): Flow<DBPatient> {
        return realm.query<DBPatient>("_id == $0", ObjectId(id))
            .asFlow()
            .map { it.list.first() }
    }

    override fun get(id: String): Flow<Patient> {
        return getDBItem(id).map { it.toPlain() }
    }

    override fun list(
        sort: Pair<String, SortBy>,
        paginate: PaginateConfig,
        filters: FilterablePatient
    ): Flow<List<Patient>> {
        val queryString = "owner_id == $0".let {
            val extraPlaceholders = filters.getStringFilters(1)
            if (extraPlaceholders.isNotEmpty()) "$it AND $extraPlaceholders" else it
        }
        val params = listOf(currentUser.id, *filters.getValues()).toTypedArray()
        return realm.query<DBPatient>(queryString, *params)
            .sort(sort.first, fromSortBy(sort.second))
            .asFlow()
            .drop(paginate.start)
            .take(paginate.end - paginate.start)
            .map { value -> value.list.toList().map { it.toPlain() } }
    }

    override suspend fun delete(id: String): Patient {
        return realm.query<DBPatient>("_id == $0", ObjectId(id))
            .first()
            .find()
            ?.let {
                realm.write {
                    findLatest(it)?.let { this.delete(it) }
                }
                it.toPlain()
            }!!
    }

    override suspend fun update(id: String, item: Patient): Patient {
        return realm.query<DBPatient>("_id == $0", ObjectId(id))
            .first()
            .find()
            ?.let {
                val updated = DBPatient.fromPlain(item)
                realm.write {
                    findLatest(it)?.update(updated)
                }
                updated.toPlain()
            }!!
    }

    override suspend fun post(item: Patient): Patient {
        val patient = DBPatient.fromPlain(item).apply {
            owner_id = currentUser.id
        }
        return realm.write {
            copyToRealm(patient).toPlain()
        }
    }

    override fun getChildRepo(parentId: String): Flow<BaseRepo<Visit, FilterableVisit>> {
        return getDBItem(parentId).map { patient -> RealmDBVisitsRepo(patient, realm, app) }
    }

}