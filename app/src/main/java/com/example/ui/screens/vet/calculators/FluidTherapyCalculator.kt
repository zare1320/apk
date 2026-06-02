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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.example.data.database.Pet

@Composable
fun FluidTherapyCalculator(
    activePet: Pet?,
    selectedSpecies: String? = null
) {
    val initWeight = activePet?.weight?.toString() ?: ""
    val isDog = if (activePet != null) {
        activePet.species.lowercase() != "cat"
    } else {
        selectedSpecies?.lowercase() != "cat"
    }

    // 1. Enter Weight - Select Species States
    var weightKgInput by remember(initWeight) { mutableStateOf(initWeight.ifEmpty { "10.0" }) }
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

    // IV Drip Rate state fields
    var dripVolumeInput by remember { mutableStateOf("100") }
    var dropFactorSelected by remember { mutableStateOf("15 gtts/ml") }
    var dripTimeMinutes by remember { mutableStateOf("30") }

    val isDark = isSystemInDarkTheme()
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

    // Let's do calculations in LTR layout as in screenshots
    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
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
                    // Title Header to make it feel premium
                    Text(
                        text = "💧 Veterinary Fluid Therapy Planner",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
                    )

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
                        Text("Enter Weight - Select Species", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (activePet != null) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "🔒 ${activePet.name} (اطلاعات ثبت شده)",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // STEP 1 Inputs
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
                            label = { Text("Pounds") },
                            placeholder = { Text("lbs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
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
                            label = { Text("Kilogram") },
                            placeholder = { Text("kgs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
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
                        Text("Set Fluid Volume/Duration/Rate", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                            OutlinedTextField(
                                value = fluidVolumeSelected,
                                onValueChange = { fluidVolumeSelected = it },
                                label = { Text("Fluid Volume") },
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
                                listOf("50 ml", "100 ml", "250 ml", "500 ml", "1000 ml").forEach { vol ->
                                    DropdownMenuItem(
                                        text = { Text(vol) },
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
                            label = { Text("Fluid Time (hrs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        // Drip Rate Dropdown
                        var dripDropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = dripRateGttSelected,
                                onValueChange = {},
                                label = { Text("Drip Rate (gtt)") },
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
                        Text("Add any Fluid Deficit (Dehydration)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                    Text("Fluid Deficit", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
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
                            Text("Set % Dehydration", fontSize = 11.sp, color = textSecondary)
                            Slider(
                                value = dehydrationPctSlider,
                                onValueChange = { dehydrationPctSlider = it },
                                valueRange = 0f..15f,
                                steps = 14,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFF59E0B),
                                    activeTrackColor = Color(0xFFF59E0B)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("0", "5", "10", "15").forEach { label ->
                                    Text(text = label, fontSize = 10.sp, color = textSecondary)
                                }
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
                        Text("Add any Ongoing Losses", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                    Text("Ongoing Losses", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
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
                            Text("Set Ongoing Losses (mls)", fontSize = 11.sp, color = textSecondary)
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
                                label = { Text("Time (hrs) to Replace Deficit and Losses") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .background(if (isDark) MaterialTheme.colorScheme.primaryContainer else Color.White)
                    .clickable { showDripRateCalculator = !showDripRateCalculator }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("💧", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Drip Rate Calculator",
                        color = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }

            // Interactive Drip Rate Calculator Component
            AnimatedVisibility(
                visible = showDripRateCalculator,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💧", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("IV Drip Rate Calculator", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        // Reset label button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    dripVolumeInput = "100"
                                    dropFactorSelected = "15 gtts/ml"
                                    dripTimeMinutes = "30"
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔄", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Calculate the drip rate for a specific volume of fluid to administer over a specific time.\ngtt = drops, gtts = drops/sec\n\nIV Drip Rate (gtt/min) =\nVolume to give (mls) x Drop factor (gtts/mL) / Time (min)",
                        fontSize = 11.sp,
                        color = textSecondary,
                        lineHeight = 16.sp
                    )

                    // Volume to Give Textfield
                    OutlinedTextField(
                        value = dripVolumeInput,
                        onValueChange = { dripVolumeInput = it },
                        label = { Text("Volume to give (mls)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Drop Factor Dropdown
                    var dripSelectorExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = dropFactorSelected,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("IV Drop Factor") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { dripSelectorExpanded = true }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = dripSelectorExpanded,
                            onDismissRequest = { dripSelectorExpanded = false }
                        ) {
                            listOf("10 gtts/ml", "15 gtts/ml", "20 gtts/ml", "60 gtts/ml").forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        dropFactorSelected = item
                                        dripSelectorExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Time in Minutes Textfield
                    OutlinedTextField(
                        value = dripTimeMinutes,
                        onValueChange = { dripTimeMinutes = it },
                        label = { Text("Time in Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Outputs in stunning light blue badges
                    val calcDripVol = dripVolumeInput.toDoubleOrNull() ?: 100.0
                    val calcDripFactor = dropFactorSelected.substringBefore(" ").toDoubleOrNull() ?: 15.0
                    val calcDripTime = dripTimeMinutes.toDoubleOrNull() ?: 30.0
                    val computedMin = if (calcDripTime > 0) (calcDripVol * calcDripFactor) / calcDripTime else 0.0
                    val computedSec = computedMin / 60.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("IV Drip Rate =", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))

                        // Badge 1: Min
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(blueBgColor)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${String.format("%.1f", computedMin)} gtts/min",
                                color = blueTextColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Badge 2: Sec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(blueBgColor)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${String.format("%.1f", computedSec)} gtts/sec",
                                color = blueTextColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
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
                        Text("Fluids - Maintenance", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = textPrimary)
                        Text("ℹ️", fontSize = 12.sp, modifier = Modifier.clickable { /* info click secondary action */ })
                    }

                    // Editable Dosage
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Dosage", fontSize = 10.sp, color = textSecondary)
                        OutlinedTextField(
                            value = maintenanceDosageCoeff,
                            onValueChange = { maintenanceDosageCoeff = it },
                            modifier = Modifier
                                .width(85.dp)
                                .height(44.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                // Formula Description
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isDog) "🐕" else "🐈", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDog) "132 x (kg)0.75, deliver over 12–24 hours" else "80 x (kg)0.75, deliver over 12–24 hours",
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
                    Text("Maintenance Fluid Rate for 24hr", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = bannerTextColor)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 1X, 1.5X, 2X Multipliers Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1X
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("1X Maintenance", fontSize = 10.sp, color = textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${String.format("%.1f", hourlyMaintenance)} ml/hr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        }
                    }

                    // 1.5X
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("1.5X Maintenance", fontSize = 10.sp, color = textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${String.format("%.1f", hourlyMaintenance * 1.5)} ml/hr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        }
                    }

                    // 2X
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("2X Maintenance", fontSize = 10.sp, color = textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${String.format("%.1f", hourlyMaintenance * 2.0)} ml/hr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
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
                        Text("Maintenance + Dehydration + Ongoing Loss Rate", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = bannerTextColor)
                    }

                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Initial Fluid Rate Subheader
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Initial Fluid Rate", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                                Text("ℹ️", fontSize = 12.sp)
                            }
                            Text("Maintenance + Deficit+ Ongoing Losses", fontSize = 11.sp, color = textSecondary)
                        }

                        // Rate for replaced hours
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "for ${replaceTimeHours.toInt()}hr: ${String.format("%.1f", initialFluidRate)} ml/hr",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Then reduce rate
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Then Reduce Rate to Maintenance", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(bulletColor, RoundedCornerShape(50)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "for ${fluidTimeHrs}hr: ${String.format("%.1f", hourlyMaintenance)} ml/hr",
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
                        Text("Fluids - Resuscitation", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = textPrimary)
                        Text("ℹ️", fontSize = 12.sp)
                    }

                    // Dosage field
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Dosage", fontSize = 10.sp, color = textSecondary)
                        OutlinedTextField(
                            value = resuscitationDosage,
                            onValueChange = { resuscitationDosage = it },
                            modifier = Modifier
                                .width(85.dp)
                                .height(44.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                // Spec description
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isDog) "🐕" else "🐈", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDog) "15-20 ml/kg IV, Give calculated volume over 15 minutes and re-assess."
                               else "10-15 ml/kg IV, Give calculated volume over 15 minutes.",
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
                    Text("Volume - 15 Minutes", fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(6.dp))
                        val factor = resuscitationDosage.toDoubleOrNull() ?: 20.0
                        Text("${String.format("%.1f", kg * factor)} ml total", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = textPrimary)
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
                        Text("Fluids - Surgical", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = textPrimary)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(if (isDog) "🐕 2 – 6 ml/kg/hr IV" else "🐈 2 – 6 ml/kg/hr IV", fontSize = 11.sp, color = textSecondary)
                    }

                    // Dosage Customizer
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Dosage", fontSize = 10.sp, color = textSecondary)
                        OutlinedTextField(
                            value = surgicalDosage,
                            onValueChange = { surgicalDosage = it },
                            modifier = Modifier
                                .width(85.dp)
                                .height(44.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
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
                    Text("Volume Rate", fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(6.dp))
                        val surgeFact = surgicalDosage.toDoubleOrNull() ?: 5.0
                        Text("${String.format("%.1f", kg * surgeFact)} ml/hr", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = textPrimary)
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
                        Text("Hetastarch", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = textPrimary)
                        Text("ℹ️", fontSize = 12.sp)
                    }

                    // Dosage Customizer
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Dosage", fontSize = 10.sp, color = textSecondary)
                        OutlinedTextField(
                            value = hetastarchDosage,
                            onValueChange = { hetastarchDosage = it },
                            modifier = Modifier
                                .width(85.dp)
                                .height(44.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("10-20 ml/kg IV over 15-30 minutes, 1mL/kg/hr CRI;", fontSize = 11.sp, color = textSecondary)

                Spacer(modifier = Modifier.height(10.dp))

                // Calculated Hetastarch details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Volume in 15min", fontSize = 10.sp, color = textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            val hetCoeff = hetastarchDosage.toDoubleOrNull() ?: 10.0
                            Text("${String.format("%.1f", kg * hetCoeff)} ml", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textPrimary)
                        }
                    }

                    Column {
                        Text("Max Volume/24hrs", fontSize = 10.sp, color = textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${String.format("%.1f", kg * 20.0)} ml", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textPrimary)
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
                        Text("Vetstarch 6%", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = textPrimary)
                        Text("ℹ️", fontSize = 12.sp)
                    }

                    // Dosage Customizer
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Dosage", fontSize = 10.sp, color = textSecondary)
                        OutlinedTextField(
                            value = vetstarchDosage,
                            onValueChange = { vetstarchDosage = it },
                            modifier = Modifier
                                .width(85.dp)
                                .height(44.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("130/0.4 IN 0.9% NACL\n10-20 ml/kg/24hrs IV", fontSize = 10.sp, color = textSecondary, lineHeight = 14.sp)

                Spacer(modifier = Modifier.height(10.dp))

                // Calculated Vetstarch Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("24 Hour Volume", fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(5.dp).background(bulletColor, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(6.dp))
                        val vetCoeff = vetstarchDosage.toDoubleOrNull() ?: 20.0
                        Text("${String.format("%.1f", kg * vetCoeff)} ml", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = textPrimary)
                    }
                }
            }
        }
    }
}
