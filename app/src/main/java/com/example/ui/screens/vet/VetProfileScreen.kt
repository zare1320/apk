package com.example.ui.screens.vet

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
import com.example.data.database.Prescription
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetProfileScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val allPrescriptions by viewModel.allPrescriptions.collectAsState()
    val currentTheme by viewModel.themeMode.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val activeSubscription by viewModel.activeSubscription.collectAsState()

    var activeProfileSection by remember { mutableStateOf("اصلی") } // "اصلی", "نسخه‌ها", "تنظیمات", "لینک‌ها", "منابع"

    // Dialog state variables
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDevicesDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var showRewardsDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableStateOf(5) }

    // Interactivity state copies
    var editedName by remember { mutableStateOf("مسعود زارع") }
    var editedPhone by remember { mutableStateOf("۰۹۲۱ ۱۰۹ ۷۷۳۶") }
    var identification by remember { mutableStateOf("") }
    var workplaceOrUni by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var isStudent by remember { mutableStateOf(false) }

    LaunchedEffect(activeSession) {
        activeSession?.let {
            editedName = it.fullName
            editedPhone = it.phoneNumber
            identification = it.identification
            workplaceOrUni = it.workplaceOrUni
            specialty = it.specialty
            isStudent = it.workplaceOrUni.isNotEmpty() && it.specialty.isEmpty()
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
    val containerBgLight = if (isDark) Color(0xFF1E293B) else Color(0xFFF7FAFC)
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
                    if (activeProfileSection != "اصلی") {
                        IconButton(onClick = { activeProfileSection = "اصلی" }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when (activeProfileSection) {
                            "نسخه‌ها" -> if (currentLang == "en") "Prescriptions & Saved Cases" else "نسخه / پرونده ذخیره شده"
                            "تنظیمات" -> if (currentLang == "en") "App Settings" else "تنظیمات برنامه"
                            "منابع" -> if (currentLang == "en") "Scientific Sources" else "منابع علمی معتبر"
                            "لینک‌ها" -> if (currentLang == "en") "Useful Links" else "لینک‌های کاربردی"
                            else -> if (currentLang == "en") "My Account" else "حساب من"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Crossfade(targetState = activeProfileSection, label = "ProfileSectionAnimation") { section ->
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
                                            // Circular Avatar with blue background and user icon
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(if (isDark) Color(0xFF1E2F4C) else Color(0xFFE0ECFC), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Avatar",
                                                    tint = Color(0xFF3B82F6),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                            Column {
                                                val displayName = if (currentLang == "en" && (editedName == "کاربر" || editedName == "کاربر جدید" || editedName.isEmpty())) "New User" else editedName
                                                Text(
                                                    text = displayName,
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
                                                if (identification.isNotEmpty() || workplaceOrUni.isNotEmpty() || specialty.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = if (isStudent) {
                                                            if (currentLang == "en") "🎓 Student/Resident: $workplaceOrUni | Code: $identification" else "🎓 دانشجو/رزیدنت: $workplaceOrUni | کد: $identification"
                                                        } else {
                                                            val dispSpecialty = if (currentLang == "en" && specialty == "داخلی حیوانات کوچک") "Small Animal Internal Medicine" else specialty
                                                            if (currentLang == "en") "🩺 Specialist: $dispSpecialty | License: $identification" else "🩺 پزشک متخصص: $specialty | پروانه: $identification"
                                                        },
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF3B82F6),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
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
                                                text = if (currentLang == "en") "Edit" else "ویرایش",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF3B82F6),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }

                                // 2. Coins (پی‌کلاپ) & Subscriptions cards side by side
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Subscription Card (Right card in RTL)
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                            .clickable { showSubscriptionDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            // Dynamic Badge and Color according to activeSubscription
                                            val subEmoji = when(activeSubscription) {
                                                "free" -> "🆓"
                                                "silver" -> "🥈"
                                                "diamond" -> "💎"
                                                else -> "🏆"
                                            }
                                            val subTitle = when(activeSubscription) {
                                                "free" -> if (currentLang == "en") "Free Subscription" else "اشتراک رایگان"
                                                "silver" -> if (currentLang == "en") "Silver Subscription" else "اشتراک نقره‌ای"
                                                "diamond" -> if (currentLang == "en") "Diamond Subscription" else "اشتراک الماس"
                                                else -> if (currentLang == "en") "Gold Subscription" else "اشتراک طلایی"
                                            }
                                            val subAccentColor = when(activeSubscription) {
                                                "free" -> Color(0xFF10B981)
                                                "silver" -> Color(0xFF64748B)
                                                "diamond" -> Color(0xFF3B82F6)
                                                else -> Color(0xFFFBBF24)
                                            }
                                            val badgeBg = if (isDark) subAccentColor.copy(alpha = 0.2f) else subAccentColor.copy(alpha = 0.15f)

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(badgeBg, CircleShape)
                                                    .border(1.dp, subAccentColor, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(subEmoji, fontSize = 18.sp)
                                            }
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = if (currentLang == "en") "Active Subscription" else "اشتراک فعال",
                                                fontSize = 11.sp,
                                                color = mutedTextColor
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = subTitle,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = if (currentLang == "en") "Manage" else "مدیریت اشتراک",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF3B82F6)
                                                )
                                                Text(
                                                    text = if (currentLang == "en") "›" else "‹",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF3B82F6)
                                                )
                                            }
                                        }
                                    }

                                    // P-Club Coins Card (Left card in RTL)
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                            .clickable { showRewardsDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            // Gold Medal Badge
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(if (isDark) Color(0xFF3E3626) else Color(0xFFFEF3C7), CircleShape)
                                                    .border(1.dp, Color(0xFFFBBF24), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🪙", fontSize = 18.sp)
                                            }
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = if (currentLang == "en") "VetClub Points" else "امتیاز وت‌کلاب",
                                                fontSize = 11.sp,
                                                color = mutedTextColor
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (currentLang == "en") "${activeSession?.coins ?: 100} Coins" else "${(activeSession?.coins ?: 100).toPersianDigits()} سکه",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = if (currentLang == "en") "Rewards & Discounts" else "جوایز و تخفیف‌ها",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF3B82F6)
                                                )
                                                Text(
                                                    text = if (currentLang == "en") "›" else "‹",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF3B82F6)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 3. Account Management Category Grouping
                                Text(
                                    text = if (currentLang == "en") "Account Management" else "مدیریت حساب",
                                    textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right,
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
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Password" else "رمز عبور",
                                            iconEmoji = "🔒",
                                            onClick = { showPasswordDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Connected Devices" else "دستگاه‌های متصل",
                                            iconEmoji = "💻",
                                            onClick = { showDevicesDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Prescriptions & Saved Cases" else "نسخه / پرونده ذخیره شده",
                                            iconEmoji = "💳",
                                            badge = if (allPrescriptions.isNotEmpty()) "${allPrescriptions.size}" else null,
                                            onClick = { activeProfileSection = "نسخه‌ها" }
                                        )
                                    }
                                }

                                // 4. Notifications Grouping
                                Text(
                                    text = if (currentLang == "en") "Notifications" else "اطلاع رسانی",
                                    textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right,
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
                                    ProfileMenuItemRedesigned(
                                        title = if (currentLang == "en") "Invite Friends" else "دعوت از دوستان",
                                        iconEmoji = "👥",
                                        onClick = { showInviteDialog = true }
                                    )
                                }

                                // 5. Support Grouping
                                Text(
                                    text = if (currentLang == "en") "Support" else "پشتیبانی",
                                    textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right,
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
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Help & About Us" else "راهنما و درباره ما",
                                            iconEmoji = "❓",
                                            onClick = { showHelpDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Terms & Conditions" else "شرایط و مقررات استفاده",
                                            iconEmoji = "📄",
                                            onClick = { showTermsDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Scientific Reference Sources" else "منابع علمی",
                                            iconEmoji = "📖",
                                            onClick = { activeProfileSection = "منابع" }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Useful Links & Portals" else "لینک های کاربردی و پورتابل",
                                            iconEmoji = "🔗",
                                            onClick = { activeProfileSection = "لینک‌ها" }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Report User Violation" else "گزارش تخلف کاربر",
                                            iconEmoji = "⚠️",
                                            onClick = { showReportDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Online Support Chat" else "پشتیبانی انلاین",
                                            iconEmoji = "💬",
                                            onClick = { showSupportDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Update App & Database" else "بروزرسانی پایگاه داده و برنامه",
                                            iconEmoji = "🔄",
                                            onClick = { showUpdateDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "General Settings" else "تنظیمات عمومی",
                                            iconEmoji = "⚙️",
                                            onClick = { activeProfileSection = "تنظیمات" }
                                        )
                                    }
                                }

                                // 6. Red Colored Logout Button Container (Separated)
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = logoutBgColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .border(1.dp, logoutBorderColor, RoundedCornerShape(16.dp))
                                        .clickable { showLogoutDialog = true }
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
                                            text = if (currentLang == "en") "Logout" else "خروج از حساب کاربری",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE53E3E)
                                        )
                                    }
                                }
                            }
                        }

                        "نسخه‌ها" -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (allPrescriptions.isEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        shape = RoundedCornerShape(24.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .background(if (isDark) Color(0xFF1E2F4D) else Color(0xFFEBF8FF), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("📝", fontSize = 28.sp)
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "هیچ نسخه ثبت‌شده‌ای یافت نشد",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = textColor
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "نسخه‌های صادر شده برای بیماران در این بخش ذخیره می‌شوند تا به صورت مکتوب یا نسخه الکترونیک در دسترس باشند.",
                                                fontSize = 12.sp,
                                                color = mutedTextColor,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                        }
                                    }
                                } else {
                                    allPrescriptions.forEach { prescription ->
                                        PrescriptionCardRedesigned(prescription = prescription, onDelete = {
                                            viewModel.deletePrescription(prescription)
                                        })
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
                                        color = Color(0xFF3B82F6),
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
                                                val bg = if (isChosen) Color(0xFF3B82F6) else (if (isDark) Color(0xFF334155) else Color(0xFFEDF2F7))
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
                                    text = "📚 منابع علمی و رفرنس‌های معتبر دارونامه:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                listOf(
                                    "Plumb's Veterinary Drug Handbook" to "کتابچه جامع راهنمای دارویی دامپزشکی پلانتون - ویرایش دهم مرجع دوزهای استاندارد کلینیکال جهت محاسبات.",
                                    "Merck Veterinary Manual (دارونامه اگزوتیک)" to "مرجع علمی پاتولوژی و دستورالعمل‌های درمانی ورم سینه گاو، درمان برونشیت سگ و کلامیدوز زیکارها.",
                                    "BSAVA Small Animal Formulary" to "فرمولاسیون دارویی جامع آکادمی دامپزشکی حیوانات کوچک بریتانیا جهت تخمین حجم‌های دقیق."
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
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B82F6))
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
                                    text = "🔗 لینک‌های ارتباطی و پورتال‌های دامپزشکی:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                listOf(
                                    "سازمان دامپزشکی کشور" to "http://www.ivo.ir",
                                    "سازمان نظام دامپزشکی کشور" to "http://www.iranveterinary.com",
                                    "سامانه بازرسی داروهای دامی" to "http://pharmacy.ivo.ir"
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
                                                    .background(if (isDark) Color(0xFF1E2F4C) else Color(0xFFE0ECFC), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🔗", fontSize = 16.sp)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(link, fontSize = 11.sp, color = Color(0xFF3B82F6))
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
                var isStudentInput by remember { mutableStateOf(isStudent) }
                var idInput by remember { 
                    mutableStateOf(if (identification.startsWith("سریع با")) "" else identification) 
                }
                var workplaceInput by remember { 
                    mutableStateOf(if (workplaceOrUni == "Google" || workplaceOrUni == "Apple" || workplaceOrUni.startsWith("سریع با")) "دانشگاه تهران" else workplaceOrUni.ifEmpty { "دانشگاه تهران" }) 
                }
                var specialtyInput by remember { 
                    mutableStateOf(if (specialty == "تایید هویت سریع مستقل" || specialty.startsWith("تایید")) "داخلی حیوانات کوچک" else specialty.ifEmpty { "داخلی حیوانات کوچک" }) 
                }

                var isUniDropdownExpanded by remember { mutableStateOf(false) }
                var isSpecialtyDropdownExpanded by remember { mutableStateOf(false) }

                val universityList = listOf(
                    "دانشگاه تهران", "دانشگاه شیراز", "دانشگاه فردوسی مشهد",
                    "دانشگاه علوم تحقیقات", "دانشگاه تبریز", "دانشگاه کار و هنر"
                )

                val specialtyList = listOf(
                    "داخلی حیوانات کوچک", "جراحی و هوشبری", "کلینیکال پاتولوژی",
                    "رادیولوژی و تصویربرداری", "مامایی و بیماری‌های تولیدمثل"
                )

                AlertDialog(
                    onDismissRequest = { showEditProfileDialog = false },
                    title = { Text("ویرایش اطلاعات حساب کاربری", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
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

                            Text(
                                text = "نوع کاربری صنفی:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6)
                            )

                            // Student vs Practitioner Toggle Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFEDF2F7))
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (!isStudentInput) Color(0xFF3B82F6) else Color.Transparent)
                                        .clickable { isStudentInput = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "👨‍⚕️ پزشک کلینیسین",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isStudentInput) Color.White else (if (isDark) Color.White else Color.Black)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isStudentInput) Color(0xFF3B82F6) else Color.Transparent)
                                        .clickable { isStudentInput = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "🎓 دانشجو یا رزیدنت",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isStudentInput) Color.White else (if (isDark) Color.White else Color.Black)
                                    )
                                }
                            }

                            if (isStudentInput) {
                                // School selection drop-down selector
                                Text("🎓 دانشگاه محل تحصیل:", fontSize = 11.sp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                        .background(cardBgColor)
                                        .clickable { isUniDropdownExpanded = true }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(workplaceInput, fontSize = 13.sp, color = textColor)
                                        Text("▼", fontSize = 10.sp, color = Color.Gray)
                                    }

                                    DropdownMenu(
                                        expanded = isUniDropdownExpanded,
                                        onDismissRequest = { isUniDropdownExpanded = false }
                                    ) {
                                        universityList.forEach { uni ->
                                            DropdownMenuItem(
                                                text = { Text(uni, fontSize = 13.sp) },
                                                onClick = {
                                                    workplaceInput = uni
                                                    isUniDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = idInput,
                                    onValueChange = { idInput = it },
                                    label = { Text("شماره دانشجویی / کد موقت") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                // Practitioner details
                                OutlinedTextField(
                                    value = idInput,
                                    onValueChange = { idInput = it },
                                    label = { Text("شماره نظام دامپزشکی") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text("🩺 تخصص کلینیکال اصلی:", fontSize = 11.sp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                        .background(cardBgColor)
                                        .clickable { isSpecialtyDropdownExpanded = true }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(specialtyInput, fontSize = 13.sp, color = textColor)
                                        Text("▼", fontSize = 10.sp, color = Color.Gray)
                                    }

                                    DropdownMenu(
                                        expanded = isSpecialtyDropdownExpanded,
                                        onDismissRequest = { isSpecialtyDropdownExpanded = false }
                                    ) {
                                        specialtyList.forEach { spec ->
                                            DropdownMenuItem(
                                                text = { Text(spec, fontSize = 13.sp) },
                                                onClick = {
                                                    specialtyInput = spec
                                                    isSpecialtyDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                editedName = nameInput
                                editedPhone = phoneInput
                                isStudent = isStudentInput
                                identification = idInput
                                workplaceOrUni = if (isStudentInput) workplaceInput else ""
                                specialty = if (!isStudentInput) specialtyInput else ""

                                viewModel.updateSession(
                                    fullName = nameInput,
                                    phoneNumber = phoneInput,
                                    identification = idInput,
                                    workplaceOrUni = if (isStudentInput) workplaceInput else "",
                                    specialty = if (!isStudentInput) specialtyInput else ""
                                )

                                showEditProfileDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
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
                                colors = CardDefaults.cardColors(containerColor = containerBgLight),
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
                            Card(
                                colors = CardDefaults.cardColors(containerColor = containerBgLight),
                                modifier = Modifier.fillMaxWidth().border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("خروج", color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.clickable { }, fontWeight = FontWeight.Bold)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Chrome Browser (Windows)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                        Text("آخرین فعالیت: ۲ روز پیش", fontSize = 11.sp, color = mutedTextColor)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showDevicesDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Text("تایید")
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
                                    Color(0xFF3B82F6)
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
                                                color = if (isSelected) Color(0xFF3B82F6) else textColor
                                            )
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedPlanTemp = planId },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3B82F6))
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
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

            if (showRewardsDialog) {
                AlertDialog(
                    onDismissRequest = { showRewardsDialog = false },
                    title = { Text("کیف پول و جوایز وت‌کلاب", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().background(if (isDark) Color(0xFF3A301E) else Color(0xFFFEF3C7)).padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text((activeSession?.coins ?: 100).toPersianDigits(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD69E2E))
                                Text("امتياز کل سکه‌ها", fontWeight = FontWeight.Medium, color = textColor)
                            }
                            Text("با ثبت نسخه و استفاده منظم از ویژگی‌های برنامه، سکه‌های هدیه جمع‌آوری کنید و کد تخفیف بگیرید.", fontSize = 12.sp, color = mutedTextColor)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor)
                            Text("پاداش های قابل فعال‌سازی:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                            Button(
                                onClick = { showRewardsDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF319795))
                            ) {
                                Text("دریافت کد تخفیف ۵۰٪ اشتراک (۲۰۰۰ سکه)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showRewardsDialog = false }) {
                            Text("بازگشت")
                        }
                    }
                )
            }

            if (showInviteDialog) {
                AlertDialog(
                    onDismissRequest = { showInviteDialog = false },
                    title = { Text("دعوت از همکاران و دوستان", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("با معرفی اپلیکیشن به همکاران دامپزشک خود، ۳۰ روز اشتراک طلایی رایگان دریافت نمایید.", fontSize = 13.sp, color = textColor)
                            OutlinedTextField(
                                value = "https://petclub-app.ir/referral/vet-masoud",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("لینک دعوت اختصاصی شما") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showInviteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Text("اشتراک‌گذاری لینک")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showInviteDialog = false }) {
                            Text("بستن")
                        }
                    }
                )
            }

            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🩺", fontSize = 22.sp)
                            Text("راهنما و معرفی قابلیت‌ها", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp)
                        ) {
                            // Section: About
                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("درباره پورتال تخصصی پت‌کلاب", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B82F6))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "سامانه هوشمند و دارونامه جامع الکترونیکی دامپزشکی پت‌کلاب، ابزار کمکی بالینی سریع و پیشرفته جهت تشخیص بیماری‌ها، محاسبات دوزینگ دقیق و تسریع فرآیندهای درمانی دام‌های کوچک و اگزوتیک می‌باشد. (نسخه v1.5.0)",
                                        fontSize = 11.sp,
                                        color = textColor,
                                        lineHeight = 17.sp
                                    )
                                }
                            }

                            Text("🚀 قابلیت‌ها و ابزارهای در دسترس کادر درمان:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)

                            // Item 1: Diagnoses
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("🔍", fontSize = 18.sp)
                                    Column {
                                        Text("تشخیص و درمان هوشمند بالینی", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("با انتخاب گونه حیوان و فیلتر کردن بر اساس اندام درگیر یا علائم فیزیکال، بلافاصله آنالیز سندرومیک نشانه‌ها همراه با رژیم‌های درمانی خط اول و دوم به شما پیشنهاد داده می‌شود.", fontSize = 11.sp, color = mutedTextColor, lineHeight = 16.sp)
                                    }
                                }
                            }

                            // Item 2: Drug Manual
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("📕", fontSize = 18.sp)
                                    Column {
                                        Text("دارونامه مکتوب و مرجع فارماکوپه", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("کاتالوگ مرجع داروها منطبق بر آخرین ویرایش کتابچه Plumb و BSAVA، شامل مکانیسم اثر، تداخلات جدی، منع مصرف، دوزهای مجزا برای سگ، کارواش، گربه و حیات وحش.", fontSize = 11.sp, color = mutedTextColor, lineHeight = 16.sp)
                                    }
                                }
                            }

                            // Item 3: Clinical Calculators
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("🧮", fontSize = 18.sp)
                                    Column {
                                        Text("مجموعه ماشین‌حساب‌های پیشرفته کلینیکال", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("• مایع‌درمانی هوشمند: تخمین کم‌آبی بدن و دبی قطرات سرم در ساعت.\n• انتقال خون کامل: اندازه‌گیری حجم ترانسفیوژن براساس تفاوت PCV.\n• نیازهای انرژی (کالری): محاسبات دقیق نیاز تغذیه‌ای RER و MER.\n• مانیتور بارداری: تعیین موعد زایمان زنده حیوانات باردار.\n• تریاژ تروما: سنجش کما گلاسکو سگ/گربه جهت فوریت درمانی اورژانسی.\n• سن معادل: معادل زیستی سن حیوانات خانگی به انسان.", fontSize = 11.sp, color = mutedTextColor, lineHeight = 16.sp)
                                    }
                                }
                            }

                            // Item 4: Prescription & SMS
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("📲", fontSize = 18.sp)
                                    Column {
                                        Text("مدیریت پرونده و اشتراک نسخه پیامکی", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("پس از محاسبه دوز داروها، نسخه نهایی را ثبت و در بایگانی نگه دارید. همچنین می‌توانید با زدن یک دکمه، نسخه را بلافاصله از طریق پیامک برای سرپرست ارسال کنید تا روند درمان آغاز گردد.", fontSize = 11.sp, color = mutedTextColor, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showHelpDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text("متوجه شدم", fontWeight = FontWeight.Bold)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
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
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📄", fontSize = 22.sp)
                            Text("شرایط و قوانین استفاده تخصصی", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "لطفاً پیش از استفاده از ابزار محاسباتی و دارونامه تخصصی پت‌کلاب، قوانین زیر را با دقت مطالعه فرمایید:",
                                fontSize = 12.sp,
                                color = textColor,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium
                            )

                            // Item 1: Clinical Responsibility
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("⚖️", fontSize = 18.sp)
                                    Column {
                                        Text("مسئولیت تشخیصی و تصمیم بالینی", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("کلیه پروتکل‌های درمانی پیشنهادی، نتایج آنالیز علائم بالینی و دوزهای استخراج‌شده از دارونامه مکتوب صرفا به عنوان راهنمای کمکی کلینیکال و برگرفته از مراجع معتبر (مانند Plumb و BSAVA) می‌باشند. تصمیم نهایی تشخیصی و درمانی در قبال حیوان بیمار، منحصراً بر عهده دامپزشک معالج است.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = mutedTextColor, lineHeight = 19.sp)
                                    }
                                }
                            }

                            // Item 2: Safe Calculator Calculations
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("🧮", fontSize = 18.sp)
                                    Column {
                                        Text("راستی‌آزمایی دقیق محاسبات کلینیکال", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("پزشک موظف است پارامترهای ورودی نظیر وزن بیمار (کیلوگرم)، درصد کم‌آبی (دهیدراتاسیون)، وضعیت هماتوکریت (PCV) برای ترانسفیوژن، و نمره کما گلاسکو (تریاژ تروما) را پیش از اعمال قطرات سرم یا تجویز نهایی بررسی و دوز حاصله را با شرایط فیزیکی حیوان تطبیق دهد.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = mutedTextColor, lineHeight = 19.sp)
                                    }
                                }
                            }

                            // Item 3: SMS and Data Privacy
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("🔒", fontSize = 18.sp)
                                    Column {
                                        Text("حریم خصوصی سرپرست و ارسال نسخه", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("سامانه پت‌کلاب هیچ‌گونه اطلاعات هویتی حساس از پت یا شماره تماس سرپرستان را بدون اجازه در سرورهای خارجی ذخیره نخواهد کرد. ارسال نسخه‌های تجویزشده از طریق پیامک منطبق بر قوانین رازداری پزشکی و حفظ اسرار بیمارستان‌های دامپزشکی کشور است.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = mutedTextColor, lineHeight = 19.sp)
                                    }
                                }
                            }

                            // Item 4: IP and Fair Use
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("🛑", fontSize = 18.sp)
                                    Column {
                                        Text("مالکیت مادی و معنوی دارونامه", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("بانک اطلاعاتی مرجع دارویی، اندیکاسیون‌ها، تریاژ و کلاسه بیماری‌های تعبیه‌شده در نرم‌افزار، حاصل کادرسازی و تلاش تیم پت‌کلاب است. هرگونه استخراج خودکار (دیتا ماینینگ) یا سوءاستفاده تجاری از داده‌ها غیرقانونی بوده و پیگرد مراجع دامپزشکی و قضایی را به همراه دارد.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = mutedTextColor, lineHeight = 19.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showTermsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text("شرایط را می‌پذیرم", fontWeight = FontWeight.Bold)
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
                            Text("🎉 تبریک! شما در حال استفاده از آخرین نسخه موجود هستید.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38A169))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("نسخه نصب شده: v1.5.0", fontSize = 11.sp, color = mutedTextColor)
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showUpdateDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))) {
                            Text("بستن")
                        }
                    }
                )
            }

            if (showSupportDialog) {
                AlertDialog(
                    onDismissRequest = { showSupportDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💬", fontSize = 22.sp)
                            Text("پشتیبانی آنلاین کادر بالینی پت‌کلاب", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                        }
                    },
                    text = {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val launchIntent = { url: String ->
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "برنامه‌ای برای باز کردن این لینک یافت نشد", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "کارشناسان فنی و همکاران بخش پشتیبانی بالینی پت‌کلاب جهت پاسخگویی به سوالات، ثبت بازخوردها و رفع باگ‌های احتمالی به صورت ۲۴ ساعته از طریق کانال‌های ارتباطی زیر پاسخگوی شما همکار گرامی هستند:",
                                fontSize = 12.sp,
                                color = textColor,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            // WhatsApp Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable { launchIntent("https://wa.me/989120000000") }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💬", fontSize = 24.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("پشتیبانی سریع در واتساپ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("انتقال نظرات، گزارش خطا و چت آنلاین", fontSize = 11.sp, color = mutedTextColor)
                                    }
                                    Text("◀", fontSize = 12.sp, color = Color(0xFF25D366), fontWeight = FontWeight.Bold)
                                }
                            }

                            // Telegram Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable { launchIntent("https://t.me/petclub_support") }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("✈️", fontSize = 24.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("ارتباط مستقیم در تلگرام", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("پاسخگویی سریع، ارسال فایل و تصویر خطا", fontSize = 11.sp, color = mutedTextColor)
                                    }
                                    Text("◀", fontSize = 12.sp, color = Color(0xFF24A1DE), fontWeight = FontWeight.Bold)
                                }
                            }

                            // Email Support Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable { launchIntent("mailto:support@petclub.ir?subject=پشتیبانی کادر درمان پت‌کلاب") }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("✉️", fontSize = 24.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("مکاتبه رسمی و ایمیل توسعه‌دهندگان", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("گزارش فنی باگ‌ها و همکاری‌های کلینیکال", fontSize = 11.sp, color = mutedTextColor)
                                    }
                                    Text("◀", fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showSupportDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text("متوجه شدم", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (showLogoutDialog) {
                val context = androidx.compose.ui.platform.LocalContext.current
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("میزان رضایت شما از برنامه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "همکار گرامی، خوشحال می‌شویم پیش از خروج، میزان رضایت خود را اعلام کنید تا در بروزرسانی‌های بعدی مورد استفاده قرار گیرد:",
                                fontSize = 13.sp,
                                color = textColor,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (i in 1..5) {
                                    val isSelected = i <= userRating
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star $i",
                                        tint = if (isSelected) Color(0xFFFFB300) else Color.LightGray.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clickable { userRating = i }
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val feedbackMsg = when (userRating) {
                                5 -> "بسیار عالی! از حمایت شما سپاسگزاریم. صمیمانه دعوت می‌کنیم با ثبت امتیاز در گوگل پلی ما را همراهی کنید. 🥰"
                                4 -> "سپاس فراوان از بازخورد خوب شما! لطفاً با ثبت نظر در گوگل پلی به ما در بهبود برنامه کمک کنید. 🌸"
                                3 -> "ممنون از رای شما. تمام تلاش خود را برای ارتقای امکانات به کار خواهیم بست. 💡"
                                else -> "پوزش می‌خواهیم که رضایت کامل شما جلب نشد. نظرات شما به ما در ارتقای اپلیکیشن یاری می‌رساند. 🛠️"
                            }
                            
                            Text(
                                text = feedbackMsg,
                                fontSize = 13.sp,
                                color = if (userRating >= 4) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (userRating >= 4) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFFF59E0B).copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    showLogoutDialog = false
                                    val playStoreIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        data = android.net.Uri.parse("market://details?id=" + context.packageName)
                                    }
                                    try {
                                        context.startActivity(playStoreIntent)
                                    } catch (e: Exception) {
                                        val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName))
                                        try {
                                            context.startActivity(webIntent)
                                        } catch (ex: Exception) {
                                            // Fallback
                                        }
                                    }
                                    viewModel.logout()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ثبت در گوگل پلی و خروج", fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        showLogoutDialog = false
                                        viewModel.logout()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("خروج بدون ثبت نظر", color = Color(0xFFEF4444), fontSize = 12.sp)
                                }
                                
                                TextButton(
                                    onClick = { showLogoutDialog = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("انصراف", color = textColor.copy(alpha = 0.6f), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItemRedesigned(
    title: String,
    iconEmoji: String,
    badge: String? = null,
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

        // Left side in RTL (Chevron indicator pointing left + potential count badge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00796B))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "‹", // Left pointing small Chevron
                fontSize = 18.sp,
                color = Color(0xFFA0AEC0),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
fun PrescriptionCardRedesigned(prescription: Prescription, onDelete: () -> Unit) {
    val isDark = isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.2f
    val cardBgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF718096)
    val borderColor = if (isDark) Color(0xFF2D3748) else Color(0xFFE2E8F0)
    val detailsBgColor = if (isDark) Color(0xFF334155) else Color(0xFFEDF2F7)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "دارو: ${prescription.drugName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF3B82F6)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53E3E))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("تجویز برای پت: ${prescription.petName}", fontSize = 12.sp, color = textColor)
            Text("تلفن صاحب حیوان: ${prescription.ownerPhone.ifEmpty { "ثبت نشده" }}", fontSize = 11.sp, color = mutedTextColor)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(detailsBgColor, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("دوز: ${String.format("%.2f", prescription.calculatedDose)} mg", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text("حجم: ${String.format("%.2f", prescription.calculatedVolume)} ml (cc)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اشتراک گذاری مستقیم نسخه بالینی",
                    fontSize = 11.sp,
                    color = mutedTextColor,
                    modifier = Modifier.padding(end = 6.dp)
                )
                IconButton(onClick = { /* Share */ }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF3B82F6))
                }
            }
        }
    }
}

private fun Int.toPersianDigits(): String {
    val english = "0123456789"
    val persian = "۰۱۲۳۴۵۶۷۸۹"
    return this.toString().map { char ->
        val index = english.indexOf(char)
        if (index != -1) persian[index] else char
    }.joinToString("")
}
