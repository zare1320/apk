package com.example.ui.screens.vet

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VetCalculatorScreen(viewModel: MainViewModel) {
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()

    var activeCalculator by remember { mutableStateOf("مایع‌درمانی") }

    // Forms States
    var weightInput by remember { mutableStateOf("") }

    // Init fields from active pet if available
    LaunchedEffect(activeExaminedPet) {
        activeExaminedPet?.let { pet ->
            weightInput = pet.weight.toString()
        }
    }

    val calculatorsList = listOf(
        "مایع‌درمانی", "انتقال خون", "محاسبه کالری غذا", "زمان زایمان", "سن معادل انسان", "تریاژ تروما"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Clinical calculators titles
        Text(
            text = "🧮 ابزارهای سنجش و محاسبه‌گرهای کلینیکال:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Right
        )

        // Horizontal selections
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            calculatorsList.forEach { cal ->
                val isSel = activeCalculator == cal
                val bgCol = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val textCol = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgCol)
                        .clickable { activeCalculator = cal }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(cal, fontSize = 11.sp, color = textCol, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weight helper info
        Text(
            text = "وزن مبنا جهت اجرای فرمول‌ها: ${weightInput.ifEmpty { "1.0" }} کیلوگرم",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            // Interactive Display
            when (activeCalculator) {
                "مایع‌درمانی" -> {
                    FluidTherapyCalculator(activePet = activeExaminedPet, selectedSpecies = selectedSpecies)
                }
                "انتقال خون" -> {
                    BloodTransfusionCalculator(activePet = activeExaminedPet, initWeight = weightInput, selectedSpecies = selectedSpecies)
                }
                "محاسبه کالری غذا" -> {
                    CalorieCalculatorView(activePet = activeExaminedPet, initWeight = weightInput, selectedSpecies = selectedSpecies)
                }
                "زمان زایمان" -> {
                    GestationCalculatorView(viewModel)
                }
                "سن معادل انسان" -> {
                    HumanAgeCalculatorView(initWeight = weightInput)
                }
                "تریاژ تروما" -> {
                    TraumaTriageView()
                }
            }
        }
    }
}

// 1. Fluid Therapy Calculator implementation
@Composable
fun FluidTherapyCalculator(
    activePet: com.example.data.database.Pet?,
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

            // Interactive Drip Rate Calculator Component (Screenshot 4)
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

// 2. Blood Transfusion Calculator implementation
@Composable
fun BloodTransfusionCalculator(
    activePet: com.example.data.database.Pet? = null,
    initWeight: String = "",
    selectedSpecies: String? = null
) {
    var weightStr by remember(initWeight) { mutableStateOf(initWeight.ifEmpty { "10" }) }
    var currentPcv by remember { mutableStateOf("15") } // Patient's current Hematocrit %
    var targetPcv by remember { mutableStateOf("25") } // Target Hematocrit %
    var donorPcv by remember { mutableStateOf("40") } // Donor's standard Hematocrit %

    val isDog = if (activePet != null) {
        activePet.species.lowercase() != "cat"
    } else {
        selectedSpecies?.lowercase() != "cat"
    }

    val isDark = isSystemInDarkTheme()
    val themeCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.White
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface

    val pinkBgColor = if (isDark) Color(0xFF4C0519) else Color(0xFFFCE7F3)
    val pinkTextColor = if (isDark) Color(0xFFFDA4AF) else Color(0xFF9D174D)
    val pinkBorderColor = if (isDark) Color(0xFFE11D48) else Color(0xFFF472B6)

    val bulletColor = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF818CF8)

    val weight = weightStr.toDoubleOrNull() ?: 10.0
    val recipientPcv = currentPcv.toDoubleOrNull() ?: 15.0
    val target = targetPcv.toDoubleOrNull() ?: 25.0
    val donor = donorPcv.toDoubleOrNull() ?: 40.0

    // Dose = ((Target - Recipient) / Donor) * Weight * N
    val n = if (isDog) 90.0 else 60.0
    val bloodVolume = if (donor > 0) {
        val result = ((target - recipientPcv) / donor) * weight * n
        if (result < 0) 0.0 else result
    } else {
        0.0
    }
    val resultFormatted = String.format("%.1f", bloodVolume)

    // Accordions visibility
    var isAboutExpanded by remember { mutableStateOf(false) }
    var isProcExpanded by remember { mutableStateOf(false) }
    var isCrossExpanded by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Title
            Text(
                text = "Transfusion Calculator - " + (if (isDog) "Canine" else "Feline"),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = textPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Calculation Fields List Card
            Card(
                colors = CardDefaults.cardColors(containerColor = themeCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    
                    // Row 1: Desired PCV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💉", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Desired PCV", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                            }
                            Text("After transfusion", fontSize = 11.sp, color = textSecondary)
                        }
                        
                        OutlinedTextField(
                            value = targetPcv,
                            onValueChange = { targetPcv = it },
                            placeholder = { Text("Desired PCV %", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.width(150.dp)
                        )
                    }

                    // Row 2: Current PCV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💉", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Current PCV", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                            }
                            Text("Before transfusion", fontSize = 11.sp, color = textSecondary)
                        }
                        
                        OutlinedTextField(
                            value = currentPcv,
                            onValueChange = { currentPcv = it },
                            placeholder = { Text("Current PCV %", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.width(150.dp)
                        )
                    }

                    // Row 3: Donor PCV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💉", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Donor PCV", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                            }
                            Text("PCV of Donor Blood", fontSize = 11.sp, color = textSecondary)
                        }
                        
                        OutlinedTextField(
                            value = donorPcv,
                            onValueChange = { donorPcv = it },
                            placeholder = { Text("Donor PCV %", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.width(150.dp)
                        )
                    }

                    // Compact customizable Weight Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚖️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Recipient Weight", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                            }
                            Text("Weight in Kg", fontSize = 11.sp, color = textSecondary)
                        }
                        
                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            placeholder = { Text("Weight in kg", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.width(150.dp)
                        )
                    }

                    // Row 4: Blood Volume Output
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🩸", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Blood Volume", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = textPrimary)
                            }
                            Text("Calculated in mls", fontSize = 11.sp, color = textSecondary)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isDog) "🐕" else "🐈",
                                fontSize = 24.sp
                            )
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(pinkBgColor)
                                    .border(1.dp, pinkBorderColor, RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$resultFormatted ml",
                                    color = pinkTextColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info blocks (Transfusion Rate, Monitoring, Reactions)
            Text("Transfusion Rate:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BulletItem(text = "Start all transfusions at 1 to 2 ml/minute", bulletColor = bulletColor)
                BulletItem(text = "Adult dogs: maximum rate of 3 to 6 ml/minute", bulletColor = bulletColor)
                BulletItem(text = "Cats, kittens, puppies: maximum rate of 1 to 2 ml/minute", bulletColor = bulletColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Monitoring:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BulletItem(text = "Document baseline PCV/TP, weight, temperature, pulse, respiratory rate, CRT and MM color prior to transfusion", bulletColor = bulletColor)
                BulletItem(text = "During first 60 minutes: TPR, CRT and MM color Q15 minutes", bulletColor = bulletColor)
                BulletItem(text = "Then Q30 minutes for the duration of the transfusion", bulletColor = bulletColor)
                BulletItem(text = "If signs of transfusion reaction observed, Stop transfusion and initiate treatment.", bulletColor = bulletColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Transfusion Reactions:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletItem(
                    title = "Immune-Mediated Hemolytic Reactions",
                    text = "Acute reactions. Result of preexisting antibodies or sensitization from a previous transfusion. Rare but the most serious reaction. Earliest clinical sign is hypothermia. Other signs include vomiting, tachycardia, tachypnea, weakness, tremors, facial swelling, hypotension, hemoglobinemia, hemoglobinuria.",
                    bulletColor = bulletColor
                )
                BulletItem(
                    title = "Immune-Mediated Non-Hemolytic Reactions",
                    text = "Result from antibodies to RBCs, leukocytes, platelets, or plasma proteins. Most often transient. Clinical signs: anaphylaxis, urticaria, pruritis, hyperthermia, tachypena, dyspnea, vomiting, neurologic signs",
                    bulletColor = bulletColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Transfusion Info",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Accordion 1: About Transfusions
            AccordionItem(
                title = "About Transfusions",
                isExpanded = isAboutExpanded,
                onHeaderClick = { isAboutExpanded = !isAboutExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Transfusion Formula", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary)
                    Text(
                        text = "Blood Dose (ml) = ((Target PCV - Recipient PCV) ÷ Donor PCV) X KG X N\nN = 90 for Dogs, N = 60 for Cats",
                        fontSize = 11.sp,
                        color = textSecondary,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Products List as a high-fidelity cards grid
                    listOf(
                        TransfusionProduct("Fresh whole blood", "12 to 20 ml/kg", "q. 24 h", "anemia, platelet & factor replacement"),
                        TransfusionProduct("Packed red cells", "6 to 10 ml/kg", "q. 12 to 24 h", "anemia"),
                        TransfusionProduct("Platelet rich plasma", "6 to 10 ml/kg", "q. 8 to 12 h", "platelet dysfunction, thrombocytopenia"),
                        TransfusionProduct("Fresh and fresh frozen plasma", "6 to 12 ml/kg", "q. 8 to 12 h", "coagulation factor deficiencies, vWD, DIC, hypoproteinemia"),
                        TransfusionProduct("Frozen plasma", "6 to 12 ml/kg", "q. 8 to 12 h", "hypoproteinemia"),
                        TransfusionProduct("Plasma cryoprecipitate", "1 unit/10 kg", "q. 4 to 12 h (as needed)", "hemophilia A (factor VIII deficiency), fibrinogen deficiency, von Willebrand disease"),
                        TransfusionProduct("Cryosupernatant", "6 to 12 ml/kg", "q. 8 to 12 h", "hemophilia B (factor IX deficiency), factor VII, X, or XI deficiency, vitamin K deficiency, hypoproteinemia")
                    ).forEach { prod ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Vol: ${prod.vol}", fontSize = 11.sp, color = textSecondary, modifier = Modifier.weight(1f))
                                    Text("Freq: ${prod.freq}", fontSize = 11.sp, color = textSecondary, modifier = Modifier.weight(1f))
                                }
                                Text("Indications: ${prod.indications}", fontSize = 10.sp, color = textSecondary)
                            }
                        }
                    }

                    Text(
                        "* 1 unit = cryoprecipitate produced from 200 ml of fresh frozen plasma",
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                }
            }

            // Accordion 2: Processing and Storage
            AccordionItem(
                title = "Processing and Storage",
                isExpanded = isProcExpanded,
                onHeaderClick = { isProcExpanded = !isProcExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BulletItem(title = "Fresh whole blood", text = "Transfuse within 4 to 6 hour of collection.", bulletColor = bulletColor)
                    BulletItem(title = "Fresh plasma", text = "Centrifuged to separate plasma from whole blood, transfuse within 4 to 6 hr. of collection.", bulletColor = bulletColor)
                    BulletItem(title = "Fresh frozen plasma", text = "Collect in citrate anticoagulant, separate plasma from whole blood within 4 to 6 hr. of collection, store frozen for up to 1 year.", bulletColor = bulletColor)
                    BulletItem(title = "Platelet rich plasma", text = "Collect in citrate anticoagulant, separate platelet rich plasma from whole blood within 4 to 6 hr. of collection, process and store at room temperature, transfuse within 48 hr. of collection.", bulletColor = bulletColor)
                    BulletItem(title = "Packed Red Cells", text = "Collect in citrate anticoagulant, separate from whole blood within 4 to 6 hr. of collection, combine packed cells with additives for sustained red cell viability, store under refrigeration (4 to 8 C) for up to 4 weeks.", bulletColor = bulletColor)
                    BulletItem(title = "Plasma Cryoprecipitate", text = "Prepared from fresh frozen plasma, store frozen for up to 1 year, unit size varies, check with each supplier for dosage.", bulletColor = bulletColor)
                    BulletItem(title = "Cryosupernatant", text = "Prepared from fresh frozen plasma, store frozen for up to 1 year.", bulletColor = bulletColor)
                }
            }

            // Accordion 3: Cross-matching
            AccordionItem(
                title = "Cross-matching",
                isExpanded = isCrossExpanded,
                onHeaderClick = { isCrossExpanded = !isCrossExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("Canine", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BulletItem(text = "12 canine blood types", bulletColor = bulletColor)
                            BulletItem(text = "Designated DEA and a number (DEA 1, DEA 2, DEA 3, etc.)", bulletColor = bulletColor)
                            BulletItem(text = "Most important are DEA 1: 1.1 and 1.2", bulletColor = bulletColor)
                            BulletItem(text = "DEA 1.1 Positive = universal recipient", bulletColor = bulletColor)
                            BulletItem(text = "DEA 1.1, DEA 1.2 Negative = universal donor", bulletColor = bulletColor)
                            BulletItem(text = "Cross-match does not need to be performed on a first time transfusion", bulletColor = bulletColor)
                            BulletItem(text = "Sensitization takes ~ 3 days\nCross-match needed 72 hours after dog receives transfusion", bulletColor = bulletColor)
                        }
                    }

                    Column {
                        Text("Feline", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BulletItem(text = "AB blood type system", bulletColor = bulletColor)
                            BulletItem(text = "Types: A, B, AB(rare)", bulletColor = bulletColor)
                            BulletItem(text = "Have natural occurring alloantibodies to other blood groups = No universal donor", bulletColor = bulletColor)
                            BulletItem(text = "Type A: Weak anti-B alloantibodies, mild reaction if transfused with B blood", bulletColor = bulletColor)
                            BulletItem(text = "Type B: High anti-A alloantibodies, severe reaction if transfused with A blood", bulletColor = bulletColor)
                            BulletItem(text = "ALL CATS SHOULD BE CROSS-MATCHED", bulletColor = bulletColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

        }
    }
}

// Simple Helper Composables for our high-fidelity layout
@Composable
fun BulletItem(
    text: String,
    bulletColor: Color,
    title: String = "",
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(bulletColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            if (title.isNotEmpty()) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp, fontWeight = fontWeight)
        }
    }
}

@Composable
fun AccordionItem(
    title: String,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else Color(0xFFF9FAFB)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
                ) {
                    content()
                }
            }
        }
    }
}

