package com.example.ui.screens.vet.calculators

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.example.data.database.Pet

@Composable
fun FluidTherapyCalculator(
    activePet: Pet?,
    selectedSpecies: String? = null,
    currentLang: String = "en"
) {
    val initWeight = activePet?.weight?.toString() ?: ""
    val isDog = if (activePet != null) {
        activePet.species.lowercase() != "cat"
    } else {
        selectedSpecies?.lowercase() != "cat"
    }

    // 1. Enter Weight - Select Species States
    var weightKgInput by remember(initWeight) { mutableStateOf(initWeight.ifEmpty { "1" }) }
    var weightLbsInput by remember(weightKgInput) {
        val kgVal = weightKgInput.toDoubleOrNull()
        mutableStateOf(if (kgVal != null) String.format("%.2f", kgVal * 2.20462) else "")
    }

    // 2. Set Fluid Volume/Duration/Rate States
    var fluidVolumeSelected by remember { mutableStateOf("250 ml") }
    var fluidTimeHrs by remember { mutableStateOf("24") }
    var dripRateGttSelected by remember { mutableStateOf("10 ggt/ml") }

    // 3. Add any Fluid Deficit (Dehydration) States
    var addFluidDeficit by remember { mutableStateOf(false) }
    var dehydrationPctSlider by remember { mutableStateOf(5f) } // default 5%

    // 4. Add any Ongoing Losses States
    var addOngoingLosses by remember { mutableStateOf(false) }
    var ongoingLossesMlsSlider by remember { mutableStateOf(100f) } // default 100ml
    var replaceTimeHrs by remember { mutableStateOf("4") } // time to replace deficit and losses

    // Expandable IV Drip Rate Calculator State
    var showDripRateCalculator by remember { mutableStateOf(false) }

    // Interactive Dosage Fields inside Result Sections
    var maintenanceDosageCoeff by remember(isDog) { mutableStateOf(if (isDog) "132" else "80") }
    var resuscitationDosage by remember { mutableStateOf("20") }
    var surgicalDosage by remember { mutableStateOf("5") }
    var hetastarchDosage by remember { mutableStateOf("10") }
    var vetstarchDosage by remember { mutableStateOf("20") }
    var hypertonicSalineDosage by remember { mutableStateOf("4") }

    // IV Drip Rate state fields
    var dripVolumeInput by remember { mutableStateOf("100") }
    var dropFactorSelected by remember { mutableStateOf("15 gtts/ml") }
    var dripTimeMinutes by remember { mutableStateOf("30") }

    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val themeCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.White
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface
    
    val goldBgColor = if (isDark) Color(0xFF5F370E) else Color(0xFFFEF3C7)
    val goldTextColor = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
    val goldBorderColor = if (isDark) Color(0xFFD97706) else Color(0xFFF59E0B)

    val blueBgColor = if (isDark) Color(0xFF162E6B) else Color(0xFFDBEAFE)
    val blueTextColor = if (isDark) Color(0xFFADC8FF) else Color(0xFF1E40AF)

    val bannerBgColor = if (isDark) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else Color(0xFFF3F4F6)
    val bannerTextColor = if (isDark) MaterialTheme.colorScheme.onSecondaryContainer else Color.DarkGray

    val bulletColor = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFFC7D2FE)

    // Let's do calculations in layout matching language direction
    val layoutDir = if (currentLang == "fa") androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Input Container Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // STEP 1 Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF4B5563), RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == "fa") "وارد کردن وزن حیوان" else "Enter animal weight",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (activePet != null) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = if (currentLang == "fa") "🔒 ${activePet.name} (اطلاعات ثبت شده)" else "🔒 ${activePet.name} (Registered)",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (currentLang == "fa") {
                            "این وزن بر اساس وزن ثبت شده در پرونده بیمار در داشبورد است. در صورتی که پرونده‌ای را باز نکرده باشید، به طور پیش‌فرض روی ۱ کیلوگرم قرار می‌گیرد."
                        } else {
                            "This weight is based on the weight recorded in the pet's file on the dashboard. If no file is open, it defaults to 1 kg."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    // STEP 1 Inputs
                    val currentKg = weightKgInput.toDoubleOrNull() ?: 0.0
                    val showWeightWarning = currentKg > 0.0 && ((!isDog && currentKg > 12.0) || (isDog && currentKg > 80.0))
                    val weightBorderColors = if (showWeightWarning) {
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFFF59E0B).copy(alpha = 0.6f),
                            focusedLabelColor = Color(0xFFD97706),
                            unfocusedLabelColor = Color(0xFFD97706).copy(alpha = 0.8f)
                        )
                    } else {
                        OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pounds
                        OutlinedTextField(
                            value = weightLbsInput,
                            onValueChange = { newVal ->
                                weightLbsInput = newVal
                                val lbsVal = newVal.toDoubleOrNull()
                                if (lbsVal != null) {
                                    weightKgInput = String.format("%.2f", lbsVal / 2.20462)
                                } else if (newVal.isEmpty()) {
                                    weightKgInput = ""
                                }
                            },
                            readOnly = activePet != null,
                            label = { Text(if (currentLang == "fa") "پوند" else "Pounds") },
                            placeholder = { Text(if (currentLang == "fa") "پوند" else "lbs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = weightBorderColors
                        )

                        // Kilogram
                        OutlinedTextField(
                            value = weightKgInput,
                            onValueChange = { newVal ->
                                weightKgInput = newVal
                                val kgVal = newVal.toDoubleOrNull()
                                if (kgVal != null) {
                                    weightLbsInput = String.format("%.2f", kgVal * 2.20462)
                                } else if (newVal.isEmpty()) {
                                    weightLbsInput = ""
                                }
                            },
                            readOnly = activePet != null,
                            label = { Text(if (currentLang == "fa") "کیلوگرم" else "Kilogram") },
                            placeholder = { Text(if (currentLang == "fa") "ک‌گ" else "kgs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = weightBorderColors
                        )
                    }

                    if (showWeightWarning) {
                        Text(
                            text = if (currentLang == "fa") "⚠️ آیا از وزن وارد شده اطمینان دارید؟" else "⚠️ Are you sure about the entered weight?",
                            color = Color(0xFFD97706),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp).fillMaxWidth(),
                            textAlign = if (currentLang == "fa") TextAlign.Right else TextAlign.Left
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // STEP 2 Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF4B5563), RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("2", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == "fa") "تعیین حجم مایع / مدت زمان / نرخ" else "Set Fluid Volume/Duration/Rate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // STEP 2 Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fluid Volume dropdown
                        var volDropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            val dispValue = if (currentLang == "fa") {
                                when (fluidVolumeSelected) {
                                    "20 ml" -> "۲۰ میلی‌لیتر"
                                    "30 ml" -> "۳۰ میلی‌لیتر"
                                    "50 ml" -> "۵۰ میلی‌لیتر"
                                    "60 ml" -> "۶۰ میلی‌لیتر"
                                    "100 ml" -> "۱۰۰ میلی‌لیتر"
                                    "250 ml" -> "۲۵۰ میلی‌لیتر"
                                    "500 ml" -> "۵۰۰ میلی‌لیتر"
                                    "1000 ml" -> "۱۰۰۰ میلی‌لیتر"
                                    else -> fluidVolumeSelected
                                }
                            } else {
                                fluidVolumeSelected
                            }

                            OutlinedTextField(
                                value = dispValue,
                                onValueChange = { fluidVolumeSelected = it },
                                label = { Text(if (currentLang == "fa") "حجم مایع" else "Fluid Volume") },
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.clickable { volDropdownExpanded = true }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = volDropdownExpanded,
                                onDismissRequest = { volDropdownExpanded = false }
                            ) {
                                listOf("20 ml", "30 ml", "50 ml", "60 ml", "100 ml", "250 ml", "500 ml", "1000 ml").forEach { vol ->
                                    val dispVol = if (currentLang == "fa") {
                                        when (vol) {
                                            "20 ml" -> "۲۰ میلی‌لیتر"
                                            "30 ml" -> "۳۰ میلی‌لیتر"
                                            "50 ml" -> "۵۰ میلی‌لیتر"
                                            "60 ml" -> "۶۰ میلی‌لیتر"
                                            "100 ml" -> "۱۰۰ میلی‌لیتر"
                                            "250 ml" -> "۲۵۰ میلی‌لیتر"
                                            "500 ml" -> "۵۰۰ میلی‌لیتر"
                                            "1000 ml" -> "۱۰۰۰ میلی‌لیتر"
                                            else -> vol
                                        }
                                    } else {
                                        vol
                                    }
                                    DropdownMenuItem(
                                        text = { Text(dispVol) },
                                        onClick = {
                                            fluidVolumeSelected = vol
                                            volDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Fluid Time
                        OutlinedTextField(
                            value = fluidTimeHrs,
                            onValueChange = { fluidTimeHrs = it },
                            label = { Text(if (currentLang == "fa") "مدت زمان (ساعت)" else "Fluid Time (hrs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Drip Rate Dropdown
                        var dripDropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = dripRateGttSelected,
                                onValueChange = {},
                                label = { Text(if (currentLang == "fa") "نرخ قطره (gtt)" else "Drip Rate (gtt)") },
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.clickable { dripDropdownExpanded = true }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = dripDropdownExpanded,
                                onDismissRequest = { dripDropdownExpanded = false }
                            ) {
                                listOf("10 ggt/ml", "15 gtts/ml", "20 gtts/ml", "60 gtts/ml").forEach { rate ->
                                    DropdownMenuItem(
                                        text = { Text(rate) },
                                        onClick = {
                                            dripRateGttSelected = rate
                                            dripDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // STEP 3 Header & Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF4B5563), RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Checkbox(
                            checked = addFluidDeficit,
                            onCheckedChange = { addFluidDeficit = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLang == "fa") "افزودن کسری مایع (کم‌آبی)" else "Add any Fluid Deficit (Dehydration)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // STEP 3 Slider Expanded Form
                    AnimatedVisibility(visible = addFluidDeficit) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("💡", fontSize = 14.sp)
                                    Text(
                                        text = if (currentLang == "fa") "کسری مایع" else "Fluid Deficit",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                                // Gold Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(goldBgColor)
                                        .border(1.dp, goldBorderColor, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${dehydrationPctSlider.toInt()} %",
                                        color = goldTextColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = if (currentLang == "fa") "تعیین درصد کم‌آبی" else "Set % Dehydration",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                            Slider(
                                value = dehydrationPctSlider,
                                onValueChange = { dehydrationPctSlider = it },
                                valueRange = 0f..20f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFF59E0B),
                                    activeTrackColor = Color(0xFFF59E0B)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("0", "5", "10", "15", "20").forEach { label ->
                                    Text(text = label, fontSize = 10.sp, color = textSecondary)
                                }
                            }
                            if (dehydrationPctSlider >= 12f) {
                                Text(
                                    text = if (currentLang == "fa") {
                                        "⚠️ آیا از درصد کم‌آبی وارد شده اطمینان دارید؟ کم‌آبی بالای ۱۲٪ نشان‌دهنده شوک بالینی شدید است."
                                    } else {
                                        "⚠️ Are you sure about the entered dehydration percentage? Dehydration above 12% indicates severe clinical shock."
                                    },
                                    color = Color(0xFFD97706),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                                    textAlign = if (currentLang == "fa") TextAlign.Right else TextAlign.Left
                                )
                            }
                        }
                    }

                    // STEP 4 Header & Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF4B5563), RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("4", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Checkbox(
                            checked = addOngoingLosses,
                            onCheckedChange = { addOngoingLosses = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLang == "fa") "افزودن هدررفت مداوم مایعات" else "Add any Ongoing Losses",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // STEP 4 Slider Expanded Form
                    AnimatedVisibility(visible = addOngoingLosses) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("💡", fontSize = 14.sp)
                                    Text(
                                        text = if (currentLang == "fa") "هدررفت مداوم مایعات" else "Ongoing Losses",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                                // Gold Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(goldBgColor)
                                        .border(1.dp, goldBorderColor, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${ongoingLossesMlsSlider.toInt()} ml",
                                        color = goldTextColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = if (currentLang == "fa") "تعیین میزان هدررفت مداوم (میلی‌لیتر)" else "Set Ongoing Losses (mls)",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                            Slider(
                                value = ongoingLossesMlsSlider,
                                onValueChange = { ongoingLossesMlsSlider = it },
                                valueRange = 0f..500f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFF59E0B),
                                    activeTrackColor = Color(0xFFF59E0B)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("0", "100", "200", "300", "400", "500").forEach { label ->
                                    Text(text = label, fontSize = 10.sp, color = textSecondary)
                                }
                            }
                        }
                    }

                    // Target hours for replacement (If 3 or 4 is checked)
                    AnimatedVisibility(visible = addFluidDeficit || addOngoingLosses) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = replaceTimeHrs,
                                onValueChange = { replaceTimeHrs = it },
                                label = { Text(if (currentLang == "fa") "زمان (ساعت) جهت جبران کسری و هدررفت" else "Time (hrs) to Replace Deficit and Losses") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // CALCULATED RESULT MODULAR SECTIONS
            val kg = weightKgInput.toDoubleOrNull() ?: 10.0
            val dayTotal = (maintenanceDosageCoeff.toDoubleOrNull() ?: (if (isDog) 132.0 else 80.0)) * Math.pow(kg, 0.75)
            val hourlyMaintenance = dayTotal / 24.0

            // 1. Fluids - Maintenance Details Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(themeCardBg)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💬", fontSize = 16.sp)
                        Text(
                            text = if (currentLang == "fa") "مایعات - نگهداری" else "Fluids - Maintenance",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        Text("ℹ️", fontSize = 12.sp, modifier = Modifier.clickable { /* info click secondary action */ })
                    }

                    // Editable Dosage
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (currentLang == "fa") "دوز تجویز" else "Dosage",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        CompactDosageField(
                            value = maintenanceDosageCoeff,
                            onValueChange = { maintenanceDosageCoeff = it },
                            isDark = isDark,
                            textPrimary = textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                // Formula Description
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isDog) "🐕" else "🐈", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLang == "fa") {
                            if (isDog) "132 × (کیلوگرم)^0.75، تجویز در طول ۱۲ تا ۲۴ ساعت" else "80 × (کیلوگرم)^0.75، تجویز در طول ۱۲ تا ۲۴ ساعت"
                        } else {
                            if (isDog) "132 x (kg)0.75, deliver over 12–24 hours" else "80 x (kg)0.75, deliver over 12–24 hours"
                        },
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Banner Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(bannerBgColor)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLang == "fa") "نرخ مایع نگهداری برای ۲۴ ساعت" else "Maintenance Fluid Rate for 24hr",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = bannerTextColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 1X, 1.5X, 2X Multipliers Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1X
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentLang == "fa") "نگهداری ۱ برابر" else "1X Maintenance",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", hourlyMaintenance)} ${if (currentLang == "fa") "میلی‌لیتر/ساعت" else "ml/hr"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                    }

                    // 1.5X
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentLang == "fa") "نگهداری ۱.۵ برابر" else "1.5X Maintenance",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", hourlyMaintenance * 1.5)} ${if (currentLang == "fa") "میلی‌لیتر/ساعت" else "ml/hr"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                    }

                    // 2X
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentLang == "fa") "نگهداری ۲ برابر" else "2X Maintenance",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", hourlyMaintenance * 2.0)} ${if (currentLang == "fa") "میلی‌لیتر/ساعت" else "ml/hr"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }
                    }
                }
            }

            // 1.5 Maintenance + Deficit + Ongoing Loss Rate (Conditional UI matching screenshot 3)
            AnimatedVisibility(visible = addFluidDeficit || addOngoingLosses) {
                val deficitVolume = if (addFluidDeficit) kg * dehydrationPctSlider * 10 else 0.0
                val lossesVolume = if (addOngoingLosses) ongoingLossesMlsSlider.toDouble() else 0.0
                val totalReplaceVolume = deficitVolume + lossesVolume
                val replaceTimeHours = replaceTimeHrs.toDoubleOrNull() ?: 4.0
                val additionalHourlyRate = if (replaceTimeHours > 0) totalReplaceVolume / replaceTimeHours else 0.0
                val initialFluidRate = hourlyMaintenance + additionalHourlyRate

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .background(themeCardBg)
                ) {
                    // Header Banner "Maintenance + Dehydration + Ongoing Loss Rate"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(bannerBgColor)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentLang == "fa") "نرخ مایع نگهداری + کم‌آبی + هدررفت مداوم" else "Maintenance + Dehydration + Ongoing Loss Rate",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = bannerTextColor
                        )
                    }

                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Initial Fluid Rate Subheader
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (currentLang == "fa") "نرخ مایع اولیه" else "Initial Fluid Rate",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textPrimary
                                )
                                Text("ℹ️", fontSize = 12.sp)
                            }
                            Text(
                                text = if (currentLang == "fa") "نگهداری + کسری + هدررفت مداوم" else "Maintenance + Deficit+ Ongoing Losses",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }

                        // Rate for replaced hours
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLang == "fa") {
                                    "برای ${replaceTimeHours.toInt()} ساعت: ${String.format("%.1f", initialFluidRate)} میلی‌لیتر/ساعت"
                                } else {
                                    "for ${replaceTimeHours.toInt()}hr: ${String.format("%.1f", initialFluidRate)} ml/hr"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Then reduce rate
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (currentLang == "fa") "سپس کاهش نرخ به میزان نگهداری" else "Then Reduce Rate to Maintenance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(bulletColor, RoundedCornerShape(50)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == "fa") {
                                        "برای ${fluidTimeHrs} ساعت: ${String.format("%.1f", hourlyMaintenance)} میلی‌لیتر/ساعت"
                                    } else {
                                        "for ${fluidTimeHrs}hr: ${String.format("%.1f", hourlyMaintenance)} ml/hr"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Fluids - Resuscitation Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(themeCardBg)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💬", fontSize = 16.sp)
                        Text(
                            text = if (currentLang == "fa") "مایعات - احیاء" else "Fluids - Resuscitation",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        Text("ℹ️", fontSize = 12.sp)
                    }

                    // Dosage field
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (currentLang == "fa") "دوز تجویز" else "Dosage",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        CompactDosageField(
                            value = resuscitationDosage,
                            onValueChange = { resuscitationDosage = it },
                            isDark = isDark,
                            textPrimary = textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                // Spec description
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isDog) "🐕" else "🐈", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDog) {
                            if (currentLang == "fa") "۱۵-۲۰ میلی‌لیتر/کیلوگرم وریدی، حجم محاسبه‌شده را در مدت ۱۵ دقیقه تجویز و مجدداً ارزیابی کنید."
                            else "15-20 ml/kg IV, Give calculated volume over 15 minutes and re-assess."
                        } else {
                            if (currentLang == "fa") "۱۰-۱۵ میلی‌لیتر/کیلوگرم وریدی، حجم محاسبه‌شده را در مدت ۱۵ دقیقه تجویز کنید."
                            else "10-15 ml/kg IV, Give calculated volume over 15 minutes."
                        },
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calculated resuscitation output
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLang == "fa") "حجم - ۱۵ دقیقه" else "Volume - 15 Minutes",
                        fontSize = 11.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(6.dp))
                        val factor = resuscitationDosage.toDoubleOrNull() ?: 20.0
                        Text(
                            text = if (currentLang == "fa") {
                                "${String.format("%.1f", kg * factor)} میلی‌لیتر در کل"
                            } else {
                                "${String.format("%.1f", kg * factor)} ml total"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                    }
                }
            }

            // 3. Fluids - Surgical Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(themeCardBg)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💬", fontSize = 16.sp)
                        Text(
                            text = if (currentLang == "fa") "مایعات - جراحی" else "Fluids - Surgical",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (currentLang == "fa") {
                                if (isDog) "🐕 ۲ – ۶ میلی‌لیتر/کیلوگرم/ساعت وریدی" else "🐈 ۲ – ۶ میلی‌لیتر/کیلوگرم/ساعت وریدی"
                            } else {
                                if (isDog) "🐕 2 – 6 ml/kg/hr IV" else "🐈 2 – 6 ml/kg/hr IV"
                            },
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }

                    // Dosage Customizer
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (currentLang == "fa") "دوز تجویز" else "Dosage",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        CompactDosageField(
                            value = surgicalDosage,
                            onValueChange = { surgicalDosage = it },
                            isDark = isDark,
                            textPrimary = textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calculate Volume Surger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLang == "fa") "نرخ حجم مایع" else "Volume Rate",
                        fontSize = 11.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(6.dp))
                        val surgeFact = surgicalDosage.toDoubleOrNull() ?: 5.0
                        Text(
                            text = "${String.format("%.1f", kg * surgeFact)} ${if (currentLang == "fa") "میلی‌لیتر/ساعت" else "ml/hr"}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                    }
                }
            }

            // 4. Hetastarch Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(themeCardBg)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💬", fontSize = 16.sp)
                        Text(
                            text = if (currentLang == "fa") "هتااستارچ (Hetastarch)" else "Hetastarch",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        Text("ℹ️", fontSize = 12.sp)
                    }

                    // Dosage Customizer
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (currentLang == "fa") "دوز تجویز" else "Dosage",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        CompactDosageField(
                            value = hetastarchDosage,
                            onValueChange = { hetastarchDosage = it },
                            isDark = isDark,
                            textPrimary = textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (currentLang == "fa") "۱۰-۲۰ میلی‌لیتر/کیلوگرم وریدی در طول ۱۵-۳۰ دقیقه، ۱ میلی‌لیتر/کیلوگرم/ساعت تزریق مداوم (CRI)؛"
                           else "10-20 ml/kg IV over 15-30 minutes, 1mL/kg/hr CRI;",
                    fontSize = 11.sp,
                    color = textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Calculated Hetastarch details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (currentLang == "fa") "حجم در ۱۵ دقیقه" else "Volume in 15min",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            val hetCoeff = hetastarchDosage.toDoubleOrNull() ?: 10.0
                            Text(
                                text = "${String.format("%.1f", kg * hetCoeff)} ${if (currentLang == "fa") "میلی‌لیتر" else "ml"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = textPrimary
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (currentLang == "fa") "حداکثر حجم در ۲۴ ساعت" else "Max Volume/24hrs",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", kg * 20.0)} ${if (currentLang == "fa") "میلی‌لیتر" else "ml"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = textPrimary
                            )
                        }
                    }
                }
            }

            // 5. Vetstarch 6% Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(themeCardBg)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💬", fontSize = 16.sp)
                        Text(
                            text = if (currentLang == "fa") "وت‌استارچ ۶٪" else "Vetstarch 6%",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        Text("ℹ️", fontSize = 12.sp)
                    }

                    // Dosage Customizer
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (currentLang == "fa") "دوز تجویز" else "Dosage",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        CompactDosageField(
                            value = vetstarchDosage,
                            onValueChange = { vetstarchDosage = it },
                            isDark = isDark,
                            textPrimary = textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (currentLang == "fa") "۱۳۰/۰.۴ در نرمال سالین ۰.۹٪\n۱۰-۲۰ میلی‌لیتر/کیلوگرم/۲۴ ساعت وریدی"
                           else "130/0.4 IN 0.9% NACL\n10-20 ml/kg/24hrs IV",
                    fontSize = 10.sp,
                    color = textSecondary,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Calculated Vetstarch Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLang == "fa") "حجم کل ۲۴ ساعت" else "24 Hour Volume",
                        fontSize = 11.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(6.dp))
                        val vetCoeff = vetstarchDosage.toDoubleOrNull() ?: 20.0
                        Text(
                            text = "${String.format("%.1f", kg * vetCoeff)} ${if (currentLang == "fa") "میلی‌لیتر" else "ml"}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                    }
                }
            }

            // 6. Hypertonic Saline Card (7.2 %)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(themeCardBg)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💬", fontSize = 16.sp)
                        Text(
                            text = if (currentLang == "fa") "سالین هایپرتونیک" else "Hypertonic Saline",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                        Text("ℹ️", fontSize = 12.sp)

                        // 7.2 % pill/badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(goldBgColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "7.2 %",
                                color = goldTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Dosage Customizer
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (currentLang == "fa") "دوز تجویز" else "Dosage",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        CompactDosageField(
                            value = hypertonicSalineDosage,
                            onValueChange = { hypertonicSalineDosage = it },
                            isDark = isDark,
                            textPrimary = textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (currentLang == "fa") "۱-۸ میلی‌لیتر/کیلوگرم وریدی" else "1-8 ml/kg IV",
                    fontSize = 11.sp,
                    color = textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Calculated Hypertonic Saline details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val hyperCoeff = hypertonicSalineDosage.toDoubleOrNull() ?: 4.0
                    val totalHyperVolume = kg * hyperCoeff
                    val hyperVolPerMin = totalHyperVolume / 4.0

                    Column {
                        Text(
                            text = if (currentLang == "fa") "حجم/دقیقه" else "Volume/minute",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", hyperVolPerMin)} ${if (currentLang == "fa") "میلی‌لیتر/دقیقه" else "ml/min"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = textPrimary
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (currentLang == "fa") "حجم کل" else "Total Volume",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", totalHyperVolume)} ${if (currentLang == "fa") "میلی‌لیتر" else "ml"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = textPrimary
                            )
                        }
                    }
                }
            }

            // 7. Potassium Supplementation Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(themeCardBg)
            ) {
                // Header Banner "Potassium Supplementation"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(bannerBgColor)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLang == "fa") "مکمل‌یاری پتاسیم" else "Potassium Supplementation",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = bannerTextColor
                    )
                }

                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val parsedVolume = try {
                        val cleaned = fluidVolumeSelected
                            .replace(" ml", "")
                            .replace(" میلی‌لیتر", "")
                            .replace(" ", "")
                            .trim()
                        cleaned.toDoubleOrNull() ?: 250.0
                    } catch (e: Exception) {
                        250.0
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = if (currentLang == "fa") "پتاسیم سرم (mEq/L)\nحداکثر نرخ* (ml/kg/hr)" else "Serum K⁺ (mEq/L)\nMax rate* (ml/kg/hr)",
                                fontSize = 11.sp,
                                color = textSecondary,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1.8f), horizontalAlignment = Alignment.End) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (currentLang == "fa") "KCL (۲ mEq/ml) جهت افزودن به:" else "KCL (2 mEq/ml) To Add To",
                                    fontSize = 10.sp,
                                    color = textSecondary,
                                    lineHeight = 12.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(blueBgColor)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    val dispVolStr = if (currentLang == "fa") "${parsedVolume.toInt()} میلی‌لیتر" else "${parsedVolume.toInt()} mls"
                                    Text(
                                        text = dispVolStr,
                                        color = blueTextColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(
                                text = if (currentLang == "fa") "حجم مایع (تعیین‌شده در بالا)" else "Fluid Volume* (set above)",
                                fontSize = 9.sp,
                                color = textSecondary
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    val kRows = listOf(
                        Triple(
                            if (currentLang == "fa") "K⁺ < ۲ mEq/L\n۶ mL/kg/hr" else "K⁺ < 2mEq/L\n6 mL/kg/hr",
                            80.0,
                            "6"
                        ),
                        Triple(
                            if (currentLang == "fa") "K⁺ = ۲.۱-۲.۵ mEq/L\n۸ mL/kg/hr" else "K⁺ = 2.1-2.5mEq/L\n8 mL/kg/hr",
                            60.0,
                            "8"
                        ),
                        Triple(
                            if (currentLang == "fa") "K⁺ = ۲.۶-۳.۰ mEq/L\n۱۲ mL/kg/hr" else "K⁺ = 2.6-3.0mEq/L\n12 mL/kg/hr",
                            40.0,
                            "12"
                        ),
                        Triple(
                            if (currentLang == "fa") "K⁺ = ۳.۱-۳.۵ mEq/L\n۱۸ mL/kg/hr" else "K⁺ = 3.1-3.5mEq/L\n18 mL/kg/hr",
                            28.0,
                            "18"
                        ),
                        Triple(
                            if (currentLang == "fa") "K⁺ = ۳.۶-۵.۰ mEq/L\n۲۵ mL/kg/hr" else "K⁺ = 3.6-5.0mEq/L\n25 mL/kg/hr",
                            20.0,
                            "25"
                        )
                    )

                    kRows.forEach { (label, kConc, maxRate) ->
                        val mEq = kConc * (parsedVolume / 1000.0)
                        val mls = mEq / 2.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary,
                                lineHeight = 14.sp,
                                modifier = Modifier.weight(1.2f)
                            )

                            Row(
                                modifier = Modifier.weight(1.8f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentLang == "fa") "افزودن " else "Add ",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE5E7EB))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    val dispResult = if (currentLang == "fa") {
                                        "${String.format("%.1f", mEq)} mEq (${String.format("%.1f", mls)} میلی‌لیتر)"
                                    } else {
                                        "${String.format("%.1f", mEq)} mEq (${String.format("%.1f", mls)} mls)"
                                    }
                                    Text(
                                        text = dispResult,
                                        color = textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = " KCL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (currentLang == "fa") {
                            "⚠️ به مایعات مورد استفاده برای انفوزیون سریع KCL اضافه نکنید.\n" +
                            "* از دوز ۰.۵ mEq/kg/hr تجاوز نکنید. (منبع: DiBartola SP. Fluid Therapy in Small Animal Practice. 3rd ed. Philadelphia (PA): WB Saunders)"
                        } else {
                            "⚠️ Do not add KCl to fluids used for rapid infusion.\n" +
                            "* Do not exceed 0.5 mEq/kg/hr. (Source: DiBartola SP. Fluid Therapy in Small Animal Practice. 3rd ed. Philadelphia (PA): WB Saunders)"
                        },
                        fontSize = 10.sp,
                        color = textSecondary,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactDosageField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean,
    textPrimary: Color
) {
    val borderColor = if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val fieldBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .width(85.dp)
            .height(40.dp)
            .background(fieldBg, RoundedCornerShape(8.dp))
            .border(1.2.dp, borderColor, RoundedCornerShape(8.dp)),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        }
    )
}
