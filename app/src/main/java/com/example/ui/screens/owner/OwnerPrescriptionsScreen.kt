package com.example.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.bounceClick
import com.example.ui.theme.StaggeredFadeInItem

@Composable
fun OwnerPrescriptionsScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val allPrescriptions by viewModel.allPrescriptions.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    val ownerPhoneNum = activeSession?.phoneNumber ?: "empty"
    // Fetch prescriptions where ownerPhone equals the user's phone number! (Real-time sync)
    val ownerPrescriptions = allPrescriptions.filter { it.ownerPhone == ownerPhoneNum }

    val layoutDirection = if (currentLang == "en") androidx.compose.ui.unit.LayoutDirection.Ltr else androidx.compose.ui.unit.LayoutDirection.Rtl
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper sync status placard
            StaggeredFadeInItem(index = 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = if (currentLang == "en") "📋 Synced Treatment Prescriptions" else "📋 نسخه‌های درمانی همگام‌سازی شده",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (currentLang == "en") "The clinical prescriptions issued by Dr. Mohaghegh for your pet are successfully loaded here, matching your mobile number. You can print them or present them to partner veterinary pharmacies." else "نسخه‌های صادر شده برای حیوان شما توسط دکتر محقق در این قسمت بر اساس مطابقت شماره تلفن همراه با موفقیت لود شده‌اند. در دسترستان است تا جهت پرینت یا ارائه به داروخانه‌های همکار ارائه کنید.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (currentLang == "en") "List of Prescribed Medicines:" else "فهرست اقلام دارویی تجویزشده دکتر:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
            )

            if (ownerPrescriptions.isEmpty()) {
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💊", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (currentLang == "en") "No active prescriptions found" else "هیچ نسخه صادر شده‌ای یافت نشد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentLang == "en") "Clinical records and veterinary prescriptions are automatically synchronized keying on your phone number. Verify that the vet has input your number accurately." else "پرونده‌های درمانی و نسخه‌ها به صورت خودکار بر اساس شماره موبایل شما همگام‌سازی می‌شوند. اطمینان حاصل کنید دامپزشک شماره شما را دقیق وارد کرده باشد.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.bounceClick {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(ownerPhoneNum))
                            }
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (currentLang == "en") "Copy Phone Number for Vet" else "کپی شماره موبایل برای ارائه به پزشک", fontSize = 11.sp)
                        }
                    }
                }
            } else {
                ownerPrescriptions.forEachIndexed { i, prescription ->
                    StaggeredFadeInItem(index = i + 1) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🧬", fontSize = 24.sp)
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = if (currentLang == "en") "Medicine: ${prescription.drugName}" else "دارو: ${prescription.drugName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (currentLang == "en") "For Pet: ${prescription.petName}" else "مخصوص پت: ${prescription.petName}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(if (currentLang == "en") "Calculated Clinical Dose:" else "دوز محاسبه‌شده بالینی:", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            text = "${String.format("%.2f", prescription.calculatedDose)} mg",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Column {
                                        Text(if (currentLang == "en") "Exact Injection Volume:" else "حجم دقیق تجویز تزریقی:", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            text = "${String.format("%.2f", prescription.calculatedVolume)} ml (cc)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == "en") "Usage Note & Instructions: This concentration is precisely computed according to your pet's body weight. Please do not modify doses without veterinary guidance." else "توضیحات و دستورالعمل مصرف: این غلظت دقیقاً برای وزن پت شما محاسبه شده است. لطفا بدون مشورت پزشک مقدار دوز را تغییر ندهید.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(onClick = { /* Print handle */ }) {
                                            Text("🖨️", fontSize = 16.sp)
                                        }
                                        IconButton(onClick = { /* Share handle */ }) {
                                            Icon(
                                                Icons.Default.Share,
                                                contentDescription = if (currentLang == "en") "Share Prescription" else "ارسال نسخه به همراه",
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (currentLang == "en") "Treating Doctor: Dr. Mohaghegh" else "پزشک معالج: دکتر محقق",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
