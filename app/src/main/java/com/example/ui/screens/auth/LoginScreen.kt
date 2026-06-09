package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.glassmorphic
import com.example.ui.theme.GlassBackgroundBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onNavigateToRegister: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var socialAuthChoice by remember { mutableStateOf<String?>(null) }

    GlassBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Emblem
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Symbolic Cross / Medical Shield
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🐾",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle / Title
            Text(
                text = "دستیار حرفه‌ای دامپزشکی",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "پلتفرم جامع محاسبات دارویی و پرونده‌های الکترونیک",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Input Column (RTL visual ordering)
            CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        showError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input"),
                    label = { Text("شماره موبایل") },
                    placeholder = { Text("مثال: 09121234567") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "تلفن") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("کلمه عبور") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "قفل") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "فراموشی رمز عبور؟",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { /* Handle forgot pass */ }
                    )
                    Text(
                        text = "فراموشی نام کاربری؟",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.clickable { /* Handle forgot user */ }
                    )
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لطفاً شماره موبایل معتبر وارد کنید.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (phoneNumber.length >= 10) {
                            viewModel.simulateLogin(phoneNumber)
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "ورود به حساب کاربری",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Alternative social logins
            Text(
                text = "ثبت‌نام و ورود سریع با شبکه‌های اجتماعی",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { socialAuthChoice = "Google" },
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("google_auth_btn")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoogleVectorIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    }
                }
                Button(
                    onClick = { socialAuthChoice = "Apple" },
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("apple_auth_btn")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppleVectorIcon(modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apple ID", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRegister() },
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "حساب کاربری ندارید؟ ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "ثبت‌نام کاربر جدید",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (socialAuthChoice != null) {
        val provider = socialAuthChoice ?: "Google"
        AlertDialog(
            onDismissRequest = { socialAuthChoice = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (provider == "Google") "🛡️ ورود سریع با Google" else " ورود سریع با Apple ID",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "لطفاً حساب کاربری و نقش خود را برای ورود تایید کنید:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Choose Profile / Account option
                    if (provider == "Google") {
                        val googleAccounts = listOf(
                            Pair("دکتر سمانه کاظمی", "samaneh.kazemi@gmail.com"),
                            Pair("امیرحسین زارعی (صاحب پت)", "amir.zarei.pet@gmail.com")
                        )

                        googleAccounts.forEach { (name, email) ->
                            val detectedRole = if (email.contains("pet")) "owner" else "vet"
                            val detectedGender = if (email.contains("samaneh")) "خانم" else "آقا"
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glassmorphic(accentGlow = true, cornerRadius = 12.dp)
                                    .clickable {
                                        viewModel.simulateSocialAuth(email, name, detectedRole, "Google", detectedGender)
                                        socialAuthChoice = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }

                                    Column {
                                        Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    } else {
                        val appleAccounts = listOf(
                            Pair("دکتر نوید کریمی", "n.karimi@icloud.com"),
                            Pair("سارا احمدی (پت اونر)", "sara.ahmadi@icloud.com")
                        )

                        appleAccounts.forEach { (name, email) ->
                            val detectedRole = if (email.contains("ahmadi")) "owner" else "vet"
                            val detectedGender = if (email.contains("ahmadi")) "خانم" else "آقا"
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glassmorphic(accentGlow = true, cornerRadius = 12.dp)
                                    .clickable {
                                        viewModel.simulateSocialAuth(email, name, detectedRole, "Apple", detectedGender)
                                        socialAuthChoice = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    }

                                    Column {
                                        Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "💡 با کلیک روی هر کدام از گزینه‌های بالا، عملیات ثبت‌نام و ورود به صورت فوری با ۱۰۰ سکه هدیه آغازین انجام می‌شود.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { socialAuthChoice = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun GoogleVectorIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sizePx = size.width
        val scale = sizePx / 24f
        
        // Red segment (top):
        val pathRed = Path().apply {
            moveTo(12f * scale, 4.77f * scale)
            cubicTo(13.76f * scale, 4.77f * scale, 15.35f * scale, 5.38f * scale, 16.59f * scale, 6.57f * scale)
            lineTo(20.03f * scale, 3.13f * scale)
            cubicTo(17.95f * scale, 1.19f * scale, 15.24f * scale, 0f, 12f * scale, 0f)
            cubicTo(7.37f * scale, 0f, 3.26f * scale, 2.71f * scale, 1.28f * scale, 6.63f * scale)
            lineTo(5.28f * scale, 9.73f * scale)
            cubicTo(6.22f * scale, 6.88f * scale, 8.87f * scale, 4.77f * scale, 12f * scale, 4.77f * scale)
            close()
        }
        drawPath(pathRed, Color(0xFFEA4335))
        
        // Yellow segment (left):
        val pathYellow = Path().apply {
            moveTo(1.28f * scale, 6.63f * scale)
            cubicTo(0.48f * scale, 8.22f * scale, 0f * scale, 10.06f * scale, 0f * scale, 12f * scale)
            cubicTo(0f * scale, 13.94f * scale, 0.48f * scale, 15.78f * scale, 1.28f * scale, 17.37f * scale)
            lineTo(5.28f * scale, 14.27f * scale)
            cubicTo(5.08f * scale, 13.55f * scale, 4.96f * scale, 12.8f * scale, 4.96f * scale, 12f * scale)
            cubicTo(4.96f * scale, 11.2f * scale, 5.08f * scale, 10.45f * scale, 5.28f * scale, 9.73f * scale)
            lineTo(1.28f * scale, 6.63f * scale)
            close()
        }
        drawPath(pathYellow, Color(0xFFFBBC05))
        
        // Green segment (bottom):
        val pathGreen = Path().apply {
            moveTo(5.28f * scale, 14.27f * scale)
            lineTo(1.28f * scale, 17.37f * scale)
            cubicTo(3.26f * scale, 21.29f * scale, 7.37f * scale, 24f * scale, 12f * scale, 24f * scale)
            cubicTo(15.24f * scale, 24f * scale, 17.96f * scale, 22.92f * scale, 19.94f * scale, 21.08f * scale)
            lineTo(16.07f * scale, 18.08f * scale)
            cubicTo(14.93f * scale, 18.85f * scale, 13.55f * scale, 19.23f * scale, 12f * scale, 19.23f * scale)
            cubicTo(8.87f * scale, 19.23f * scale, 6.22f * scale, 17.12f * scale, 5.28f * scale, 14.27f * scale)
            close()
        }
        drawPath(pathGreen, Color(0xFF34A853))
        
        // Blue segment (right):
        val pathBlue = Path().apply {
            moveTo(24f * scale, 12f * scale)
            cubicTo(24f * scale, 11.17f * scale, 23.93f * scale, 10.38f * scale, 23.79f * scale, 9.61f * scale)
            lineTo(12f * scale, 9.61f * scale)
            lineTo(12f * scale, 14.12f * scale)
            lineTo(18.44f * scale, 14.12f * scale)
            cubicTo(18.16f * scale, 15.63f * scale, 17.29f * scale, 16.92f * scale, 16.07f * scale, 18.08f * scale)
            lineTo(19.94f * scale, 21.08f * scale)
            cubicTo(22.21f * scale, 19f * scale, 24f * scale, 15.93f * scale, 24f * scale, 12f * scale)
            close()
        }
        drawPath(pathBlue, Color(0xFF4285F4))
    }
}

@Composable
fun AppleVectorIcon(modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Canvas(modifier = modifier) {
        val sizePx = size.width
        val scale = sizePx / 24f
        
        // Leaf
        val leafPath = Path().apply {
            moveTo(15.22f * scale, 6.01f * scale)
            cubicTo(15.76f * scale, 5.35f * scale, 16.13f * scale, 4.43f * scale, 16.03f * scale, 3.51f * scale)
            cubicTo(15.24f * scale, 3.54f * scale, 14.28f * scale, 4.04f * scale, 13.71f * scale, 4.71f * scale)
            cubicTo(13.22f * scale, 5.27f * scale, 12.79f * scale, 6.21f * scale, 12.91f * scale, 7.11f * scale)
            cubicTo(13.79f * scale, 7.18f * scale, 14.68f * scale, 6.68f * scale, 15.22f * scale, 6.01f * scale)
            close()
        }
        drawPath(leafPath, tint)
        
        // Apple Body
        val bodyPath = Path().apply {
            moveTo(13.68f * scale, 7.37f * scale)
            cubicTo(12.34f * scale, 7.37f * scale, 11.2f * scale, 8.21f * scale, 10.56f * scale, 8.21f * scale)
            cubicTo(9.91f * scale, 8.21f * scale, 8.98f * scale, 7.5f * scale, 7.87f * scale, 7.52f * scale)
            cubicTo(6.41f * scale, 7.54f * scale, 5.06f * scale, 8.38f * scale, 4.31f * scale, 9.68f * scale)
            cubicTo(2.8f * scale, 12.31f * scale, 3.92f * scale, 16.18f * scale, 5.38f * scale, 18.28f * scale)
            cubicTo(6.09f * scale, 19.31f * scale, 6.93f * scale, 20.45f * scale, 8.04f * scale, 20.41f * scale)
            cubicTo(9.11f * scale, 20.37f * scale, 9.52f * scale, 19.72f * scale, 10.81f * scale, 19.72f * scale)
            cubicTo(12.1f * scale, 19.72f * scale, 12.48f * scale, 20.41f * scale, 13.6f * scale, 20.39f * scale)
            cubicTo(14.74f * scale, 20.37f * scale, 15.48f * scale, 19.36f * scale, 16.18f * scale, 18.33f * scale)
            cubicTo(16.99f * scale, 17.14f * scale, 17.33f * scale, 15.99f * scale, 17.35f * scale, 15.93f * scale)
            cubicTo(17.32f * scale, 15.92f * scale, 15.1f * scale, 15.07f * scale, 15.08f * scale, 12.52f * scale)
            cubicTo(15.06f * scale, 10.38f * scale, 16.83f * scale, 9.36f * scale, 16.91f * scale, 9.31f * scale)
            cubicTo(15.91f * scale, 7.84f * scale, 14.35f * scale, 7.69f * scale, 13.68f * scale, 7.37f * scale)
            close()
        }
        drawPath(bodyPath, tint)
    }
}
