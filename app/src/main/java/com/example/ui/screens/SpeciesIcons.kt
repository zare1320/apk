package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun DogVectorIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f
        val radius = width * 0.35f

        // Draw Ears (Floppy)
        val leftEar = Path().apply {
            moveTo(cx - radius * 0.7f, cy - radius * 0.4f)
            quadraticTo(cx - radius * 1.5f, cy - radius * 0.1f, cx - radius * 1.2f, cy + radius * 0.7f)
            quadraticTo(cx - radius * 0.8f, cy + radius * 0.5f, cx - radius * 0.6f, cy + radius * 0.1f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(cx + radius * 0.7f, cy - radius * 0.4f)
            quadraticTo(cx + radius * 1.5f, cy - radius * 0.1f, cx + radius * 1.2f, cy + radius * 0.7f)
            quadraticTo(cx + radius * 0.8f, cy + radius * 0.5f, cx + radius * 0.6f, cy + radius * 0.1f)
            close()
        }
        drawPath(leftEar, tint)
        drawPath(rightEar, tint)

        // Draw Head
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(cx, cy)
        )

        // Draw Snout
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(cx - radius * 0.42f, cy + radius * 0.05f),
            size = Size(radius * 0.84f, radius * 0.55f),
            cornerRadius = CornerRadius(radius * 0.25f, radius * 0.25f)
        )

        // Draw Nose (dark slate)
        drawCircle(
            color = Color(0xFF1E293B),
            radius = radius * 0.14f,
            center = Offset(cx, cy + radius * 0.18f)
        )

        // Draw Eyes
        drawCircle(
            color = Color(0xFF1E293B),
            radius = radius * 0.09f,
            center = Offset(cx - radius * 0.32f, cy - radius * 0.12f)
        )
        drawCircle(
            color = Color(0xFF1E293B),
            radius = radius * 0.09f,
            center = Offset(cx + radius * 0.32f, cy - radius * 0.12f)
        )
        // Eye highlights
        drawCircle(
            color = Color.White,
            radius = radius * 0.03f,
            center = Offset(cx - radius * 0.34f, cy - radius * 0.14f)
        )
        drawCircle(
            color = Color.White,
            radius = radius * 0.03f,
            center = Offset(cx + radius * 0.30f, cy - radius * 0.14f)
        )
    }
}

@Composable
fun CatVectorIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f
        val radius = width * 0.35f

        // Draw Pointy Ears
        val leftEar = Path().apply {
            moveTo(cx - radius * 0.95f, cy - radius * 0.3f)
            lineTo(cx - radius * 1.05f, cy - radius * 1.05f)
            lineTo(cx - radius * 0.35f, cy - radius * 0.8f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(cx + radius * 0.95f, cy - radius * 0.3f)
            lineTo(cx + radius * 1.05f, cy - radius * 1.05f)
            lineTo(cx + radius * 0.35f, cy - radius * 0.8f)
            close()
        }
        drawPath(leftEar, tint)
        drawPath(rightEar, tint)

        // Draw Inner Ears (soft pink accent)
        val innerLeftEar = Path().apply {
            moveTo(cx - radius * 0.85f, cy - radius * 0.4f)
            lineTo(cx - radius * 0.95f, cy - radius * 0.95f)
            lineTo(cx - radius * 0.45f, cy - radius * 0.72f)
            close()
        }
        val innerRightEar = Path().apply {
            moveTo(cx + radius * 0.85f, cy - radius * 0.4f)
            lineTo(cx + radius * 0.95f, cy - radius * 0.95f)
            lineTo(cx + radius * 0.45f, cy - radius * 0.72f)
            close()
        }
        drawPath(innerLeftEar, Color(0xFFFDA4AF))
        drawPath(innerRightEar, Color(0xFFFDA4AF))

        // Draw Head
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(cx, cy)
        )

        // Draw Eyes (stylish cat eyes)
        drawCircle(
            color = Color(0xFFFDE047), // lime/yellow-green
            radius = radius * 0.14f,
            center = Offset(cx - radius * 0.32f, cy - radius * 0.05f)
        )
        drawCircle(
            color = Color(0xFFFDE047),
            radius = radius * 0.14f,
            center = Offset(cx + radius * 0.32f, cy - radius * 0.05f)
        )
        // Slit pupils
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(cx - radius * 0.34f, cy - radius * 0.14f),
            size = Size(radius * 0.04f, radius * 0.18f),
            cornerRadius = CornerRadius(radius * 0.02f, radius * 0.02f)
        )
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(cx + radius * 0.30f, cy - radius * 0.14f),
            size = Size(radius * 0.04f, radius * 0.18f),
            cornerRadius = CornerRadius(radius * 0.02f, radius * 0.02f)
        )

        // Nose (pink)
        val nose = Path().apply {
            moveTo(cx - radius * 0.1f, cy + radius * 0.12f)
            lineTo(cx + radius * 0.1f, cy + radius * 0.12f)
            lineTo(cx, cy + radius * 0.22f)
            close()
        }
        drawPath(nose, Color(0xFFFDA4AF))

        // Whiskers (elegant lines)
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(cx - radius * 0.4f, cy + radius * 0.18f),
            end = Offset(cx - radius * 1.05f, cy + radius * 0.10f),
            strokeWidth = 3f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(cx - radius * 0.4f, cy + radius * 0.23f),
            end = Offset(cx - radius * 1.05f, cy + radius * 0.28f),
            strokeWidth = 3f
        )

        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(cx + radius * 0.4f, cy + radius * 0.18f),
            end = Offset(cx + radius * 1.05f, cy + radius * 0.10f),
            strokeWidth = 3f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(cx + radius * 0.4f, cy + radius * 0.23f),
            end = Offset(cx + radius * 1.05f, cy + radius * 0.28f),
            strokeWidth = 3f
        )
    }
}

