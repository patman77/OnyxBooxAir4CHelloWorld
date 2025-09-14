package com.example.helloworld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.helloworld.ui.theme.HelloWorldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelloWorldTheme {
                LogoScreen()
            }
        }
    }
}

@Composable
fun LogoScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = "https://logo.com/image-cdn/images/kts928pd/production/9fa92ac5a9498502d2707ced798d763fe7490ecc-1600x1026.png",
            contentDescription = "Dunreeb Logo"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LogoPreview() {
    HelloWorldTheme {
        LogoScreen()
    }
}