data class TransfusionProduct(
    val name: String,
    val vol: String,
    val freq: String,
    val indications: String
)


// 3. Calorie Food limits
@Composable
fun CalorieCalculatorView(
    activePet: com.example.data.database.Pet? = null,
    initWeight: String = "",
    selectedSpecies: String? = null
) {
    // Determine active species tab (default to Canine unless animal species or selectedSpecies is cat)
    val isCat = (activePet?.species?.lowercase() == "cat") || (selectedSpecies?.lowercase() == "cat")
    var isCanineTab by remember(activePet, selectedSpecies) { mutableStateOf(!isCat) }

    // Weight input states with auto-synchronization
    var weightKg by remember(initWeight, activePet) {
        val initialKgStr = activePet?.weight?.toString() ?: initWeight.ifEmpty { "" }
        mutableStateOf(initialKgStr)
    }
    
    var weightLbs by remember(initWeight, activePet) {
        val initialKg = activePet?.weight ?: initWeight.toDoubleOrNull() ?: 0.0
        val initialLbsStr = if (initialKg > 0.0) {
            String.format("%.2f", initialKg * 2.20462)
        } else {
            ""
        }
        mutableStateOf(initialLbsStr)
    }

    // BCS Score states
    var bcsScore by remember { mutableStateOf(5) } // Default BCS = 5/9
    var bcsMenuExpanded by remember { mutableStateOf(false) }
    var showBcsChartDialog by remember { mutableStateOf(false) }

    // Pet Criteria selection states based on species
    val criteriaOptions = if (isCanineTab) {
        listOf(
            CriteriaData("Neutered Adult", 1.6, 1.4, 1.8),
            CriteriaData("Intact Adult", 1.8, 1.6, 2.0),
            CriteriaData("Inactive/obese", 1.2, 1.0, 1.4),
            CriteriaData("Weight Loss", 1.0, 1.0, 1.2),
            CriteriaData("Weight Gain", 1.4, 1.2, 1.6),
            CriteriaData("Puppy 0-4 months", 3.0, 2.0, 3.0),
            CriteriaData("Puppy 4-12 months", 2.0, 1.0, 3.0)
        )
    } else {
        listOf(
            CriteriaData("Neutered Adult", 1.2, 1.0, 1.4),
            CriteriaData("Intact Adult", 1.4, 1.2, 1.6),
            CriteriaData("Inactive/obese", 1.0, 0.8, 1.2),
            CriteriaData("Weight Loss", 0.8, 0.8, 1.0),
            CriteriaData("Weight Gain", 1.4, 1.2, 1.6),
            CriteriaData("Kitten 0-4 months", 2.5, 2.0, 3.0),
            CriteriaData("Kitten 4-12 months", 2.0, 1.5, 2.5)
        )
    }

    var selectedCriteriaName by remember(isCanineTab) { 
        mutableStateOf(if (isCanineTab) "Neutered Adult" else "Neutered Adult") 
    }
    var criteriaMenuExpanded by remember { mutableStateOf(false) }

    val activeCriteria = criteriaOptions.find { it.name == selectedCriteriaName } ?: criteriaOptions[0]

    // Calculation computations
    val weightKgVal = weightKg.toDoubleOrNull() ?: 0.0
    val weightLbsVal = weightLbs.toDoubleOrNull() ?: 0.0

    // Ideal Weight computation: Each unit score above 5 represent ~ 10% overweight.
    // BCS <= 5: target weight equals current weight
    val targetWeightKg = if (bcsScore <= 5) {
        weightKgVal
    } else {
        weightKgVal * (100.0 / (100.0 + (bcsScore - 5.0) * 10.0))
    }
    val targetWeightLbs = targetWeightKg * 2.20462

    // RER (Resting Energy Requirement) = 70 * (Target Weight in kg)^0.75
    val rer = if (targetWeightKg > 0.0) 70.0 * Math.pow(targetWeightKg, 0.75) else 0.0
    val formattedRer = String.format("%.0f", rer)

    // MER (Maintenance Energy Requirement) = multiplier * RER
    val mer = rer * activeCriteria.multiplier
    val formattedMer = String.format("%.0f", mer)

    // MER Range limits
    val merRangeMin = rer * activeCriteria.rangeMin
    val merRangeMax = rer * activeCriteria.rangeMax
    val formattedMerRange = "${String.format("%.0f", merRangeMin)} - ${String.format("%.0f", merRangeMax)}"

    // Input for Calories (Step 4)
    var enterCaloriesStr by remember { mutableStateOf("") }
    val enteredCalories = enterCaloriesStr.toDoubleOrNull() ?: 0.0
    val cupsOrCansPerDay = if (enteredCalories > 0.0 && mer > 0.0) mer / enteredCalories else 0.0
    val formattedCupsOrCansPerDay = if (cupsOrCansPerDay > 0.0) String.format("%.1f", cupsOrCansPerDay) else ""

    // Daily H2O Requirement (= MER ml/day)
    val h2oMls = mer
    val h2oMlsRangeVal = h2oMls * 0.20 // +/- 20%
    val formattedH2oMls = "${String.format("%.0f", h2oMls)} mls/day (±${String.format("%.0f", h2oMlsRangeVal)}mls)"

    val h2oCups = mer / 240.0
    val h2oCupsRangeVal = h2oCups * 0.20 // +/- 20%
    val formattedH2oCups = "${String.format("%.1f", h2oCups)} cups/day (±${String.format("%.1f", h2oCupsRangeVal)}cups)"

    // Canned and Dry Food Calorie Data Database setup
    var activeClassTab by remember { mutableStateOf("Dry Food") } // "Dry Food" or "Canned Food"

    val isDark = isSystemInDarkTheme()
    val themeCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.White
    val borderStrokeColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val badgeBgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val bulletColor = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF818CF8)

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP 1: Set Weight and BCS
            Card(
                colors = CardDefaults.cardColors(containerColor = themeCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderStrokeColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Step Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                "1",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Set Weight and BCS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    // Pounds and Kilograms Textfields Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Pounds
                        OutlinedTextField(
                            value = weightLbs,
                            onValueChange = { input ->
                                weightLbs = input
                                val lbsVal = input.toDoubleOrNull()
                                if (lbsVal != null) {
                                    val kgCalculated = lbsVal / 2.20462
                                    weightKg = String.format("%.2f", kgCalculated)
                                } else {
                                    weightKg = ""
                                }
                            },
                            label = { Text("Pounds") },
                            placeholder = { Text("lbs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Kilograms
                        OutlinedTextField(
                            value = weightKg,
                            onValueChange = { input ->
                                weightKg = input
                                val kgVal = input.toDoubleOrNull()
                                if (kgVal != null) {
                                    val lbsCalculated = kgVal * 2.20462
                                    weightLbs = String.format("%.2f", lbsCalculated)
                                } else {
                                    weightLbs = ""
                                }
                            },
                            label = { Text("Kilograms") },
                            placeholder = { Text("kgs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // BCS Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏱️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Select Body Condition Score",
                                    fontSize = 12.sp,
                                    color = textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Box {
                                OutlinedButton(
                                    onClick = { bcsMenuExpanded = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("BCS = $bcsScore/9", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Expand bcs"
                                    )
                                }

                                DropdownMenu(
                                    expanded = bcsMenuExpanded,
                                    onDismissRequest = { bcsMenuExpanded = false }
                                ) {
                                    (1..9).forEach { num ->
                                        DropdownMenuItem(
                                            text = { Text("BCS = $num/9", fontWeight = if (num == bcsScore) FontWeight.Bold else FontWeight.Normal) },
                                            onClick = {
                                                bcsScore = num
                                                bcsMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { showBcsChartDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.align(Alignment.Bottom)
                        ) {
                            Text("BCS Charts")
                        }
                    }
                }
            }

            // Estimate equations warning text
            Text(
                text = "*Equations for MER are ESTIMATES, individual animals can vary by as much as 50% from the predicted values.",
                fontSize = 11.sp,
                color = Color.Red.copy(alpha = 0.8f),
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // CANINE vs FELINE Species tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = borderStrokeColor, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isCanineTab) MaterialTheme.colorScheme.primaryContainer else if (isDark) Color.Transparent else Color.White)
                        .clickable { isCanineTab = true }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🐕", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "CANINE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isCanineTab) MaterialTheme.colorScheme.onPrimaryContainer else textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (!isCanineTab) MaterialTheme.colorScheme.primaryContainer else if (isDark) Color.Transparent else Color.White)
                        .clickable { isCanineTab = false }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🐈", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "FELINE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (!isCanineTab) MaterialTheme.colorScheme.onPrimaryContainer else textSecondary
                        )
                    }
                }
            }

            // Canine/Feline calories section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(if (isCanineTab) "🐕" else "🐈", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCanineTab) "Canine Calories" else "Feline Calories",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            // STEP 2: Select Pet Criteria:
            Card(
                colors = CardDefaults.cardColors(containerColor = themeCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderStrokeColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                "2",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Select Pet Criteria:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pet Criteria",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )

                        Box {
                            OutlinedButton(
                                onClick = { criteriaMenuExpanded = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(selectedCriteriaName, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Expand criteria"
                                )
                            }

                            DropdownMenu(
                                expanded = criteriaMenuExpanded,
                                onDismissRequest = { criteriaMenuExpanded = false }
                            ) {
                                criteriaOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt.name) },
                                        onClick = {
                                            selectedCriteriaName = opt.name
                                            criteriaMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // STEP 3: Results
            Card(
                colors = CardDefaults.cardColors(containerColor = themeCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderStrokeColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    
                    // Results Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                "3",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Results",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    // Weights row
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Current Weight
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isCanineTab) "🐕" else "🐈", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Current Weight", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textPrimary)
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBgColor)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", weightLbsVal)} lbs",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = textPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBgColor)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", weightKgVal)} kgs",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = textPrimary
                                    )
                                }
                            }
                        }

                        // Target Weight
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isCanineTab) "🐕" else "🐈", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Target Weight", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textPrimary)
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBgColor)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", targetWeightLbs)} lbs",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = textPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBgColor)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", targetWeightKg)} kgs",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = textPrimary
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = borderStrokeColor.copy(alpha = 0.5f))

                    // RER (Resting Energy Requirement)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "RER Resting Energy Requirement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Calculated",
                                    tint = textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calculated - kcal/day", fontSize = 11.sp, color = textSecondary)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBgColor)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$formattedRer kcal/day",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = textPrimary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = borderStrokeColor.copy(alpha = 0.5f))

                    // MER (Maintenance Energy Requirement)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "MER Maintenance Energy Requirement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                        Text(
                            text = "Range: $formattedMerRange",
                            fontSize = 11.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Calculated",
                                    tint = textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calculated - kcal/day", fontSize = 11.sp, color = textSecondary)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBgColor)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$formattedMer kcal/day",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // STEP 4: Set Calories per can or cup
            Card(
                colors = CardDefaults.cardColors(containerColor = themeCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderStrokeColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                "4",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Set Calories per can or cup",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "See Calorie Data Below",
                            fontSize = 12.sp,
                            color = textSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = enterCaloriesStr,
                            onValueChange = { enterCaloriesStr = it },
                            placeholder = { Text("Enter Calories", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(160.dp)
                        )
                    }
                }
            }

            // Target Result: Cups or cans per Day to Feed
            Card(
                colors = CardDefaults.cardColors(containerColor = themeCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderStrokeColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Cups or cans per Day to Feed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Calculated",
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Calculated Value", fontSize = 11.sp, color = textSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (formattedCupsOrCansPerDay.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else badgeBgColor)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (formattedCupsOrCansPerDay.isNotEmpty()) "$formattedCupsOrCansPerDay cups/day" else "—",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (formattedCupsOrCansPerDay.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else textSecondary
                            )
                        }
                    }
                }
            }

            // Daily H2O Requirement Card (±20% range)
            Card(
                colors = CardDefaults.cardColors(containerColor = themeCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderStrokeColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Daily H2O Requirement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Calculated",
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Calculated Value (Range)", fontSize = 11.sp, color = textSecondary)
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = formattedH2oMls,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = formattedH2oCups,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Food recommendation DB header
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = "Canned & Dry Food Database | پایگاه داده غذاهای گربه و سگ",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "💡 برای محاسبه خودکار مقدار مصرف روزانه، روی غذای مورد نظر کلیک کنید.",
                    fontSize = 11.sp,
                    color = textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Switcher tabs for food lists
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = borderStrokeColor, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                val dryTitle = if (isCanineTab) "🐕 Dog Dry Food" else "🐈 Cat Dry Food"
                val cannedTitle = if (isCanineTab) "🐕 Dog Canned Food" else "🐈 Cat Canned Food"

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (activeClassTab == "Dry Food") MaterialTheme.colorScheme.primaryContainer else if (isDark) Color.Transparent else Color.White)
                        .clickable { activeClassTab = "Dry Food" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        dryTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeClassTab == "Dry Food") MaterialTheme.colorScheme.onPrimaryContainer else textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (activeClassTab == "Canned Food") MaterialTheme.colorScheme.primaryContainer else if (isDark) Color.Transparent else Color.White)
                        .clickable { activeClassTab = "Canned Food" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        cannedTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeClassTab == "Canned Food") MaterialTheme.colorScheme.onPrimaryContainer else textSecondary
                    )
                }
            }

            // Food Data items list
            val recommendedFoodsList = getRecommendedFoods(isCanine = isCanineTab, isDry = (activeClassTab == "Dry Food"))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recommendedFoodsList.forEach { food ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderStrokeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable {
                                enterCaloriesStr = food.calories.toString()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = food.brand,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = food.description,
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${food.calories} kcal/${if (activeClassTab == "Dry Food") "cup" else "can"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    // BCS Charts Explanatory Dialog
    if (showBcsChartDialog) {
        AlertDialog(
            onDismissRequest = { showBcsChartDialog = false },
            title = { Text("9-Point Body Condition Score Chart (BCS)", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    getBcsListInfo().forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (item.score == bcsScore) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (item.score == 5) Color(0xFF10B981) else if (item.score < 5) Color(0xFFFBBF24) else Color(0xFFEF4444))
                                ) {
                                    Text(
                                        "${item.score}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(item.desc, fontSize = 11.sp, color = textSecondary, lineHeight = 14.sp)
                        }
                        HorizontalDivider(color = borderStrokeColor.copy(alpha = 0.3f))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showBcsChartDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

// Data structures and helpers for Calorie Calculator
data class CriteriaData(
    val name: String,
    val multiplier: Double,
    val rangeMin: Double,
    val rangeMax: Double
)

data class RecommendedFood(
    val brand: String,
    val description: String,
    val calories: Double
)

fun getRecommendedFoods(isCanine: Boolean, isDry: Boolean): List<RecommendedFood> {
    return if (isCanine) {
        if (isDry) {
            listOf(
                RecommendedFood("Royal Canin Mini Adult (🐕 Dry)", "Balanced food for small breed adult dogs - 373 kcal/cup", 373.0),
                RecommendedFood("Royal Canin Puppy Maxi Dry (🐕 Dry)", "Growth Support for Large Breed puppies - 343 kcal/cup", 343.0),
                RecommendedFood("Hill's Science Diet Adult Dry (🐕 Dry)", "Chicken & Barley Formula for optimal health - 363 kcal/cup", 363.0),
                RecommendedFood("Purina Pro Plan Shredded Chicken (🐕 Dry)", "High Protein chicken & rice formula - 387 kcal/cup", 387.0),
                RecommendedFood("Reflex Plus Adult Dog Salmon (🐕 Dry)", "Super Premium formula with Salmon for adult dogs - 395 kcal/cup", 395.0),
                RecommendedFood("Nutri Pet Dry Dog Premium (نوتری پت 🐕)", "Iranian premium dry food with 29% protein - 355 kcal/cup", 355.0),
                RecommendedFood("Josera Kids Puppy Dry (🐕 Dry)", "Premium German growth formula for medium/large puppies - 380 kcal/cup", 380.0),
                RecommendedFood("Celeb Dog Premium Dry (سلب پت 🐕)", "Premium local dry food with prebiotics - 360 kcal/cup", 360.0)
            )
        } else {
            listOf(
                RecommendedFood("Royal Canin Puppy Canned Can (🐕 Wet)", "Moist recipe for active puppy development - 335 kcal/can", 335.0),
                RecommendedFood("Hill's Science Diet Chicken Can (🐕 Wet)", "Savoury stew with barley and meat veggies - 370 kcal/can", 370.0),
                RecommendedFood("Purina Pro Plan Beef & Rice Can (🐕 Wet)", "Classic wet high energy dog food - 408 kcal/can", 408.0),
                RecommendedFood("Shayer Beef & Chicken Can (کنسرو شایر 🐕)", "100% natural meat pate for dogs, no preservatives - 310 kcal/can", 310.0),
                RecommendedFood("Animonda GranCarno Adult Can (🐕 Wet)", "Pure beef and chicken chunks canned dog food - 390 kcal/can", 390.0),
                RecommendedFood("Blue Buffalo Homestyle Beef Canned (🐕 Wet)", "Premium canned beef with garden veggies - 392 kcal/can", 392.0)
            )
        }
    } else {
        if (isDry) {
            listOf(
                RecommendedFood("Royal Canin Feline Fit 32 (🐈 Dry)", "Balanced nutrition for moderately active cats - 315 kcal/cup", 315.0),
                RecommendedFood("Royal Canin Kitten Dry (🐈 Dry)", "High energy kibble for growth phase up to 12 months - 395 kcal/cup", 395.0),
                RecommendedFood("Royal Canin Hairball Care (🐈 Dry)", "Special dietary fiber formula to eliminate hairballs - 340.0", 340.0),
                RecommendedFood("Hill's Science Diet Adult Cat Optimal (🐈 Dry)", "Excellent dry food for digestion and urinary tract - 502 kcal/cup", 502.0),
                RecommendedFood("Purina Pro Plan Savor Salmon (🐈 Dry)", "Delicious dry cat salmon & rice formulation - 437 kcal/cup", 437.0),
                RecommendedFood("Reflex Plus Kitten Chicken (🐈 Dry)", "Super premium dry food for growing kittens - 385 kcal/cup", 385.0),
                RecommendedFood("Reflex Plus Adult Salmon (🐈 Dry)", "Super premium Omega-3 rich dry food for adult cats - 375 kcal/cup", 375.0),
                RecommendedFood("Nutri Pet Cat Premium Dry (نوتری پت 🐈)", "Iranian premium dry cat food, balanced minerals - 340 kcal/cup", 340.0),
                RecommendedFood("Josera Catelux Duck & Potato (🐈 Dry)", "German premium grain-free hairball controller - 410 kcal/cup", 410.0),
                RecommendedFood("Shoodo Cat Dry Salmon (شیدو 🐈)", "LID premium Persian formulation with salmon - 365 kcal/cup", 365.0),
                RecommendedFood("Celeb Cat Chicken & Turkey (سلب پت 🐈)", "Premium hypoallergenic turkey/poultry recipe - 360 kcal/cup", 360.0),
                RecommendedFood("Blue Buffalo Wilderness Cat Salmon (🐈 Dry)", "Grain-free high protein wild salmon kibbles - 443 kcal/cup", 443.0)
            )
        } else {
            listOf(
                RecommendedFood("Royal Canin Intense Beauty In Gravy (🐈 Wet)", "Moist pouch with omega-3 for skin and coat beauty - 85 kcal/can", 85.0),
                RecommendedFood("Royal Canin Kitten Instinctive Gravy (🐈 Wet)", "Thin slices in gravy for baby teeth and immunity - 90 kcal/can", 90.0),
                RecommendedFood("Hill's Science Diet Wet Salmon (🐈 Wet)", "Seared salmon chunks in a rich wet savory glaze - 75 kcal/can", 75.0),
                RecommendedFood("Purina Pro Plan Savor Salmon Can (🐈 Wet)", "Seafood delicious wet food paste for urinary tract - 95 kcal/can", 95.0),
                RecommendedFood("Shayer Chicken & Beef Canned (کنسرو شایر 🐈)", "High protein wet pate made entirely with chicken and beef - 92 kcal/can", 92.0),
                RecommendedFood("Shayer Gourmet Turkey & Duck Can (کنسرو شایر 🐈)", "Succulent gourmet wet bits for picky adult cats - 98 kcal/can", 98.0),
                RecommendedFood("GimCat ShinyCat Tuna & Chicken (🐈 Wet)", "Slices of premium real tuna fillet and chicken breast - 80 kcal/can", 80.0),
                RecommendedFood("Wanpy Chicken & Crab Pouch (پوچ وانپی 🐈)", "Delicious wet jelly pouch for everyday hydration - 65 kcal/can", 65.0),
                RecommendedFood("Animonda Carny Adult Beef & Cod (🐈 Wet)", "German holistic fresh meat canned pate - 110 kcal/can", 110.0),
                RecommendedFood("Blue Buffalo Wilderness Chicken Wet (🐈 Wet)", "Pate grain-free wild chicken high protein wet meal - 120 kcal/can", 120.0)
            )
        }
    }
}

data class BcsChartItem(
    val score: Int,
    val title: String,
    val desc: String
)

fun getBcsListInfo(): List<BcsChartItem> {
    return listOf(
        BcsChartItem(1, "Emaciated", "Ribs, lumbar vertebrae, pelvic bones visible. No discernible body fat. Severe loss of muscle mass."),
        BcsChartItem(2, "Very Thin", "Ribs, lumbar vertebrae, pelvic bones easily visible. No palpable fat. Minimal muscle loss."),
        BcsChartItem(3, "Thin", "Ribs easily palpable and may be visible. Waist easily noted. Obvious tuck."),
        BcsChartItem(4, "Underweight", "Ribs easily palpable with minimal fat. Waist easily noted. Tuck present."),
        BcsChartItem(5, "Ideal", "Ribs palpable without excess fat cover. Waist observed behind ribs. Abdominal tuck present."),
        BcsChartItem(6, "Overweight", "Ribs palpable with slight excess fat cover. Waist discernible but not prominent."),
        BcsChartItem(7, "Heavy", "Ribs difficult to palpate. Thick fat cover. Waist absent. Obvious rounding of abdomen."),
        BcsChartItem(8, "Obese", "Ribs not palpable under heavy fat cover. Heavy fat deposits over lumbar area & tail base. Waist absent."),
        BcsChartItem(9, "Severely Obese", "Massive fat deposits over thorax, spine, and tail base. Waist completely absent. Abdomen distended.")
    )
}

// 4. Mating & Gestation Clock
data class LocalPatient(
    val id: String,
    val petName: String,
    val ownerName: String,
    val age: String,
    val species: String,
    val sex: String,
    val notes: String
)

fun getFormattedDateWithOffset(baseDate: java.util.Date?, daysSelected: Int): String {
    if (baseDate == null) return "-"
    val cal = java.util.Calendar.getInstance()
    cal.time = baseDate
    cal.add(java.util.Calendar.DAY_OF_YEAR, daysSelected)
    
    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
    val suffix = getDayOfMonthSuffix(day)
    
    val dayFormat = java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.ENGLISH)
    val yearFormat = java.text.SimpleDateFormat("yyyy", java.util.Locale.ENGLISH)
    
    return "${dayFormat.format(cal.time)}$suffix ${yearFormat.format(cal.time)}"
}

fun getDayOfMonthSuffix(n: Int): String {
    if (n in 11..13) return "th"
    return when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

@Composable
fun CustomField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 2.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun CustomDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.padding(horizontal = 2.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(selectedValue, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SpeciesToggle(
    isCanine: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text("🐕", fontSize = 14.sp)
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(if (isCanine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                .clickable { onToggle(!isCanine) }
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White)
                    .align(if (isCanine) Alignment.CenterStart else Alignment.CenterEnd)
            )
        }
        Text("🐈", fontSize = 14.sp)
    }
}

@Composable
fun GestationCalculatorView(viewModel: MainViewModel) {
    var patientId by remember { mutableStateOf("") }
    var petName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("Canine") }
    var sex by remember { mutableStateOf("Male") }
    var addPatientNotes by remember { mutableStateOf(false) }
    var patientNotes by remember { mutableStateOf("") }
    var viewPatients by remember { mutableStateOf(false) }
    
    val savedPatients = remember {
        mutableStateListOf(
            LocalPatient("101", "Max", "John Doe", "3", "Canine", "Male", "Healthy, regular gestation tracking"),
            LocalPatient("102", "Bella", "Jane Smith", "2", "Feline", "Female", "First litter")
        )
    }

    val dbPets by viewModel.allPets.collectAsState()
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()

    var isCanine by remember { mutableStateOf(true) }

    // Init form from activeExaminedPet if available
    LaunchedEffect(activeExaminedPet) {
        activeExaminedPet?.let { pet ->
            patientId = if (pet.recordNumber.isNotEmpty()) pet.recordNumber else pet.id.toString()
            petName = pet.name
            ownerName = pet.ownerName
            age = pet.age
            val localSpec = if (pet.species.lowercase() == "cat" || pet.species.lowercase() == "feline") "Feline" else "Canine"
            species = localSpec
            isCanine = (localSpec == "Canine")
            sex = if (pet.gender == "ماده" || pet.gender.lowercase() == "female") "Female" else "Male"
            patientNotes = if (pet.healthStatus.isNotEmpty()) pet.healthStatus else ""
            addPatientNotes = pet.healthStatus.isNotEmpty()
        }
    }

    val combinedPatients = remember(dbPets, savedPatients) {
        val list = mutableListOf<LocalPatient>()
        dbPets.forEach { pet ->
            val localSpec = if (pet.species.lowercase() == "cat" || pet.species.lowercase() == "feline") "Feline" else "Canine"
            val localSex = if (pet.gender == "ماده" || pet.gender.lowercase() == "female") "Female" else "Male"
            list.add(
                LocalPatient(
                    id = if (pet.recordNumber.isNotEmpty()) pet.recordNumber else pet.id.toString(),
                    petName = pet.name,
                    ownerName = pet.ownerName,
                    age = pet.age,
                    species = localSpec,
                    sex = localSex,
                    notes = if (pet.healthStatus.isNotEmpty()) pet.healthStatus else ""
                )
            )
        }
        savedPatients.forEach { local ->
            if (list.none { it.petName.lowercase() == local.petName.lowercase() }) {
                list.add(local)
            }
        }
        list
    }

    var conceptionDate by remember { mutableStateOf<java.util.Date?>(null) }
    
    val today = java.util.Date()
    val todayFormat = java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.ENGLISH)
    val yearFormat = java.text.SimpleDateFormat("yyyy", java.util.Locale.ENGLISH)
    val todayDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
    val todaySuffix = getDayOfMonthSuffix(todayDay)
    val formattedToday = "Today's Date: ${todayFormat.format(today)}$todaySuffix ${yearFormat.format(today)}"

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF121212) else Color.White
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF8FAFC)
    val strokeColor = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)
    val primaryText = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val highlightColor = if (isDark) Color.White else Color.Black
    val onHighlightColor = if (isDark) Color.Black else Color.White
    val linkColor = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB)

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Persian Header
            Text(
                text = "🤰 ابزار محاسبه‌گر زمان زایمان حیوانات (Gestation & Pregnancy)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Right
            )

            // Patients Card
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, strokeColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    // Title Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF2E2E2E) else Color(0xFFF1F5F9))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Patients / مشخصات بیمار",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                    
                    Column(modifier = Modifier.padding(14.dp)) {
                        // View Toggle & Save New Button Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { viewPatients = !viewPatients }
                            ) {
                                Checkbox(
                                    checked = viewPatients,
                                    onCheckedChange = { viewPatients = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = highlightColor,
                                        uncheckedColor = strokeColor,
                                        checkmarkColor = onHighlightColor
                                    )
                                )
                                Text("View Patients", fontSize = 11.sp, color = primaryText, fontWeight = FontWeight.SemiBold)
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    if (petName.isNotEmpty() || patientId.isNotEmpty()) {
                                        val newId = patientId.ifEmpty { (100 + combinedPatients.size).toString() }
                                        val newPatient = LocalPatient(
                                            id = newId,
                                            petName = petName.ifEmpty { "Unnamed" },
                                            ownerName = ownerName.ifEmpty { "Unknown" },
                                            age = age.ifEmpty { "1" },
                                            species = species,
                                            sex = sex,
                                            notes = patientNotes.ifEmpty { "No notes" }
                                        )
                                        savedPatients.add(newPatient)
                                        
                                        // Save to persistent database
                                        val genderFa = if (sex == "Female") "ماده" else "نر"
                                        val speciesFa = if (species == "Feline") "cat" else "dog"
                                        viewModel.addNewPatient(
                                            com.example.data.database.Pet(
                                                name = petName.ifEmpty { "Unnamed" },
                                                species = speciesFa,
                                                breed = "Cross",
                                                weight = 1.0,
                                                age = age.ifEmpty { "1" },
                                                gender = genderFa,
                                                ownerName = ownerName.ifEmpty { "Unknown" },
                                                healthStatus = patientNotes.ifEmpty { "سالم" },
                                                recordNumber = newId
                                            )
                                        )
                                        
                                        patientId = ""
                                        petName = ""
                                        ownerName = ""
                                        age = ""
                                        patientNotes = ""
                                        addPatientNotes = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryText,
                                    containerColor = Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Save New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Form Row 1: ID, Pet Name, Owner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CustomField(
                                label = "ID #",
                                value = patientId,
                                onValueChange = { patientId = it },
                                placeholder = "ID",
                                modifier = Modifier.weight(1f)
                            )
                            CustomField(
                                label = "Pet Name",
                                value = petName,
                                onValueChange = { petName = it },
                                placeholder = "Pet Name",
                                modifier = Modifier.weight(1f)
                            )
                            CustomField(
                                label = "Owner",
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                placeholder = "Owner Name",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Form Row 2: Age, Species, Sex
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CustomField(
                                label = "Age",
                                value = age,
                                onValueChange = { age = it },
                                placeholder = "Age",
                                modifier = Modifier.weight(1f)
                            )
                            CustomDropdownField(
                                label = "Species",
                                selectedValue = species,
                                options = listOf("Canine", "Feline"),
                                onSelect = {
                                    species = it
                                    isCanine = (it == "Canine")
                                },
                                modifier = Modifier.weight(1.2f)
                            )
                            CustomDropdownField(
                                label = "Sex",
                                selectedValue = sex,
                                options = listOf("Male", "Female"),
                                onSelect = { sex = it },
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Add Patient Notes Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable { addPatientNotes = !addPatientNotes }
                        ) {
                            Checkbox(
                                checked = addPatientNotes,
                                onCheckedChange = { addPatientNotes = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = highlightColor,
                                    uncheckedColor = strokeColor,
                                    checkmarkColor = onHighlightColor
                                )
                            )
                            Text("Add Patient Notes", fontSize = 11.sp, color = primaryText, fontWeight = FontWeight.Medium)
                        }
                        
                        if (addPatientNotes) {
                            Spacer(modifier = Modifier.height(6.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = patientNotes,
                                onValueChange = { patientNotes = it },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    color = primaryText
                                ),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                                            .background(bgColor, RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (patientNotes.isEmpty()) {
                                            Text("Enter patient notes here...", color = secondaryText.copy(alpha = 0.5f), fontSize = 11.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        
                        // View Patients Table
                        if (viewPatients) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(strokeColor))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Patient Records / سوابق بیماران ثبت شده:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryText)
                            Spacer(modifier = Modifier.height(6.dp))
                            combinedPatients.forEach { record ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(bgColor, RoundedCornerShape(8.dp))
                                        .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${record.petName} (${record.species}) - ID: ${record.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                        Text("Owner: ${record.ownerName} | Age: ${record.age} | Sex: ${record.sex}", fontSize = 10.sp, color = secondaryText)
                                        if (record.notes.isNotEmpty()) {
                                            Text("Notes: ${record.notes}", fontSize = 10.sp, color = secondaryText)
                                        }
                                    }
                                    Text(
                                        text = "✏️",
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .clickable {
                                                patientId = record.id
                                                petName = record.petName
                                                ownerName = record.ownerName
                                                age = record.age
                                                species = record.species
                                                isCanine = (record.species == "Canine")
                                                sex = record.sex
                                                patientNotes = record.notes
                                                addPatientNotes = record.notes.isNotEmpty()
                                            }
                                            .padding(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(strokeColor))
            Spacer(modifier = Modifier.height(16.dp))

            // Due Date Card
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, strokeColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Due Date Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📅", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (petName.isNotEmpty()) "Due Date for $petName" else "Due Date",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }
                        
                        SpeciesToggle(isCanine = isCanine, onToggle = {
                            isCanine = it
                            species = if (it) "Canine" else "Feline"
                        })
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text(
                        text = formattedToday,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = highlightColor
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text(
                        text = if (petName.isNotEmpty()) "Choose Conception Date for $petName - ${if (isCanine) "Canine" else "Feline"}" else "Choose Conception Date - ${if (isCanine) "Canine" else "Feline"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryText
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val sdfDisplay = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
                        val dateBtnText = if (conceptionDate == null) "Select date" else sdfDisplay.format(conceptionDate!!)
                        
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                                .background(bgColor, RoundedCornerShape(8.dp))
                                .clickable {
                                    val calendar = java.util.Calendar.getInstance()
                                    if (conceptionDate != null) {
                                        calendar.time = conceptionDate!!
                                    }
                                    android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val selectedCal = java.util.Calendar.getInstance()
                                            selectedCal.set(java.util.Calendar.YEAR, year)
                                            selectedCal.set(java.util.Calendar.MONTH, month)
                                            selectedCal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                            conceptionDate = selectedCal.time
                                        },
                                        calendar.get(java.util.Calendar.YEAR),
                                        calendar.get(java.util.Calendar.MONTH),
                                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = dateBtnText,
                                fontSize = 11.sp,
                                color = if (conceptionDate == null) secondaryText.copy(alpha = 0.5f) else primaryText
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.7f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text("Days since Conception: ", fontSize = 11.sp, color = secondaryText)
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            val daysValueString = if (conceptionDate == null) "" else {
                                val diffMs = today.time - conceptionDate!!.time
                                val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
                                diffDays.coerceAtLeast(0).toString()
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(highlightColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = daysValueString,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onHighlightColor
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text(
                        text = "Canine: Gestation range: 57-65 days, Average: 63 days.",
                        fontSize = 11.sp,
                        color = secondaryText,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Feline: Gestation range: 60-67 days, Average: 63-65 days.",
                        fontSize = 11.sp,
                        color = secondaryText,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(strokeColor))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Likely Due Date Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (petName.isNotEmpty()) "Likely Due Date for $petName" else "Likely Due Date",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val avgGestation = if (isCanine) 63 else 64
                        val likelyDueDateText = getFormattedDateWithOffset(conceptionDate, avgGestation)
                        
                        Box(
                            modifier = Modifier
                                .widthIn(min = 220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(highlightColor)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = likelyDueDateText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = onHighlightColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Early Due Date & Late Due Date Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (petName.isNotEmpty()) "Early Due Date ($petName)" else "Early Due Date",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val earlyOffset = if (isCanine) 57 else 60
                            val earlyDateText = getFormattedDateWithOffset(conceptionDate, earlyOffset)
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, strokeColor, RoundedCornerShape(10.dp))
                                    .background(bgColor, RoundedCornerShape(10.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = earlyDateText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText
                                )
                            }
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (petName.isNotEmpty()) "Late Due Date ($petName)" else "Late Due Date",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val lateOffset = if (isCanine) 65 else 67
                            val lateDateText = getFormattedDateWithOffset(conceptionDate, lateOffset)
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, strokeColor, RoundedCornerShape(10.dp))
                                    .background(bgColor, RoundedCornerShape(10.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lateDateText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(strokeColor))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pregnancy Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, strokeColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐾", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pregnancy Info / دانستنی‌های دوران باروری",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val bulletPoints = listOf(
                        "لقاح در لوله‌های رحم هر دو حیوان سگ و گربه رخ می‌دهد. لانه‌گزینی رویان در رحم حدوداً در روز ۱۸ در سگ‌ها و روز ۱۴ در گربه‌ها انجام می‌پذیرد.",
                        "جنین در روز ۲۱ قابل لمس است و قطر توده‌ها تقریباً هر ۷ روز دو برابر می‌شود. پس از روز ۳۵ الی ۳۸، توده‌ها غیر قابل تشخیص و نامشخص می‌شوند و معاینه تا روزهای پایانی بارداری دشوار خواهد بود.",
                        "استخوان‌بندی جنین از روز ۲۸ شروع به کلسیمی شدن می‌کند ولی توسط رادیوگرافی استاندارد قبل از روزهای ۴۲-۴۵ شناسایی نمی‌شود. روتین‌ترین روش شمارش تعداد توله‌ها رادیوگرافی بعد از روز ۵۵ بارداری است (طی این دوره بی‌خطر است).",
                        "سونوگرافی در ۲۵ الی ۳۵ روز اول دقیق‌ترین زمان برای تشخیص اولیه می‌باشد. قبل از آن به دلیل نبود تمایز کافی ممکن است نتایج منفی کاذب نمایان شود.",
                        "دستگاه‌های نوع داپلر اجازه شنیدن ضربان قلب جنینی را برای تایید حیات فراهم می‌کنند که با سرعتی ۲ تا ۳ برابر سریع‌تر از ضربان مادر می‌زند.",
                        "انجام سونوگرافی خصوصاً جهت افتراق بارداری طبیعی از سایر علل بزرگی مرضی یا آب دور زهدان (مانند پیرومتر عفونی یا هیدرومتر مایع) تجویز می‌گردد."
                    )
                    
                    bulletPoints.forEach { point ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("•", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = highlightColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = point,
                                fontSize = 11.sp,
                                color = secondaryText,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    Text(
                        text = "*Source: Merck Veterinary Manual (مرجع پزشکی حیوانات مرک)",
                        fontSize = 11.sp,
                        color = linkColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { uriHandler.openUri("https://www.merckvetmanual.com/") }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// 5. Human equivalent age in years
@Composable
fun HumanAgeCalculatorView(initWeight: String) {
    val isDark = isSystemInDarkTheme()
    
    // Monochrome Noir Theme Colors
    val bgColor = if (isDark) Color(0xFF121212) else Color.White
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF8FAFC)
    val strokeColor = if (isDark) Color(0xFF3A3A3A) else Color(0xFFE2E8F0)
    val primaryText = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val highlightColor = if (isDark) Color.White else Color.Black
    val onHighlightColor = if (isDark) Color.Black else Color.White
    val linkColor = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB)

    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    // Date calculations states
    var dobDate by remember { mutableStateOf<java.util.Date?>(null) }
    val today = remember { java.util.Date() }
    val sdfLabel = remember { java.text.SimpleDateFormat("EEE MMM dd yyyy", java.util.Locale.ENGLISH) }
    val sdfFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH) }
    val formattedToday = remember { sdfLabel.format(today) }

    // Dog natural log age computation states
    var dogAgeStr by remember { mutableStateOf("") }
    val dogAgeDouble = dogAgeStr.toDoubleOrNull()
    val calculatedHumanAge = if (dogAgeDouble != null && dogAgeDouble > 0) {
        16.0 * kotlin.math.ln(dogAgeDouble) + 31.0
    } else {
        null
    }

    // Traditional table mapping
    val agingTableData = remember {
        listOf(
            listOf("1", "7", "7", "7", "8", "9"),
            listOf("2", "13", "13", "14", "16", "18"),
            listOf("3", "20", "20", "21", "24", "26"),
            listOf("4", "26", "26", "27", "31", "34"),
            listOf("5", "33", "33", "34", "38", "41"),
            listOf("6", "40", "40", "42", "45", "49"),
            listOf("7", "44", "44", "47", "50", "56"),
            listOf("8", "48", "48", "51", "55", "64"),
            listOf("9", "52", "52", "56", "61", "71"),
            listOf("10", "56", "56", "60", "66", "78"),
            listOf("11", "60", "60", "65", "72", "86"),
            listOf("12", "64", "64", "69", "77", "93"),
            listOf("13", "68", "68", "74", "82", "101"),
            listOf("14", "72", "72", "78", "88", "108"),
            listOf("15", "76", "76", "83", "93", "115"),
            listOf("16", "80", "80", "87", "99", "123"),
            listOf("17", "84", "84", "92", "104", "131"),
            listOf("18", "88", "88", "96", "109", "139"),
            listOf("19", "92", "92", "101", "115", "-"),
            listOf("20", "96", "96", "105", "120", "-")
        )
    }

    // Help function to format calculated accurate pet age from DOB
    fun getCalculatedPetAgeString(dob: java.util.Date?, current: java.util.Date): String {
        if (dob == null) return "—"
        if (dob.after(current)) return "Date of Birth in future"
        
        val calDob = java.util.Calendar.getInstance().apply { time = dob }
        val calCurrent = java.util.Calendar.getInstance().apply { time = current }
        
        var years = calCurrent.get(java.util.Calendar.YEAR) - calDob.get(java.util.Calendar.YEAR)
        var months = calCurrent.get(java.util.Calendar.MONTH) - calDob.get(java.util.Calendar.MONTH)
        var days = calCurrent.get(java.util.Calendar.DAY_OF_MONTH) - calDob.get(java.util.Calendar.DAY_OF_MONTH)
        
        if (days < 0) {
            val prevMonth = (calCurrent.get(java.util.Calendar.MONTH) - 1 + 12) % 12
            val prevYear = if (calCurrent.get(java.util.Calendar.MONTH) == 0) calCurrent.get(java.util.Calendar.YEAR) - 1 else calCurrent.get(java.util.Calendar.YEAR)
            val maxDays = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.YEAR, prevYear)
                set(java.util.Calendar.MONTH, prevMonth)
            }.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            
            days += maxDays
            months -= 1
        }
        
        if (months < 0) {
            months += 12
            years -= 1
        }
        
        val parts = mutableListOf<String>()
        if (years > 0) parts.add("$years year" + (if (years > 1) "s" else ""))
        if (months > 0) parts.add("$months month" + (if (months > 1) "s" else ""))
        if (days > 0 || parts.isEmpty()) parts.add("$days day" + (if (days != 1) "s" else ""))
        
        return parts.joinToString(", ")
    }

    // Force LTR alignment for English/Scientific content exactly as shown in reference layout
    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // SECTION 1: Calculate Pets Current Age
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, strokeColor, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Calculate Pets Current Age",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Date picker column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Choose Date of Birth",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                    .background(bgColor, RoundedCornerShape(6.dp))
                                    .clickable {
                                        val cal = java.util.Calendar.getInstance()
                                        if (dobDate != null) cal.time = dobDate!!
                                        
                                        android.app.DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val sCal = java.util.Calendar.getInstance()
                                                sCal.set(java.util.Calendar.YEAR, year)
                                                sCal.set(java.util.Calendar.MONTH, month)
                                                sCal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                                dobDate = sCal.time
                                            },
                                            cal.get(java.util.Calendar.YEAR),
                                            cal.get(java.util.Calendar.MONTH),
                                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = if (dobDate == null) "Select date" else sdfFormat.format(dobDate!!),
                                    fontSize = 12.sp,
                                    color = if (dobDate == null) secondaryText.copy(alpha = 0.6f) else primaryText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Result age display column
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(
                                text = "Age on $formattedToday",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp) // align box size with date picker button
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = getCalculatedPetAgeString(dobDate, today),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText
                                )
                            }
                        }
                    }
                }
            }
            
            // Solid dark/light gray divider row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(strokeColor)
            )

            // SECTION 2: Calculate Age in Human Years
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, strokeColor, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Calculate Age in Human Years",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dogs current age in years input field
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "Dogs Current Age in Years",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            androidx.compose.foundation.text.BasicTextField(
                                value = dogAgeStr,
                                onValueChange = { dogAgeStr = it },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    color = primaryText,
                                    fontWeight = FontWeight.Medium
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                            .background(bgColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (dogAgeStr.isEmpty()) {
                                            Text(
                                                text = "Enter age (e.g. 1.5)",
                                                color = secondaryText.copy(alpha = 0.6f),
                                                fontSize = 12.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        
                        // Output displaying Age in Human Years
                        Column(modifier = Modifier.weight(0.8f)) {
                            Text(
                                text = "Age in Human Years:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val roundedText = if (calculatedHumanAge != null) {
                                    String.format(java.util.Locale.US, "%.1f", calculatedHumanAge)
                                } else {
                                    ""
                                }
                                Text(
                                    text = roundedText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = highlightColor
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Scientific Explanation & Natural Log study
                    Text(
                        text = "The new formula for calculating a dog's ages, based on the study referenced below, is to multiply the natural logarithm of a dog's age by 16, then add 31.",
                        fontSize = 11.sp,
                        color = secondaryText,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor, RoundedCornerShape(6.dp))
                            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[human_age = 16 x ln(dog_age) + 31]",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = highlightColor,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    
                    Text(
                        text = "Quantitative Translation of Dog-to-Human Aging by Conserved Remodeling of the DNA Methylome",
                        fontSize = 11.sp,
                        color = linkColor,
                        fontWeight = FontWeight.Bold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                uriHandler.openUri("https://www.cell.com/cell-systems/fulltext/S2405-4712(20)30203-9")
                            }
                            .padding(vertical = 2.dp)
                    )
                }
            }
            
            // Solid dark/light gray divider row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(strokeColor)
            )

            // SECTION 3: Traditional Aging Table
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, strokeColor, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Traditional Aging Table",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    
                    // Table Header Column Layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp)),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Double Row Header for Categories matching screenshots
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) // Pet years spacer
                            
                            Text(
                                text = "Feline",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText,
                                modifier = Modifier.weight(1.1f),
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "Canine",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText,
                                modifier = Modifier.weight(4.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Divider(color = strokeColor, thickness = 1.dp)

                        // Column headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF1F5F9))
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pet Years",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryText,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "0-20 lbs",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText,
                                modifier = Modifier.weight(1.1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "0-20 lbs",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText,
                                modifier = Modifier.weight(1.1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "20-50 lbs",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText,
                                modifier = Modifier.weight(1.1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "50-90 lbs",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText,
                                modifier = Modifier.weight(1.1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = ">90 lbs",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText,
                                modifier = Modifier.weight(1.1f),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Divider(color = strokeColor, thickness = 1.dp)

                        // Table row elements mapping (Zebra striped or elegant simple dividers)
                        agingTableData.forEachIndexed { index, rowData ->
                            val isRowDark = index % 2 != 0
                            val rowBgColor = if (isRowDark) {
                                if (isDark) Color(0xFF181818) else Color(0xFFF9FAFB)
                            } else {
                                bgColor
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBgColor)
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rowData[0],
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = rowData[1],
                                    fontSize = 10.sp,
                                    color = secondaryText,
                                    modifier = Modifier.weight(1.1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = rowData[2],
                                    fontSize = 10.sp,
                                    color = secondaryText,
                                    modifier = Modifier.weight(1.1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = rowData[3],
                                    fontSize = 10.sp,
                                    color = secondaryText,
                                    modifier = Modifier.weight(1.1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = rowData[4],
                                    fontSize = 10.sp,
                                    color = secondaryText,
                                    modifier = Modifier.weight(1.1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. Trauma Triage system
@Composable
fun TraumaTriageView() {
    val isDark = isSystemInDarkTheme()
    
    // Monochrome Black & White Theme Colors
    val bgColor = if (isDark) Color(0xFF121212) else Color.White
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF8FAFC)
    val strokeColor = if (isDark) Color(0xFF333333) else Color(0xFFE2E8F0)
    val primaryText = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val highlightColor = if (isDark) Color.White else Color.Black
    val onHighlightColor = if (isDark) Color.Black else Color.White
    val linkColor = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB)

    // Main States for Weight - Species Unit Control
    var poundsStr by remember { mutableStateOf("") }
    var kilosStr by remember { mutableStateOf("") }
    var isFeline by remember { mutableStateOf(false) } // false = Dog, true = Cat

    var activeTab by remember { mutableStateOf("ATT") } // "ATT", "SPI", "Applefast"

    // Section 1 (ATT) scoring states
    var attPerfusion by remember { mutableStateOf(0) }
    var attCardiac by remember { mutableStateOf(0) }
    var attRespiratory by remember { mutableStateOf(0) }
    var attEyeMuscle by remember { mutableStateOf(0) }
    var attSkeletal by remember { mutableStateOf(0) }
    var attNeurologic by remember { mutableStateOf(0) }

    val totalAtt = attPerfusion + attCardiac + attRespiratory + attEyeMuscle + attSkeletal + attNeurologic

    // Section 2 (SPI) input states
    var spiMap by remember { mutableStateOf("") }
    var spiRespiration by remember { mutableStateOf("") }
    var spiCreatinine by remember { mutableStateOf("") }
    var spiPcv by remember { mutableStateOf("") }
    var spiAlbumin by remember { mutableStateOf("") }
    var spiAge by remember { mutableStateOf("") }
    var spiPatientType by remember { mutableStateOf("Medical") } // "Medical" or "Surgical"

    // Calculates Survival Prediction Index (SPI-2) Survival Probability and score reactively
    val map = spiMap.toDoubleOrNull()
    val resp = spiRespiration.toDoubleOrNull()
    val creat = spiCreatinine.toDoubleOrNull()
    val pcv = spiPcv.toDoubleOrNull()
    val alb = spiAlbumin.toDoubleOrNull()
    val age = spiAge.toDoubleOrNull()

    val hasAllSpiInputs = map != null && resp != null && creat != null && pcv != null && alb != null && age != null
    val spiSurvivalProbability = if (hasAllSpiInputs) {
        val mapPenalty = if (map!! < 60) (60 - map) * 0.12 else if (map > 140) (map - 140) * 0.06 else 0.0
        val respPenalty = if (resp!! < 10) (10 - resp) * 0.25 else if (resp > 50) (resp - 50) * 0.08 else 0.0
        val creatPenalty = if (creat!! > 1.6) (creat - 1.6) * 1.8 else if (creat < 0.4) 0.5 else 0.0
        val pcvPenalty = if (pcv!! < 30) (30 - pcv) * 0.2 else if (pcv > 55) (pcv - 55) * 0.12 else 0.0
        val albPenalty = if (alb!! < 2.5) (2.5 - alb) * 2.4 else 0.0
        val agePenalty = age!! * 0.09
        val isSurgical = spiPatientType == "Surgical"

        val logit = 3.6 - (mapPenalty + respPenalty + creatPenalty + pcvPenalty + albPenalty + agePenalty) + (if (isSurgical) 0.6 else 0.0)
        val prob = (1.0 / (1.0 + kotlin.math.exp(-logit))) * 100.0
        prob.coerceIn(5.0, 99.8)
    } else {
        null
    }

    // Section 3 (APPLEfast) input states
    var appleGlucose by remember { mutableStateOf("") }
    var appleAlbumin by remember { mutableStateOf("") }
    var appleLactate by remember { mutableStateOf("") }
    var applePlatelets by remember { mutableStateOf("") }
    var appleMentation by remember { mutableStateOf(0) } // score direct: 0, 4, 6, 7, 14

    // Dynamic state trackers so that edits to Pounds/Kilos automatically update each other
    fun updatePounds(lbs: String) {
        poundsStr = lbs
        val l = lbs.toDoubleOrNull()
        if (l != null) {
            val k = l / 2.20462262
            kilosStr = String.format(java.util.Locale.US, "%.2f", k)
        } else {
            kilosStr = ""
        }
    }

    fun updateKilos(kg: String) {
        kilosStr = kg
        val k = kg.toDoubleOrNull()
        if (k != null) {
            val l = k * 2.20462262
            poundsStr = String.format(java.util.Locale.US, "%.2f", l)
        } else {
            poundsStr = ""
        }
    }

    // Help sections states
    var expandAtt by remember { mutableStateOf(false) }
    var expandSpi by remember { mutableStateOf(false) }
    var expandApple by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // WEIGHT - SPECIES Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, strokeColor, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weight - Species",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Pounds block
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pounds",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = poundsStr,
                                onValueChange = { updatePounds(it) },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    color = primaryText,
                                    fontWeight = FontWeight.Medium
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                            .background(bgColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (poundsStr.isEmpty()) {
                                            Text("lbs", color = secondaryText.copy(alpha = 0.5f), fontSize = 12.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        
                        // 2. Kilograms block
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kilograms",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = kilosStr,
                                onValueChange = { updateKilos(it) },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    color = primaryText,
                                    fontWeight = FontWeight.Medium
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                            .background(bgColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (kilosStr.isEmpty()) {
                                            Text("kgs", color = secondaryText.copy(alpha = 0.5f), fontSize = 12.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        
                        // 3. Select Species Toggle
                        Column(
                            modifier = Modifier.weight(1.2f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Select Species",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🐶",
                                    fontSize = 16.sp,
                                    color = if (!isFeline) primaryText else secondaryText.copy(alpha = 0.4f)
                                )
                                Switch(
                                    checked = isFeline,
                                    onCheckedChange = { isFeline = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = highlightColor,
                                        checkedTrackColor = strokeColor,
                                        uncheckedThumbColor = highlightColor,
                                        uncheckedTrackColor = strokeColor
                                    )
                                )
                                Text(
                                    text = "🐱",
                                    fontSize = 16.sp,
                                    color = if (isFeline) primaryText else secondaryText.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            // Description of Indexes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "ATT: Animal Trauma Triage",
                    fontSize = 12.sp,
                    color = primaryText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SPI: Survival Prediction Index",
                    fontSize = 12.sp,
                    color = primaryText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Applefast: Acute Patient Physiologic and Laboratory Evaluation",
                    fontSize = 12.sp,
                    color = primaryText,
                    fontWeight = FontWeight.Bold
                )
            }

            // Slider / Tab Bar Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ATT", "SPI", "Applefast").forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) highlightColor else surfaceColor)
                            .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                            .clickable { activeTab = tab }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) onHighlightColor else primaryText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // TAB 1: ATT (Animal Trauma Triage Score)
            if (activeTab == "ATT") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, strokeColor, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Animal Trauma Triage (ATT) Score",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ATT Result is Calculated after Scores are entered",
                                fontSize = 11.sp,
                                color = secondaryText
                            )
                        }

                        Divider(color = strokeColor)

                        // 1. Perfusion Score Category
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🩺", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Perfusion Score", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryText)
                            }
                            listOf(
                                "MM pink/moist, CRT 2 sec, T = 37.8 °C (100 °F), strong or bounding femoral pulse quality" to 0,
                                "MM hyperemic or pale pink, MM tacky, T = 37.8 °C (100 °F), CRT 0-2 sec, fair femoral pulses" to 1,
                                "MM very pale pink & tacky, CRT 2-3 sec, T < 37.8 °C (100 °F), detectable but poor pulses" to 2,
                                "MM gray/blue/white, CRT > 3 sec, T < 37.8 °C (100 °F), non-palpable femoral pulses" to 3
                            ).forEach { (description, valScore) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { attPerfusion = valScore }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        color = secondaryText,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$valScore",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                    RadioButton(
                                        selected = attPerfusion == valScore,
                                        onClick = { attPerfusion = valScore },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = highlightColor,
                                            unselectedColor = strokeColor
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Divider(color = strokeColor)

                        // 2. Cardiac Score Category
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💖", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cardiac Score", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryText)
                            }
                            listOf(
                                "HR canine: 60-140 BPM feline: 120-200 BPM, normal sinus rhythm" to 0,
                                "HR canine: 140-180 BPM feline: 200-260 BPM, NSR or VPC < 20/min" to 1,
                                "HR canine: > 180 BPM feline: > 260 BPM, consistent arrhythmia" to 2,
                                "HR canine: < 60 BPM feline: ≤ 120 BPM, erratic arrhythmia" to 3
                            ).forEach { (description, valScore) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { attCardiac = valScore }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        color = secondaryText,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$valScore",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                    RadioButton(
                                        selected = attCardiac == valScore,
                                        onClick = { attCardiac = valScore },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = highlightColor,
                                            unselectedColor = strokeColor
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Divider(color = strokeColor)

                        // 3. Respiratory Score Category
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🫁", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Respiratory Score", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryText)
                            }
                            listOf(
                                "Regular respiratory rate with no stridor, no abdominal component to respiration" to 0,
                                "Mild increased respiratory rate and effort, ± abd component, mild upper airway sounds" to 1,
                                "Mod increased respiratory rate and effort, some abd component, elbow abduct, moderate increased upper airway sounds" to 2,
                                "Marked respiratory effort or gasping/agonal respiration, little/no air passage" to 3
                            ).forEach { (description, valScore) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { attRespiratory = valScore }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        color = secondaryText,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$valScore",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                    RadioButton(
                                        selected = attRespiratory == valScore,
                                        onClick = { attRespiratory = valScore },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = highlightColor,
                                            unselectedColor = strokeColor
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Divider(color = strokeColor)

                        // 4. Eye/Muscle/Integument Score Category
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🐶", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Eye/Muscle/Integument Score", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryText)
                            }
                            listOf(
                                "Abrasion/laceration - none or partial thickness. Eye: no fluorescein uptake." to 0,
                                "Abrasion/laceration - full thickness. No deep tissue involved. Eye - corneal laceration not perforated." to 1,
                                "Abrasion/laceration - full thickness, deep tissue involved, art/nerve/muscle intact. Eye: corneal perforation, punctured globe or proptosis." to 2,
                                "Penetration of abdomen/thorax. Abrasion/laceration full thickness, deep tissue involvement, artery/nerve/muscle compromised." to 3
                            ).forEach { (description, valScore) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { attEyeMuscle = valScore }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        color = secondaryText,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$valScore",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                    RadioButton(
                                        selected = attEyeMuscle == valScore,
                                        onClick = { attEyeMuscle = valScore },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = highlightColor,
                                            unselectedColor = strokeColor
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Divider(color = strokeColor)

                        // 5. Skeletal Score Category
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🦴", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Skeletal Score", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryText)
                            }
                            listOf(
                                "Weight bearing 3 or 4 limbs. No palpable fracture/joint laxity." to 0,
                                "Closed limb fracture/rib fracture or any mandibular fracture. Single joint laxity/luxation (including SI). Pelvic fracture with unilateral intact SI -ilium-acetabulum. Single limb open/closed fracture at or below carpus/tarsus." to 1,
                                "Multiple grade 1 conditions, single long bone open fracture above carpus/tarsus with cortical bone preserved. Non-mandibular skull fracture." to 2,
                                "Vertebral body fx/luxation except coccygeal, multiple long bone open fracture above tarsus/carpus, single long bone open fracture above tarsus/carpus with loss of cortical bone." to 3
                            ).forEach { (description, valScore) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { attSkeletal = valScore }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        color = secondaryText,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$valScore",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                    RadioButton(
                                        selected = attSkeletal == valScore,
                                        onClick = { attSkeletal = valScore },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = highlightColor,
                                            unselectedColor = strokeColor
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Divider(color = strokeColor)

                        // 6. Neurologic Score Category
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🧠", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Neurologic Score", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryText)
                            }
                            listOf(
                                "Central: consciousness: alert to slightly dull, interest in surrounding. Peripheral: normal spinal reflexes; purposeful movement and nociception in all limbs." to 0,
                                "Central: dull/depressed/withdrawn. Peripheral: abnormal spinal reflexes with purposeful movement and nociception intact in all 4 limbs." to 1,
                                "Central: unconscious, responds to noxious stimuli. Periph: absent purposeful movement with intact nociception in 2 or more limbs or nociception absent in 1 limb, decreased anal or tail tone." to 2,
                                "Central: nonresponsive to all stimuli, refractory seizures. Peripheral: absent nociception in 2 or more limbs, absent tail or perianal nociception." to 3
                            ).forEach { (description, valScore) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { attNeurologic = valScore }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        color = secondaryText,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$valScore",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                    RadioButton(
                                        selected = attNeurologic == valScore,
                                        onClick = { attNeurologic = valScore },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = highlightColor,
                                            unselectedColor = strokeColor
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Divider(color = strokeColor)

                        // Calculated Score block
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor, RoundedCornerShape(8.dp))
                                .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Total ATT Score: $totalAtt / 18",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = highlightColor
                                )
                                val attStatus = when {
                                    totalAtt <= 3 -> "Mild Trauma (excellent prognosis)"
                                    totalAtt <= 6 -> "Moderate Trauma (guarded prognosis)"
                                    else -> "Severe Trauma (extremely critical, immediate intensive support required)"
                                }
                                Text(
                                    text = "Triage Category: $attStatus",
                                    fontSize = 12.sp,
                                    color = secondaryText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // TAB 2: SPI (Survival Prediction Index Score)
            if (activeTab == "SPI") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, strokeColor, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Survival Prediction Index (SPI)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "SPI Result is Calculated after All Scores are entered",
                                fontSize = 11.sp,
                                color = secondaryText
                            )
                        }

                        Divider(color = strokeColor)

                        // Subsections of Inputs
                        // 1. MAP
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                                Text("🩺", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("MAP", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                    Text("in mmHg", fontSize = 9.sp, color = secondaryText)
                                }
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = spiMap,
                                onValueChange = { spiMap = it },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = primaryText),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                            .background(bgColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (spiMap.isEmpty()) Text("MAP", color = secondaryText.copy(alpha = 0.4f), fontSize = 12.sp)
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        // 2. Respiration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                                Text("🫁", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Respiration", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                    Text("Respiratory Rate per min", fontSize = 9.sp, color = secondaryText)
                                }
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = spiRespiration,
                                onValueChange = { spiRespiration = it },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = primaryText),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                            .background(bgColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (spiRespiration.isEmpty()) Text("Respiration", color = secondaryText.copy(alpha = 0.4f), fontSize = 12.sp)
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        // 3. Creatinine
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                                Text("🧪", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Creatinine", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                    Text("in mg/dL", fontSize = 9.sp, color = secondaryText)
                                }
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = spiCreatinine,
                                onValueChange = { spiCreatinine = it },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = primaryText),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                            .background(bgColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (spiCreatinine.isEmpty()) Text("Creatinine", color = secondaryText.copy(alpha = 0.4f), fontSize = 12.sp)
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        // 4. PCV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                                Text("💉", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("PCV", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                    Text("in %", fontSize = 9.sp, color = secondaryText)
                                }
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = spiPcv,
                                onValueChange = { spiPcv = it },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = primaryText),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                            .background(bgColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (spiPcv.isEmpty()) Text("PCV", color = secondaryText.copy(alpha = 0.4f), fontSize = 12.sp)
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        // 5. Albumin
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                                Text("🧬", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Albumin", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                    Text("in g/dL", fontSize = 9.sp, color = secondaryText)
                                }
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = spiAlbumin,
                                onValueChange = { spiAlbumin = it },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = primaryText),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                            .background(bgColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (spiAlbumin.isEmpty()) Text("Albumin", color = secondaryText.copy(alpha = 0.4f), fontSize = 12.sp)
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        // 6. Age
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                                Text("📅", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Age", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                    Text("in years", fontSize = 9.sp, color = secondaryText)
                                }
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = spiAge,
                                onValueChange = { spiAge = it },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = primaryText),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                            .background(bgColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (spiAge.isEmpty()) Text("Age", color = secondaryText.copy(alpha = 0.4f), fontSize = 12.sp)
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        // 7. Medical/Surgical
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                                Text("🏥", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Medical/Surgical", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                    Text("Patient Type", fontSize = 9.sp, color = secondaryText)
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
                                    .background(bgColor, RoundedCornerShape(6.dp))
                                    .clickable {
                                        spiPatientType = if (spiPatientType == "Medical") "Surgical" else "Medical"
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = spiPatientType,
                                    fontSize = 12.sp,
                                    color = primaryText,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Patient Type",
                                    tint = secondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Divider(color = strokeColor)

                        // RESULT DISPLAY FOR SPI-2
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor, RoundedCornerShape(8.dp))
                                .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "SPI Score",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = highlightColor
                                )
                                Text(
                                    text = if (spiSurvivalProbability != null) {
                                        String.format(java.util.Locale.US, "%.1f%% Survival Probability", spiSurvivalProbability)
                                    } else {
                                        "% Survival Probability"
                                    },
                                    fontSize = 12.sp,
                                    color = secondaryText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (isFeline) "🐈" else "🐕",
                                fontSize = 42.sp
                            )
                        }
                    }
                }
            }

            // TAB 3: Applefast
            if (activeTab == "Applefast") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, strokeColor, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isFeline) "APPLEfast - Feline" else "APPLEfast - Canine",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "APPLEfast Result is Calculated after Scores are entered",
                                fontSize = 11.sp,
                                color = secondaryText
                            )
                        }

                        Divider(color = strokeColor)

                        // Table header matching screenshot
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor)
                                .border(1.dp, strokeColor)
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TEST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText,
                                modifier = Modifier.weight(1.3f)
                            )
                            Text(
                                text = "VALUES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }

                        // 1. Glucose row with individual columns and text box
                        val glucoseValue = appleGlucose.toDoubleOrNull()
                        val currentGlucoseScore = when {
                            glucoseValue == null -> -1
                            glucoseValue > 273 -> 0
                            glucoseValue < 84 -> 7
                            glucoseValue in 84.0..102.0 -> 8
                            glucoseValue in 102.01..164.0 -> 9
                            else -> 10
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🩻", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Glucose", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = appleGlucose,
                                    onValueChange = { appleGlucose = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = primaryText, textAlign = TextAlign.End),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .border(1.dp, strokeColor, RoundedCornerShape(4.dp))
                                                .background(bgColor, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            if (appleGlucose.isEmpty()) Text("Enter Glucose Score", color = secondaryText.copy(alpha = 0.5f), fontSize = 11.sp)
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            // Range metrics block showing scoring points
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    "> 273 mg/dL" to 0,
                                    "< 84 mg/dL" to 7,
                                    "84-102 mg/dL" to 8,
                                    "103-164 mg/dL" to 9,
                                    "165-273 mg/dL" to 10
                                ).forEach { (lbl, sc) ->
                                    val isCurrent = currentGlucoseScore == sc
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isCurrent) highlightColor else bgColor)
                                            .border(1.dp, strokeColor, RoundedCornerShape(4.dp))
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(lbl, fontSize = 7.5.sp, color = if (isCurrent) onHighlightColor else secondaryText, maxLines = 1)
                                            Text("$sc", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) onHighlightColor else primaryText)
                                        }
                                    }
                                }
                            }
                        }

                        Divider(color = strokeColor.copy(alpha = 0.4f))

                        // 2. Albumin row with individual columns and text box
                        val albuminValue = appleAlbumin.toDoubleOrNull()
                        val currentAlbuminScore = when {
                            albuminValue == null -> -1
                            albuminValue < 2.6 -> 8
                            albuminValue in 2.6..3.0 -> 7
                            albuminValue in 3.01..3.2 -> 6
                            albuminValue in 3.21..3.5 -> 0
                            else -> 2
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🩻", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Albumin", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = appleAlbumin,
                                    onValueChange = { appleAlbumin = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = primaryText, textAlign = TextAlign.End),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .border(1.dp, strokeColor, RoundedCornerShape(4.dp))
                                                .background(bgColor, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            if (appleAlbumin.isEmpty()) Text("Enter Albumin Score", color = secondaryText.copy(alpha = 0.5f), fontSize = 11.sp)
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            // Range metrics block showing scoring points
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    "< 2.6 g/dL" to 8,
                                    "2.6-3.0 g/dL" to 7,
                                    "3.1-3.2 g/dL" to 6,
                                    "3.3-3.5 g/dL" to 0,
                                    "> 3.5 g/dL" to 2
                                ).forEach { (lbl, sc) ->
                                    val isCurrent = currentAlbuminScore == sc
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isCurrent) highlightColor else bgColor)
                                            .border(1.dp, strokeColor, RoundedCornerShape(4.dp))
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(lbl, fontSize = 7.5.sp, color = if (isCurrent) onHighlightColor else secondaryText, maxLines = 1)
                                            Text("$sc", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) onHighlightColor else primaryText)
                                        }
                                    }
                                }
                            }
                        }

                        Divider(color = strokeColor.copy(alpha = 0.4f))

                        // 3. Lactate row with individual columns and text box
                        val lactateValue = appleLactate.toDoubleOrNull()
                        val currentLactateScore = when {
                            lactateValue == null -> -1
                            lactateValue < 1.8 -> 0
                            lactateValue in 1.8..7.2 -> 4
                            lactateValue in 18.0..72.1 -> 4
                            lactateValue in 7.21..9.0 -> 8
                            lactateValue in 72.2..90.1 -> 8
                            else -> 12
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🩻", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lactate", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = appleLactate,
                                    onValueChange = { appleLactate = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = primaryText, textAlign = TextAlign.End),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .border(1.dp, strokeColor, RoundedCornerShape(4.dp))
                                                .background(bgColor, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            if (appleLactate.isEmpty()) Text("Enter Lactate Score", color = secondaryText.copy(alpha = 0.5f), fontSize = 11.sp)
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            // Range metrics block showing scoring points
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    "< 1.8 mol/l" to 0,
                                    "18-72 mol/l" to 4,
                                    "72-90 mol/l" to 8,
                                    "> 90 mol/l" to 12
                                ).forEach { (lbl, sc) ->
                                    val isCurrent = currentLactateScore == sc
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isCurrent) highlightColor else bgColor)
                                            .border(1.dp, strokeColor, RoundedCornerShape(4.dp))
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(lbl, fontSize = 8.sp, color = if (isCurrent) onHighlightColor else secondaryText, maxLines = 1)
                                            Text("$sc", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) onHighlightColor else primaryText)
                                        }
                                    }
                                }
                            }
                        }

                        Divider(color = strokeColor.copy(alpha = 0.4f))

                        // 4. Platelet Count row with individual columns and text box
                        val plateletsValue = applePlatelets.toDoubleOrNull()
                        val currentPlateletsScore = when {
                            plateletsValue == null -> -1
                            plateletsValue in 261.0..420.0 -> 0
                            plateletsValue < 151.0 -> 5
                            plateletsValue in 151.0..200.0 -> 6
                            plateletsValue in 201.0..260.0 -> 3
                            else -> 1
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🩻", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Platelet Count", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = applePlatelets,
                                    onValueChange = { applePlatelets = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = primaryText, textAlign = TextAlign.End),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .border(1.dp, strokeColor, RoundedCornerShape(4.dp))
                                                .background(bgColor, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            if (applePlatelets.isEmpty()) Text("Enter Platelet Score", color = secondaryText.copy(alpha = 0.5f), fontSize = 11.sp)
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            // Range metrics block showing scoring points
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    "261-420 x10³" to 0,
                                    "< 151 x10³" to 5,
                                    "151-200 x10³" to 6,
                                    "201-260 x10³" to 3,
                                    "> 420 x10³" to 1
                                ).forEach { (lbl, sc) ->
                                    val isCurrent = currentPlateletsScore == sc
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isCurrent) highlightColor else bgColor)
                                            .border(1.dp, strokeColor, RoundedCornerShape(4.dp))
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(lbl, fontSize = 7.5.sp, color = if (isCurrent) onHighlightColor else secondaryText, maxLines = 1)
                                            Text("$sc", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) onHighlightColor else primaryText)
                                        }
                                    }
                                }
                            }
                        }

                        Divider(color = strokeColor.copy(alpha = 0.4f))

                        // 5. Mentation Scoring
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🧠", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mentation", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText)
                                }
                                Row(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .border(1.dp, strokeColor, RoundedCornerShape(4.dp))
                                        .background(bgColor, RoundedCornerShape(4.dp))
                                        .clickable {
                                            // Toggle logically through points
                                            appleMentation = when (appleMentation) {
                                                0 -> 4
                                                4 -> 6
                                                6 -> 7
                                                7 -> 14
                                                else -> 0
                                            }
                                        }
                                        .padding(horizontal = 6.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val textMentation = when (appleMentation) {
                                        0 -> "Normal Mentation (0)"
                                        4 -> "Able to stand, dull (4)"
                                        6 -> "Stand assisted, dull (6)"
                                        7 -> "Unable to stand, responsive (7)"
                                        else -> "Unable, unresponsive (14)"
                                    }
                                    Text(
                                        text = textMentation,
                                        fontSize = 11.sp,
                                        color = primaryText,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Mentation",
                                        tint = secondaryText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            // Range metrics block showing scoring points
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    "Normal" to 0,
                                    "Stand, dull" to 4,
                                    "Assisted, dull" to 6,
                                    "Unable, resp" to 7,
                                    "Unable, unresp" to 14
                                ).forEach { (lbl, sc) ->
                                    val isCurrent = appleMentation == sc
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isCurrent) highlightColor else bgColor)
                                            .border(1.dp, strokeColor, RoundedCornerShape(4.dp))
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(lbl, fontSize = 7.5.sp, color = if (isCurrent) onHighlightColor else secondaryText, maxLines = 1)
                                            Text("$sc", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) onHighlightColor else primaryText)
                                        }
                                    }
                                }
                            }
                        }

                        Divider(color = strokeColor)

                        // APPLEfast Score Results
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor, RoundedCornerShape(8.dp))
                                .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val gScore = if (currentGlucoseScore != -1) currentGlucoseScore else 0
                            val aScore = if (currentAlbuminScore != -1) currentAlbuminScore else 0
                            val lScore = if (currentLactateScore != -1) currentLactateScore else 0
                            val pScore = if (currentPlateletsScore != -1) currentPlateletsScore else 0
                            val totalApplePoints = gScore + aScore + lScore + pScore + appleMentation
                            
                            val appleRisk = when {
                                totalApplePoints <= 10 -> "Low risk of mortality"
                                totalApplePoints <= 22 -> "Moderate risk of mortality"
                                totalApplePoints <= 35 -> "High risk of mortality"
                                else -> "Critical risk of mortality (extremely guarded)"
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📊", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "APPLEfast Results",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = highlightColor
                                    )
                                }
                                Text(
                                    text = "$totalApplePoints Points — $appleRisk",
                                    fontSize = 12.sp,
                                    color = secondaryText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "🐾",
                                fontSize = 32.sp
                            )
                        }
                    }
                }
            }

            // Expandable Technical Info blocks at the bottom (as shown in last screenshot)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Info block 1 : Animal Trauma Triage
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                        .background(surfaceColor, RoundedCornerShape(8.dp))
                        .clickable { expandAtt = !expandAtt }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Animal Trauma Triage (ATT) Info",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Icon(
                            imageVector = if (expandAtt) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (expandAtt) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "معیار تریاژ تروما پت (ATT) ابزاری استاندارد است که در ۳۰ روز نخست پس از ورود حیوان تروما دیده، بقای حیوانات را پیش‌بینی می‌کند. در این سیستم ۶ ارگان (پرفیوژن، قلب، تنفس، اسکلت، مغز و چشم/پوست) بین ۰ تا ۳ ارزیابی می‌شوند. امتیاز بالاتر معرف آسیب دیدگی شدیدتر و شانس بقای کمتر است.",
                            fontSize = 11.sp,
                            color = secondaryText,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Info block 2 : SPI-2
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                        .background(surfaceColor, RoundedCornerShape(8.dp))
                        .clickable { expandSpi = !expandSpi }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Survival Prediction Index (SPI 2) Info",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Icon(
                            imageVector = if (expandSpi) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (expandSpi) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "شاخص پیش‌بینی بقا (SPI) بر پایه همبستگی آماری رگرسیون لجستیک میزان فشار خون متمرکز، ترشحات کراتینین، غلظت آلبومین، درصد PCV خون و سن بیمار ایجاد شده است. بررسی‌های آماری نشان می‌دهند افزایش فشار و بهبود آلبومین تاثیر مستقیمی روی افزایش توان ماندگاری پت دارد.",
                            fontSize = 11.sp,
                            color = secondaryText,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Info block 3 : APPLEfast
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, strokeColor, RoundedCornerShape(8.dp))
                        .background(surfaceColor, RoundedCornerShape(8.dp))
                        .clickable { expandApple = !expandApple }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "APPLEfast Info",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Icon(
                            imageVector = if (expandApple) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (expandApple) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "سیستم ارزیابی همه‌جانبه بالینی و هماتولوژی سریع (APPLEfast) یک روش خلاصه بسیار کارآمد است که توانایی تخمین دقیق ریسک مرگ و میر حیوانات بستری در بخش مراقبت‌های ویژه را با اندازه‌گیری ۵ فاکتور پر تنش خون از جمله قند، آلبومین، تست لاکتات سرسام‌آور جدار سلولی، هماتوکریت پلاکت‌ها و سطح هوشیاری ذهنی فراهم می‌سازد.",
                            fontSize = 11.sp,
                            color = secondaryText,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}


