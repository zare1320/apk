package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var isVetMode by remember { mutableStateOf(true) } // Mode switcher: true for vet, false for pet owner

    // General Fields
    var fullName by remember { mutableStateOf("") }
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

    // Populate standard lists of breed based on species in Persian and English for Register pet screen
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("عضویت در دستیار حرفه ای دامپزشکی", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Role Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // Pet Owner Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(21.dp))
                        .background(
                            animateColorAsState(
                                if (!isVetMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                label = ""
                            ).value
                        )
                        .clickable { isVetMode = false }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐾 صاحب پت هستم",
                        color = if (!isVetMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Veterinarian/Student Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(21.dp))
                        .background(
                            animateColorAsState(
                                if (isVetMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                label = ""
                            ).value
                        )
                        .clickable { isVetMode = true }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🩺 پزشک یا دانشجو هستم",
                        color = if (isVetMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Form General Inputs
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("نام و نام خانوادگی") },
                    placeholder = { Text("مثال: دکتر علیرضا رضایی") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("شماره تلفن همراه یا ایمیل") },
                    placeholder = { Text("مثال: 09121234567 یا zahra@outlook.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // OTP Verification Flow Simulation
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
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(
                            text = if (isEmail) "ارسال کد فعال‌سازی به ایمیل (OTP)" else "ارسال پیامک تایید هویت (OTP)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (!isOtpVerified) {
                    val isEmail = phoneNumber.contains("@")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isEmail) "کد فعال‌سازی ۶ رقمی ایمیل شده را وارد کنید (شبیه‌ساز: ۱۲۳۴۵۶)"
                                       else "کد فعال‌سازی ۶ رقمی پیامک شده را وارد کنید (شبیه‌ساز: ۱۲۳۴۵۶)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // 6-digit individual code inputs (OTP Verification V2)
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
                                                        color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .background(
                                                        color = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                                else MaterialTheme.colorScheme.surface,
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
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "", tint = Color(0xFF15803D))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEmail) "نشانی ایمیل و کد فعال‌سازی ۶ رقمی تایید گردید." else "شماره همراه شما تایید کد دو عاملی شد.",
                            color = Color(0xFF15803D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Conditionally display inputs based on role Selected
                if (isVetMode) {
                    // Dentist or Vet specifics
                    Text(
                        text = "اطلاعات پروانه و صلاحیت علمی پزشک:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Student / Clinician Sub-Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isStudentMode) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                .clickable { isStudentMode = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👨‍⚕️ پروانه و طبابت کلینیکال", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isStudentMode) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                .clickable { isStudentMode = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎓 رزیدنت یا دانشجوی دکترا", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isStudentMode) {
                        // School Selection
                        Text("🎓 مشخصات دانشگاه صادرکننده کارت:", fontSize = 11.sp)
                         UniversityRowDropdown(
                            currentSelection = schoolSelected,
                            allSelections = universityList,
                            onChoose = { schoolSelected = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = studentId,
                            onValueChange = { studentId = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("شماره دانشجویی / کد موقت صنفی") },
                            placeholder = { Text("مثال: ۹۹۰۱۵۴۳") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        // Clinician details
                        OutlinedTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("شماره پروانه نظام دامپزشکی") },
                            placeholder = { Text("مثال: ۹۱۰۴۲") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("🩺 تخصص یا گرایش اصلی کلینیکال:", fontSize = 11.sp)
                         UniversityRowDropdown(
                            currentSelection = specialtySelected,
                            allSelections = specialtyList,
                            onChoose = { specialtySelected = it }
                        )
                    }
                } else {
                    // Pet Owner Specific fields
                    Text(
                        text = "🦮 ثبت اولین پَت در سامانه:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("نام حیوان خانگی (پَت)") },
                        placeholder = { Text("مثال: فیدو / لوسی") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Species chosen
                    Text("نوع حیوان (گونه زیستی):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("dog", "سگ", Color(0xFFC084FC)),
                            Triple("cat", "گربه", Color(0xFF60A5FA)),
                            Triple("exotic", "پرنده/اگزوتیک", Color(0xFF34D399))
                        ).forEach { (code, label, color) ->
                            val isSel = petSpecies == code
                            val bg = if (isSel) color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .border(
                                        width = if (isSel) 0.dp else 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
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
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = petWeight,
                            onValueChange = { petWeight = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("وزن (کیلوگرم)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = petAge,
                            onValueChange = { petAge = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("سن تخمینی (سال)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Breed selection with quick pill filters and intelligent autocomplete
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
                            label = { Text("نژاد حیوان") },
                            placeholder = { Text("مثال: پرشین / شیتزو / همستر سوری") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
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
                                    text = { Text(option, fontSize = 13.sp) },
                                    onClick = {
                                        petBreed = option
                                        isBreedDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (filteredBreeds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("پیشنهادهای نژاد بر اساس گونه:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            filteredBreeds.take(12).forEach { option ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                        .clickable { 
                                            petBreed = option 
                                            isBreedDropdownExpanded = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(option, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                                specOrUni = if (isStudentMode) schoolSelected else specialtySelected
                            )
                        } else {
                            val doubleWeight = petWeight.toDoubleOrNull() ?: 1.0
                            val intAge = petAge.toIntOrNull() ?: 1
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
                                specOrUni = ""
                            )
                            viewModel.addNewPatient(newPet)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("ثبت‌نام نهایی و ورود به داشبورد اختصاصی", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
