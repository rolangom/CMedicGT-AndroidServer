package com.rolangom.cmedicgt.domains

import android.content.Context
import com.rolangom.cmedicgt.domains.patients.Patient
import com.rolangom.cmedicgt.domains.patients.PatientRepo
import com.rolangom.cmedicgt.domains.visits.Visit
import com.rolangom.cmedicgt.shared.readAssetsFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer


// to forward port
// adb forward tcp:8080 tcp:8080

class Http4KAppService(
    val port: Int,
    private val context: Context,
    private val patientRepo: PatientRepo
): AppService {

    private val server: Http4kServer



    private val getPatientsHandler: HttpHandler = { req ->
        runBlocking {
            patientRepo.getPatients().map {
                buildResponseOkWithCount(it.size)
                    .with(
                        Body.auto<List<Patient>>().toLens() of it
                    )
            }.first()
        }
    }

    private val getPatientHandler: HttpHandler = { req ->
        runBlocking {
            val id = req.path("id")!!
            patientRepo.getPatient(id).map {
                Response(Status.OK).with(
                    Body.auto<Patient>().toLens() of it
                )
            }.first()
        }
    }

    private val getPatientVisitsHandler: HttpHandler = { req ->
        runBlocking {
            val id = req.path("id")!!
            patientRepo.buildVisitsRepo(id).getVisits().map {
                buildResponseOkWithCount(it.size)
                    .with(
                        Body.auto<List<Visit>>().toLens() of it
                    )
            }.first()
        }
    }

    private val handleExceptionsFilter: Filter = Filter {
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
                        else -> throw err
                    }
                }
        }
    }

    init {
        val routes = routes (
            "/" bind Method.GET to {
                Response(Status.OK)
                    .header("content-type", "text/html")
                    .body(readAssetsFile(context, "index.html"))
            },
            "/api/patients" bind Method.GET to getPatientsHandler,
            "/api/patients/{id}" bind Method.GET to getPatientHandler,
            "/api/patients/{id}/visits" bind Method.GET to getPatientVisitsHandler,
            singlePageApp(ResourceLoader.Classpath("/assets") )
        )
            .withFilter(handleExceptionsFilter)
            .withFilter(ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive))
        server = routes.asServer(Undertow(port))
    }

    override fun startService() {
        server.start()
    }

    override fun stopService() {
        server.stop()
    }
}

private fun buildResponseOkWithCount(total: Int): Response {
    return Response(Status.OK)
        .header("Access-Control-Expose-Headers", "x-total-count")
        .header("x-total-count", total.toString())
}