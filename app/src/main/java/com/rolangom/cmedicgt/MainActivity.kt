package com.rolangom.cmedicgt

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rolangom.cmedicgt.domains.auth.AuthRepo
import com.rolangom.cmedicgt.domains.auth.RealmAuthRepo
import com.rolangom.cmedicgt.presentations.home.HomeEvent
import com.rolangom.cmedicgt.presentations.home.HomeViewModel
import com.rolangom.cmedicgt.ui.theme.CMedicGTTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val repository: AuthRepo = RealmAuthRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onCreate(this)
        setContent {
            CMedicGTTheme {
                CMedicoGTComposedApp(viewModel)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Subscribe to navigation and message-logging events
                viewModel.event
                    .collect { event ->
                        when (event) {
                            is HomeEvent.Logout -> {
                                val intent = Intent(this@MainActivity, ComposeLoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            is HomeEvent.ShowMessage -> {
                                Log.e(TAG(), event.message)
                                Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
            }
        }

    }

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.factory(repository, this)
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMedicoGTComposedApp(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val localURL by remember { derivedStateOf { viewModel.localAppURL } }
    val publicURL by remember { derivedStateOf { viewModel.publicAppURL } }
    Surface(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.padding(24.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "C-Medic GT")
                    Text(text = "Service")
                }
                ElevatedButton(
                    onClick = { viewModel.toggleService() }
                ) {
                    Text(if (viewModel.isServiceRunning.value) "Stop" else "Start")
                }
            }
            TextField(
                enabled = !viewModel.isServiceRunning.value,
                value = (viewModel.port.value ?: 8080).toString(),
                onValueChange = {
                    viewModel.updatePort(it)
                },
                label = { Text(stringResource(R.string.port)) }
            )
            Column() {
                Text(text = "IP Address")
                Text(text = viewModel.localIpAddress.value ?: "-")
            }
            if (viewModel.isServiceRunning.value) {
                ElevatedButton(
                    onClick = { viewModel.browseWebURL(context, localURL) }
                ) {
                    Text("Browse: \"${localURL}\"")
                }
            }
            if (!viewModel.publicIpAddress.value.isNullOrEmpty()) {
                ElevatedButton(
                    onClick = { viewModel.browseWebURL(context, publicURL) }
                ) {
                    Text("Browse: \"${publicURL}\"")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            ElevatedButton(
                onClick = { viewModel.logout() }
            ) {
                Text("Log out")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val viewModel = HomeViewModel(RealmAuthRepo)
    CMedicGTTheme {
        CMedicoGTComposedApp(
            viewModel
        )
    }
}