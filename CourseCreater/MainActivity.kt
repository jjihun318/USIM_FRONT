package com.example.coursecreater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.coursecreater.screens.RunningCourseScreen
import com.example.coursecreater.screens.CourseDetailScreen
import com.example.coursecreater.screens.PopularCourseScreen
import com.example.coursecreater.screens.MyCourseScreen
import com.example.coursecreater.screens.AddCourseScreen
import com.example.coursecreater.screens.RegisterCourseScreen
import androidx.activity.compose.BackHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var currentScreen by remember { mutableStateOf("main") }

                when (currentScreen) {
                    "main" -> RunningCourseScreen(
                        onLocationCourseClick = { currentScreen = "detail" },
                        onPopularCourseClick = { currentScreen = "popular" },
                        onMyCourseClick = { currentScreen = "mycourse" }
                    )
                    "detail" -> {
                        BackHandler {
                            currentScreen = "main"
                        }
                        CourseDetailScreen(
                            onBackClick = { currentScreen = "main" }
                        )
                    }
                    "popular" -> {
                        BackHandler {
                            currentScreen = "main"
                        }
                        PopularCourseScreen(
                            onBackClick = { currentScreen = "main" }
                        )
                    }
                    "mycourse" -> {
                        BackHandler {
                            currentScreen = "main"
                        }
                        MyCourseScreen(
                            onBackClick = { currentScreen = "main" },
                            onAddCourseClick = { currentScreen = "addcourse" }
                        )
                    }
                    "addcourse" -> {
                        BackHandler {
                            currentScreen = "mycourse"
                        }
                        AddCourseScreen(
                            onBackClick = { currentScreen = "mycourse" },
                            onRegisterClick = { date, location, distance, time ->
                                currentScreen = "registercourse"
                            }
                        )
                    }
                    "registercourse" -> {
                        BackHandler {
                            currentScreen = "addcourse"
                        }
                        RegisterCourseScreen(
                            onBackClick = { currentScreen = "addcourse" }
                        )
                    }
                }
            }
        }
    }
}