@Composable
fun ExoticVectorIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f
        val radius = width * 0.35f

        // Base parrot head
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(cx - radius * 0.1f, cy)
        )

        // Plume / Crest
        val crest = Path().apply {
            moveTo(cx - radius * 0.4f, cy - radius * 0.8f)
            quadraticTo(cx - radius * 0.7f, cy - radius * 1.4f, cx - radius * 0.25f, cy - radius * 1.35f)
            quadraticTo(cx + radius * 0.15f, cy - radius * 1.05f, cx, cy - radius * 0.8f)
            close()
        }
        drawPath(crest, tint)

        // Beak (Curved Vibrant Orange parrot beak)
        val beak = Path().apply {
            moveTo(cx + radius * 0.45f, cy - radius * 0.25f)
            quadraticTo(cx + radius * 1.25f, cy - radius * 0.05f, cx + radius * 0.95f, cy + radius * 0.55f)
            quadraticTo(cx + radius * 0.35f, cy + radius * 0.25f, cx + radius * 0.25f, cy + radius * 0.05f)
            close()
        }
        drawPath(beak, Color(0xFFF97316))

        // Cute face patch helper
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = radius * 0.32f,
            center = Offset(cx - radius * 0.18f, cy - radius * 0.08f)
        )

        // Eye
        drawCircle(
            color = Color(0xFF1E293B),
            radius = radius * 0.11f,
            center = Offset(cx - radius * 0.18f, cy - radius * 0.18f)
        )
        // Eye highlight
        drawCircle(
            color = Color.White,
            radius = radius * 0.04f,
            center = Offset(cx - radius * 0.22f, cy - radius * 0.22f)
        )
    }
}

@Composable
fun ReptileVectorIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f
        val radius = width * 0.35f

        // Head
        drawCircle(
            color = tint,
            radius = radius * 0.5f,
            center = Offset(cx - radius * 0.2f, cy - radius * 0.3f)
        )
        // Big funny eye
        drawCircle(
            color = Color(0xFFFBBF24), // Yellow
            radius = radius * 0.2f,
            center = Offset(cx - radius * 0.3f, cy - radius * 0.35f)
        )
        drawCircle(
            color = Color(0xFF1E293B), // Pupil
            radius = radius * 0.08f,
            center = Offset(cx - radius * 0.3f, cy - radius * 0.35f)
        )

        // Body curve
        val bodyPath = Path().apply {
            moveTo(cx - radius * 0.4f, cy - radius * 0.1f)
            quadraticTo(cx + radius * 0.8f, cy - radius * 0.4f, cx + radius * 0.7f, cy + radius * 0.4f)
            quadraticTo(cx + radius * 0.3f, cy + radius * 0.8f, cx - radius * 0.3f, cy + radius * 0.5f)
            quadraticTo(cx + radius * 0.2f, cy + radius * 0.4f, cx + radius * 0.3f, cy + radius * 0.1f)
            quadraticTo(cx + radius * 0.3f, cy - radius * 0.1f, cx - radius * 0.2f, cy - radius * 0.05f)
            close()
        }
        drawPath(bodyPath, tint)

        // Cute back spikes
        drawCircle(
            color = Color(0xFFEF4444),
            radius = radius * 0.1f,
            center = Offset(cx + radius * 0.1f, cy - radius * 0.42f)
        )
        drawCircle(
            color = Color(0xFFEF4444),
            radius = radius * 0.1f,
            center = Offset(cx + radius * 0.35f, cy - radius * 0.3f)
        )
        drawCircle(
            color = Color(0xFFEF4444),
            radius = radius * 0.1f,
            center = Offset(cx + radius * 0.55f, cy - radius * 0.1f)
        )
    }
}

@Composable
fun SharedSpeciesCircleButton(
    speciesKey: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outlineCol by animateColorAsState(if (isSelected) Color(0xFF2DD4BF) else Color.Transparent)
    val textCol by animateColorAsState(if (isSelected) Color(0xFF2DD4BF) else MaterialTheme.colorScheme.onSurfaceVariant)

    val gradient = when (speciesKey) {
        "dog" -> Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
        "cat" -> Brush.verticalGradient(listOf(Color(0xFF2DD4BF), Color(0xFF0D9488)))
        "bird" -> Brush.verticalGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB)))
        "rodent" -> Brush.verticalGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))
        "aquatic" -> Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0369A1)))
        "amphibian" -> Brush.verticalGradient(listOf(Color(0xFF4ADE80), Color(0xFF16A34A)))
        else -> Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706))) // reptile
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(85.dp)
                .clip(CircleShape)
                .background(gradient)
                .border(3.dp, outlineCol, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when (speciesKey) {
                "dog" -> Image(
                    painter = painterResource(id = R.drawable.img_species_dog),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
                "cat" -> Image(
                    painter = painterResource(id = R.drawable.img_species_cat),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
                "bird" -> Image(
                    painter = painterResource(id = R.drawable.img_species_bird),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
                "rodent" -> Image(
                    painter = painterResource(id = R.drawable.img_species_rodent),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
                "aquatic" -> Image(
                    painter = painterResource(id = R.drawable.img_species_aquatic),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(0.9f)
                )
                "amphibian" -> Image(
                    painter = painterResource(id = R.drawable.img_species_amphibian),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(0.9f)
                )
                else -> {
                    ReptileVectorIcon(
                        modifier = Modifier.fillMaxSize(0.7f),
                        tint = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textCol
        )
    }
}

