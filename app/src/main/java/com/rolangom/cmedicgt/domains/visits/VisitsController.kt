package com.rolangom.cmedicgt.domains.visits

import com.rolangom.cmedicgt.domains.BaseRepo
import com.rolangom.cmedicgt.domains.buildResponseOkWithInfCount
import com.rolangom.cmedicgt.domains.filterableVisitLens
import com.rolangom.cmedicgt.domains.pageableLens
import com.rolangom.cmedicgt.domains.sortByLens
import com.rolangom.cmedicgt.domains.visitBodyLens
import kotlinx.coroutines.flow.first
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

class VisitsController(repo: BaseRepo<Visit, FilterableVisit>) {

    val getVisitsHandler: HttpHandler = { req ->
        runBlocking {
            val pageable = pageableLens(req)
            val sortBy = sortByLens(req)
            val filters = filterableVisitLens(req)
            repo.list(sortBy, pageable, filters).map {
                buildResponseOkWithInfCount()
                    .with(
                        Body.auto<List<Visit>>().toLens() of it
                    )
            }.first()
        }
    }

    val getVisitHandler: HttpHandler = { req ->
        runBlocking {
            val id = req.path("id")!!
            repo.get(id).map {
                Response(Status.OK).with(
                    visitBodyLens of it
                )
            }.first()
        }
    }

    val postVisitHandler: HttpHandler = { req ->
        runBlocking {
            println("postVisitHandler 1 ${req.bodyString()}")
            val visit = visitBodyLens(req)
            println("postVisitHandler 2 ${visit}")
            val result = repo.post(visit)
            println("postVisitHandler 3 ${result}")
            Response(Status.CREATED).with(
                visitBodyLens of result
            )
        }
    }

    val putVisitHandler: HttpHandler = { req ->
        runBlocking {
            val id = req.path("id")!!
            val visit = visitBodyLens(req)
            val result = repo.update(id, visit)
            Response(Status.OK).with(
                visitBodyLens of result
            )
        }
    }

    val deleteVisitHandler: HttpHandler = { req ->
        runBlocking {
            val id = req.path("id")!!
            val result = repo.delete(id)
            Response(Status.OK).with(
                visitBodyLens of result
            )
        }
    }

    val visitsRoutes = routes(
        "/" bind Method.GET to getVisitsHandler,
        "/" bind Method.POST to postVisitHandler,
        "/{id}" bind Method.GET to getVisitHandler,
        "/{id}" bind Method.PUT to putVisitHandler,
        "/{id}" bind Method.DELETE to deleteVisitHandler,
    )
}