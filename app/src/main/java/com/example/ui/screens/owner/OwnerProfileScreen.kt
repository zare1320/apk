package com.example.ui.screens.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.testTag
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerProfileScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val currentTheme by viewModel.themeMode.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val activeSubscription by viewModel.activeSubscription.collectAsState()

    var activeOwnerSection by remember { mutableStateOf("اصلی") } // "اصلی", "تنظیمات", "لینک‌ها"

    // Dialog state variables
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDevicesDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    // Interactivity state copies
    var editedName by remember { mutableStateOf("صاحب پت گرامی") }
    var editedPhone by remember { mutableStateOf("۰۹۱۲۳۴۵۶۷۸۹") }

    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            editedName = activeSession?.fullName ?: "صاحب پت گرامی"
            editedPhone = activeSession?.phoneNumber ?: "۰۹۱۲۳۴۵۶۷۸۹"
        }
    }

    // Dynamic Theme-Aware Palette based on MainViewModel theme state
    val isDark = currentTheme == "dark"
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val mutedTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF718096)
    val borderColor = if (isDark) Color(0xFF2D3748) else Color(0xFFE2E8F0)
    val dividerColor = if (isDark) Color(0xFF2D3748) else Color(0xFFEDF2F7)
    val logoutBorderColor = if (isDark) Color(0xFF742A2A) else Color(0xFFFED7D7)
    val logoutBgColor = if (isDark) Color(0xFF1E293B) else Color.White

    // Force RTL layout direction for Arabic/Persian
    val layoutDir = if (currentLang == "fa" || currentLang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Header Title: My Account (حساب من)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeOwnerSection != "اصلی") {
                        IconButton(onClick = { activeOwnerSection = "اصلی" }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when (activeOwnerSection) {
                            "تنظیمات" -> "تنظیمات برنامه"
                            "لینک‌ها" -> "لینک های کاربردی و پورتابل"
                            "منابع" -> "منابع علمی"
                            else -> "حساب من"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Crossfade(targetState = activeOwnerSection, label = "OwnerProfileSectionAnimation") { section ->
                    when (section) {
                        "اصلی" -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                
                                // 1. Upper Profile Card (Avatar, Name, Phone & Edit button)
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Right-side (in RTL): Avatar + Name & Phone
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Circular Avatar with pink/purple background for cute owner theme
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(if (isDark) Color(0xFF3F2B30) else Color(0xFFFEE2E2), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Avatar",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = editedName,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = editedPhone,
                                                    fontSize = 12.sp,
                                                    color = mutedTextColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        // Left-side (in RTL): Improved, un-squishable Edit button with edit icon
                                        OutlinedButton(
                                            onClick = { showEditProfileDialog = true },
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .testTag("edit_profile_btn"),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Color(0xFF3B82F6))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Profile",
                                                tint = Color(0xFF3B82F6),
                                                modifier = Modifier.size(16.dp)
                                             )
                                             Spacer(modifier = Modifier.width(6.dp))
                                             Text(
                                                text = "ویرایش",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF3B82F6),
                                                maxLines = 1,
                                                softWrap = false
                                             )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2. Account Management Category Grouping
                                Text(
                                    text = "مدیریت حساب",
                                    textAlign = TextAlign.Right,
                                    fontSize = 13.sp,
                                    color = mutedTextColor,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 6.dp)
                                )
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                ) {
                                    Column {
                                        OwnerProfileMenuItemRedesigned(
                                            title = "رمز عبور",
                                            iconEmoji = "🔒",
                                            onClick = { showPasswordDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = "دستگاه‌های متصل",
                                            iconEmoji = "💻",
                                            onClick = { showDevicesDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        val subEmoji = when(activeSubscription) {
                                            "free" -> "🎁"
                                            "silver" -> "🥈"
                                            "diamond" -> "💎"
                                            else -> "🏆"
                                        }
                                        val subTitle = when(activeSubscription) {
                                            "free" -> "رایگان"
                                            "silver" -> "نقره‌ای (۳ ماهه)"
                                            "diamond" -> "الماس (یکساله)"
                                            else -> "طلایی (۶ ماهه)"
                                        }
                                        OwnerProfileMenuItemRedesigned(
                                            title = "مدیریت اشتراک: $subTitle $subEmoji",
                                            iconEmoji = "🛡️",
                                            onClick = { showSubscriptionDialog = true }
                                        )
                                    }
                                }

                                // 3. Support & Utils Grouping
                                Text(
                                    text = "پشتیبانی و امکانات",
                                    textAlign = TextAlign.Right,
                                    fontSize = 13.sp,
                                    color = mutedTextColor,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 6.dp)
                                )
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 24.dp)
                                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                ) {
                                    Column {
                                        OwnerProfileMenuItemRedesigned(
                                            title = "راهنما و درباره ما",
                                            iconEmoji = "❓",
                                            onClick = { showHelpDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = "شرایط و مقررات استفاده",
                                            iconEmoji = "📄",
                                            onClick = { showTermsDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = "منابع علمی",
                                            iconEmoji = "📖",
                                            onClick = { activeOwnerSection = "منابع" }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = "لینک های کاربردی و پورتابل",
                                            iconEmoji = "🔗",
                                            onClick = { activeOwnerSection = "لینک‌ها" }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = "گزارش تخلف کاربر",
                                            iconEmoji = "⚠️",
                                            onClick = { showReportDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = "پشتیبانی انلاین",
                                            iconEmoji = "💬",
                                            onClick = { showSupportDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = "بروزرسانی پایگاه داده و برنامه",
                                            iconEmoji = "🔄",
                                            onClick = { showUpdateDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = "تنظیمات عمومی",
                                            iconEmoji = "⚙️",
                                            onClick = { activeOwnerSection = "تنظیمات" }
                                        )
                                    }
                                }

                                // 4. Red Colored Logout Button Container (Separated)
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = logoutBgColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .border(1.dp, logoutBorderColor, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.logout() }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "Logout",
                                            tint = Color(0xFFE53E3E),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "خروج از حساب کاربری",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE53E3E)
                                        )
                                    }
                                }
                            }
                        }

                        "تنظیمات" -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "⚙️ تنظیمات عمومی برنامه:",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    // Light / Dark mode
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("حالت شب برنامه (تم تاریک):", fontSize = 13.sp, color = textColor)
                                        Switch(
                                            checked = currentTheme == "dark",
                                            onCheckedChange = { viewModel.toggleTheme() }
                                        )
                                    }

                                    HorizontalDivider(color = dividerColor)

                                    // Language
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("تغییر زبان:", fontSize = 13.sp, color = textColor)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(
                                                "fa" to "فارسی",
                                                "en" to "English"
                                            ).forEach { (code, display) ->
                                                val isChosen = currentLang == code
                                                val bg = if (isChosen) Color(0xFFEF4444) else (if (isDark) Color(0xFF334155) else Color(0xFFEDF2F7))
                                                val textCol = if (isChosen) Color.White else textColor
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(bg)
                                                        .clickable { viewModel.setLanguage(code) }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        display,
                                                        fontSize = 11.sp,
                                                        color = textCol,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = dividerColor)

                                    // App version info
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("نسخه کلاینت اندروید:", fontSize = 13.sp, color = textColor)
                                        Text("v1.5.0", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = mutedTextColor)
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Reset database cache for client-side cleanliness
                                    Button(
                                        onClick = { viewModel.resetAllData() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E)),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("پاک‌سازی کامل پایگاه داده محلی (ریست)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        "منابع" -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "📚 مراجع علمی معتبر دامپزشکی دارونامه پت‌کلاب:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                listOf(
                                    "Plumb's Veterinary Drug Handbook" to "کتابچه جامع راهنمای دارویی دامپزشکی پلانتون - ویرایش دهم مرجع دوزهای استاندارد کلینیکال جهت محاسبات برای سگ و گربه.",
                                    "Merck Veterinary Manual (دارونامه اگزوتیک)" to "مرجع جهانی تشخیص بیماری‌ها، پاتولوژی کلینیکال و درمان‌های اختصاصی پرندگان و حیوانات خانگی خاص.",
                                    "BSAVA Small Animal Formulary" to "راهنمای دارویی تخصصی حیوانات کوچک آکادمی دامپزشکی بریتانیا جهت سنجش مقادیر داروها."
                                ).forEach { (title, description) ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEF4444))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(description, fontSize = 12.sp, color = textColor, lineHeight = 18.sp)
                                        }
                                    }
                                }
                            }
                        }

                        "لینک‌ها" -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "🔗 پرورتال‌های حامي حیوانات حیوان خانگی:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                listOf(
                                    "سازمان حفاظت از حقوق حیوانات خانگی" to "http://www.irandogpet.ir",
                                    "کلینیک جامع شبانه‌روزی حامیان پت" to "http://www.clinic-pet.ir",
                                    "سامانه واکسیناسیون کشوری زئونوز" to "http://vaccine.ivo.ir"
                                ).forEach { (title, link) ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(if (isDark) Color(0xFF3F2B30) else Color(0xFFFEE2E2), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🔗", fontSize = 16.sp)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(link, fontSize = 11.sp, color = Color(0xFFEF4444))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- PROTOTYPE DIALOGS FOR INTERACTIVE USABILITY ---

            if (showEditProfileDialog) {
                var nameInput by remember { mutableStateOf(editedName) }
                // Clear any social credential placeholder email or dummy login from phone so they can complete it
                var phoneInput by remember { 
                    mutableStateOf(if (editedPhone.contains("@") || editedPhone.startsWith("سریع با")) "" else editedPhone) 
                }
                AlertDialog(
                    onDismissRequest = { showEditProfileDialog = false },
                    title = { Text("ویرایش اطلاعات حساب کاربری", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("نام و نام خانوادگی") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("شماره همراه") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                editedName = nameInput
                                editedPhone = phoneInput
                                viewModel.updateSession(
                                    fullName = nameInput,
                                    phoneNumber = phoneInput
                                )
                                showEditProfileDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("ذخیره تغییرات")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditProfileDialog = false }) {
                            Text("انصراف")
                        }
                    }
                )
            }

            if (showPasswordDialog) {
                var oldPass by remember { mutableStateOf("") }
                var newPass by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showPasswordDialog = false },
                    title = { Text("تغییر رمز عبور", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = oldPass,
                                onValueChange = { oldPass = it },
                                label = { Text("رمز عبور فعلی") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text("رمز عبور جدید") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showPasswordDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("بروزرسانی رمز عبور")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPasswordDialog = false }) {
                            Text("بازگشت")
                        }
                    }
                )
            }

            if (showDevicesDialog) {
                AlertDialog(
                    onDismissRequest = { showDevicesDialog = false },
                    title = { Text("دستگاه‌های متصل به حساب", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF7FAFC)),
                                modifier = Modifier.fillMaxWidth().border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("فعال (این دستگاه)", color = Color(0xFF48BB78), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Xiaomi Redmi 12", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                        Text("آخرین فعالیت: هم‌اکنون", fontSize = 11.sp, color = mutedTextColor)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showDevicesDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("تایید")
                        }
                    }
                )
            }

            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    title = { Text("راهنما و درباره ما", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text("درباره پت‌کلاب:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                            Text("پت‌کلاب: پورتال هوشمند تشخیص و دارونامه مکتوب دامپزشکی کشور\nطراحی شده جهت بهبود سرعت عمل و بهینه‌سازی فرآیندهای بالینی و محاسبات دارویی دام‌های کوچک و اگزوتیک.\nنسخه: v1.5.0", fontSize = 12.sp, color = textColor, lineHeight = 18.sp)
                            
                            HorizontalDivider(color = dividerColor, thickness = 1.dp)
                            
                            Text("نحوه استفاده از پرتال حامی پت:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                            Text("۱. جدول تغذیه منظم روزانه بر حسب نژاد سگ یا گربه را مطالعه کنید.\n۲. زمان‌بندی دقیق واکسیناسیون‌ها و دوره‌های ضدانگل را از پزشک معالج بپرسید.\n۳. در صورت بروز هرگونه مشکل اضطراری از پشتیبانی آنلاین کادر درمان راهنمایی بگیرید.", fontSize = 11.sp, lineHeight = 16.sp, color = textColor)
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showHelpDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                            Text("بستن")
                        }
                    }
                )
            }

            if (showReportDialog) {
                var shopName by remember { mutableStateOf("") }
                var reportMsg by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showReportDialog = false },
                    title = { Text("گزارش تخلف کاربر", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = shopName,
                                onValueChange = { shopName = it },
                                label = { Text("نام یا شناسه کاربر") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = reportMsg,
                                onValueChange = { reportMsg = it },
                                label = { Text("شرح جزئیات تخلف") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showReportDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("ارسال گزارش")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReportDialog = false }) {
                            Text("انصراف")
                        }
                    }
                )
            }

            if (showTermsDialog) {
                AlertDialog(
                    onDismissRequest = { showTermsDialog = false },
                    title = { Text("شرایط و قوانین استفاده", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text("۱. دوزهای محاسبه شده پیشنهادی هستند و نباید جایگزین تصمیم بالینی پزشک معالج شوند.\n\n۲. حفظ حریم خصوصی حیوانات و اطلاعات صاحبان پت بر عهده سرور کلینیکال می‌باشد.\n\n۳. هرگونه استفاده تجاری غیر‌مجاز پیگرد قانونی دارد.", fontSize = 11.sp, color = textColor)
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showTermsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                            Text("موافقم")
                        }
                    }
                )
            }

            if (showUpdateDialog) {
                AlertDialog(
                    onDismissRequest = { showUpdateDialog = false },
                    title = { Text("بروزرسانی نرم‌افزار", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉 تبریک! شما در حال استفاده از آخرین نسخه موجود هستید.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF48BB78))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("نسخه نصب شده: v1.5.0", fontSize = 11.sp, color = mutedTextColor)
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showUpdateDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                            Text("بستن")
                        }
                    }
                )
            }

            if (showSupportDialog) {
                AlertDialog(
                    onDismissRequest = { showSupportDialog = false },
                    title = { Text("پشتیبانی آنلاین پت‌کلاب", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("کارشناسان فنی و پشتیبان‌های کلینیکال پت‌کلاب جهت رفع هرگونه ابهام یا خطا به صورت ۲۴ ساعته از طریق پیام‌رسان‌ها آماده پاسخگویی هستند.", fontSize = 13.sp)
                    },
                    confirmButton = {
                        Button(
                            onClick = { showSupportDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("ارسال پیام در واتساپ پشتیبانی")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSupportDialog = false }) {
                            Text("بستن")
                        }
                    }
                )
            }

            if (showSubscriptionDialog) {
                var selectedPlanTemp by remember { mutableStateOf(activeSubscription) }
                AlertDialog(
                    onDismissRequest = { showSubscriptionDialog = false },
                    title = { 
                        Text(
                            text = "مدیریت اشتراک پت‌کلاب", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 16.sp,
                            color = textColor,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        ) 
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                "لطفاً یکی از اشتراک‌های زیر را انتخاب کنید:", 
                                fontSize = 12.sp, 
                                color = mutedTextColor,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            
                            val plans = listOf(
                                Triple("free", "🎁 اشتراک رایگان", "امکانات اولیه بدون انقضا"),
                                Triple("silver", "🥈 اشتراک نقره‌ای (۳ ماه اعتبار)", "با امکان ثبت تا ۵۰ نسخه و دسترسی ۲۴ ساعته"),
                                Triple("gold", "🏆 اشتراک طلایی (۶ ماه اعتبار)", "نسخه‌های نامحدود ابری، هوش مصنوعی دوزینگ دامی"),
                                Triple("diamond", "💎 اشتراک الماس (یکسال اعتبار)", "پشتیبانی VIP، همه‌ی امکانات نسخه پرو + وبینارها")
                            )

                            plans.forEach { (planId, planName, planDesc) ->
                                val isSelected = selectedPlanTemp == planId
                                val planBg = if (isSelected) {
                                    if (isDark) Color(0xFF1E2F4C) else Color(0xFFE0ECFC)
                                } else {
                                    cardBgColor
                                }
                                val planBorder = if (isSelected) {
                                    Color(0xFFEF4444) // Cute Pink/Red accents for Owner Profile
                                } else {
                                    borderColor
                                }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = planBg),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, planBorder, RoundedCornerShape(12.dp))
                                        .clickable { selectedPlanTemp = planId }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = planName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) Color(0xFFEF4444) else textColor
                                            )
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedPlanTemp = planId },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEF4444))
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = planDesc,
                                            fontSize = 11.sp,
                                            color = mutedTextColor,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { 
                                viewModel.setSubscription(selectedPlanTemp)
                                showSubscriptionDialog = false 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("تایید و فعال‌سازی", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSubscriptionDialog = false }) {
                            Text("انصراف")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OwnerProfileMenuItemRedesigned(
    title: String,
    iconEmoji: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.2f
    val textColor = MaterialTheme.colorScheme.onSurface
    val circleBgColor = if (isDark) Color(0xFF334155) else Color(0xFFEDF2F7)
    val circleBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFE2E8F0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Right side in RTL (Icon circle badge + Title)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stylized Circle containing the Emoji Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(circleBgColor, CircleShape)
                    .border(1.dp, circleBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(iconEmoji, fontSize = 16.sp)
            }

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Left side in RTL (Chevron indicator pointing left)
        Text(
            text = "‹", // Left pointing small Chevron
            fontSize = 18.sp,
            color = Color(0xFFA0AEC0),
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}
