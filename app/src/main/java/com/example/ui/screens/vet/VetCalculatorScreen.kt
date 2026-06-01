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
        "مایع‌درمانی", "انتقال خون", "محاسبه کالری غذا", "زمان زایمان", "سن معادل انسان", "تریاژ تروما", "کاهش میزان درد"
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
                "کاهش میزان درد" -> {
                    PainScoreView()
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

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Patients Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    // Title Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Patients / مشخصات بیمار",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                                    onCheckedChange = { viewPatients = it }
                                )
                                Text("View Patients", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
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
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Save New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Checkbox(
                                checked = addPatientNotes,
                                onCheckedChange = { addPatientNotes = it }
                            )
                            Text("Add Patient Notes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        if (addPatientNotes) {
                            Spacer(modifier = Modifier.height(6.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = patientNotes,
                                onValueChange = { patientNotes = it },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (patientNotes.isEmpty()) {
                                            Text("Enter patient notes here...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 12.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        
                        // View Patients Table
                        if (viewPatients) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Patient Records:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            combinedPatients.forEach { record ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${record.petName} (${record.species}) - ID: ${record.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Owner: ${record.ownerName} | Age: ${record.age} | Sex: ${record.sex}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (record.notes.isNotEmpty()) {
                                            Text("Notes: ${record.notes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Spacer(modifier = Modifier.height(16.dp))

            // Due Date Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text(
                        text = if (petName.isNotEmpty()) "Choose Conception Date for $petName - ${if (isCanine) "Canine" else "Feline"}" else "Choose Conception Date - ${if (isCanine) "Canine" else "Feline"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
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
                                fontSize = 12.sp,
                                color = if (conceptionDate == null) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.7f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text("Days since Conception: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = daysValueString,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text(
                        text = "Canine: Gestation range: 57-65 days, Average: 63 days.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Feline: Gestation range: 60-67 days, Average: 63-65 days.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Likely Due Date Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (petName.isNotEmpty()) "Likely Due Date for $petName" else "Likely Due Date",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val avgGestation = if (isCanine) 63 else 64
                        val likelyDueDateText = getFormattedDateWithOffset(conceptionDate, avgGestation)
                        
                        Box(
                            modifier = Modifier
                                .widthIn(min = 220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = likelyDueDateText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val earlyOffset = if (isCanine) 57 else 60
                            val earlyDateText = getFormattedDateWithOffset(conceptionDate, earlyOffset)
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = earlyDateText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val lateOffset = if (isCanine) 65 else 67
                            val lateDateText = getFormattedDateWithOffset(conceptionDate, lateOffset)
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lateDateText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pregnancy Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🐾", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pregnancy Info*",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val bulletPoints = listOf(
                        "Fertilization occurs in the oviducts in both the bitch and queen. Implantation of zygotes in the uterus occurs at ~18 days in the bitch and 14 days in the queen.",
                        "Palpable at 21 days and these swellings double in diameter every 7 days. After day 35-38, they become indistinct, and palpation becomes difficult until late pregnancy.",
                        "the fetal skeleton begins to calcify as early as day 28, it is not detectable by routine radiography until approximately day 42-45 and is quite prominent by day 47-48. Radiography at this time is not teratogenic. Late gestational radiography, >55 days is the best method to determine litter size.",
                        "Ultrasonography is best performed at 25-35 days gestation. Before 21 days, \"false-negative\" results are seen.",
                        "Doppler-type instruments allow one to \"hear\" the fetal heart, which beats 2–3 times faster than that of the dam. Placental sounds may also be heard.",
                        "Ultrasonography is especially helpful in differentiating pregnancy from other causes of uterine distention (eg. hydrometra, pyometra, mucometra)."
                    )
                    
                    bulletPoints.forEach { point ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("•", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = point,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    Text(
                        text = "*Source Link",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
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
    var ageStr by remember { mutableStateOf("2") }
    var selectedAnimal by remember { mutableStateOf("dog") }

    val age = ageStr.toIntOrNull() ?: 2

    val humanAge = if (selectedAnimal == "cat") {
        if (age == 1) 15 else if (age == 2) 24 else 24 + ((age - 2) * 4)
    } else {
        // Dog average based on weight
        if (age == 1) 15 else if (age == 2) 24 else 24 + ((age - 2) * 5)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🐹 تخمین سن معادل انسان (Human Age Checker)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedAnimal == "dog") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedAnimal = "dog" }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("سگ‌سانان", color = if (selectedAnimal == "dog") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedAnimal == "cat") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedAnimal = "cat" }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("گربه‌سانان", color = if (selectedAnimal == "cat") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = ageStr,
                onValueChange = { ageStr = it },
                label = { Text("سن تقویمی پت (سالیانه)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("نتایج معادل‌سازی سنی:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))

            Text("سن فیزیکی پت: $age سال کامل", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("سن معادل در انسان: حدود $humanAge ساله!", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// 6. Trauma Triage system
@Composable
fun TraumaTriageView() {
    var scoreMotor by remember { mutableStateOf(6) } // MGCS motor score: 1 to 6
    var scoreBrainstem by remember { mutableStateOf(6) } // MGCS brainstem score: 1 to 6
    var scoreConscience by remember { mutableStateOf(6) } // MGCS conscience: 1 to 6

    val totalMgcs = scoreMotor + scoreBrainstem + scoreConscience

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🧠 غربالگری مغزی MGCS (Modified Glassgow Score)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("غربالگری سطح هوشیاری پس از تصادف و تروما:", fontSize = 10.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            // Score lists selectors
            Text("۱. مهارت‌های ارادی حرکتی (موتور):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1, 3, 5, 6).forEach { score ->
                    val isSel = scoreMotor == score
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { scoreMotor = score }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("امتیاز $score", fontSize = 10.sp, color = if (isSel) Color.White else Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("۲. تست هم‌گرایی مردمک و رفلکس مغز غضروفی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1, 3, 5, 6).forEach { score ->
                    val isSel = scoreBrainstem == score
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { scoreBrainstem = score }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("امتیاز $score", fontSize = 10.sp, color = if (isSel) Color.White else Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val triageRecommendation = when {
                totalMgcs <= 8 -> "⚠️ وخیم (بحران قرمز) - احتمال کما و خون‌ریزی جمجمه، نیاز اضطراری به مانیتور تنفس دایم و مسکن‌های کاهنده فشار درون کله."
                totalMgcs <= 14 -> "⚠️ پایدار فرعی (زرد) - ضرورت مانیتورینگ متمرکز ۱۲ ساعته، احتمال ضربه کله متوسط."
                else -> "✅ کاملاً پایدار (سبز) - مانیتور علائم حیاتی و ترخیص موقتی با استراحت خانگی."
            }

            Text("امتیاز نهایی تروما MGCS: $totalMgcs از ۱۸", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("مراقبت تجویزی: $triageRecommendation", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        }
    }
}

// 7. CSU Pain estimation
@Composable
fun PainScoreView() {
    var vocalBy by remember { mutableStateOf(false) }
    var postureBy by remember { mutableStateOf(false) }
    var touchBy by remember { mutableStateOf(false) }

    val painScore = (if (vocalBy) 1 else 0) + (if (postureBy) 1 else 0) + (if (touchBy) 2 else 0)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("😿 سنجش درد بر اساس معیار جامعه درد کانادا (CSU)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = vocalBy, onCheckedChange = { vocalBy = it })
                Text("نالیدن حین جابجایی یا میاو/پارس‌های دردناک", fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = postureBy, onCheckedChange = { postureBy = it })
                Text("کمر قوز کرده متمایل به خواب شکم روی زمین (Prayer position)", fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = touchBy, onCheckedChange = { touchBy = it })
                Text("فرار یا تلاش به گاز گرفتگی حین لمس موضعی زخم (امتیاز ۲)", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val painRating = when (painScore) {
                0 -> "فاقد درد (بدون بیقراری)"
                1 -> "درد خفیف (سفارش ملوکسیکام موضعی)"
                2 -> "درد متوسط (سفارش مخدرهای خوراکی ترامادول تضعیف شده)"
                else -> "درد شدید ⚠️ (احتمال شوک قلبی، سفارش دگزامتازون به پیوست پتدین مسکن فوری)"
            }

            Text("شاخص تخمینی شدت درد: $painScore از ۴", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("توصیه دندان‌پزشکی تگ: $painRating", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
