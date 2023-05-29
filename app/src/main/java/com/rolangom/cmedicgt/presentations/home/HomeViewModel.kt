package com.rolangom.cmedicgt.presentations.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.rolangom.cmedicgt.TAG
import com.rolangom.cmedicgt.domains.auth.AuthRepo
import com.rolangom.cmedicgt.domains.auth.RealmAuthRepo
import com.rolangom.cmedicgt.shared.fetchPublicIPAddress
import com.rolangom.cmedicgt.shared.getLocalIpAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class HomeEvent {
    object Logout : HomeEvent()
    class ShowMessage(val message: String) : HomeEvent()
}


class HomeViewModel(
    private val authRepo: AuthRepo = RealmAuthRepo
): ViewModel() {

    private var serviceBinding: ServiceBinding? = null

    private val _event: MutableSharedFlow<HomeEvent> = MutableSharedFlow()
    val event: Flow<HomeEvent> get() = _event

    private val _port: MutableState<Int?> = mutableStateOf(serviceBinding?.port)
    val port: State<Int?> get() = _port

    private val _isServiceRunning: MutableState<Boolean> = mutableStateOf(serviceBinding?.isServiceAlive() ?: false)
    val isServiceRunning: State<Boolean> get() = _isServiceRunning

    private val _localIpAddress: MutableState<String?> = mutableStateOf(null)
    private val _publicIpAddress: MutableState<String?> = mutableStateOf(null)
    val localIpAddress: State<String?> get() = _localIpAddress
    val publicIpAddress: State<String?> get() = _publicIpAddress

    val localAppURL: String? get() =
        if (isServiceRunning.value)
            "http://${localIpAddress.value ?: "localhost"}:${port.value ?: 8080}"
        else
            null

    val publicAppURL: String? get() =
        if (isServiceRunning.value && !publicIpAddress.value.isNullOrEmpty())
            "http://${publicIpAddress.value ?: "localhost"}:${port.value ?: 8080}"
        else
            null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            event.collect {
                when (it) {
                    is HomeEvent.Logout -> _logout()
                    else -> Log.d(TAG(), "HomeViewModel Un expected event")
                }
            }
        }
        maybeFetchDeviceIpAddress()
    }

    val isServiceAlive: Boolean get() = serviceBinding?.isServiceAlive() ?: false

    private fun handleChange() {
        _port.value = serviceBinding?.port
        _isServiceRunning.value = serviceBinding?.isServiceAlive() ?: false
        Log.d(TAG(), "handleChange Service isServiceAlive: ${serviceBinding?.isServiceAlive()}, ${_isServiceRunning.value}")
    }

    fun onCreate(context: Context) {
        serviceBinding = ServiceBinding(context, ::handleChange, ::handleChange)
        if (isServiceAlive) {
            serviceBinding!!.doBindService()
        }

        Log.d(TAG(), "_isServiceRunning ${serviceBinding?.isServiceAlive()}, ${_isServiceRunning.value}")
    }

    fun browseWebURL(context: Context, url: String) {
        val urlIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        startActivity(context, urlIntent, null)
    }

    fun onDestroy() {
        serviceBinding?.doUnbindService()
    }

    fun updatePort(port: String) {
        _port.value = port.toInt() 
    }

    private fun maybeFetchDeviceIpAddress() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _localIpAddress.value = getLocalIpAddress()
                _publicIpAddress.value = fetchPublicIPAddress()
                println("maybeFetchPublicIPAddress ${_publicIpAddress.value}")
            } catch (err: Exception) {
                err.printStackTrace()
            }
        }
    }

    fun startService() {
        serviceBinding?.startHTTP4KServer(_port.value ?: 8080)
        serviceBinding!!.doBindService()

        _isServiceRunning.value = true
        Log.d(TAG(), "startService $serviceBinding ${serviceBinding?.isServiceAlive()} ${serviceBinding?.port}")

        maybeFetchDeviceIpAddress()
    }

    fun stopService() {
        serviceBinding?.stopHTTP4KServer()
        serviceBinding?.doUnbindService()

        _isServiceRunning.value = false
        Log.d(TAG(), "stopService $serviceBinding ${serviceBinding?.isServiceAlive()} ${serviceBinding?.port}")
    }

    fun toggleService() {
        if (_isServiceRunning.value) {
            stopService()
        } else {
            startService()
        }
    }

    fun logout() {
        if (_isServiceRunning.value) {
            stopService()
        }
        CoroutineScope(Dispatchers.IO).launch {
            _event.emit(HomeEvent.Logout)
        }
    }

    private fun _logout() {

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                authRepo.logout()
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    _event.emit(HomeEvent.ShowMessage("Logged out successfully."))
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    _event.emit(
                        HomeEvent.ShowMessage(
                            "There was an error while Logging out '${it.message}'"
                        )
                    )
                }
            }
        }
    }

    companion object {
        fun factory(
            repository: AuthRepo,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return HomeViewModel(repository) as T
                }
            }
        }
    }

}