package com.example.perfilsice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.perfilsice.ui.screens.LoginScreen
import com.example.perfilsice.ui.screens.ProfileScreen
import com.example.perfilsice.ui.theme.PerfilSiceTheme
import com.example.perfilsice.ui.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PerfilSiceTheme {
                val viewModel: LoginViewModel = viewModel()
                if (viewModel.loginSuccess) {
                    ProfileScreen(viewModel = viewModel)
                } else {
                    LoginScreen(viewModel = viewModel)
                }
            }
        }
    }
}
