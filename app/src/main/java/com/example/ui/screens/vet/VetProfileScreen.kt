package com.example.ui.screens.vet

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import com.example.data.database.Prescription
import com.example.viewmodel.MainViewModel

@Composable
fun VetProfileScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val allPrescriptions by viewModel.allPrescriptions.collectAsState()
    val currentTheme by viewModel.themeMode.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var activeProfileSection by remember { mutableStateOf("اصلی") } // "اصلی", "نسخه‌ها", "تنظیمات", "لینک‌ها", "منابع"

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
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🩺", fontSize = 48.sp, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Doctor Name / ID
        Text(
            text = activeSession?.fullName ?: "دکتر محقق دامپزشک",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "کد پروانه پرشکی: ${activeSession?.identification?.ifEmpty { "شماره دانشجویی" } ?: "۹۹۰۱۵۴۳"}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        activeSession?.specialty?.ifEmpty { "" }?.let { specialty ->
            if (specialty.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = specialty,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 20.dp))

        // Back action button if sub-section is active
        if (activeProfileSection != "اصلی") {
            Button(
                onClick = { activeProfileSection = "اصلی" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("بازگشت به منوی پروفایل ↩️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
            Crossfade(targetState = activeProfileSection) { section ->
                when (section) {
                    "اصلی" -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            ProfileMenuItem(
                                emoji = "📋",
                                title = "نسخه‌های دارویی ذخیره‌شده (${allPrescriptions.size} نسخه)",
                                onClick = { activeProfileSection = "نسخه‌ها" }
                            )
                            ProfileMenuItem(
                                emoji = "⚙️",
                                title = "تنظیمات برنامه (تغییر تم، زبان و ریست)",
                                onClick = { activeProfileSection = "تنظیمات" }
                            )
                            ProfileMenuItem(
                                emoji = "📚",
                                title = "فهرست رفرنس‌های علمی معتبر",
                                onClick = { activeProfileSection = "منابع" }
                            )
                            ProfileMenuItem(
                                emoji = "🔗",
                                title = "لینک‌های کاربردی و سازمان‌های همکار",
                                onClick = { activeProfileSection = "لینک‌ها" }
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

                    "نسخه‌ها" -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "📝 نسخه‌های دارویی صادر شده:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            if (allPrescriptions.isEmpty()) {
                                Text(
                                    text = "هنوز هیچ نسخه دارویی تولید و ثبت مکتوب نشده است.",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                                )
                            } else {
                                allPrescriptions.forEach { prescription ->
                                    PrescriptionCard(prescription = prescription, onDelete = {
                                        viewModel.deletePrescription(prescription)
                                    })
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

                    "منابع" -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "📚 منابع علمی و رفرنس‌های دارونامه:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            listOf(
                                "Plumb's Veterinary Drug Handbook" to "کتابچه جامع راهنمای دارویی دامپزشکی پلانتون - ویرایش دهم مرجع دوزهای استاندارد کلینیکال جهت محاسبات.",
                                "Merck Veterinary Manual (دارونامه اگزوتیک)" to "مرجع علمی پاتولوژی و دستورالعمل‌های درمانی ورم سینه گاو، درمان برونشیت سگ و کلامیدوز زیکارها.",
                                "BSAVA Small Animal Formulary" to "فرمولاسیون دارویی جامع آکادمی دامپزشکی حیوانات کوچک بریتانیا جهت تخمین حجم‌های دقیق."
                            ).forEach { (title, description) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
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
                                "سازمان دامپزشکی کشور" to "http://www.ivo.ir",
                                "سازمان نظام دامپزشکی کشور" to "http://www.iranveterinary.com",
                                "سامانه بازرسی داروهای دامی" to "http://pharmacy.ivo.ir"
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
fun ProfileMenuItem(
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

@Composable
fun PrescriptionCard(prescription: Prescription, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
                Text(
                    text = "دارو: ${prescription.drugName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("جهت تجویز برای پت: ${prescription.petName}", fontSize = 11.sp)
            Text("موبایل صاحب پت جهت تبادل: ${prescription.ownerPhone.ifEmpty { "ثبت نشده" }}", fontSize = 11.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("دوز: ${String.format("%.2f", prescription.calculatedDose)} mg", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("حجم: ${String.format("%.2f", prescription.calculatedVolume)} ml (cc)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = { /* Print handle */ }) {
                    Text("🖨️", fontSize = 16.sp)
                }
                IconButton(onClick = { /* Share prescription sms */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}
