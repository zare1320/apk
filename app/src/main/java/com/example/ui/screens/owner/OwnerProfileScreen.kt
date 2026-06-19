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
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

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
    var showLogoutDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableStateOf(5) }

    // Interactivity state copies
    var editedName by remember { mutableStateOf("صاحب پت گرامی") }
    var editedPhone by remember { mutableStateOf("۰۹۱۲۳۴۵۶۷۸۹") }
    var editedGender by remember { mutableStateOf("آقا") }
    var showOwnerRewardUnlockDialog by remember { mutableStateOf(false) }
    var showAddPetDialog by remember { mutableStateOf(false) }
    var showPromoCodeCard by remember { mutableStateOf(true) }

    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            editedName = activeSession?.fullName ?: "صاحب پت گرامی"
            editedPhone = activeSession?.phoneNumber ?: "۰۹۱۲۳۴۵۶۷۸۹"
            editedGender = activeSession?.gender ?: "آقا"
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
                            "تنظیمات" -> if (currentLang == "en") "App Settings" else "تنظیمات برنامه"
                            "لینک‌ها" -> if (currentLang == "en") "Useful & Portable Links" else "لینک های کاربردی و پورتابل"
                            "منابع" -> if (currentLang == "en") "Scientific Resources" else "منابع علمی"
                            else -> if (currentLang == "en") "My Account" else "حساب من"
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
                                                    text = if (editedName == "صاحب پت گرامی") { if (currentLang == "en") "Dear Pet Owner" else "صاحب پت گرامی" } else editedName,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = if (editedPhone == "۰۹۱۲۳۴۵۶۷۸۹") { if (currentLang == "en") "09123456789" else "۰۹۱۲۳۴۵۶۷۸۹" } else editedPhone,
                                                    fontSize = 12.sp,
                                                    color = mutedTextColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (isDark) Color(0xFF3F2B30) else Color(0xFFFEE2E2), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (currentLang == "en") {
                                                            "Gender: ${if (editedGender == "خانم") "Female" else "Male"}"
                                                         } else {
                                                             "جنسیت: ${if (editedGender == "خانم") "خانم" else "آقا"}"
                                                         },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFEF4444)
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

                                 Spacer(modifier = Modifier.height(12.dp))

                                 // Promo code reward card for completed profile
                                 if (showPromoCodeCard) {
                                 Card(
                                     shape = RoundedCornerShape(16.dp),
                                     colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF3B1E2B) else Color(0xFFFFF1F2)),
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .padding(bottom = 16.dp)
                                         .border(
                                             BorderStroke(1.5.dp, androidx.compose.ui.graphics.Brush.linearGradient(
                                                 listOf(Color(0xFFF43F5E), Color(0xFFD946EF), Color(0xFF8B5CF6))
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
                                             Column(modifier = Modifier.weight(1f)) {
                                                 Text(
                                                     text = if (currentLang == "en") "Diamond Code Unlocked!" else "کد هدیه اشتراک الماس فعال شد!",
                                                     fontSize = 14.sp,
                                                     fontWeight = FontWeight.Bold,
                                                     color = textColor
                                                 )
                                                 Text(
                                                     text = if (currentLang == "en") "Completed profile bonus reward" else "پاداش تکمیل اطلاعات سرپرستی حیوانات",
                                                     fontSize = 11.sp,
                                                     color = mutedTextColor
                                                 )
                                             }
                                             IconButton(
                                                 onClick = { showPromoCodeCard = false },
                                                 modifier = Modifier.size(24.dp)
                                             ) {
                                                 Icon(
                                                     imageVector = Icons.Default.Close,
                                                     contentDescription = "Ignore / Dismiss",
                                                     tint = mutedTextColor,
                                                     modifier = Modifier.size(16.dp)
                                                 )
                                             }
                                         }
                                         Spacer(modifier = Modifier.height(10.dp))
                                         Text(
                                             text = if (currentLang == "en") {
                                                 "Since you have completed your user profile information, you can use the discount code below to claim a one-month Diamond subscription 100% free!"
                                             } else {
                                                 "به پاس قدردانی از تکمیل مشخصات کاربر و اطلاعات تبارشناسی پت‌، می‌توانید از کد تخفیف اختصاصی زیر جهت فعال‌سازی یک ماه اشتراک ممتاز الماس به صورت کاملاً رایگان استفاده نمایید!"
                                             },
                                             fontSize = 11.sp,
                                             color = textColor.copy(alpha = 0.85f),
                                             lineHeight = 16.sp
                                         )
                                         Spacer(modifier = Modifier.height(12.dp))

                                         val context = androidx.compose.ui.platform.LocalContext.current
                                         val promoCode = "DIAMOND100_OWNER"
                                         Row(
                                             horizontalArrangement = Arrangement.spacedBy(8.dp),
                                             verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             Button(
                                                 onClick = {
                                                     val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                     val clipData = android.content.ClipData.newPlainText("Vetaris Owner Diamond Promo Code", promoCode)
                                                     clipboardManager.setPrimaryClip(clipData)
                                                     showOwnerRewardUnlockDialog = true
                                                 },
                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
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

                                             TextButton(
                                                 onClick = { showPromoCodeCard = false }
                                             ) {
                                                 Text(
                                                     text = if (currentLang == "en") "Ignore" else "نادیده گرفتن",
                                                     fontSize = 11.sp,
                                                     fontWeight = FontWeight.SemiBold,
                                                     color = if (isDark) Color(0xFFF43F5E) else Color(0xFFEF4444)
                                                  )
                                             }
                                         }
                                     }
                                 }
                                 }

                                 // My Pets Section Card
                                 val allPets by viewModel.allPets.collectAsState()
                                 Card(
                                     shape = RoundedCornerShape(16.dp),
                                     colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .padding(bottom = 16.dp)
                                         .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                 ) {
                                     Column(
                                         modifier = Modifier.padding(16.dp)
                                     ) {
                                         Row(
                                             modifier = Modifier.fillMaxWidth(),
                                             horizontalArrangement = Arrangement.SpaceBetween,
                                             verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                 Text("🐾", fontSize = 18.sp)
                                                 Text(
                                                     text = if (currentLang == "en") "My Registered Pets" else "شناسنامه حیوانات من",
                                                     fontSize = 14.sp,
                                                     fontWeight = FontWeight.Bold,
                                                     color = Color(0xFFEF4444)
                                                 )
                                             }
                                             
                                             // Add Pet button inside main panel
                                             Button(
                                                 onClick = { showAddPetDialog = true },
                                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                 contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                 shape = RoundedCornerShape(8.dp),
                                                 modifier = Modifier.height(30.dp)
                                             ) {
                                                 Icon(
                                                     imageVector = Icons.Default.Add,
                                                     contentDescription = "Add Pet",
                                                     tint = Color.White,
                                                     modifier = Modifier.size(14.dp)
                                                 )
                                                 Spacer(modifier = Modifier.width(4.dp))
                                                 Text(
                                                     text = if (currentLang == "en") "Add Pet" else "افزودن پت",
                                                     fontSize = 11.sp,
                                                     fontWeight = FontWeight.Bold,
                                                     color = Color.White
                                                 )
                                             }
                                         }
                                         
                                         Spacer(modifier = Modifier.height(12.dp))

                                         val ownerPets = allPets.filter { it.ownerPhone == editedPhone || it.ownerPhone.isEmpty() }
                                         
                                         if (ownerPets.isEmpty()) {
                                             Box(
                                                 modifier = Modifier
                                                     .fillMaxWidth()
                                                     .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                                     .padding(16.dp),
                                                 contentAlignment = Alignment.Center
                                             ) {
                                                 Text(
                                                     text = if (currentLang == "en") "No pets registered yet. Click 'Add Pet' above!" else "هنوز هیچ پتی ثبت نکرده‌اید. روی دکمه افزودن پت کلیک کنید!",
                                                     fontSize = 11.sp,
                                                     color = mutedTextColor,
                                                     textAlign = TextAlign.Center
                                                 )
                                             }
                                         } else {
                                             Column(
                                                 verticalArrangement = Arrangement.spacedBy(8.dp),
                                                 modifier = Modifier.fillMaxWidth()
                                             ) {
                                                 ownerPets.forEach { pet ->
                                                     val petIcon = when (pet.species.lowercase()) {
                                                         "dog", "سگ" -> "🐕"
                                                         "cat", "گربه" -> "🐈"
                                                         "bird", "پرنده" -> "🦜"
                                                         "rodent", "همستر", "خرگوش" -> "🐇"
                                                         "fish", "آبزی" -> "🐠"
                                                         else -> "🐾"
                                                     }
                                                     
                                                     Row(
                                                         modifier = Modifier
                                                             .fillMaxWidth()
                                                             .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                                             .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                                             .padding(12.dp),
                                                         verticalAlignment = Alignment.CenterVertically,
                                                         horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                     ) {
                                                         Text(petIcon, fontSize = 28.sp)
                                                         
                                                         Column(
                                                             modifier = Modifier.weight(1f)
                                                         ) {
                                                             Text(
                                                                 text = pet.name,
                                                                 fontSize = 14.sp,
                                                                 fontWeight = FontWeight.Bold,
                                                                 color = textColor
                                                             )
                                                             Spacer(modifier = Modifier.height(2.dp))
                                                             Text(
                                                                 text = if (currentLang == "en") {
                                                                     "Species: ${pet.species} | Breed: ${pet.breed} | Age: ${pet.age}"
                                                                 } else {
                                                                     "گونه: ${pet.species} | نژاد: ${pet.breed} | سن: ${pet.age}"
                                                                 },
                                                                 fontSize = 11.sp,
                                                                 color = mutedTextColor
                                                             )
                                                             Spacer(modifier = Modifier.height(2.dp))
                                                             Text(
                                                                 text = if (currentLang == "en") {
                                                                     "Gender: ${if (pet.gender == "ماده") "Female ♀" else "Male ♂"} | Weight: ${pet.weight} kg"
                                                                 } else {
                                                                     "جنسیت: ${if (pet.gender == "ماده") "ماده ♀" else "نر ♂"} | وزن: ${pet.weight} کیلوگرم"
                                                                 },
                                                                 fontSize = 11.sp,
                                                                 color = mutedTextColor
                                                             )
                                                         }
                                                     }
                                                 }
                                             }
                                         }
                                     }
                                 }

                                 Spacer(modifier = Modifier.height(8.dp))

                                 // 2. Account Management Category Grouping
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
                                        OwnerProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Password Management" else "رمز عبور",
                                            iconEmoji = "🔒",
                                            onClick = { showPasswordDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Connected Devices" else "دستگاه‌های متصل",
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
                                            "free" -> if (currentLang == "en") "Free" else "رایگان"
                                            "silver" -> if (currentLang == "en") "Silver (3-Month)" else "نقره‌ای (۳ ماهه)"
                                            "diamond" -> if (currentLang == "en") "Diamond (1-Year)" else "الماس (یکساله)"
                                            else -> if (currentLang == "en") "Gold (6-Month)" else "طلایی (۶ ماهه)"
                                        }
                                        OwnerProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Subscription: $subTitle $subEmoji" else "مدیریت اشتراک: $subTitle $subEmoji",
                                            iconEmoji = "🛡️",
                                            onClick = { showSubscriptionDialog = true }
                                        )
                                    }
                                }

                                // 3. Support & Utils Grouping
                                Text(
                                    text = if (currentLang == "en") "Support & Options" else "پشتیبانی و امکانات",
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
                                            title = if (currentLang == "en") "Help & About Us" else "راهنما و درباره ما",
                                            iconEmoji = "❓",
                                            onClick = { showHelpDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Terms & Conditions" else "شرایط و مقررات استفاده",
                                            iconEmoji = "📄",
                                            onClick = { showTermsDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Scientific Resources" else "منابع علمی",
                                            iconEmoji = "📖",
                                            onClick = { activeOwnerSection = "منابع" }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Useful & Portable Links" else "لینک های کاربردی و پورتابل",
                                            iconEmoji = "🔗",
                                            onClick = { activeOwnerSection = "لینک‌ها" }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Report User Violation" else "گزارش تخلف کاربر",
                                            iconEmoji = "⚠️",
                                            onClick = { showReportDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Online Support" else "پشتیبانی انلاین",
                                            iconEmoji = "💬",
                                            onClick = { showSupportDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "Database & App Update" else "بروزرسانی پایگاه داده و برنامه",
                                            iconEmoji = "🔄",
                                            onClick = { showUpdateDialog = true }
                                        )
                                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                        OwnerProfileMenuItemRedesigned(
                                            title = if (currentLang == "en") "General Settings" else "تنظیمات عمومی",
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
                                                    if (currentLang == "en") "Notifications Activated" else "اعلام‌ها فعال شد",
                                                    if (currentLang == "en") "You will receive real-time vaccine checks and clinical alerts!" else "از این پس پیام‌ها و هشدارهای واکسیناسیون و دستیار پزشکی ارسال خواهند شد!"
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
                                            Column(horizontalAlignment = Alignment.Start) {
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
                var genderInput by remember { mutableStateOf(editedGender) }
                // Clear any social credential placeholder email or dummy login from phone so they can complete it
                var phoneInput by remember { 
                    mutableStateOf(if (editedPhone.contains("@") || editedPhone.startsWith("سریع با")) "" else editedPhone) 
                }
                AlertDialog(
                    onDismissRequest = { showEditProfileDialog = false },
                    title = { Text(if (currentLang == "en") "Edit Profile Information" else "ویرایش اطلاعات حساب کاربری", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text(if (currentLang == "en") "Full Name" else "نام و نام خانوادگی") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text(if (currentLang == "en") "Mobile Number" else "شماره همراه") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentLang == "en") "Owner Gender:" else "جنسیت صاحب حیوان خانگی:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = mutedTextColor
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val genders = listOf("آقا", "خانم")
                                genders.forEach { g ->
                                    val isSelected = genderInput == g
                                    Button(
                                        onClick = { genderInput = g },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) Color(0xFFEF4444) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                            contentColor = if (isSelected) Color.White else textColor
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = if (g == "آقا") {
                                                if (currentLang == "en") "Male 👨" else "آقا 👨"
                                            } else {
                                                if (currentLang == "en") "Female 👩" else "خانم 👩"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
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
                                editedGender = genderInput
                                viewModel.updateSession(
                                    fullName = nameInput,
                                    phoneNumber = phoneInput,
                                    gender = genderInput
                                )
                                showEditProfileDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
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

            if (showOwnerRewardUnlockDialog) {
                AlertDialog(
                    onDismissRequest = { showOwnerRewardUnlockDialog = false },
                    title = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("🎉✨🐈✨🎉", fontSize = 24.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (currentLang == "en") "Congratulations!" else "تبریک و تهنیت!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFFEF4444),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    text = {
                        Text(
                            text = if (currentLang == "en") {
                                "Your promo code is copied to clipboard!\n\nUse code: DIAMOND100_OWNER to unlock 1-Month Diamond Subscription for free in the subscription menu."
                            } else {
                                "کد تخفیف شما با موفقیت در حافظه موقت (Clipboard) کپی شد!\n\nکد طلایی: DIAMOND100_OWNER\n\nمی‌توانید با ورود به بخش اشتراک‌ها، این کد را جهت دریافت یک ماه اشتراک الماس رایگان ثبت نمایید!"
                            },
                            fontSize = 13.sp,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { showOwnerRewardUnlockDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text(if (currentLang == "en") "Awesome!" else "بسیار عالی")
                        }
                    }
                )
            }

            if (showAddPetDialog) {
                var petNameInput by remember { mutableStateOf("") }
                var petBreedInput by remember { mutableStateOf("") }
                var petAgeInput by remember { mutableStateOf("") }
                var petWeightInput by remember { mutableStateOf("") }
                var petGenderInput by remember { mutableStateOf("نر") } // "نر" (Male) or "ماده" (Female)
                var petSpeciesInput by remember { mutableStateOf("Cat") } // "Cat", "Dog", etc.
                var errorMessage by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showAddPetDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🐾", fontSize = 20.sp)
                            Text(
                                text = if (currentLang == "en") "Add New Pet Details" else "ثبت شناسنامه و مشخصات پت جدید",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()
                        ) {
                            // Pet Name
                            OutlinedTextField(
                                value = petNameInput,
                                onValueChange = { petNameInput = it },
                                label = { Text(if (currentLang == "en") "Pet Name" else "نام حیوان خانگی") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Species Choice
                            Text(
                                text = if (currentLang == "en") "Species:" else "نوع/گونه حیوان:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = mutedTextColor
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val speciesList = listOf(
                                    "Cat" to "🐈",
                                    "Dog" to "🐕",
                                    "Bird" to "🦜",
                                    "Rodent" to "🐇",
                                    "Other" to "🐾"
                                )
                                speciesList.forEach { (spCode, spEmoji) ->
                                    val isSelected = petSpeciesInput == spCode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFFEF4444) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)))
                                            .clickable { petSpeciesInput = spCode }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(spEmoji, fontSize = 18.sp)
                                            Text(
                                                text = if (currentLang == "en") spCode else {
                                                    when(spCode) {
                                                        "Cat" -> "گربه"
                                                        "Dog" -> "سگ"
                                                        "Bird" -> "پرنده"
                                                        "Rodent" -> "جونده"
                                                        else -> "سایر"
                                                    }
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else textColor
                                            )
                                        }
                                    }
                                }
                            }

                            // Breed
                            OutlinedTextField(
                                value = petBreedInput,
                                onValueChange = { petBreedInput = it },
                                label = { Text(if (currentLang == "en") "Breed (e.g. Persian, DSH...)" else "نژاد پت (مثلاً پرشین، ژرمن...)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Age & Weight Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = petAgeInput,
                                    onValueChange = { petAgeInput = it },
                                    label = { Text(if (currentLang == "en") "Age (e.g., 2 years)" else "سن (مثلاً ۲ ساله)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.5f)
                                )
                                OutlinedTextField(
                                    value = petWeightInput,
                                    onValueChange = { petWeightInput = it },
                                    label = { Text(if (currentLang == "en") "Weight (kg)" else "وزن (کیلوگرم)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Gender Choice
                            Text(
                                text = if (currentLang == "en") "Pet Gender:" else "جنسیت حیوان:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = mutedTextColor
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val petGenders = listOf("نر", "ماده")
                                petGenders.forEach { g ->
                                    val isSelected = petGenderInput == g
                                    Button(
                                        onClick = { petGenderInput = g },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) Color(0xFFEF4444) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                            contentColor = if (isSelected) Color.White else textColor
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (g == "نر") {
                                                if (currentLang == "en") "Male ♂" else "نر ♂"
                                            } else {
                                                if (currentLang == "en") "Female ♀" else "ماده ♀"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (errorMessage.isNotEmpty()) {
                                Text(errorMessage, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (petNameInput.isBlank() || petBreedInput.isBlank() || petAgeInput.isBlank() || petWeightInput.isBlank()) {
                                    errorMessage = if (currentLang == "en") "Please fill all fields!" else "لطفاً تمامی فیلدها را تکمیل کنید!"
                                    return@Button
                                }
                                val dWeight = petWeightInput.toDoubleOrNull()
                                if (dWeight == null) {
                                    errorMessage = if (currentLang == "en") "Weight must be a number!" else "وزن باید عددی معتبر باشد!"
                                    return@Button
                                }
                                
                                val newPet = com.example.data.database.Pet(
                                    name = petNameInput,
                                    species = petSpeciesInput,
                                    breed = petBreedInput,
                                    weight = dWeight,
                                    age = petAgeInput,
                                    gender = petGenderInput,
                                    ownerPhone = editedPhone,
                                    ownerName = editedName
                                )

                                viewModel.addNewPatient(newPet)
                                showAddPetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text(if (currentLang == "en") "Save Pet" else "ثبت و تایید پت")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddPetDialog = false }) {
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
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
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF7FAFC)),
                                modifier = Modifier.fillMaxWidth().border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(if (currentLang == "en") "Active (This Device)" else "فعال (این دستگاه)", color = Color(0xFF48BB78), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text("Xiaomi Redmi 12", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                        Text(if (currentLang == "en") "Last Activity: Now" else "آخرین فعالیت: هم‌اکنون", fontSize = 11.sp, color = mutedTextColor)
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
                            Text(if (currentLang == "en") "OK" else "تایید")
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
                            Text("🐾", fontSize = 22.sp)
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
                                    Text("درباره پورتال سرپرستان پت‌کلاب", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEF4444))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "سامانه هوشمند پت‌کلاب ویژه سرپرستان دلسوز، ابزاری جامع جهت پایش روزانه سلامت، واکسیناسیون سازمان‌یافته، مشاهده نقشه داروخانه‌ها و کلینیک‌های حیوانات و بایگانی ایمنِ نسخه‌های تجویزی پزشکان می‌باشد. (نسخه v1.5.0)",
                                        fontSize = 11.sp,
                                        color = textColor,
                                        lineHeight = 17.sp
                                    )
                                }
                            }

                            Text("🚀 قابلیت‌ها و ابزارهای حامی پت شما:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)

                            // Item 1: Health Dashboard
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
                                    Text("🏡", fontSize = 18.sp)
                                    Column {
                                        Text("داشبورد سلامت و پایش وضعیت عمومی", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("در صفحه اصلی، وضعیت روزانه روحیه، بهداشت دندان، نظافت مو زنی و عقیم سازی پت خود را بررسی کنید و از نکات و مقالات مراقبتی روزانه بهره‌مند شوید.", fontSize = 11.sp, color = mutedTextColor, lineHeight = 16.sp)
                                    }
                                }
                            }

                            // Item 2: Calendar & Vaccine Scheduler
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
                                    Text("📅", fontSize = 18.sp)
                                    Column {
                                        Text("تقویم هوشمند درمان و واکسن‌ها", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("سازماندهی کامل دوره‌های انگل‌زدایی استاندارد، واکسن‌های سالانه، مراجعات دندان‌پزشکی و آرایشگاهی نوبت بعدی بدون نگرانی از فراموشی.", fontSize = 11.sp, color = mutedTextColor, lineHeight = 16.sp)
                                    }
                                }
                            }

                            // Item 3: Maps & Clinics Directory
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
                                    Text("📍", fontSize = 18.sp)
                                    Column {
                                        Text("موقیعت‌یاب و نقشه هوشمند پت‌سنترها", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("مسیریابی یکپارچه و فیلتر شده مراجع، داروخانه‌ها و کلینیک‌های معتبر دولتی و خصوصی پیرامون شما همراه با جزئیات دقیق شماره تماس و مشاوره.", fontSize = 11.sp, color = mutedTextColor, lineHeight = 16.sp)
                                    }
                                }
                            }

                            // Item 4: Prescription Ledger
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
                                    Text("🩺", fontSize = 18.sp)
                                    Column {
                                        Text("پرونده و بایگانی لایو نسخه‌ها", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("دریافت مستقیم نسخه‌های درمانی صادر شده توسط دامپزشک پت‌کلاب، ثبت مجزای پرونده‌ها و دوز داروهای خانگی جهت شفافیت دوره نقاهت بالینی حیوان.", fontSize = 11.sp, color = mutedTextColor, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showHelpDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
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

            if (showTermsDialog) {
                AlertDialog(
                    onDismissRequest = { showTermsDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📄", fontSize = 22.sp)
                            Text("شرایط و قوانین ویژه سرپرستان", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
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
                                text = "لطفاً برای مراقبت از پت دلبندتان و بهره‌مندی ایمن از خدمات جامع پت‌کلاب، مفاد استفاده زیر را مطالعه فرمایید:",
                                fontSize = 12.sp,
                                color = textColor,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium
                            )

                            // Item 1: Virtual vs Real Clinic Visit
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
                                    Text("🏠", fontSize = 18.sp)
                                    Column {
                                        Text("عدم جایگزینی معاینه حضوری و تخصصی", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("ابزارهای مراقبی نظافتی، بررسی بهداشت دندان و مو، پایش روحی روزانه و تقویم واکسیناسیون پت‌کلاب، ابزارهای پیشگیرانه خودمراقبتی هستند. ثبت این پارامترها هیچ‌گونه مجوزی برای نادیده گرفتن ویزیت دوره‌ای یا اورژانسی حیوان در کلینیک‌های مجاز نیست.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = mutedTextColor, lineHeight = 19.sp)
                                    }
                                }
                            }

                            // Item 2: Safe Drug Application
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
                                    Text("💊", fontSize = 18.sp)
                                    Column {
                                        Text("مسئولیت خطیر مصرف خودسرانه داروها", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("سرپرست پت موظف است داروها، مکمل‌ها و فرآورده‌های بهداشتی ذخیره شده در بایگانی نسخه‌های خود را دقیقاً بر اساس دوز اعلامی پزشک و تحت نظارت کلینیکال به حیوان بدهد. هرگونه درمان آزمایشی و خودسرانه مسولیت‌زا بوده و برای پت خطرناک است.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = mutedTextColor, lineHeight = 19.sp)
                                    }
                                }
                            }

                            // Item 3: Maps and Contact Verification
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
                                    Text("📍", fontSize = 18.sp)
                                    Column {
                                        Text("راستی‌آزمایی نقشه و موقعیت کلوپ‌ها", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("مسیریابی داروخانه‌ها، کلینیک‌ها و بیمارستان‌های شبانه‌روزی بر روی نقشه هوشمند پت‌کلاب بر اساس داده‌های خدمات مکان‌محور باز ارائه می‌شود. با توجه به تغییرات جوی یا ساعت‌های کاری، حتماً پیش از عزیمت فیزیکی با مرکز مربوطه هماهنگی تلفنی انجام دهید.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = mutedTextColor, lineHeight = 19.sp)
                                    }
                                }
                            }

                            // Item 4: Pet Memory & Data Security
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
                                    Text("🔐", fontSize = 18.sp)
                                    Column {
                                        Text("حفاظت جامع از پرونده سلامتی پت", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("تمام اطلاعات ثبت‌شده مانند تقویم درمانِ نوبت بعدی، جزئیات روحی حیوان و پرونده بیماری‌های بایگانی‌شده، به صورت محلی در حافظه امن برنامه کش می‌شوند تا از هرگونه دسترسی غیرمجاز متفرقه به حریم خصوصی پت دلبندتان ممانعت شود.", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = mutedTextColor, lineHeight = 19.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showTermsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text("قوانین را می‌پذیرم", fontWeight = FontWeight.Bold)
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
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💬", fontSize = 22.sp)
                            Text("پشتیبانی همه‌جانبه سرپرستان پت", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
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
                                text = "کارشناسان بخش فنی و تیم پشتیبانی روی خط پت‌کلاب جهت پاسخ به سوالات شما، ثبت پیشنهادات و حل سریع مشکلات کاربری، به صورت ۲۴ ساعته در کنار شما سرپرست دلسوز خواهند بود:",
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
                                        Text("پشتیبانی فعال در واتساپ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("مشاوره کاربری، همفکری و چت سریع", fontSize = 11.sp, color = mutedTextColor)
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
                                        Text("کانال و پشتیبانی تلگرام", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("ارسال مستقیم مستندات، عکس‌ها و خطاها", fontSize = 11.sp, color = mutedTextColor)
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
                                    .clickable { launchIntent("mailto:support@petclub.ir?subject=پشتیبانی سرپرستان پت‌کلاب") }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("✉️", fontSize = 24.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("مکاتبه مستقیم با ایمیل فنی", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("ارائه بازخوردهای ساختاری و توسعه سیستم", fontSize = 11.sp, color = mutedTextColor)
                                    }
                                    Text("◀", fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showSupportDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
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
                                text = "کاربر گرامی، خوشحال می‌شویم پیش از خروج، میزان رضایت خود را اعلام کنید تا در بروزرسانی‌های بعدی مورد استفاده قرار گیرد:",
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
                                    Text(if (currentLang == "en") "Rate on Google Play & Logout" else "ثبت در گوگل پلی و خروج", fontWeight = FontWeight.Bold)
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
                                    Text(if (currentLang == "en") "Logout without rating" else "خروج بدون ثبت نظر", color = Color(0xFFEF4444), fontSize = 12.sp)
                                }
                                
                                TextButton(
                                    onClick = { showLogoutDialog = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (currentLang == "en") "Cancel" else "انصراف", color = textColor.copy(alpha = 0.6f), fontSize = 12.sp)
                                }
                            }
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
                                             text = if (currentLang == "en") "e.g., DIAMOND100_OWNER" else "مثال: DIAMOND100_OWNER",
                                             fontSize = 11.sp
                                         ) 
                                     },
                                     singleLine = true,
                                     modifier = Modifier.weight(1f),
                                     shape = RoundedCornerShape(8.dp),
                                     colors = OutlinedTextFieldDefaults.colors(
                                         focusedBorderColor = Color(0xFFEF4444),
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
                                     colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text(if (currentLang == "en") "Confirm & Activate" else "تایید و فعال‌سازی", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSubscriptionDialog = false }) {
                            Text(if (currentLang == "en") "Cancel" else "انصراف")
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
