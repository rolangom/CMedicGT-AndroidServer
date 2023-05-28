package com.rolangom.cmedicgt.domains

import com.rolangom.cmedicgt.domains.patients.FilterablePatient
import com.rolangom.cmedicgt.domains.patients.Patient
import com.rolangom.cmedicgt.domains.visits.FilterableVisit
import com.rolangom.cmedicgt.domains.visits.Visit
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.lens.Query
import org.http4k.lens.composite
import org.http4k.lens.int
import org.http4k.lens.string


fun tweakRealmIdField(field: String): String =
    field.replace(Regex("^id$"), "_id")

val pageableLens = Query.composite {
    PaginateConfig(
        int().defaulted("_start", 0)(it),
        int().defaulted("_end", 10)(it)
    )
}
val sortByLens = Query.composite {
    Pair<String, SortBy>(
        tweakRealmIdField(string().defaulted("_sort", "_id")(it)),
        string().defaulted("_order", "ASC")(it).let { SortBy.valueOf(it) }
    )
}

val patientBodyLens = Body.auto<Patient>().toLens()
val visitBodyLens = Body.auto<Visit>().toLens()


val filterablePatientLens = Query.composite {
    FilterablePatient(
        ids = Query.string().multi.optional("id")(it), // .optional("id")(it),
        firstName = string().optional("firstName")(it),
        lastName = string().optional("lastName")(it),
    )
}


val filterableVisitLens = Query.composite {
    FilterableVisit(
        ids = string().multi.optional("id")(it),
        patientId = string().optional("patientId")(it),
    )
}


fun buildResponseOkWithCount(total: Int): Response {
    return Response(Status.OK)
        .header("Access-Control-Expose-Headers", "x-total-count")
        .header("x-total-count", total.toString())
}

fun buildResponseOkWithInfCount(): Response {
    return Response(Status.OK)
        .header("Access-Control-Expose-Headers", "x-total-count")
        .header("x-total-count", "Infinity")
}


val handleExceptionsFilter: Filter = Filter {
        next: HttpHandler -> {
            request: Request ->
        try {
            val response = next(request)
            response
        } catch (err: Exception) {
            when(err) {
                is NoSuchElementException,
                is IllegalStateException ->
                    Response(Status.NOT_FOUND).with(
                        Body.auto<ErrorResp>().toLens() of ErrorResp(err.message ?: "Not found")
                    )
                is IllegalArgumentException ->
                    Response(Status.NOT_ACCEPTABLE).with(
                        Body.auto<ErrorResp>().toLens() of ErrorResp(err.message ?: "Invalid arguments")
                    )
                else -> {
                    println("handleExceptionsFilter ${err}", )
                    throw err
                }
            }
        }
    }
}

