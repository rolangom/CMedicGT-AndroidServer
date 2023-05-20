package com.rolangom.cmedicgt

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rolangom.cmedicgt.ui.theme.CMedicGTTheme
import com.rolangom.cmedicgt.domains.auth.EventSeverity
import com.rolangom.cmedicgt.domains.auth.LoginAction
import com.rolangom.cmedicgt.domains.auth.LoginEvent
import com.rolangom.cmedicgt.domains.auth.LoginViewModel
import com.rolangom.cmedicgt.ui.login.LoginScaffold
import kotlinx.coroutines.launch

class ComposeLoginActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fast-track task list screen if we are logged in
        if (app.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Subscribe to navigation and message-logging events
                loginViewModel.event
                    .collect { event ->
                        when (event) {
                            is LoginEvent.GoToHome -> {
                                event.process()

                                val intent = Intent(this@ComposeLoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            is LoginEvent.ShowMessage -> event.process()
                        }
                    }
            }
        }

        setContent {
            CMedicGTTheme {
                LoginScaffold(loginViewModel)
            }
        }
    }

    private fun LoginEvent.process() {
        when (severity) {
            EventSeverity.INFO -> Log.i(TAG(), message)
            EventSeverity.ERROR -> {
                Log.e(TAG(), message)
                Toast.makeText(this@ComposeLoginActivity, message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginActivityPreview() {
    CMedicGTTheme {
        val viewModel = LoginViewModel().also {
            it.switchToAction(LoginAction.LOGIN)
            it.setEmail("test@test.com")
            it.setPassword("123456")
        }
        LoginScaffold(viewModel)
    }
}