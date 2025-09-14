package com.example.helloworld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.helloworld.ui.theme.HelloWorldTheme
import kotlin.math.roundToInt
import kotlin.random.Random

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
    val density = LocalDensity.current
    
    // Logo size in dp
    val logoSizeDp = 200.dp
    val logoSizePx = with(density) { logoSizeDp.toPx() }
    
    // Container dimensions
    var containerWidth by remember { mutableStateOf(0f) }
    var containerHeight by remember { mutableStateOf(0f) }
    
    // Animation values for position
    val animatedX = remember { Animatable(0f) }
    val animatedY = remember { Animatable(0f) }
    
    // Velocity direction (1 or -1 for each axis)
    val velocityX = remember { if (Random.nextBoolean()) 1f else -1f }
    val velocityY = remember { if (Random.nextBoolean()) 1f else -1f }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                containerWidth = size.width.toFloat()
                containerHeight = size.height.toFloat()
                
                // Set initial random position within bounds
                if (containerWidth > 0 && containerHeight > 0) {
                    val maxX = containerWidth - logoSizePx
                    val maxY = containerHeight - logoSizePx
                    
                    val initialX = Random.nextFloat() * maxX
                    val initialY = Random.nextFloat() * maxY
                    
                    animatedX.snapTo(initialX)
                    animatedY.snapTo(initialY)
                }
            }
    ) {
        AsyncImage(
            model = R.drawable.dunreeb_logo,
            contentDescription = "Dunreeb Logo",
            modifier = Modifier
                .size(logoSizeDp)
                .offset {
                    IntOffset(
                        animatedX.value.roundToInt(),
                        animatedY.value.roundToInt()
                    )
                }
        )
    }
    
    // Animation logic
    LaunchedEffect(containerWidth, containerHeight) {
        if (containerWidth > 0 && containerHeight > 0) {
            val maxX = containerWidth - logoSizePx
            val maxY = containerHeight - logoSizePx
            
            // Animation duration for crossing the screen (3-4 seconds)
            val animationDuration = 3500 // 3.5 seconds
            
            var currentVelocityX = velocityX
            var currentVelocityY = velocityY
            
            while (true) {
                // Calculate target positions based on current velocity
                val targetX = if (currentVelocityX > 0) maxX else 0f
                val targetY = if (currentVelocityY > 0) maxY else 0f
                
                // Calculate time needed to reach each boundary
                val timeToReachX = if (maxX > 0) {
                    ((targetX - animatedX.value) / (maxX * currentVelocityX)) * animationDuration
                } else animationDuration.toFloat()
                
                val timeToReachY = if (maxY > 0) {
                    ((targetY - animatedY.value) / (maxY * currentVelocityY)) * animationDuration
                } else animationDuration.toFloat()
                
                // Determine which boundary will be hit first
                val timeToHit = minOf(timeToReachX.coerceAtLeast(0f), timeToReachY.coerceAtLeast(0f))
                
                if (timeToHit > 0) {
                    // Calculate positions when the first boundary is hit
                    val deltaX = (maxX * currentVelocityX * timeToHit) / animationDuration
                    val deltaY = (maxY * currentVelocityY * timeToHit) / animationDuration
                    
                    val newX = (animatedX.value + deltaX).coerceIn(0f, maxX)
                    val newY = (animatedY.value + deltaY).coerceIn(0f, maxY)
                    
                    // Animate to the collision point
                    animatedX.animateTo(
                        targetValue = newX,
                        animationSpec = tween(
                            durationMillis = timeToHit.roundToInt(),
                            easing = LinearEasing
                        )
                    )
                    animatedY.animateTo(
                        targetValue = newY,
                        animationSpec = tween(
                            durationMillis = timeToHit.roundToInt(),
                            easing = LinearEasing
                        )
                    )
                    
                    // Reflect velocity when hitting boundaries
                    if (newX <= 0f || newX >= maxX) {
                        currentVelocityX = -currentVelocityX
                    }
                    if (newY <= 0f || newY >= maxY) {
                        currentVelocityY = -currentVelocityY
                    }
                } else {
                    // Fallback: just reverse direction
                    currentVelocityX = -currentVelocityX
                    currentVelocityY = -currentVelocityY
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogoPreview() {
    HelloWorldTheme {
        LogoScreen()
    }
}
