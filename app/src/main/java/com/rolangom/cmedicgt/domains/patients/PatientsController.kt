package com.rolangom.cmedicgt.domains.patients

import com.rolangom.cmedicgt.domains.BaseRepoWithChildren
import com.rolangom.cmedicgt.domains.buildResponseOkWithInfCount
import com.rolangom.cmedicgt.domains.filterablePatientLens
import com.rolangom.cmedicgt.domains.filterableVisitLens
import com.rolangom.cmedicgt.domains.pageableLens
import com.rolangom.cmedicgt.domains.patientBodyLens
import com.rolangom.cmedicgt.domains.sortByLens
import com.rolangom.cmedicgt.domains.visitBodyLens
import com.rolangom.cmedicgt.domains.visits.FilterableVisit
import com.rolangom.cmedicgt.domains.visits.Visit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

class PatientsController(repo: BaseRepoWithChildren<Patient, FilterablePatient, Visit, FilterableVisit>) {

    private val getPatientsHandler: HttpHandler = { req ->
        runBlocking {
            val pageable = pageableLens(req)
            val sortBy = sortByLens(req)
            val filters = filterablePatientLens(req)
            repo.list(sortBy, pageable, filters).map {
                buildResponseOkWithInfCount()
                    .with(
                        Body.auto<List<Patient>>().toLens() of it
                    )
            }.first()
        }
    }

    private val getPatientHandler: HttpHandler = { req ->
        runBlocking {
            val id = req.path("id")!!
            repo.get(id).map {
                Response(Status.OK).with(
                    patientBodyLens of it
                )
            }.first()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val getVisitsHandler: HttpHandler = { req ->
        runBlocking {
            val id = req.path("id")!!
            val pageable = pageableLens(req)
            val sortBy = sortByLens(req)
            val filters = filterableVisitLens(req)
            repo.getChildRepo(id)
                .flatMapLatest { repo ->
                    repo.list(sortBy, pageable, filters)
                }
                .map {
                    buildResponseOkWithInfCount()
                        .with(
                            Body.auto<List<Visit>>().toLens() of it
                        )
                }.first()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val getVisitHandler: HttpHandler = { req ->
        runBlocking {
            val patientId = req.path("id")!!
            val visitId = req.path("visitId")!!
            repo.getChildRepo(patientId)
                .flatMapLatest { repo ->
                    repo.get(visitId)
                }
                .map {
                    Response(Status.OK).with(
                        visitBodyLens of it
                    )
                }.first()
        }
    }

    val postPatientHandler: HttpHandler = { req ->
        runBlocking {
            val patient = patientBodyLens(req)
            val result = repo.post(patient)
            Response(Status.CREATED).with(
                patientBodyLens of result
            )
        }
    }

    val postVisitHandler: HttpHandler = { req ->
        runBlocking {
            val patientId = req.path("id")!!
            val visit = visitBodyLens(req)
            repo.getChildRepo(patientId)
                .map { repo ->
                    val result = repo.post(visit)
                    Response(Status.CREATED).with(
                        visitBodyLens of result
                    )
                }.first()
        }
    }

    val putPatientHandler: HttpHandler = { req ->
        runBlocking {
            val id = req.path("id")!!
            val patient = patientBodyLens(req)
            val result = repo.update(id, patient)
            Response(Status.OK).with(
                patientBodyLens of result
            )
        }
    }

    val putVisitHandler: HttpHandler = { req ->
        runBlocking {
            val patientId = req.path("id")!!
            val visitId = req.path("visitId")!!
            val visit = visitBodyLens(req)
            repo.getChildRepo(patientId)
                .map { repo ->
                    val result = repo.update(visitId, visit)
                    Response(Status.OK).with(
                        visitBodyLens of result
                    )
                }.first()
        }
    }

    val deletePatientHandler: HttpHandler = { req ->
        runBlocking {
            val id = req.path("id")!!
            val result = repo.delete(id)
            Response(Status.OK).with(
                patientBodyLens of result
            )
        }
    }

    val deleteVisitHandler: HttpHandler = { req ->
        runBlocking {
            val patientId = req.path("id")!!
            val visitId = req.path("visitId")!!
            repo.getChildRepo(patientId)
                .map { repo ->
                    val result = repo.delete(visitId)
                    Response(Status.OK).with(
                        visitBodyLens of result
                    )
                }.first()
        }
    }

    val patientRoutes = routes(
        "/" bind Method.GET to getPatientsHandler,
        "/" bind Method.POST to postPatientHandler,
        "/{id}" bind Method.GET to getPatientHandler,
        "/{id}" bind Method.PUT to putPatientHandler,
        "/{id}" bind Method.DELETE to deletePatientHandler,
        "/{id}/visits" bind Method.GET to getVisitsHandler,
        "/{id}/visits" bind Method.POST to postVisitHandler,
        "/{id}/visits/{visitId}" bind Method.GET to getVisitHandler,
        "/{id}/visits/{visitId}" bind Method.PUT to putVisitHandler,
        "/{id}/visits/{visitId}" bind Method.DELETE to deleteVisitHandler,
    )
}