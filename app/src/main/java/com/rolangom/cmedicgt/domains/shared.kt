package com.rolangom.cmedicgt.domains

import com.rolangom.cmedicgt.domains.patients.DBPatient
import com.rolangom.cmedicgt.domains.visits.DBVisit
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.exceptions.SyncException
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.mongodb.sync.SyncSession
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import java.time.Instant

data class ErrorResp(val message: String)


fun RealmInstant.toInstant(): Instant {
    val sec: Long = this.epochSeconds
    // The value always lies in the range `-999_999_999..999_999_999`.
    // minus for timestamps before epoch, positive for after
    val nano: Int = this.nanosecondsOfSecond
    return if (sec >= 0) { // For positive timestamps, conversion can happen directly
        Instant.ofEpochSecond(sec, nano.toLong())
    } else {
        // For negative timestamps, RealmInstant starts from the higher value with negative
        // nanoseconds, while Instant starts from the lower value with positive nanoseconds
        // TODO This probably breaks at edge cases like MIN/MAX
        Instant.ofEpochSecond(sec - 1, 1_000_000 + nano.toLong())
    }
}

fun Instant.toRealmInstant(): RealmInstant {
    val sec: Long = this.epochSecond // this.epochSeconds
    // The value is always positive and lies in the range `0..999_999_999`.
    val nano: Int = this.nano //  .nanosecondsOfSecond
    return if (sec >= 0) { // For positive timestamps, conversion can happen directly
        RealmInstant.from(sec, nano)
    } else {
        // For negative timestamps, RealmInstant starts from the higher value with negative
        // nanoseconds, while Instant starts from the lower value with positive nanoseconds
        // TODO This probably breaks at edge cases like MIN/MAX
        RealmInstant.from(sec + 1, -1_000_000 + nano)
    }
}

fun SortBy.toRealmSort(): Sort {
    return if (this == SortBy.ASC) Sort.ASCENDING else Sort.DESCENDING
}
fun fromSortBy(value: SortBy): Sort {
    val result = if (value == SortBy.ASC) Sort.ASCENDING else Sort.DESCENDING
    return result
}

fun buildRealm(app: App, onSyncError: (session: SyncSession, error: SyncException) -> Unit): Realm {
    val config = SyncConfiguration.Builder(app.currentUser!!, setOf(DBPatient::class, DBVisit::class))
        .initialSubscriptions { realm ->
            add(
                realm.query<DBPatient>("owner_id == $0", app.currentUser!!.id),
                "cmedico-patients",
                true
            )
            add(
                realm.query<DBVisit>("owner_id == $0", app.currentUser!!.id),
                "cmedico-visits",
                true
            )
        }
        .errorHandler(onSyncError::invoke)
        .waitForInitialRemoteData()
        .build()
    val realm = Realm.open(config)
    return realm
}