package com.example.ui.screens.auth

import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.glassmorphic
import com.example.ui.theme.GlassBackgroundBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onNavigateToRegister: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: ورود / ثبت نام, 2: کد تایید (OTP + Register if new)
    var inputUsername by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    var isCheckingUser by remember { mutableStateOf(false) }
    var existingSession by remember { mutableStateOf<com.example.data.database.UserSession?>(null) }
    
    // Step 2 profile fields
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isVetMode by remember { mutableStateOf(true) } // default vet, switcher in case they want owner
    var secondsLeft by remember { mutableStateOf(114) } // 01:54 = 114 seconds
    
    var showError by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    var showTermsError by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var socialAuthChoice by remember { mutableStateOf<String?>(null) }

    val themeMode by viewModel.themeMode.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isDark = themeMode == "dark"

    // Timer countdown effect when step 2 active
    LaunchedEffect(key1 = step) {
        if (step == 2) {
            secondsLeft = 114
            while (secondsLeft > 0) {
                kotlinx.coroutines.delay(1000)
                secondsLeft--
            }
        }
    }

    val timerText = remember(secondsLeft) {
        val mins = secondsLeft / 60
        val secs = secondsLeft % 60
        String.format("%02d:%02d", mins, secs)
    }

    GlassBackgroundBox {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Corner Theme & Language Toggle Buttons Row (Adapts neatly to LayoutDirection RTL/LTR)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Toggle Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable {
                            viewModel.setLanguage(if (currentLanguage == "en") "fa" else "en")
                        }
                        .testTag("language_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLanguage == "en") "FA" else "EN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Theme Toggle Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable {
                            viewModel.toggleTheme()
                        }
                        .testTag("theme_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.WbSunny else Icons.Default.DarkMode,
                        contentDescription = "Theme Toggle",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .navigationBarsPadding()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            
            // Header elements (App Info) can fade/slide
            AnimatedVisibility(
                visible = step == 1,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // App Emblem
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF312E81).copy(alpha = 0.45f))
                            .border(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo),
                            contentDescription = "Vetaris Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Vetaris",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (currentLanguage == "en") {
                            "Smart veterinary reference and your trusted companion for pet health management"
                        } else {
                            "مرجع هوشمند دامپزشکی و همراه قابل اعتماد شما برای مدیریت سلامت حیوانات"
                        },
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Glassmorphic Card Container - Page direction set dynamically: RTL/LTR
            val cardLayoutDirection = if (currentLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
            CompositionLocalProvider(LocalLayoutDirection provides cardLayoutDirection) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(cornerRadius = 24.dp, accentGlow = step == 2)
                        .padding(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        
                        // Back Icon overlay for Step 2
                        if (step == 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                IconButton(
                                    onClick = { step = 1 },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "بازگشت",
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // --- STEP 1: LOGIN / REGISTER INITIAL ---
                        if (step == 1) {
                            Text(
                                text = if (currentLanguage == "en") "Login / Sign Up" else "ورود / ثبت نام",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (currentLanguage == "en") {
                                    "Hello!\nPlease enter your user credentials"
                                } else {
                                    "سلام!\nلطفا اطلاعات کاربری خود را وارد کنید"
                                },
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Custom Textfield look matching screenshot 2
                            val containerCol = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F4F9)
                            val borderCol = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)

                            OutlinedTextField(
                                value = inputUsername,
                                onValueChange = {
                                    inputUsername = it
                                    showError = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_input"),
                                placeholder = {
                                    Text(
                                        if (currentLanguage == "en") "Mobile number, email, or username" else "شماره موبایل یا ایمیل یا نام کاربری",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = containerCol,
                                    unfocusedContainerColor = containerCol,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = borderCol,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                )
                            )

                            if (showError) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "لطفاً مقداری معتبر وارد کنید.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = termsAccepted,
                                    onCheckedChange = { 
                                        termsAccepted = it 
                                        if (it) showTermsError = false
                                    },
                                    modifier = Modifier.testTag("terms_checkbox"),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = if (currentLanguage == "en") "I accept the " else "من ",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = if (currentLanguage == "en") "Terms & Conditions" else "قوانین و مقررات استفاده",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clickable { showTermsDialog = true }
                                            .testTag("terms_and_conditions_text")
                                    )
                                    Text(
                                        text = if (currentLanguage == "en") " of Vetaris." else " را می‌پذیرم.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            if (showTermsError) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentLanguage == "en") "Please accept the terms and conditions to proceed." else "لطفاً برای ادامه، قوانین و مقررات را بپذیرید.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action button matching color of screenshot (rust reddish-brown #B55D57)
                            Button(
                                onClick = {
                                    if (!termsAccepted) {
                                        showTermsError = true
                                    } else if (inputUsername.length >= 4) {
                                        isCheckingUser = true
                                        coroutineScope.launch {
                                            val found = viewModel.checkUserExists(inputUsername)
                                            existingSession = found
                                            if (found != null) {
                                                val spaceIndex = found.fullName.trim().indexOf(' ')
                                                if (spaceIndex != -1) {
                                                    firstName = found.fullName.substring(0, spaceIndex).trim()
                                                    lastName = found.fullName.substring(spaceIndex + 1).trim()
                                                } else {
                                                    firstName = found.fullName
                                                    lastName = ""
                                                }
                                                isVetMode = (found.userType == "vet")
                                            } else {
                                                firstName = ""
                                                lastName = ""
                                            }
                                            isCheckingUser = false
                                            step = 2
                                        }
                                    } else {
                                        showError = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("submit_button"),
                                enabled = !isCheckingUser,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isCheckingUser) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = if (currentLanguage == "en") "Login" else "ورود",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // --- STEP 2: VERIFICATION & REGISTRATION ---
                        if (step == 2) {
                            Text(
                                text = if (currentLanguage == "en") "Verification Code" else "کد تایید",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Banner: Dynamic greeting based on user status
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (existingSession != null) {
                                        if (isDark) Color(0xFF1E3A1E) else Color(0xFFE6F4EA)
                                    } else {
                                        if (isDark) Color(0xFF33201F) else Color(0xFFFDF2F2)
                                    }
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (existingSession != null) {
                                        if (isDark) Color(0xFF2E7D32) else Color(0xFFA3E2AB)
                                    } else {
                                        if (isDark) Color(0xFF5C3331) else Color(0xFFF5D0CE)
                                    }
                                )
                            ) {
                                Text(
                                    text = if (existingSession != null) {
                                        if (currentLanguage == "en") {
                                            "Welcome back! Your account under \"${existingSession?.fullName}\" was found. Please enter the verification code sent to your phone (you can type any code or 123456)."
                                        } else {
                                            "خوش‌آمیدید! حساب کاربری شما با عنوان «${existingSession?.fullName}» یافت شد. برای ورود به سامانه، لطفاً کد تایید پیامک‌شده را وارد کنید (می‌توانید کدهای دلخواه یا ۱۲۳۴۵۶ را تایپ کنید)."
                                        }
                                    } else {
                                        if (currentLanguage == "en") {
                                            "No account associated with mobile $inputUsername exists. A verification code has been sent to register a new account (you can type any code or 123456)."
                                        } else {
                                            "حساب کاربری با شماره موبایل $inputUsername وجود ندارد. برای ساخت حساب جدید، کد تایید برای این شماره ارسال گردید (می‌توانید کدهای دلخواه یا ۱۲۳۴۵۶ را وارد کنید)."
                                        }
                                    },
                                    fontSize = 13.sp,
                                    lineHeight = 22.sp,
                                    color = if (existingSession != null) {
                                        if (isDark) Color(0xFF81C784) else Color(0xFF1E4620)
                                    } else {
                                        if (isDark) Color(0xFFF4B3B0) else Color(0xFF9B2C2C)
                                    },
                                    modifier = Modifier.padding(14.dp),
                                    textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // First and Last Name in one side-by-side Row - REGISTER ONLY
                            if (existingSession == null) {
                                val containerCol = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F4F9)
                                val borderCol = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = firstName,
                                        onValueChange = { firstName = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = {
                                            Text(
                                                if (currentLanguage == "en") "First Name" else "نام",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = containerCol,
                                            unfocusedContainerColor = containerCol,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = borderCol,
                                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                        )
                                    )

                                    OutlinedTextField(
                                        value = lastName,
                                        onValueChange = { lastName = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = {
                                            Text(
                                                if (currentLanguage == "en") "Last Name" else "نام خانوادگی",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = containerCol,
                                            unfocusedContainerColor = containerCol,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = borderCol,
                                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            // "کد تایید" input below them
                            val containerCol = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F4F9)
                            val borderCol = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        if (currentLanguage == "en") "Verification Code" else "کد تایید",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = containerCol,
                                    unfocusedContainerColor = containerCol,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = borderCol,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Timer Text details: e.g. "01:54 تا دریافت مجدد کد"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (secondsLeft > 0) {
                                        if (currentLanguage == "en") "$timerText until resend" else "$timerText تا دریافت مجدد کد"
                                    } else {
                                        if (currentLanguage == "en") "Resend verification code" else "ارسال مجدد کد تایید"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (secondsLeft > 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable(enabled = secondsLeft == 0) {
                                        secondsLeft = 114
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Role Selection Tab Switcher - REGISTER ONLY
                            if (existingSession == null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0))
                                        .padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isVetMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable { isVetMode = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (currentLanguage == "en") "🩺 Veterinarian" else "🩺 دامپزشک",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isVetMode) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (!isVetMode) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                            .clickable { isVetMode = false },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (currentLanguage == "en") "🐾 Pet Owner" else "🐾 صاحب حیوان",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isVetMode) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // Confirm Button matching screenshot
                            Button(
                                onClick = {
                                    if (existingSession != null) {
                                        viewModel.simulateLogin(inputUsername)
                                    } else {
                                        val finalFN = firstName.ifEmpty { "کاربر" }
                                        val finalLN = lastName.ifEmpty { "جدید" }
                                        viewModel.simulateRegistration(
                                            fullName = "$finalFN $finalLN",
                                            phoneNumber = inputUsername,
                                            userType = if (isVetMode) "vet" else "owner",
                                            licenseNum = if (isVetMode) "95843" else "",
                                            specOrUni = if (isVetMode) "داخلی حیوانات کوچک" else "",
                                            gender = "آقا"
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("submit_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (currentLanguage == "en") "Verify" else "تایید",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                    }
                }
            }


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
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
                                Pair("خانم دکتر سمانه زارع", "samaneh.zare@gmail.com"),
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

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLanguage == "en") "📋 Terms & Conditions" else "📋 شرایط و قوانین استفاده",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                val dialogLayoutDir = if (currentLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
                CompositionLocalProvider(LocalLayoutDirection provides dialogLayoutDir) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == "en") {
                                "Welcome to Vetaris, your smart veterinary companion.\n\n" +
                                "By accepting these terms and logging into Vetaris, you agree to the conditions listed below regarding application features, permissions, and professional practices:"
                            } else {
                                "به وتاریس، دستیار هوشمند دامپزشکی شما خوش آمدید.\n\n" +
                                "با پذیرش این شرایط و ورود به وتاریس، شما موافقت خود را با قوانین زیر در زمینه امکانات برنامه، سطوح دسترسی و استفاده حرفه‌ای اعلام می‌نمایید:"
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Feature 1: Drug Manual & Calculations
                        Text(
                            text = if (currentLanguage == "en") "1. Drug Reference & Dose Calculations" else "۱. مرجع دارویی و محاسبه دوز",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (currentLanguage == "en") {
                                "• Vetaris provides an automated veterinarian drug manual (pharmacopoeia) and clinical dose calculator (mg/ml/mcg).\n" +
                                "• Standard references are guides. Vets must double-check formulas with clinical judgment before medical administration."
                            } else {
                                "• وتاریس دسترسی به مرجع دارویی جامع دامپزشکی و محاسبه هوشمند دوز (بر اساس میلی‌گرم، میلی‌لیتر و میکروگرم) را فراهم می‌کند.\n" +
                                "• دوزهای مرجع صرفاً راهنما هستند. دامپزشک موظف است قبل از تجویز نهایی، محاسبات را با وضعیت بالینی حیوان تطبیق دهد."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 17.sp
                        )

                        // Feature 2: Prescription Generation
                        Text(
                            text = if (currentLanguage == "en") "2. Digital Prescription System" else "۲. سامانه نسخه‌نویسی دیجیتال",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (currentLanguage == "en") {
                                "• Veterinarians can draft, customize, and finalize digital prescriptions with automated calculations.\n" +
                                "• Finalized prescriptions are permanently logged to the patient's record for continuous reference."
                            } else {
                                "• دامپزشکان محترم می‌توانند با استفاده از سیستم هوشمند، نسخه نهایی را ثبت کرده و مقادیر محاسبه‌شده را ضمیمه نمایند.\n" +
                                "• نسخه‌های نهایی ثبت‌شده در پرونده الکترونیکی بیمار ذخیره شده و قابل ردیابی دائم هستند."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 17.sp
                        )

                        // Feature 3: Customized Role Spaces (Vets vs Pet Owners)
                        Text(
                            text = if (currentLanguage == "en") "3. Role-Based Access (Vet vs Pet Owner)" else "۳. سطوح دسترسی مبتنی بر نقش (دامپزشک و صاحب پت)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (currentLanguage == "en") {
                                "• Veterinarians are granted advanced clinical menus, medical histories, and prescription toolkits.\n" +
                                "• Pet Owners receive simplified explanations of medications, and access to view their pet's health log book."
                            } else {
                                "• پزشکان عزیز به منوهای پیشرفته بالینی، بانک داروهای تخصصی و ابزار مجهز نسخه‌نویسی دسترسی دارند.\n" +
                                "• صاحبان حیوانات خانگی به راهنمای ساده عوارض جانبی داروهای پرکاربرد، سوابق پزشکی حیوان و پرونده بهداشتی دسترسی خواهند داشت."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 17.sp
                        )

                        // Feature 4: Privacy & HIPAA Alignment
                        Text(
                            text = if (currentLanguage == "en") "4. Secure Pet Profile Storage" else "۴. امنیت اطلاعات و پرونده‌های بیماری",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (currentLanguage == "en") {
                                "• All patient information, phone credentials, and logs are kept encrypted on the offline database.\n" +
                                "• Your data remains secure on your device unless backup options are explicitly enabled."
                            } else {
                                "• تمامی اطلاعات سگ، گربه یا سایر پت‌ها به همراه شماره موبایل کاربران به صورت کاملاً امن و محلی ذخیره می‌شود.\n" +
                                "• امنیت شما در اولویت وتاریس است و اطلاعات محرمانه بیماران فاش نگردیده و در دستگاه شما ایمن خواهد بود."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 17.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        termsAccepted = true
                        showTermsError = false
                        showTermsDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (currentLanguage == "en") "Accept & Close" else "پذیرش و بستن")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTermsDialog = false }
                ) {
                    Text(if (currentLanguage == "en") "Reject" else "رد")
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
