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
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.glassmorphic
import com.example.ui.theme.GlassBackgroundBox
import com.example.ui.theme.VazirFontFamily
import com.example.ui.theme.InterFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onNavigateToRegister: () -> Unit
) {
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val isDark = currentThemeMode == "dark"

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
    var socialAuthChoice by remember { mutableStateOf<String?>(null) }

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

    val currentLanguage by viewModel.currentLanguage.collectAsState()

    val appDescription = when (currentLanguage) {
        "en" -> "Intelligent veterinary reference & your trusted companion for pet health management"
        "ar" -> "مرجع الطب البيطري الذكي ورفيقك الموثوق لإدارة صحة الحيوانات الأليفة"
        else -> "مرجع هوشمند دامپزشکی و همراه قابل اعتماد شما برای مدیریت سلامت حیوانات"
    }
    val titleLoginRegister = when (currentLanguage) {
        "en" -> "Login / Register"
        "ar" -> "تسجيل الدخول / التسجيل"
        else -> "ورود / ثبت نام"
    }
    val subtitleHello = when (currentLanguage) {
        "en" -> "Hello!\nPlease enter your credentials"
        "ar" -> "مرحباً!\nيرجى إدخال بيانات الاعتماد الخاصة بك"
        else -> "سلام!\nلطفا اطلاعات کاربری خود را وارد کنید"
    }

    GlassBackgroundBox(isDark = isDark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            // Modern Settings Header Bar (Floating at Top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Theme Toggle Icon
                IconButton(
                    onClick = { viewModel.toggleTheme() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), CircleShape)
                        .testTag("login_theme_toggle")
                ) {
                    Text(
                        text = if (currentThemeMode == "dark") "☀️" else "🌙",
                        fontSize = 18.sp
                    )
                }

                // Language Selection pill
                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "en" to "🇬🇧 EN",
                        "fa" to "🇮🇷 FA",
                        "ar" to "🇸🇦 AR"
                    ).forEach { (code, label) ->
                        val isSelected = currentLanguage == code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .clickable { viewModel.setLanguage(code) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("login_lang_$code"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Main centered login content
            val loginLayoutDirection = if (currentLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
            CompositionLocalProvider(LocalLayoutDirection provides loginLayoutDirection) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
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
                                text = appDescription,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Main Glassmorphic Card Container
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphic(isDark = isDark, cornerRadius = 24.dp, accentGlow = step == 2)
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
                                            contentDescription = if (currentLanguage == "en") "Back" else "بازگشت",
                                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            // --- STEP 1: LOGIN / REGISTER INITIAL ---
                            if (step == 1) {
                                Text(
                                    text = titleLoginRegister,
                                    fontFamily = VazirFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = subtitleHello,
                                    fontFamily = VazirFontFamily,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
                                )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Custom Textfield look matching screenshot 2
                            val containerCol = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F4F9)
                            val borderCol = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)

                            val placeholderPhoneInput = when (currentLanguage) {
                                "en" -> "Phone number, email or username"
                                "ar" -> "رقم الهاتف أو البريد الإلكتروني أو اسم المستخدم"
                                else -> "شماره موبایل یا ایمیل یا نام کاربری"
                            }
                            val errorInvalidInput = when (currentLanguage) {
                                "en" -> "Please enter a valid value."
                                "ar" -> "يرجى إدخال قيمة صالحة."
                                else -> "لطفاً مقداری معتبر وارد کنید."
                            }
                            val buttonLoginText = when (currentLanguage) {
                                "en" -> "Login"
                                "ar" -> "تسجيل الدخول"
                                else -> "ورود"
                            }
                            val textNoAccount = when (currentLanguage) {
                                "en" -> "Don't have an account yet? "
                                "ar" -> "ليس لديك حساب بعد؟ "
                                else -> "هنوز ثبت‌نام نکرده‌اید؟ "
                            }
                            val textCreateAccount = when (currentLanguage) {
                                "en" -> "Create new account"
                                "ar" -> "إنشاء حساب جديد"
                                else -> "ایجاد حساب کاربری جدید"
                            }

                            OutlinedTextField(
                                value = inputUsername,
                                onValueChange = {
                                    inputUsername = it
                                    showError = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_input"),
                                textStyle = TextStyle(
                                    fontFamily = InterFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                placeholder = {
                                    Text(
                                        placeholderPhoneInput,
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
                                    text = errorInvalidInput,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action button matching color of screenshot (rust reddish-brown #B55D57)
                            Button(
                                onClick = {
                                    if (inputUsername.length >= 4) {
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
                                        text = buttonLoginText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = textNoAccount,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = textCreateAccount,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { onNavigateToRegister() }
                                        .testTag("navigate_to_register")
                                )
                            }
                        }

                        // --- STEP 2: VERIFICATION & REGISTRATION ---
                        if (step == 2) {
                            val headerTitle = when (currentLanguage) {
                                "en" -> "Verification Code"
                                "ar" -> "رمز التحقق"
                                else -> "کد تایید"
                            }
                            val firstNameLabel = when (currentLanguage) {
                                "en" -> "First Name"
                                "ar" -> "الاسم الأول"
                                else -> "نام"
                            }
                            val lastNameLabel = when (currentLanguage) {
                                "en" -> "Last Name"
                                "ar" -> "الاسم الأخير"
                                else -> "نام خانوادگی"
                            }
                            val otpPlaceholder = when (currentLanguage) {
                                "en" -> "Verification Code"
                                "ar" -> "رمز التحقق"
                                else -> "کد تایید"
                            }
                            val vetRoleLabel = when (currentLanguage) {
                                "en" -> "🩺 Veterinarian"
                                "ar" -> "🩺 طبيب بيطري"
                                else -> "🩺 دامپزشک"
                            }
                            val ownerRoleLabel = when (currentLanguage) {
                                "en" -> "🐾 Pet Owner"
                                "ar" -> "🐾 صاحب الحيوان"
                                else -> "🐾 صاحب حیوان"
                            }
                            val confirmButtonLabel = when (currentLanguage) {
                                "en" -> "Confirm"
                                "ar" -> "تأكيد"
                                else -> "تایید"
                            }

                            Text(
                                text = headerTitle,
                                fontFamily = VazirFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
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
                                        when (currentLanguage) {
                                            "en" -> "Welcome! Your account under \"${existingSession?.fullName}\" was found. Please enter the SMS verification code to enter (you can type any code or 123456)."
                                            "ar" -> "مرحباً! تم العثور على حسابك باسم \"${existingSession?.fullName}\". يرجى إدخال رمز التحقق الوارد في الرسالة للدخول (يمكنك كتابة أي رمز أو 123456)."
                                            else -> "خوش‌آمدید! حساب کاربری شما با عنوان «${existingSession?.fullName}» یافت شد. برای ورود به سامانه، لطفاً کد تایید پیامک‌شده را وارد کنید (می‌توانید کدهای دلخواه یا ۱۲۳۴۵۶ را تایپ کنید)."
                                        }
                                    } else {
                                        when (currentLanguage) {
                                            "en" -> "An account with number $inputUsername does not exist. A verification code has been sent to create a new account (you can enter any code or 123456)."
                                            "ar" -> "لا يوجد حساب برقم الهاتف $inputUsername. تم إرسال رمز التحقق لإنشاء حساب جديد لكم (يمكنكم إدخال أي رمز أو 123456)."
                                            else -> "حساب کاربری با شماره موبایل $inputUsername وجود ندارد. برای ساخت حساب جدید، کد تایید برای این شماره ارسال گردید (می‌توانید کدهای دلخواه یا ۱۲۳۴۵۶ را وارد کنید)."
                                        }
                                    },
                                    fontFamily = VazirFontFamily,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
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
                                                firstNameLabel,
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
                                                lastNameLabel,
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
                                textStyle = TextStyle(
                                    fontFamily = InterFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                placeholder = {
                                    Text(
                                        otpPlaceholder,
                                        fontFamily = VazirFontFamily,
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
                                val timerMessage = if (secondsLeft > 0) {
                                    when (currentLanguage) {
                                        "en" -> "$timerText until resend"
                                        "ar" -> "إعادة الإرسال خلال $timerText"
                                        else -> "$timerText تا دریافت مجدد کد"
                                    }
                                } else {
                                    when (currentLanguage) {
                                        "en" -> "Resend code"
                                        "ar" -> "إعادة إرسال الرمز"
                                        else -> "ارسال مجدد کد تایید"
                                    }
                                }

                                Text(
                                    text = timerMessage,
                                    fontFamily = InterFontFamily,
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
                                        .height(54.dp)
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
                                            text = vetRoleLabel,
                                            fontFamily = VazirFontFamily,
                                            fontSize = 13.sp,
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
                                            text = ownerRoleLabel,
                                            fontFamily = VazirFontFamily,
                                            fontSize = 13.sp,
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
                                        val finalFN = firstName.ifEmpty { "User" }
                                        val finalLN = lastName.ifEmpty { "" }
                                        viewModel.simulateRegistration(
                                            fullName = "$finalFN $finalLN",
                                            phoneNumber = inputUsername,
                                            userType = if (isVetMode) "vet" else "owner",
                                            licenseNum = if (isVetMode) "95843" else "",
                                            specOrUni = if (isVetMode) "General Practice" else "",
                                            gender = "Male"
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
                                    text = confirmButtonLabel,
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
                                        .glassmorphic(isDark = isDark, accentGlow = true, cornerRadius = 12.dp)
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
                                            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
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
                                        .glassmorphic(isDark = isDark, accentGlow = true, cornerRadius = 12.dp)
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
                                            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
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
                    Text(text = "انصراف", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
