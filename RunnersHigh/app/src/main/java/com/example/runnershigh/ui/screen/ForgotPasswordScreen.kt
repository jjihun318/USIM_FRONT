package com.example.runnershigh.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnershigh.ui.theme.RacingSansOne

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onResetClick: (email: String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Runner's",
            fontFamily = RacingSansOne,
            fontSize = 96.sp,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 40.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBackClick
                ) {
                    Text(text = "Cancel", color = Color.Black)
                }

                Spacer(modifier = Modifier.width(24.dp))

                Button(
                    onClick = { onResetClick(email) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(text = "Reset Password")
                }
            }
        }

        Text(
            text = "High.",
            fontFamily = RacingSansOne,
            fontSize = 96.sp,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-24).dp)
        )
    }
}
