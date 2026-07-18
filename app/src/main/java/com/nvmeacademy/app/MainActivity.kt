package com.nvmeacademy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nvmeacademy.app.data.LocalContentRepository
import com.nvmeacademy.app.navigation.NvmeAcademyNavHost
import com.nvmeacademy.app.ui.theme.NvmeAcademyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as NvmeAcademyApp).repository

        setContent {
            NvmeAcademyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var isReady by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        repository.seedIfEmpty()
                        isReady = true
                    }

                    if (isReady) {
                        CompositionLocalProvider(LocalContentRepository provides repository) {
                            NvmeAcademyNavHost()
                        }
                    } else {
                        LoadingScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
