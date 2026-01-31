package com.example.perfilsice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.perfilsice.ui.screens.LoginScreen
import com.example.perfilsice.ui.theme.PerfilSiceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PerfilSiceTheme {
                LoginScreen()
            }
        }
    }
}
