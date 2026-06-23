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
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

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

    // Supplementary professional fields
    var activityProvince by remember { mutableStateOf("تهران") }
    var activityCity by remember { mutableStateOf("تهران") }
    var activityType by remember { mutableStateOf("متخصص داخلی دام‌های کوچک") }
    var activityAddress by remember { mutableStateOf("تهران، میدان انقلاب، خیابان آزادی، پلاک ۱۲") }
    var mapLatitude by remember { mutableStateOf(35.6892) }
    var mapLongitude by remember { mutableStateOf(51.3890) }
    var isIdVerified by remember { mutableStateOf(false) }
    var showRewardUnlockDialog by remember { mutableStateOf(false) }

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
                            "نسخه‌ها" -> if (currentLang == "en") "Saved Prescriptions/Ledgers" else "نسخه / پرونده ذخیره شده"
                            "تنظیمات" -> if (currentLang == "en") "App Settings" else "تنظیمات برنامه"
                            "منابع" -> if (currentLang == "en") "Scientific Resources" else "منابع علمی معتبر"
                            "لینک‌ها" -> if (currentLang == "en") "Useful & Portable Links" else "لینک‌های کاربردی"
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
                                            Column(
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = editedName,
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
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

                                Spacer(modifier = Modifier.height(8.dp))

                                if (false) { // Card removed to avoid clutter
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = if (currentLang == "en") "📍 Workplace & Map Location" else "📍 محل فعالیت و موقعیت جغرافیایی",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Province Badge
                                            Box(
                                                modifier = Modifier
                                                    .background(if (isDark) Color(0xFF1E3A8A) else Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (currentLang == "en") "Province: $activityProvince" else "استان: $activityProvince",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDark) Color(0xFF93C5FD) else Color(0xFF0284C7)
                                                )
                                            }

                                            // Type of activity Badge
                                            Box(
                                                modifier = Modifier
                                                    .background(if (isDark) Color(0xFF3B0764) else Color(0xFFF3E8FF), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (currentLang == "en") "Activity: $activityType" else "فعالیت: $activityType",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = if (currentLang == "en") "Address:" else "آدرس محل فعالیت:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Text(
                                            text = activityAddress.ifEmpty { if (currentLang == "en") "Not completed yet" else "تکمیل نشده است" },
                                            fontSize = 12.sp,
                                            color = mutedTextColor
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Map simulator block
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                        ) {
                                            // Draw simulated road lines on canvas
                                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                                val width = size.width
                                                val height = size.height
                                                
                                                // Background soft map features
                                                drawRect(
                                                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                                                    size = size
                                                )
                                                
                                                // Roads
                                                drawLine(
                                                    color = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                                    start = androidx.compose.ui.geometry.Offset(width * 0.2f, 0f),
                                                    end = androidx.compose.ui.geometry.Offset(width * 0.2f, height),
                                                    strokeWidth = 16f
                                                )
                                                drawLine(
                                                    color = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                                    start = androidx.compose.ui.geometry.Offset(0f, height * 0.5f),
                                                    end = androidx.compose.ui.geometry.Offset(width, height * 0.5f),
                                                    strokeWidth = 20f
                                                )
                                                drawLine(
                                                    color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
                                                    start = androidx.compose.ui.geometry.Offset(width * 0.6f, 0f),
                                                    end = androidx.compose.ui.geometry.Offset(width * 0.9f, height),
                                                    strokeWidth = 12f
                                                )
                                            }

                                            // Centered Glowing Pin Map Marker Simulation
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .size(36.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = "Map Pin",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }

                                            // Coordinates banner overlay at bottom
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.62f))
                                                    .align(Alignment.BottomCenter)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (currentLang == "en") {
                                                        "Map coordinates: N ${String.format("%.4f", mapLatitude)}°, E ${String.format("%.4f", mapLongitude)}°"
                                                    } else {
                                                        "مختصات جغرافیایی: شمالی ${String.format("%.4f", mapLatitude)}، شرقی ${String.format("%.4f", mapLongitude)}"
                                                    },
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }

                                // Unlocked Diamond Promotion Reward Banner Card from completing professional info
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .border(
                                            BorderStroke(1.5.dp, androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899))
                                            )),
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("🎁", fontSize = 24.sp)
                                            Column {
                                                Text(
                                                    text = if (currentLang == "en") "Diamond Discount Code Active!" else "کد هدیه اشتراک الماس فعال شد!",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                                Text(
                                                    text = if (currentLang == "en") "Completed profile bonus reward" else "پاداش تکمیل اطلاعات صنفی دامپزشکی",
                                                    fontSize = 11.sp,
                                                    color = mutedTextColor
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (currentLang == "en") {
                                                "Since you have completed your user profile information, you can use the discount code below to claim a one-month Diamond subscription 100% free!"
                                            } else {
                                                "به پاس قدردانی از تکمیل مشخصات کاربری و تخصصی دامپزشکی، می‌توانید مقتدرانه از کد تخفیف زیر جهت دریافت یک ماه اشتراک الماس به صورت کاملاً رایگان استفاده نمایید!"
                                            },
                                            fontSize = 11.sp,
                                            color = textColor.copy(alpha = 0.85f),
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        val context = androidx.compose.ui.platform.LocalContext.current
                                        val promoCode = "DIAMOND100_VET"
                                        Button(
                                            onClick = {
                                                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clipData = android.content.ClipData.newPlainText("Vetaris Diamond Promo Code", promoCode)
                                                clipboardManager.setPrimaryClip(clipData)
                                                showRewardUnlockDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy code",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (currentLang == "en") "Copy Code: $promoCode" else "کپی کد هدیه: $promoCode",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
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
                                                    text = if (currentLang == "en") "Manage Subscription" else "مدیریت اشتراک",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF3B82F6)
                                                )
                                                Text(
                                                    text = "‹",
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
                                                text = if (currentLang == "en") "Doctor's Club Rating" else "امتیاز علمی و باشگاه پزشکان",
                                                fontSize = 11.sp,
                                                color = mutedTextColor
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (currentLang == "en") "${activeSession?.coins ?: 100} Clinician Stars" else "${(activeSession?.coins ?: 100).toPersianDigits()} ستاره علمی",
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
                                                    text = if (currentLang == "en") "Awards & Perks" else "جوایز، تخفیف‌ها و ارتقای رتبه",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF3B82F6)
                                                )
                                                Text(
                                                    text = "‹",
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
                                            title = if (currentLang == "en") "Saved Prescriptions" else "نسخه / پرونده ذخیره شده",
                                            iconEmoji = "💳",
                                            badge = if (allPrescriptions.isNotEmpty()) "${allPrescriptions.size}" else null,
                                            onClick = { activeProfileSection = "نسخه‌ها" }
                                        )
                                    }
                                }

                                // 4. Notifications Grouping
                                Text(
                                    text = if (currentLang == "en") "Notifications" else "اطلاع رسانی",
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
                                        // Inline notification toggle item
                                        val context = androidx.compose.ui.platform.LocalContext.current
                                        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                                            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                                        ) { isGranted ->
                                            if (isGranted) {
                                                viewModel.setNotificationsEnabled(true)
                                                com.example.util.NotificationHelper.sendNotification(
                                                    context,
                                                    if (currentLang == "en") "System Notifications Active 🔔" else "فعال‌سازی سیستم پدیده 🔔",
                                                    if (currentLang == "en") "You will receive real-time vaccine checks and clinical alerts!" else "از این پس پیام‌ها و هشدارهای واکسیناسیون و دستیار پزشکی ارسال خواهند شد!"
                                                )
                                            } else {
                                                viewModel.setNotificationsEnabled(false)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                             Row(
                                                 verticalAlignment = Alignment.CenterVertically,
                                                 horizontalArrangement = Arrangement.spacedBy(12.dp)
                                             ) {
                                                 val circleBgColor = if (isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.2f) Color(0xFF334155) else Color(0xFFEDF2F7)
                                                 val circleBorderColor = if (isSystemInDarkTheme() || MaterialTheme.colorScheme.background.red < 0.2f) Color(0xFF475569) else Color(0xFFE2E8F0)
                                                 Box(
                                                     modifier = Modifier
                                                         .size(36.dp)
                                                         .background(circleBgColor, CircleShape)
                                                         .border(1.dp, circleBorderColor, CircleShape),
                                                     contentAlignment = Alignment.Center
                                                 ) {
                                                     Text("🔔", fontSize = 16.sp)
                                                 }

                                                 Column {
                                                     Text(
                                                         text = if (currentLang == "en") "Push Notifications" else "دریافت نوتیفیکیشن‌ها",
                                                         fontSize = 14.sp,
                                                         fontWeight = FontWeight.Bold,
                                                         color = textColor
                                                     )
                                                     val statusDesc = if (notificationsEnabled) {
                                                         if (currentLang == "en") "Enabled" else "فعال"
                                                     } else {
                                                         if (currentLang == "en") "Disabled" else "غیرفعال"
                                                     }
                                                     Text(
                                                         text = statusDesc,
                                                         fontSize = 11.sp,
                                                         color = mutedTextColor
                                                     )
                                                 }
                                             }

                                             Switch(
                                                 checked = notificationsEnabled,
                                                 onCheckedChange = { isChecked ->
                                                     if (isChecked) {
                                                         if (android.os.Build.VERSION.SDK_INT >= 33) {
                                                             val isPermissionGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                                                 context,
                                                                 "android.permission.POST_NOTIFICATIONS"
                                                             ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                                             if (isPermissionGranted) {
                                                                 viewModel.setNotificationsEnabled(true)
                                                                 com.example.util.NotificationHelper.sendNotification(
                                                                     context,
                                                                     if (currentLang == "en") "Vetaris Reminders Live" else "هشدارهای پزشکی فعال شد",
                                                                     if (currentLang == "en") "Notification system successfully verified!" else "دستیار صوتی و سیستم هشدارهای پزشکی شما فعال است!"
                                                                 )
                                                             } else {
                                                                 launcher.launch("android.permission.POST_NOTIFICATIONS")
                                                             }
                                                         } else {
                                                             viewModel.setNotificationsEnabled(true)
                                                             com.example.util.NotificationHelper.sendNotification(
                                                                 context,
                                                                 if (currentLang == "en") "Vetaris Reminders Live" else "هشدارهای پزشکی فعال شد",
                                                                 if (currentLang == "en") "Notification system successfully verified!" else "دستیار صوتی و سیستم هشدارهای پزشکی شما فعال است!"
                                                             )
                                                         }
                                                     } else {
                                                         viewModel.setNotificationsEnabled(false)
                                                     }
                                                 }
                                             )
                                         }

                                         HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                         ProfileMenuItemRedesigned(
                                             title = if (currentLang == "en") "Invite Friends" else "دعوت از دوستان",
                                             iconEmoji = "👥",
                                             onClick = { showInviteDialog = true }
                                         )
                                     }
                                 }

                                // 5. Support Grouping
                                Text(
                                    text = if (currentLang == "en") "Support" else "پشتیبانی",
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
                                            title = if (currentLang == "en") "Scientific Resources" else "منابع علمی",
                                            iconEmoji = "📖",
                                            onClick = { activeProfileSection = "منابع" }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Useful & Portable Links" else "لینک های کاربردی و پورتابل",
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
                                            title = if (currentLang == "en") "Online Support" else "پشتیبانی انلاین",
                                            iconEmoji = "💬",
                                            onClick = { showSupportDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        ProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Database & App Update" else "بروزرسانی پایگاه داده و برنامه",
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

                                    // Notifications Switch toggle
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val context = androidx.compose.ui.platform.LocalContext.current
                                        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                                            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                                        ) { isGranted ->
                                            if (isGranted) {
                                                viewModel.setNotificationsEnabled(true)
                                                com.example.util.NotificationHelper.sendNotification(
                                                    context,
                                                    if (currentLang == "en") "Notifications Enabled" else "اعلام‌ها فعال شد",
                                                    if (currentLang == "en") "You will receive system reminders and updates!" else "اطلاع‌رسانی یادآورها و خدمات واکسیناسیون شما فعال شد."
                                                )
                                            } else {
                                                viewModel.setNotificationsEnabled(false)
                                            }
                                        }
                                         
                                        Text(
                                            text = if (currentLang == "en") "Enable Push Notifications" else "دریافت نوتیفیکیشن‌ها:",
                                            fontSize = 13.sp,
                                            color = textColor
                                        )
                                         
                                        Switch(
                                            checked = notificationsEnabled,
                                            onCheckedChange = { isChecked ->
                                                if (isChecked) {
                                                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                                                        val isPermissionGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                                            context,
                                                            "android.permission.POST_NOTIFICATIONS"
                                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                                        if (isPermissionGranted) {
                                                            viewModel.setNotificationsEnabled(true)
                                                            com.example.util.NotificationHelper.sendNotification(
                                                                context,
                                                                if (currentLang == "en") "Notifications Active 🔔" else "فعال‌سازی اطلاع‌رسانی 🔔",
                                                                if (currentLang == "en") "You have verified notification services successfully." else "پیکربندی سیستم اطلاع‌رسانی با موفقیت تایید شد."
                                                            )
                                                        } else {
                                                            launcher.launch("android.permission.POST_NOTIFICATIONS")
                                                        }
                                                    } else {
                                                        viewModel.setNotificationsEnabled(true)
                                                        com.example.util.NotificationHelper.sendNotification(
                                                            context,
                                                            if (currentLang == "en") "Notifications Active 🔔" else "فعال‌سازی اطلاع‌رسانی 🔔",
                                                            if (currentLang == "en") "You have verified notification services successfully." else "پیکربندی سیستم اطلاع‌رسانی با موفقیت تایید شد."
                                                        )
                                                    }
                                                } else {
                                                    viewModel.setNotificationsEnabled(false)
                                                }
                                            }
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
                var phoneInput by remember { 
                    mutableStateOf(if (editedPhone.contains("@") || editedPhone.startsWith("سریع با")) "" else editedPhone) 
                }
                var isStudentInput by remember { mutableStateOf(isStudent) }
                var idInput by remember { 
                    mutableStateOf(if (identification.startsWith("سریع با")) "" else identification) 
                }
                var workplaceInput by remember { 
                    mutableStateOf(if (workplaceOrUni == "Google" || workplaceOrUni == "Apple" || workplaceOrUni.startsWith("سریع با")) (if (currentLang == "en") "University of Tehran" else "دانشگاه تهران") else workplaceOrUni.ifEmpty { if (currentLang == "en") "University of Tehran" else "دانشگاه تهران" }) 
                }
                var specialtyInput by remember { 
                    mutableStateOf(if (specialty == "تایید هویت سریع مستقل" || specialty.startsWith("تایید")) (if (currentLang == "en") "Small Animal Internal Medicine" else "داخلی حیوانات کوچک") else specialty.ifEmpty { if (currentLang == "en") "Small Animal Internal Medicine" else "داخلی حیوانات کوچک" }) 
                }

                // New fields inside dialog state
                var provinceInput by remember { mutableStateOf(activityProvince) }
                var cityInput by remember { mutableStateOf(activityCity) }
                var activityTypeInput by remember { mutableStateOf(activityType) }
                var addressInput by remember { mutableStateOf(activityAddress) }
                var mapLatInput by remember { mutableStateOf(mapLatitude) }
                var mapLngInput by remember { mutableStateOf(mapLongitude) }
                
                var isProvinceDropdownExpanded by remember { mutableStateOf(false) }
                var isCityDropdownExpanded by remember { mutableStateOf(false) }
                var isUniDropdownExpanded by remember { mutableStateOf(false) }
                var isSpecialtyDropdownExpanded by remember { mutableStateOf(false) }
                var isActivityTypeDropdownExpanded by remember { mutableStateOf(false) }
                var isVerifying by remember { mutableStateOf(false) }
                var isVerifiedByOrg by remember { mutableStateOf(isIdVerified) }

                val universityList = if (currentLang == "en") listOf(
                    "University of Tehran", "Shiraz University", "Ferdowsi University of Mashhad",
                    "Science and Research Branch", "University of Tabriz", "University of Science and Arts"
                ) else listOf(
                    "دانشگاه تهران", "دانشگاه شیراز", "دانشگاه فردوسی مشهد",
                    "دانشگاه علوم تحقیقات", "دانشگاه تبریز", "دانشگاه کار و هنر"
                )

                val specialtyList = if (currentLang == "en") listOf(
                    "Small Animal Internal Medicine", "Surgery & Anesthesia", "Clinical Pathology",
                    "Radiology & Imaging", "Obstetrics & Reproductive Diseases"
                ) else listOf(
                    "داخلی حیوانات کوچک", "جراحی و هوشبری", "کلینیکال پاتولوژی",
                    "رادیولوژی و تصویربرداری", "مامایی و بیماری‌های تولیدمثل"
                )

                val provinceList = if (currentLang == "en") listOf(
                    "Alborz", "Ardabil", "East Azerbaijan", "West Azerbaijan", "Bushehr",
                    "Chaharmahal and Bakhtiari", "Fars", "Gilan", "Golestan", "Hamadan",
                    "Hormozgan", "Ilam", "Isfahan", "Kerman", "Kermanshah",
                    "North Khorasan", "Razavi Khorasan", "South Khorasan", "Khuzestan", "Kohgiluyeh and Boyer-Ahmad",
                    "Kurdistan", "Lorestan", "Markazi", "Mazandaran", "Qazvin",
                    "Qom", "Semnan", "Sistan and Baluchestan", "Tehran", "Yazd", "Zanjan"
                ) else listOf(
                    "البرز", "اردبیل", "آذربایجان شرقی", "آذربایجان غربی", "بوشهر",
                    "چهارمحال و بختیاری", "فارس", "گیلان", "گلستان", "همدان",
                    "هرمزگان", "ایلام", "اصفهان", "کرمان", "کرمانشاه",
                    "خراسان شمالی", "خراسان رضوی", "خراسان جنوبی", "خوزستان", "کهگیلویه و بویراحمد",
                    "کردستان", "لرستان", "مرکزی", "مازندران", "قزوین",
                    "قم", "سمنان", "سیستان و بلوچستان", "تهران", "یزد", "زنجان"
                )

                fun getCitiesListForProvince(prov: String): List<String> {
                    val isEn = currentLang == "en"
                    return if (isEn) {
                        when (prov) {
                            "Tehran" -> listOf("Tehran", "Rey", "Shemiranat", "Eslamshahr", "Shahriar", "Malard", "Qods", "Pakdasht", "Damavand")
                            "Isfahan" -> listOf("Isfahan", "Kashan", "Khomeyni Shahr", "Najafabad", "Shahin Shahr", "Shahreza", "Golpayegan")
                            "Fars" -> listOf("Shiraz", "Marvdasht", "Jahrom", "Fasa", "Darab", "Kazerun", "Lar", "Abadeh")
                            "Razavi Khorasan" -> listOf("Mashhad", "Nishapur", "Sabzevar", "Torbat-e Heydarieh", "Quchan", "Kashmar", "Gonabad")
                            "East Azerbaijan" -> listOf("Tabriz", "Maragheh", "Marand", "Miyaneh", "Ahar", "Bonab", "Sarab")
                            "West Azerbaijan" -> listOf("Urmia", "Khoy", "Miandoab", "Mahabad", "Bukan", "Salmas", "Piranshahr")
                            "Mazandaran" -> listOf("Sari", "Babil", "Amol", "Qaem Shahr", "Behshahr", "Tonekabon", "Chalus", "Babolsar")
                            "Gilan" -> listOf("Rasht", "Bandar-e Anzali", "Lahijan", "Langarud", "Talysh", "Rudsar", "Fuman")
                            "Alborz" -> listOf("Karaj", "Fardis", "Savojbolagh", "Nazarbad", "Hashtgerd")
                            "Khuzestan" -> listOf("Ahvaz", "Dezful", "Abadan", "Khorramshahr", "Mahshahr", "Behbehan", "Andimeshk")
                            "Kerman" -> listOf("Kerman", "Sirjan", "Rafsanjan", "Jiroft", "Bam", "Zarand", "Shahr-e Babak")
                            "Yazd" -> listOf("Yazd", "Meybod", "Ardakan", "Bafq", "Mehriz", "Taft")
                            "Qazvin" -> listOf("Qazvin", "Takestan", "Alvand", "Abyek", "Buin Zahra")
                            "Markazi" -> listOf("Arak", "Saveh", "Khomein", "Mahallat", "Delijan", "Tafresh")
                            "Qom" -> listOf("Qom", "Qanavat", "Kahak")
                            "Hamadan" -> listOf("Hamadan", "Malayer", "Nahavand", "Tuyserkan", "Kabudarahang")
                            "Zanjan" -> listOf("Zanjan", "Abhar", "Khorramdarreh", "Khodabandeh")
                            "Semnan" -> listOf("Semnan", "Shahrud", "Damghan", "Garmsar", "Mehdishahr")
                            "Kermanshah" -> listOf("Kermanshah", "Islamabad-e Gharb", "Kangavar", "Sunqur", "Javanrud")
                            "Kurdistan" -> listOf("Sanandaj", "Saqqez", "Marivan", "Baneh", "Qorveh", "Bijar")
                            "Ardabil" -> listOf("Ardabil", "Parsabad", "Meshgin Shahr", "Khalkhal", "Germi")
                            "Lorestan" -> listOf("Khorramabad", "Borujerd", "Dorud", "Kuhdasht", "Aligudarz")
                            "Ilam" -> listOf("Ilam", "Dehloran", "Eyvan", "Abdanan", "Mehran")
                            "Chaharmahal and Bakhtiari" -> listOf("Shahrekord", "Borujen", "Lordegan", "Farsan")
                            "Kohgiluyeh and Boyer-Ahmad" -> listOf("Yasuj", "Dogonbadan", "Dehdasht", "Sisakht")
                            "Sistan and Baluchestan" -> listOf("Zahedan", "Zabol", "Chabahar", "Iranshahr", "Saravan")
                            "Hormozgan" -> listOf("Bandar Abbas", "Minab", "Qeshm", "Kish", "Bandar Lengeh")
                            "Bushehr" -> listOf("Bushehr", "Borazjan", "Kangan", "Genaveh", "Asaluyeh")
                            "Golestan" -> listOf("Gorgan", "Gonbad-e Qabus", "Bandar Torkaman", "Aliabad-e Katol")
                            "North Khorasan" -> listOf("Bojnurd", "Shirvan", "Esfarayen", "Jajarm")
                            "South Khorasan" -> listOf("Birjand", "Qaen", "Ferdows", "Tabs")
                            else -> listOf("Tehran")
                        }
                    } else {
                        when (prov) {
                            "تهران" -> listOf("تهران", "ری", "شمیرانات", "اسلامشهر", "شهریار", "ملارد", "قدس", "پاکدشت", "دماوند")
                            "اصفهان" -> listOf("اصفهان", "کاشان", "خمینی‌شهر", "نجف‌آباد", "شاهین‌شهر", "شهرضا", "گلپایگان")
                            "فارس" -> listOf("شیراز", "مرودشت", "جهرم", "فسا", "داراب", "کازرون", "لار", "آباده")
                            "خراسان رضوی" -> listOf("مشهد", "نیشابور", "سبزوار", "تربت حیدریه", "قوچان", "کاشمر", "گناباد")
                            "آذربایجان شرقی" -> listOf("تبریز", "مراغه", "مرند", "میانه", "اهر", "بناب", "سراب")
                            "آذربایجان غربی" -> listOf("ارومیه", "خوی", "میاندوآب", "مهاباد", "بوکان", "سلماس", "پیرانشهر")
                            "مازندران" -> listOf("ساری", "بابل", "آمل", "قائم‌شهر", "بهشهر", "تنکابن", "چالوس", "بابلسر")
                            "گیلان" -> listOf("رشت", "بندر انزلی", "لاهیجان", "لنگرود", "تالش", "رودسر", "فومن")
                            "البرز" -> listOf("کرج", "فردیس", "ساوجبلاغ", "نظرآباد", "هشتگرد")
                            "خوزستان" -> listOf("اهواز", "دزفول", "آبادان", "خرمشهر", "ماهشهر", "بهبهان", "اندیمشک")
                            "کرمان" -> listOf("کرمان", "سیرجان", "رفسنجان", "جیرفت", "بم", "زرند", "شهربابک")
                            "یزد" -> listOf("یزد", "میبد", "اردکان", "بافق", "مهریز", "تفت")
                            "قزوین" -> listOf("قزوین", "تاکستان", "الوند", "آبیک", "بویین‌زهرا")
                            "مرکزی" -> listOf("اراک", "ساوه", "خمین", "محلات", "دلیجان", "تفرش")
                            "قم" -> listOf("قم", "قنوات", "کهک")
                            "همدان" -> listOf("همدان", "ملایر", "نهاوند", "تویسرکان", "کبودرآهنگ")
                            "زنجان" -> listOf("زنجان", "ابهر", "خرمدره", "خدابنده")
                            "سمنان" -> listOf("سمنان", "شاهرود", "دامغان", "گرمسار", "مهدیشهر")
                            "کرمانشاه" -> listOf("کرمانشاه", "اسلام‌آباد غرب", "کنگاور", "سنقر", "جوانرود")
                            "کردستان" -> listOf("سنندج", "سقز", "مریوان", "بانه", "قروه", "بیجار")
                            "اردبیل" -> listOf("اردبیل", "پارس‌آباد", "مشگین‌شهر", "خلخال", "گرمی")
                            "لرستان" -> listOf("خرم‌آباد", "بروجرد", "دورود", "کوهدشت", "الیگودرز")
                            "ایلام" -> listOf("ایلام", "دهلران", "ایوان", "آبدانان", "مهران")
                            "چهارمحال و بختیاری" -> listOf("شهرکرد", "بروجن", "لردگان", "فارسان")
                            "کهگیلویه و بویراحمد" -> listOf("یاسوج", "دوگنبدان", "دهدشت", "سی‌سخت")
                            "سیستان و بلوچستان" -> listOf("زاهدان", "زابل", "چابهار", "ایرانشهر", "سراوان")
                            "هرمزگان" -> listOf("بندرعباس", "میناب", "قشم", "کیش", "بندرلنگه")
                            "بوشهر" -> listOf("بوشهر", "برازجان", "کنگان", "گناوه", "عسلویه")
                            "گلستان" -> listOf("گرگان", "گنبد کاووس", "بندر ترکمن", "علی‌آباد کتول")
                            "خراسان شمالی" -> listOf("بجنورد", "شیروان", "اسفراین", "جاجرم")
                            "خراسان جنوبی" -> listOf("بیرجند", "قائن", "فردوس", "طبس")
                            else -> listOf("تهران")
                        }
                    }
                }

                val activityTypesList = if (currentLang == "en") listOf(
                    "General", "Small Animal specialist", "Avian specialist", "Aquatic specialist", "Wildlife and exotic", "Other"
                ) else listOf(
                    "عمومی", "متخصص داخلی دام‌های کوچک", "متخصص پرندگان", "متخصص آبزیان", "حیات وحش و اگزوتیک", "سایر"
                )

                if (isVerifying) {
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1200)
                        isVerifying = false
                        isVerifiedByOrg = true
                        nameInput = if (currentLang == "en") "Dr. Masoud Zare" else "دکتر مسعود زارع"
                    }
                }

                AlertDialog(
                    onDismissRequest = { showEditProfileDialog = false },
                    title = { Text(if (currentLang == "en") "Complete & Verify Professional Info" else "تکمیل و احراز اطلاعات تخصصی", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = if (currentLang == "en") "Enter details or confirm with Vet Organization to autocomplete name:" else "اطلاعات زیر را تکمیل کنید تا استعلام نظام انجام شود:",
                                fontSize = 11.sp,
                                color = mutedTextColor,
                                lineHeight = 16.sp
                            )

                            // 1. License ID Input
                            OutlinedTextField(
                                value = idInput,
                                onValueChange = { 
                                    idInput = it 
                                    isVerifiedByOrg = false // reset verification if license changes
                                },
                                label = { Text(if (currentLang == "en") "Veterinary License Number / Student ID" else "کد نظام دامپزشکی / شماره دانشجویی") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Verification button
                            if (!isVerifiedByOrg) {
                                Button(
                                    onClick = {
                                        if (idInput.isNotBlank()) {
                                            isVerifying = true
                                        }
                                    },
                                    enabled = !isVerifying && idInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    if (isVerifying) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                                    } else {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Verify", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (currentLang == "en") "Verify with Veterinary Organization" else "استعلام و تایید توسط سازمان نظام دامپزشکی", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFDCFCE7), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLang == "en") "✓ Verified & Auto-completed!" else "✓ تایید هویت شد و نام خودکار تکمیل گردید",
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // 2. Name & Phone (Name is disabled if verified but can be entered manually otherwise)
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text(if (currentLang == "en") "Full Name" else "نام و نام خانوادگی") },
                                singleLine = true,
                                enabled = !isVerifiedByOrg, // Lock name field if verified automatically
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text(if (currentLang == "en") "Mobile Number" else "شماره همراه") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = if (currentLang == "en") "Professional Role:" else "نوع کاربری صنفی:",
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
                                        if (currentLang == "en") "👨‍⚕️ Clinician/Doctor" else "👨‍⚕️ پزشک کلینیسین",
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
                                        if (currentLang == "en") "🎓 Student or Resident" else "🎓 دانشجو یا رزیدنت",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isStudentInput) Color.White else (if (isDark) Color.White else Color.Black)
                                    )
                                }
                            }

                            if (isStudentInput) {
                                // School selection drop-down selector
                                Text(if (currentLang == "en") "🎓 University / Institute:" else "🎓 دانشگاه محل تحصیل:", fontSize = 11.sp)
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
                            }

                            // 3. Province of activity selector (Iran Provinces)
                            Text(
                                text = if (currentLang == "en") "📍 Province of Activity:" else "📍 استان فعالیت:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                    .background(cardBgColor)
                                    .clickable { isProvinceDropdownExpanded = true }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(provinceInput, fontSize = 13.sp, color = textColor)
                                    Text("▼", fontSize = 10.sp, color = Color.Gray)
                                }

                                DropdownMenu(
                                    expanded = isProvinceDropdownExpanded,
                                    onDismissRequest = { isProvinceDropdownExpanded = false }
                                ) {
                                    provinceList.forEach { prov ->
                                        DropdownMenuItem(
                                            text = { Text(prov, fontSize = 13.sp) },
                                            onClick = {
                                                provinceInput = prov
                                                isProvinceDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // 4. City of activity selector (Iran Cities based on Province)
                            Text(
                                text = if (currentLang == "en") "🏙️ City of Activity:" else "🏙️ شهر فعالیت:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                    .background(cardBgColor)
                                    .clickable { isCityDropdownExpanded = true }
                                    .padding(12.dp)
                            ) {
                                val citiesOfProvince = getCitiesListForProvince(provinceInput)
                                LaunchedEffect(provinceInput) {
                                    if (citiesOfProvince.isNotEmpty() && !citiesOfProvince.contains(cityInput)) {
                                        cityInput = citiesOfProvince.first()
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cityInput, fontSize = 13.sp, color = textColor)
                                    Text("▼", fontSize = 10.sp, color = Color.Gray)
                                }

                                DropdownMenu(
                                    expanded = isCityDropdownExpanded,
                                    onDismissRequest = { isCityDropdownExpanded = false }
                                ) {
                                    citiesOfProvince.forEach { city ->
                                        DropdownMenuItem(
                                            text = { Text(city, fontSize = 13.sp) },
                                            onClick = {
                                                cityInput = city
                                                isCityDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // 4. Type of Activity Choice Dropdown Selector
                            Text(
                                text = if (currentLang == "en") "💼 Type of Veterinary Activity:" else "💼 نوع فعالیت دامپزشکی:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                    .background(cardBgColor)
                                    .clickable { isActivityTypeDropdownExpanded = true }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(activityTypeInput, fontSize = 13.sp, color = textColor)
                                    Text("▼", fontSize = 10.sp, color = Color.Gray)
                                }

                                DropdownMenu(
                                    expanded = isActivityTypeDropdownExpanded,
                                    onDismissRequest = { isActivityTypeDropdownExpanded = false }
                                ) {
                                    activityTypesList.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type, fontSize = 13.sp) },
                                            onClick = {
                                                activityTypeInput = type
                                                isActivityTypeDropdownExpanded = false
                                            }
                                        )
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
                                
                                // Save supplemental fields
                                activityProvince = provinceInput
                                activityCity = cityInput
                                activityType = activityTypeInput
                                activityAddress = addressInput
                                mapLatitude = mapLatInput
                                mapLongitude = mapLngInput
                                isIdVerified = isVerifiedByOrg

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
                            Text(if (currentLang == "en") "Save Changes" else "ذخیره تغییرات")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditProfileDialog = false }) {
                            Text(if (currentLang == "en") "Cancel" else "انصراف")
                        }
                    }
                )
            }

            if (showPasswordDialog) {
                var oldPass by remember { mutableStateOf("") }
                var newPass by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showPasswordDialog = false },
                    title = { Text(if (currentLang == "en") "Change Password" else "تغییر رمز عبور", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = oldPass,
                                onValueChange = { oldPass = it },
                                label = { Text(if (currentLang == "en") "Current Password" else "رمز عبور فعلی") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text(if (currentLang == "en") "New Password" else "رمز عبور جدید") },
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
                            Text(if (currentLang == "en") "Update Password" else "بروزرسانی رمز عبور")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPasswordDialog = false }) {
                            Text(if (currentLang == "en") "Back" else "بازگشت")
                        }
                    }
                )
            }

            if (showDevicesDialog) {
                AlertDialog(
                    onDismissRequest = { showDevicesDialog = false },
                    title = { Text(if (currentLang == "en") "Devices Connected to Account" else "دستگاه‌های متصل به حساب", fontWeight = FontWeight.Bold) },
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
                var giftCodeInput by remember { mutableStateOf("") }
                var giftCodeStatus by remember { mutableStateOf("") }
                var isGiftApplied by remember { mutableStateOf<Boolean?>(null) }
                AlertDialog(
                    onDismissRequest = { showSubscriptionDialog = false },
                    title = { 
                        Text(
                            text = if (currentLang == "en") "Subscription Management" else "مدیریت اشتراک", 
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

                            // Gift/Promo Code Section
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (currentLang == "en") "🎟️ Apply Promo / Gift Code" else "🎟️ ثبت کد هدیه یا تخفیف ویژه",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = textColor
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = giftCodeInput,
                                    onValueChange = { 
                                        giftCodeInput = it
                                        isGiftApplied = null 
                                        giftCodeStatus = ""
                                    },
                                    placeholder = { 
                                        Text(
                                            text = if (currentLang == "en") "e.g., DIAMOND100_VET" else "مثال: DIAMOND100_VET",
                                            fontSize = 11.sp
                                        ) 
                                    },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF3B82F6),
                                        unfocusedBorderColor = borderColor
                                    )
                                )
                                Button(
                                    onClick = {
                                        val cleanCode = giftCodeInput.trim().uppercase()
                                        if (cleanCode.isEmpty()) {
                                            isGiftApplied = false
                                            giftCodeStatus = if (currentLang == "en") "Please enter a code!" else "لطفاً کد را وارد کنید!"
                                        } else if (cleanCode == "DIAMOND100_VET" || cleanCode == "DIAMOND100_OWNER") {
                                            selectedPlanTemp = "diamond"
                                            isGiftApplied = true
                                            giftCodeStatus = if (currentLang == "en") 
                                                "Success! Diamond Plan selected. Accept to activate." 
                                                else "کد معتبر است! اشتراک الماس انتخاب شد. جهت تایید نهایی ثبت کنید."
                                        } else if (cleanCode == "SILVER_GIFT") {
                                            selectedPlanTemp = "silver"
                                            isGiftApplied = true
                                            giftCodeStatus = if (currentLang == "en") 
                                                "Success! Silver Plan selected." 
                                                else "کد معتبر است! اشتراک نقره‌ای انتخاب شد."
                                        } else if (cleanCode == "GOLD_GIFT") {
                                            selectedPlanTemp = "gold"
                                            isGiftApplied = true
                                            giftCodeStatus = if (currentLang == "en") 
                                                "Success! Gold Plan selected." 
                                                else "کد معتبر است! اشتراک طلایی انتخاب شد."
                                        } else {
                                            isGiftApplied = false
                                            giftCodeStatus = if (currentLang == "en") "Invalid promo code" else "کد هدیه وارد شده معتبر نیست!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == "en") "Apply" else "اعمال کد",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (giftCodeStatus.isNotEmpty()) {
                                Text(
                                    text = giftCodeStatus,
                                    color = if (isGiftApplied == true) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
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
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🏅", fontSize = 24.sp)
                            Text(
                                text = if (currentLang == "en") "Clinician Elite Club" else "باشگاه پزشکان نخبه پت‌کلاب",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textColor
                            )
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 4.dp)
                        ) {
                            // Point Balance Card
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isDark) Color(0xFF334155) else Color(0xFFBFDBFE),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (currentLang == "en") "Scientific Credibility Score" else "رتبه اعتبار علمی شما",
                                            fontSize = 11.sp,
                                            color = mutedTextColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (currentLang == "en") "Level 3 Clinician Badge (Gold) ⭐️" else "نشان پزشک برجسته (سطح ۳ طلایی) ⭐️",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = (activeSession?.coins ?: 100).toPersianDigits(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color(0xFFF59E0B)
                                        )
                                        Text(
                                            text = if (currentLang == "en") "Stars" else "ستاره تخصصی",
                                            fontSize = 10.sp,
                                            color = mutedTextColor
                                        )
                                    }
                                }
                            }

                            // Dynamic call-to-action description
                            Text(
                                text = if (currentLang == "en") {
                                    "In Vet-Club, your medical contributions and daily activities are highly valued! Increase your score by logging prescriptions, utilizing emergency calculators, and keeping diagnostic records. Gain premium benefits, map prioritization, and partner perks!"
                                } else {
                                    "در باشگاه پزشکان پت‌کلاب، هر فعالیت بالینی شما گامی به سوی تعالی حرفه‌ای است! با ارتقای اعتبار علمی خود از طریق ثبت بهینه‌سازها، انجام فرمولاسیون دوز دارویی و تکمیل رزومه، قفل ابزارهای مجهز، رتبه‌بندی برتر در نقشه نوبت‌دهی و تخفیف‌های گران‌بهای همایش‌ها را باز کنید."
                                },
                                fontSize = 12.sp,
                                color = textColor,
                                lineHeight = 18.sp
                            )

                            HorizontalDivider(color = dividerColor)

                            // How to Earn Points Section
                            Text(
                                text = if (currentLang == "en") "📈 How to Earn Stars:" else "📈 شیوه جمع‌آوری ستاره‌های علمی:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = textColor
                            )

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        "✍️" to (if (currentLang == "en") "Write and log an E-Prescription: +50 Stars" else "ثبت هر نسخه موفق الکترونیکی: +۵۰ ستاره"),
                                        "🚑" to (if (currentLang == "en") "Calculate Trauma Triage / clinical index: +20 Stars" else "محاسبه تریاژ تروما یا شاخص‌های بالینی: +۲۰ ستاره"),
                                        "🧪" to (if (currentLang == "en") "Perform medical dosage / daily usage: +10 Stars" else "استفاده روزانه از محاسبات دوز دارویی: +۱۰ ستاره"),
                                        "👤" to (if (currentLang == "en") "Complete professional profile & docs: +200 Stars" else "تکمیل مدارک نظام دامپزشکی و رزومه: +۲۰۰ ستاره")
                                    ).forEach { (emoji, textStr) ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(emoji, fontSize = 14.sp)
                                            Text(textStr, fontSize = 11.sp, color = textColor)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = dividerColor)

                            // Perks & Rewards Section
                            Text(
                                text = if (currentLang == "en") "🎁 Redeemable Professional Rewards:" else "🎁 صندوق پاداش‌ها و جوایز فعال:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = textColor
                            )

                            // Reward 1: Diamond Subscription
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (currentLang == "en") "Unlock 30 Days Diamond Access" else "فعال‌سازی ۳۰ روز اشتراک ویژه الماس",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (currentLang == "en") "Full offline access to Plumb's & BSAVA Guides" else "دسترسی کامل آفلاین به دارونامه و محاسبات تروما",
                                            fontSize = 9.sp,
                                            color = mutedTextColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (currentLang == "en") "Requires 500 Stars" else "نیاز به ۵۰۰ ستاره علمی",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                    Button(
                                        onClick = { showRewardsDialog = false },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(if (currentLang == "en") "Redeem" else "دریافت", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Reward 2: Search Ranking Priority boost
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (currentLang == "en") "Search Result Boost (Priority Map)" else "ارتقای رتبه و ثبت اولویت در نقشه",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (currentLang == "en") "Priority placement in pet owners' searches" else "قرارگیری در صدر جستجوهای آنلاین صاحبان پت کشور",
                                            fontSize = 9.sp,
                                            color = mutedTextColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (currentLang == "en") "Requires 150 Stars" else "نیاز به ۱۵۰ ستاره علمی",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                    Button(
                                        onClick = { showRewardsDialog = false },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(if (currentLang == "en") "Redeem" else "دریافت", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Reward 3: Partner National Veterinary Congress discount
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (currentLang == "en") "50% Off National Vet Congress Ticket" else "کد تخفیف ۵۰٪ ثبت‌نام در همایش‌های تخصصی کادر درمان",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (currentLang == "en") "Sponsored discount in coordination with authorities" else "کاهش ۵۰ درصدی ورودی معتبرترین مجامع دامپزشکی ایران",
                                            fontSize = 9.sp,
                                            color = mutedTextColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (currentLang == "en") "Requires 1000 Stars" else "نیاز به ۱۰۰۰ ستاره علمی",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                    Button(
                                        onClick = { showRewardsDialog = false },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(if (currentLang == "en") "Redeem" else "دریافت", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showRewardsDialog = false }) {
                            Text(if (currentLang == "en") "Back" else "بازگشت")
                        }
                    }
                )
            }

            if (showInviteDialog) {
                AlertDialog(
                    onDismissRequest = { showInviteDialog = false },
                    title = { Text(if (currentLang == "en") "Invite Colleagues & Friends" else "دعوت از همکاران و دوستان", fontWeight = FontWeight.Bold) },
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
                            Text(if (currentLang == "en") "Help & Features Introduction" else "راهنما و معرفی قابلیت‌ها", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
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
                    title = { Text(if (currentLang == "en") "Report User Violation" else "گزارش تخلف کاربر", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = shopName,
                                onValueChange = { shopName = it },
                                label = { Text(if (currentLang == "en") "User Name/ID" else "نام یا شناسه کاربر") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = reportMsg,
                                onValueChange = { reportMsg = it },
                                label = { Text(if (currentLang == "en") "Violation Details" else "شرح جزئیات تخلف") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showReportDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
                        ) {
                            Text(if (currentLang == "en") "Send Report" else "ارسال گزارش")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReportDialog = false }) {
                            Text(if (currentLang == "en") "Cancel" else "انصراف")
                        }
                    }
                )
            }

            if (showRewardUnlockDialog) {
                AlertDialog(
                    onDismissRequest = { showRewardUnlockDialog = false },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("🎉✨🏆✨🎉", fontSize = 24.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (currentLang == "en") "Congratulations!" else "تبریک و تهنیت!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF8B5CF6),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (currentLang == "en") {
                                    "Your promo code is copied to clipboard!\n\nUse code: DIAMOND100_VET to unlock 1-Month Diamond Subscription for free in the subscription menu."
                                } else {
                                    "کد تخفیف شما با موفقیت در حافظه موقت (Clipboard) کپی شد!\n\nکد طلایی: DIAMOND100_VET\n\nمی‌توانید با ورود به بخش اشتراک‌ها، این کد را جهت دریافت یک ماه اشتراک الماس رایگان ثبت نمایید!"
                                },
                                fontSize = 13.sp,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showRewardUnlockDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Text(if (currentLang == "en") "Awesome!" else "بسیار عالی")
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
                            Text(if (currentLang == "en") "Terms & Specialty Usage Conditions" else "شرایط و قوانین استفاده تخصصی", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
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
                    title = { Text(if (currentLang == "en") "Software Update" else "بروزرسانی نرم‌افزار", fontWeight = FontWeight.Bold) },
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
                            Text(if (currentLang == "en") "PetClub Clinical Support Online" else "پشتیبانی آنلاین کادر بالینی پت‌کلاب", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
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
                            Text(if (currentLang == "en") "How satisfied are you with the app?" else "میزان رضایت شما از برنامه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
