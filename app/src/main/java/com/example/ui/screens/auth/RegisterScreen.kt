package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.DogVectorIcon
import com.example.ui.screens.CatVectorIcon
import com.example.ui.screens.ExoticVectorIcon
import com.example.data.database.Pet
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.glassmorphic
import com.example.ui.theme.GlassBackgroundBox
import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var isVetMode by remember { mutableStateOf(true) } // MODE switcher

    // General Fields
    var fullName by remember { mutableStateOf("") }
    var genderSelected by remember { mutableStateOf("آقا") } // "آقا" or "خانم"
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isOtpVerified by remember { mutableStateOf(false) }

    // Vet-Specific fields
    var isStudentMode by remember { mutableStateOf(false) } // true if student, false if clinical vet
    var schoolSelected by remember { mutableStateOf("دانشگاه تهران") }
    var studentId by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var specialtySelected by remember { mutableStateOf("داخلی حیوانات کوچک") }

    // Pet Owner fields
    var petName by remember { mutableStateOf("") }
    var petSpecies by remember { mutableStateOf("dog") } // "dog", "cat", "exotic"
    var petAge by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petWeight by remember { mutableStateOf("") }
    var showBreedHelpDialog by remember { mutableStateOf(false) }

    val registerBreedOptions = when (petSpecies) {
        "dog" -> listOf(
            "شیتزو (Shih Tzu)",
            "ژرمن شپرد (German Shepherd)",
            "هاسکی سیبرین (Siberian Husky)",
            "پودل (Poodle)",
            "پمرانین (Pomeranian)",
            "گلدن رتریور (Golden Retriever)",
            "پاگ (Pug)",
            "بولداگ (Bulldog)",
            "روتوایلر (Rottweiler)",
            "دوبرمن (Doberman)",
            "پیتبول (Pitbull)",
            "سرابی (Sarabi Mastiff)",
            "تریر (Terrier)",
            "داکسهوند (Dachshund)",
            "ساموید (Samoyed)",
            "گریت دین (Great Dane)",
            "باکسر (Boxer)",
            "بیگل (Beagle)",
            "چاو چاو (Chow Chow)",
            "کوکر اسپنیل (Cocker Spaniel)",
            "بومی / دورگه (Mixed Breed)"
        )
        "cat" -> listOf(
            "پرشین (Persian)",
            "دی‌اس‌اچ (DSH)",
            "اسکاتیش فولد (Scottish Fold)",
            "بریتیش فولد (British Fold)",
            "بریتیش شورت‌هر (British Shorthair)",
            "دی‌ال‌اچ (DLH)",
            "سیامی (Siamese)",
            "راگدول (Ragdoll)",
            "مین کون (Maine Coon)",
            "اسفینکس (Sphynx)",
            "راشن بلو (Russian Blue)",
            "بنگال (Bengal)",
            "آنگورای ترکی (Turkish Angora)",
            "بیرمن (Birman)",
            "بومی / دورگه (Mixed Breed)"
        )
        "exotic" -> listOf(
            "عروس هلندی (Cockatiel)",
            "مرغ عشق (Budgerigar)",
            "کاسکو (Grey Parrot)",
            "همستر روسی (Russian Hamster)",
            "خوکچه هندی (Guinea Pig)",
            "خرگوش لوپ (Lop Rabbit)",
            "ماهی قرمز (Goldfish)",
            "گوپی (Guppy)",
            "لاک‌پشت گوش‌قرمز (Red-eared Slider)",
            "آنجل (Angel Fish)"
        )
        else -> emptyList()
    }

    var isBreedDropdownExpanded by remember { mutableStateOf(false) }
    var breedTextFieldFocused by remember { mutableStateOf(false) }

    val filteredBreeds = remember(petBreed, registerBreedOptions) {
        if (petBreed.isEmpty()) {
            registerBreedOptions
        } else {
            registerBreedOptions.filter {
                it.contains(petBreed, ignoreCase = true)
            }
        }
    }

    val universityList = listOf(
        "دانشگاه تهران", "دانشگاه شیراز", "دانشگاه فردوسی مشهد",
        "دانشگاه علوم تحقیقات", "دانشگاه تبریز", "دانشگاه کار و هنر"
    )

    val specialtyList = listOf(
        "داخلی حیوانات کوچک", "جراحی و هوشبری", "کلینیکال پاتولوژی",
        "رادیولوژی و تصویربرداری", "مامایی و بیماری‌های تولیدمثل"
    )

    val currentThemeMode by viewModel.themeMode.collectAsState()
    val isDark = currentThemeMode == "dark"
    val textfieldBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F4F9)
    val textfieldBorder = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)
    val customAccentColor = Color(0xFFB55D57) // reddish rust premium color
    val activeThemeColor = if (isVetMode) customAccentColor else MaterialTheme.colorScheme.secondary

    GlassBackgroundBox(isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "ثبت نام کاربر جدید",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            // Scrollable Content wrapped in Glass Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphic(isDark = isDark, cornerRadius = 24.dp, accentGlow = true)
                                .padding(2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Role Switcher Tab
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0))
                                        .padding(2.dp)
                                ) {
                                    // Veterinarian
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isVetMode) customAccentColor else Color.Transparent
                                            )
                                            .clickable { isVetMode = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "🩺 دامپزشک هستم",
                                            color = if (isVetMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Pet Owner
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (!isVetMode) MaterialTheme.colorScheme.secondary else Color.Transparent
                                            )
                                            .clickable { isVetMode = false },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "🐾 صاحب پت هستم",
                                            color = if (!isVetMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Information Form General
                                Text(
                                    text = "اطلاعات کاربری عمومی",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text("نام و نام خانوادگی (مثال: دکتر علیرضا رضایی)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = textfieldBg,
                                        unfocusedContainerColor = textfieldBg,
                                        focusedBorderColor = customAccentColor,
                                        unfocusedBorderColor = textfieldBorder,
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Gender and prefix Select Rows
                                Text(
                                    text = "جنسیت و پیش‌وند نام:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf(
                                        Pair("آقا", if (isVetMode) "آقای دکتر" else "جناب آقای"),
                                        Pair("خانم", if (isVetMode) "خانم دکتر" else "سرکار خانم")
                                    ).forEach { (code, label) ->
                                        val isSel = genderSelected == code
                                        val bg = if (isSel) (if (isVetMode) customAccentColor else MaterialTheme.colorScheme.secondary) else textfieldBg
                                        val textColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(bg)
                                                .clickable { genderSelected = code }
                                                .defaultMinSize(minHeight = 44.dp)
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (code == "آقا") "👨 $label" else "👩 $label",
                                                color = textColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text("شماره تلفن همراه یا ایمیل (مثال: 09121234567)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = textfieldBg,
                                        unfocusedContainerColor = textfieldBg,
                                        focusedBorderColor = customAccentColor,
                                        unfocusedBorderColor = textfieldBorder,
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // OTP CODE FLOW VISUALS
                                if (!isOtpSent) {
                                    val isEmail = phoneNumber.contains("@")
                                    Button(
                                        onClick = {
                                            if (phoneNumber.isNotEmpty()) {
                                                isOtpSent = true
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = activeThemeColor,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = if (isEmail) "ارسال کد فعال‌سازی به ایمیل (OTP)" else "ارسال پیامک تایید هویت (OTP)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (!isOtpVerified) {
                                    val isEmail = phoneNumber.contains("@")
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC)
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, textfieldBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (isEmail) "کد فعال‌سازی ۶ رقمی ایمیل شده را وارد کنید (شبیه‌ساز: ۱۲۳۴۵۶)"
                                                else "کد فعال‌سازی ۶ رقمی پیامک شده را وارد کنید (شبیه‌ساز: ۱۲۳۴۵۶)",
                                                fontSize = 12.sp,
                                                color = customAccentColor,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 18.sp
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))

                                            BasicTextField(
                                                value = otpCode,
                                                onValueChange = {
                                                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                                        otpCode = it
                                                        if (it == "123456") {
                                                            isOtpVerified = true
                                                        }
                                                    }
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                decorationBox = {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        for (i in 0 until 6) {
                                                            val char = otpCode.getOrNull(i)?.toString() ?: ""
                                                            val isFocused = i == otpCode.length
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(42.dp)
                                                                    .border(
                                                                        width = if (isFocused) 2.dp else 1.dp,
                                                                        color = if (isFocused) customAccentColor else textfieldBorder,
                                                                        shape = RoundedCornerShape(10.dp)
                                                                    )
                                                                    .background(
                                                                        color = if (isFocused) customAccentColor.copy(alpha = 0.08f)
                                                                        else textfieldBg,
                                                                        shape = RoundedCornerShape(10.dp)
                                                                    ),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = char,
                                                                    fontSize = 18.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.onSurface,
                                                                    textAlign = TextAlign.Center
                                                                )
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("otp_code_6_digit")
                                            )
                                        }
                                    }
                                } else {
                                    val isEmail = phoneNumber.contains("@")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isDark) Color(0xFF1B4332) else Color(0xFFDCFCE7), RoundedCornerShape(14.dp))
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF52B788) else Color(0xFF15803D)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = if (isEmail) "نشانی ایمیل و کد تایید تایید گردید." else "شماره همراه شما تایید دو عاملی شد.",
                                            color = if (isDark) Color(0xFF95D5B2) else Color(0xFF15803D),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(28.dp))

                                // Role Specific Forms
                                if (isVetMode) {
                                    Text(
                                        text = "اطلاعات پروانه و صلاحیت علمی دامپزشکی",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Student or Clinician
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE2E8F0))
                                            .padding(2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (!isStudentMode) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                                .clickable { isStudentMode = false },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("👨‍⚕️ پروانه و طبابت کلینیکال", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isStudentMode) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                                .clickable { isStudentMode = true },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎓 رزیدنت یا دانشجوی دکترا", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (isStudentMode) {
                                        Text("🎓 مشخصات دانشگاه صادرکننده کارت:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                                        UniversityRowDropdown(
                                            currentSelection = schoolSelected,
                                            allSelections = universityList,
                                            onChoose = { schoolSelected = it }
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        OutlinedTextField(
                                            value = studentId,
                                            onValueChange = { studentId = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = {
                                                Text("شماره دانشجویی / کد موقت صنفی", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = textfieldBg,
                                                unfocusedContainerColor = textfieldBg,
                                                focusedBorderColor = customAccentColor,
                                                unfocusedBorderColor = textfieldBorder,
                                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                            )
                                        )
                                    } else {
                                        OutlinedTextField(
                                            value = licenseNumber,
                                            onValueChange = { licenseNumber = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = {
                                                Text("شماره پروانه نظام دامپزشکی (مثال: ۹۱۰۴۲)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = textfieldBg,
                                                unfocusedContainerColor = textfieldBg,
                                                focusedBorderColor = customAccentColor,
                                                unfocusedBorderColor = textfieldBorder,
                                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text("🩺 تخصص یا گرایش اصلی کلینیکال:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                                        UniversityRowDropdown(
                                            currentSelection = specialtySelected,
                                            allSelections = specialtyList,
                                            onChoose = { specialtySelected = it }
                                        )
                                    }
                                } else {
                                    // Pet Owner Fields
                                    Text(
                                        text = "🦮 ثبت اولین پَت در سامانه",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = petName,
                                        onValueChange = { petName = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = {
                                            Text("نام حیوان خانگی (پَت)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = textfieldBg,
                                            unfocusedContainerColor = textfieldBg,
                                            focusedBorderColor = customAccentColor,
                                            unfocusedBorderColor = textfieldBorder,
                                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "نوع حیوان (گونه زیستی):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            Triple("dog", "سگ", Color(0xFFC084FC)),
                                            Triple("cat", "گربه", Color(0xFF60A5FA)),
                                            Triple("exotic", "پرنده/اگزوتیک", Color(0xFF34D399))
                                        ).forEach { (code, label, color) ->
                                            val isSel = petSpecies == code
                                            val bg = if (isSel) color else textfieldBg
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(bg)
                                                    .border(
                                                        width = if (isSel) 0.dp else 1.dp,
                                                        color = textfieldBorder,
                                                        shape = RoundedCornerShape(14.dp)
                                                    )
                                                    .clickable { petSpecies = code }
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .padding(horizontal = 4.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    when (code) {
                                                        "dog" -> DogVectorIcon(modifier = Modifier.size(18.dp), tint = if (isSel) Color.White else color)
                                                        "cat" -> CatVectorIcon(modifier = Modifier.size(18.dp), tint = if (isSel) Color.White else color)
                                                        else -> ExoticVectorIcon(modifier = Modifier.size(18.dp), tint = if (isSel) Color.White else color)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        label,
                                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = petWeight,
                                            onValueChange = { petWeight = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = {
                                                Text("وزن (کیلوگرم)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = textfieldBg,
                                                unfocusedContainerColor = textfieldBg,
                                                focusedBorderColor = customAccentColor,
                                                unfocusedBorderColor = textfieldBorder,
                                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                            )
                                        )

                                        OutlinedTextField(
                                            value = petAge,
                                            onValueChange = { petAge = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = {
                                                Text("سن تخمینی (سال)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = textfieldBg,
                                                unfocusedContainerColor = textfieldBg,
                                                focusedBorderColor = customAccentColor,
                                                unfocusedBorderColor = textfieldBorder,
                                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Breed selection
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = petBreed,
                                            onValueChange = { newValue ->
                                                petBreed = newValue
                                                isBreedDropdownExpanded = newValue.isNotEmpty()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .onFocusChanged { focusState ->
                                                    breedTextFieldFocused = focusState.isFocused
                                                    if (focusState.isFocused && petBreed.isNotEmpty()) {
                                                        isBreedDropdownExpanded = true
                                                    }
                                                },
                                            placeholder = {
                                                Text("نژاد حیوان (مثال: پرشین / شیتزو)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { showBreedHelpDialog = true }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = "راهنمای نژادها",
                                                        tint = customAccentColor
                                                    )
                                                }
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = textfieldBg,
                                                unfocusedContainerColor = textfieldBg,
                                                focusedBorderColor = customAccentColor,
                                                unfocusedBorderColor = textfieldBorder,
                                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                            )
                                        )

                                        DropdownMenu(
                                            expanded = isBreedDropdownExpanded && filteredBreeds.isNotEmpty() && filteredBreeds.any { it != petBreed },
                                            onDismissRequest = { isBreedDropdownExpanded = false },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 200.dp)
                                        ) {
                                            filteredBreeds.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
                                                    onClick = {
                                                        petBreed = option
                                                        isBreedDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Breed recommendation helper dialog
                                    if (showBreedHelpDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showBreedHelpDialog = false },
                                            title = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = customAccentColor,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    val speciesName = when(petSpecies) {
                                                        "dog" -> "سگ‌ها"
                                                        "cat" -> "گربه‌ها"
                                                        else -> "پرندگان و حیوانات خاص / اگزوتیک"
                                                    }
                                                    Text(
                                                        text = "نژادهای پیشنهادی برای $speciesName",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            },
                                            text = {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(max = 300.dp)
                                                        .verticalScroll(rememberScrollState())
                                                ) {
                                                    Text(
                                                        text = "برای انتخاب خودکار هر نژاد، روی آن ضربه بزنید:",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(bottom = 12.dp),
                                                        textAlign = TextAlign.Right
                                                    )

                                                    @OptIn(ExperimentalLayoutApi::class)
                                                    FlowRow(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        registerBreedOptions.forEach { option ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                                                    .border(
                                                                        width = 1.dp,
                                                                        color = MaterialTheme.colorScheme.outlineVariant,
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    )
                                                                    .clickable {
                                                                        petBreed = option
                                                                        showBreedHelpDialog = false
                                                                    }
                                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                                            ) {
                                                                Text(
                                                                    text = option,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            confirmButton = {
                                                TextButton(onClick = { showBreedHelpDialog = false }) {
                                                    Text("بستن", fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Final Action Register Button
                                Button(
                                    onClick = {
                                        if (fullName.isEmpty() || phoneNumber.isEmpty()) return@Button

                                        if (isVetMode) {
                                            val identificationCode = if (isStudentMode) studentId else licenseNumber
                                            viewModel.simulateRegistration(
                                                fullName = fullName,
                                                phoneNumber = phoneNumber,
                                                userType = "vet",
                                                licenseNum = identificationCode,
                                                specOrUni = if (isStudentMode) schoolSelected else specialtySelected,
                                                gender = genderSelected
                                            )
                                        } else {
                                            val doubleWeight = petWeight.toDoubleOrNull() ?: 1.0
                                            val newPet = Pet(
                                                name = petName.ifEmpty { "پت خانگی" },
                                                species = petSpecies,
                                                breed = petBreed.ifEmpty { "ناپیدا" },
                                                weight = doubleWeight,
                                                age = petAge,
                                                gender = "نر",
                                                isNeutered = false,
                                                ownerName = fullName,
                                                ownerPhone = phoneNumber,
                                                recordNumber = "10011"
                                            )
                                            viewModel.simulateRegistration(
                                                fullName = fullName,
                                                phoneNumber = phoneNumber,
                                                userType = "owner",
                                                licenseNum = "",
                                                specOrUni = "",
                                                gender = genderSelected
                                            )
                                            viewModel.addNewPatient(newPet)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("submit_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = activeThemeColor,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "ثبت‌نام نهایی و ورود به داشبورد اختصاصی",
                                        fontSize = 14.sp,
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
}

@Composable
fun UniversityRowDropdown(
    currentSelection: String,
    allSelections: List<String>,
    onChoose: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { isExpanded = !isExpanded }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(currentSelection, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("▼", fontSize = 11.sp, color = Color.Gray)
        }

        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                allSelections.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
                        onClick = {
                            onChoose(item)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}
