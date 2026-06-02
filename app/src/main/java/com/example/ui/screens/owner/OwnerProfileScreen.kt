package com.example.ui.screens.owner

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel

@Composable
fun OwnerProfileScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val currentTheme by viewModel.themeMode.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var activeOwnerSection by remember { mutableStateOf("اصلی") } // "اصلی", "تنظیمات", "لینک‌ها"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Profile Card
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .border(3.dp, MaterialTheme.colorScheme.secondary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 48.sp, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Owner Name
        Text(
            text = activeSession?.fullName ?: "صاحب پت گرامی",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "شماره اکانت: ${activeSession?.phoneNumber ?: "۰۹۱۲۳۴۵۶۷۸۹"}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Divider(modifier = Modifier.padding(vertical = 20.dp))

        // Back action button if sub-section is active
        if (activeOwnerSection != "اصلی") {
            Button(
                onClick = { activeOwnerSection = "اصلی" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("بازگشت به منوی پروفایل ↩️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
            Crossfade(targetState = activeOwnerSection) { section ->
                when (section) {
                    "اصلی" -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OwnerProfileMenuItem(
                                emoji = "⚙️",
                                title = "تنظیمات برنامه (تغییر تم، زبان و ریست)",
                                onClick = { activeOwnerSection = "تنظیمات" }
                            )
                            OwnerProfileMenuItem(
                                emoji = "🔗",
                                title = "لینک‌های کاربردی و حامیان حیوانات",
                                onClick = { activeOwnerSection = "لینک‌ها" }
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.ExitToApp,
                                        contentDescription = "Logout",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "خروج از حساب کاربری",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    "تنظیمات" -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "⚙️ تنظیمات عمومی برنامه:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
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
                                Text("حالت شب برنامه (تم تاریک):", fontSize = 13.sp)
                                Switch(
                                    checked = currentTheme == "dark",
                                    onCheckedChange = { viewModel.toggleTheme() }
                                )
                            }

                            Divider()

                            // Language
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("تغییر زبان:", fontSize = 13.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        "fa" to "فارسی",
                                        "en" to "English",
                                        "ar" to "العربية"
                                    ).forEach { (code, display) ->
                                        val isChosen = currentLang == code
                                        val bg = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(bg)
                                                .clickable { viewModel.setLanguage(code) }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                display,
                                                fontSize = 10.sp,
                                                color = if (isChosen) Color.White else Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Divider()

                            // App version info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("نسخه کلاینت اندروید:", fontSize = 13.sp)
                                Text("v1.5.0", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Reset database cache for client-side cleanliness
                            Button(
                                onClick = { viewModel.resetAllData() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("پاک‌سازی کامل پایگاه داده محلی (ریست)", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }

                    "لینک‌ها" -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "🔗 لینک‌های ارتباطی و پورتال‌ها:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            listOf(
                                "سازمان حفاظت از حقوق حیوانات خانگی" to "http://www.irandogpet.ir",
                                "کلینیک جامع شبانه‌روزی حامیان پت" to "http://www.clinic-pet.ir",
                                "سامانه واکسیناسیون کشوری زئونوز" to "http://vaccine.ivo.ir"
                            ).forEach { (title, link) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🔗", fontSize = 14.sp)
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(link, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
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
fun OwnerProfileMenuItem(
    emoji: String,
    title: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
