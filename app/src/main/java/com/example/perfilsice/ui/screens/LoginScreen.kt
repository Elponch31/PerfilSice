package com.example.perfilsice.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.perfilsice.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel) { // Recibimos el ViewModel por parámetro

    var matricula by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        // Header Verde
        Box(
            modifier = Modifier.fillMaxWidth().height(64.dp).background(Color(0xFF6CBF2A)),
            contentAlignment = Alignment.Center
        ) {
            Text("SICE net", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = matricula,
                onValueChange = { matricula = it },
                placeholder = { Text("Ingresa matrícula...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña...") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.loginAndFetchProfile(matricula, password)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6CBF2A))
                ) {
                    Text("Entrar", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.errorMessage.isNotEmpty()) {
                Text(text = viewModel.errorMessage, color = Color.Red)
            }
        }
    }
}