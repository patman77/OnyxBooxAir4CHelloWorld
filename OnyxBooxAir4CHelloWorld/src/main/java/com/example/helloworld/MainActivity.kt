package com.example.helloworld

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
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
    
    // E-ink device detection
    val isEInkDevice = remember {
        Build.MODEL.contains("BOOX", ignoreCase = true) ||
        Build.MANUFACTURER.contains("ONYX", ignoreCase = true) ||
        Build.BRAND.contains("ONYX", ignoreCase = true)
    }
    
    // Generate proper diagonal velocity vector (normalized)
    val velocityVector = remember {
        val angle = Random.nextFloat() * 2 * Math.PI // Random angle in radians
        val speed = 1f // Normalized speed
        Pair(
            (kotlin.math.cos(angle) * speed).toFloat(),
            (kotlin.math.sin(angle) * speed).toFloat()
        )
    }
    
    // Current velocity (will be modified during bouncing)
    var currentVelocityX by remember { mutableStateOf(velocityVector.first) }
    var currentVelocityY by remember { mutableStateOf(velocityVector.second) }
    
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
    
    // Optimized animation logic for e-ink and regular displays
    LaunchedEffect(containerWidth, containerHeight) {
        if (containerWidth > 0 && containerHeight > 0) {
            val maxX = containerWidth - logoSizePx
            val maxY = containerHeight - logoSizePx
            
            // E-ink optimized settings
            val baseAnimationDuration = if (isEInkDevice) 2000 else 1500 // Slower for e-ink
            val frameDelay = if (isEInkDevice) 100L else 16L // ~10fps for e-ink, ~60fps for regular
            val movementStep = if (isEInkDevice) 8f else 4f // Larger steps for e-ink to reduce updates
            
            while (true) {
                // Calculate the distance to each boundary
                val distanceToRightWall = maxX - animatedX.value
                val distanceToLeftWall = animatedX.value
                val distanceToBottomWall = maxY - animatedY.value
                val distanceToTopWall = animatedY.value
                
                // Calculate time to hit each boundary based on current velocity
                val timeToHitVerticalWall = if (currentVelocityX > 0) {
                    distanceToRightWall / abs(currentVelocityX)
                } else {
                    distanceToLeftWall / abs(currentVelocityX)
                }
                
                val timeToHitHorizontalWall = if (currentVelocityY > 0) {
                    distanceToBottomWall / abs(currentVelocityY)
                } else {
                    distanceToTopWall / abs(currentVelocityY)
                }
                
                // Determine which wall will be hit first
                val timeToCollision = minOf(timeToHitVerticalWall, timeToHitHorizontalWall)
                
                // Calculate the actual movement distance for this frame
                val actualMovementX = currentVelocityX * movementStep
                val actualMovementY = currentVelocityY * movementStep
                
                // Calculate new position
                var newX = animatedX.value + actualMovementX
                var newY = animatedY.value + actualMovementY
                
                // Handle boundary collisions and velocity reflection
                var hitWall = false
                
                if (newX <= 0f) {
                    newX = 0f
                    currentVelocityX = abs(currentVelocityX) // Bounce right
                    hitWall = true
                } else if (newX >= maxX) {
                    newX = maxX
                    currentVelocityX = -abs(currentVelocityX) // Bounce left
                    hitWall = true
                }
                
                if (newY <= 0f) {
                    newY = 0f
                    currentVelocityY = abs(currentVelocityY) // Bounce down
                    hitWall = true
                } else if (newY >= maxY) {
                    newY = maxY
                    currentVelocityY = -abs(currentVelocityY) // Bounce up
                    hitWall = true
                }
                
                // Animate to new position
                val animationDuration = if (isEInkDevice && hitWall) {
                    // Slightly longer pause on e-ink when hitting walls to reduce flicker
                    baseAnimationDuration / 8
                } else {
                    baseAnimationDuration / 20
                }
                
                // Use concurrent animations to maintain diagonal movement
                val animationSpec = tween<Float>(
                    durationMillis = animationDuration,
                    easing = LinearEasing
                )
                
                // Launch both animations concurrently to maintain diagonal movement
                val xAnimation = async { animatedX.animateTo(newX, animationSpec) }
                val yAnimation = async { animatedY.animateTo(newY, animationSpec) }
                
                // Wait for both animations to complete
                xAnimation.await()
                yAnimation.await()
                
                // Add frame delay for e-ink optimization
                if (isEInkDevice) {
                    delay(frameDelay)
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
