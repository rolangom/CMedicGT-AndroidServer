package com.rolangom.cmedicgt.domains

import android.content.Context
import com.rolangom.cmedicgt.domains.patients.FilterablePatient
import com.rolangom.cmedicgt.domains.patients.Patient
import com.rolangom.cmedicgt.domains.patients.PatientsController
import com.rolangom.cmedicgt.domains.visits.FilterableVisit
import com.rolangom.cmedicgt.domains.visits.Visit
import com.rolangom.cmedicgt.domains.visits.VisitsController
import com.rolangom.cmedicgt.shared.readAssetsFile
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer


// to forward port
// adb forward tcp:8080 tcp:8080

class Http4KAppService(
    port: Int,
    private val context: Context,
    patientRepo: BaseRepoWithChildren<Patient, FilterablePatient, Visit, FilterableVisit>,
    visitsRepo: BaseRepo<Visit, FilterableVisit>
): AppService {

    private val server: Http4kServer

    init {
        val patientsController = PatientsController(patientRepo)
        val visitsController = VisitsController(visitsRepo)
        val routes = routes (
            "/" bind Method.GET to {
                Response(Status.OK)
                    .header("content-type", "text/html")
                    .body(readAssetsFile(context, "index.html"))
            },
            "/api/patients" bind patientsController.patientRoutes,
            "/api/visits" bind visitsController.visitsRoutes,
            singlePageApp(ResourceLoader.Classpath("/assets") )
        )
            .withFilter(handleExceptionsFilter)
            .withFilter(ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive))
            .withFilter(ServerFilters.CatchAll())
        server = routes.asServer(Netty(port))
    }

    override fun startService() {
        server.start()
    }

    override fun stopService() {
        server.stop()
    }

    override fun getPort(): Int {
        return server.port()
    }
}
