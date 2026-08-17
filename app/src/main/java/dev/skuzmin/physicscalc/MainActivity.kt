package dev.skuzmin.physicscalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.skuzmin.physicscalc.ui.PhysicsCalcApp
import dev.skuzmin.physicscalc.ui.theme.PhysicsCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhysicsCalcTheme {
                PhysicsCalcApp()
            }
        }
    }
}
