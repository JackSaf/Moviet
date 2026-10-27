package com.jacks.moviet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.jacks.moviet.app.navigation.NavigationRoot
import com.jacks.moviet.core.designsystem.theme.MovietTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovietTheme {
                NavigationRoot(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
