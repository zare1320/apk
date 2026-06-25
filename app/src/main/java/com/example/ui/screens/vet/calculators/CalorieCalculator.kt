package com.example.ui.screens.vet.calculators

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.example.data.database.Pet
import com.example.data.database.FoodItem
import com.example.viewmodel.MainViewModel

@Composable
fun CalorieCalculatorView(
    viewModel: MainViewModel? = null,
    activePet: Pet? = null,
    initWeight: String = "",
    selectedSpecies: String? = null,
    currentLang: String = "en"
) {
    // Determine active species tab (default to Canine unless animal species or selectedSpecies is cat)
    val isCat = (activePet?.species?.lowercase() == "cat") || (selectedSpecies?.lowercase() == "cat")
    var isCanineTab by remember(activePet, selectedSpecies) { mutableStateOf(!isCat) }

    // Weight input states with auto-synchronization (default to 1 kg / 2.20 lbs)
    var weightKg by remember(initWeight, activePet) {
        val initialKgStr = activePet?.weight?.toString() ?: initWeight.ifEmpty { "1" }
        mutableStateOf(initialKgStr)
    }
    
    var weightLbs by remember(initWeight, activePet) {
        val initialKg = activePet?.weight ?: initWeight.toDoubleOrNull() ?: 1.0
        val initialLbsStr = if (initialKg > 0.0) {
            String.format("%.2f", initialKg * 2.20462)
        } else {
            String.format("%.2f", 1.0 * 2.20462)
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
    val formattedH2oMls = if (currentLang == "fa") {
        "${String.format("%.0f", h2oMls)} میلی‌لیتر در روز (±${String.format("%.0f", h2oMlsRangeVal)} میلی‌لیتر)"
    } else {
        "${String.format("%.0f", h2oMls)} mls/day (±${String.format("%.0f", h2oMlsRangeVal)}mls)"
    }

    val h2oCups = mer / 240.0
    val h2oCupsRangeVal = h2oCups * 0.20 // +/- 20%
    val formattedH2oCups = if (currentLang == "fa") {
        "${String.format("%.1f", h2oCups)} پیمانه در روز (±${String.format("%.1f", h2oCupsRangeVal)} پیمانه)"
    } else {
        "${String.format("%.1f", h2oCups)} cups/day (±${String.format("%.1f", h2oCupsRangeVal)}cups)"
    }

    // Canned and Dry Food Calorie Data Database setup
    var activeClassTab by remember { mutableStateOf("Dry Food") } // "Dry Food" or "Canned Food"
    var selectedFoodCategory by remember {
        mutableStateOf<String?>(null)
    }

    val allFoodsFromDb by if (viewModel != null) {
        viewModel.allFoods.collectAsState()
    } else {
        remember { mutableStateOf(emptyList<FoodItem>()) }
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val themeCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.White
    val borderStrokeColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val badgeBgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    val isRtl = currentLang == "fa"
    val layoutDirection = if (isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
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
                            text = if (currentLang == "fa") "تنظیم وزن و امتیاز وضعیت بدنی (BCS)" else "Set Weight and BCS",
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
                            label = { Text(if (currentLang == "fa") "پوند" else "Pounds") },
                            placeholder = { Text(if (currentLang == "fa") "پوند" else "lbs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
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
                            label = { Text(if (currentLang == "fa") "کیلوگرم" else "Kilograms") },
                            placeholder = { Text(if (currentLang == "fa") "کیلوگرم" else "kgs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
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
                                    text = if (currentLang == "fa") "انتخاب امتیاز وضعیت بدنی" else "Select Body Condition Score",
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
                                    Text(
                                        text = if (currentLang == "fa") "امتیاز بدنی (BCS) = $bcsScore/۹" else "BCS = $bcsScore/9",
                                        fontWeight = FontWeight.Bold
                                    )
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
                                            text = {
                                                Text(
                                                    text = if (currentLang == "fa") "امتیاز بدنی = $num/۹" else "BCS = $num/9",
                                                    fontWeight = if (num == bcsScore) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
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
                            Text(if (currentLang == "fa") "جدول‌های BCS" else "BCS Charts")
                        }
                    }
                }
            }

            // Estimate equations warning text
            Text(
                text = if (currentLang == "fa") {
                    "* فرمول‌های محاسبه انرژی مورد نیاز برای بقا (MER) تقریبی هستند و نیازهای حیوان واقعی ممکن است تا ۵۰٪ با این مقادیر تفاوت داشته باشد."
                } else {
                    "*Equations for MER are ESTIMATES, individual animals can vary by as much as 50% from the predicted values."
                },
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
                        .clickable { 
                            isCanineTab = true 
                            selectedFoodCategory = null
                        }
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
                            text = if (currentLang == "fa") "سگ‌سانان (سگ)" else "CANINE",
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
                        .clickable { 
                            isCanineTab = false 
                            selectedFoodCategory = null
                        }
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
                            text = if (currentLang == "fa") "گربه‌سانان (گربه)" else "FELINE",
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
                    text = if (isCanineTab) {
                        if (currentLang == "fa") "کالری سگ‌سانان" else "Canine Calories"
                    } else {
                        if (currentLang == "fa") "کالری گربه‌سانان" else "Feline Calories"
                    },
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
                            text = if (currentLang == "fa") "انتخاب شرایط حیوان خانگی:" else "Select Pet Criteria:",
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
                            text = if (currentLang == "fa") "شرایط حیوان" else "Pet Criteria",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )

                        Box {
                            OutlinedButton(
                                onClick = { criteriaMenuExpanded = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = getCriteriaDisplayName(selectedCriteriaName, currentLang),
                                    fontWeight = FontWeight.Bold
                                )
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
                                        text = { Text(getCriteriaDisplayName(opt.name, currentLang)) },
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
                            text = if (currentLang == "fa") "نتایج" else "Results",
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
                                Text(
                                    text = if (currentLang == "fa") "وزن فعلی" else "Current Weight",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = textPrimary
                                )
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBgColor)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == "fa") "${String.format("%.1f", weightLbsVal)} پوند" else "${String.format("%.1f", weightLbsVal)} lbs",
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
                                        text = if (currentLang == "fa") "${String.format("%.1f", weightKgVal)} کیلوگرم" else "${String.format("%.1f", weightKgVal)} kgs",
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
                                Text(
                                    text = if (currentLang == "fa") "وزن هدف (ایده‌آل)" else "Target Weight",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = textPrimary
                                )
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBgColor)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == "fa") "${String.format("%.1f", targetWeightLbs)} پوند" else "${String.format("%.1f", targetWeightLbs)} lbs",
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
                                        text = if (currentLang == "fa") "${String.format("%.1f", targetWeightKg)} کیلوگرم" else "${String.format("%.1f", targetWeightKg)} kgs",
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
                            text = if (currentLang == "fa") "میزان انرژی مورد نیاز در حالت استراحت (RER)" else "RER Resting Energy Requirement",
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
                                Text(
                                    text = if (currentLang == "fa") "محاسبه شده - کیلوکالری/روز" else "Calculated - kcal/day",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBgColor)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (currentLang == "fa") "$formattedRer کیلوکالری/روز" else "$formattedRer kcal/day",
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
                            text = if (currentLang == "fa") "میزان انرژی مورد نیاز برای بقا و فعالیت (MER)" else "MER Maintenance Energy Requirement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                        Text(
                            text = if (currentLang == "fa") "محدوده: $formattedMerRange" else "Range: $formattedMerRange",
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
                                Text(
                                    text = if (currentLang == "fa") "محاسبه شده - کیلوکالری/روز" else "Calculated - kcal/day",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBgColor)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (currentLang == "fa") "$formattedMer کیلوکالری/روز" else "$formattedMer kcal/day",
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
                            text = if (currentLang == "fa") "تنظیم کالری در هر پیمانه یا قوطی" else "Set Calories per can or cup",
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
                            text = if (currentLang == "fa") "اطلاعات کالری غذاهای زیر را ببینید" else "See Calorie Data Below",
                            fontSize = 12.sp,
                            color = textSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = enterCaloriesStr,
                            onValueChange = { enterCaloriesStr = it },
                            placeholder = { Text(if (currentLang == "fa") "وارد کردن کالری" else "Enter Calories", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(160.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
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
                        text = if (currentLang == "fa") "مقدار تغذیه روزانه (پیمانه یا کنسرو)" else "Cups or cans per Day to Feed",
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
                            Text(
                                text = if (currentLang == "fa") "مقدار محاسبه شده" else "Calculated Value",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (formattedCupsOrCansPerDay.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else badgeBgColor)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            val feedUnit = if (activeClassTab == "Dry Food") {
                                if (currentLang == "fa") "پیمانه در روز" else "cups/day"
                            } else {
                                if (currentLang == "fa") "قوطی در روز" else "cans/day"
                            }
                            Text(
                                text = if (formattedCupsOrCansPerDay.isNotEmpty()) "$formattedCupsOrCansPerDay $feedUnit" else "—",
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
                        text = if (currentLang == "fa") "نیاز روزانه به آب" else "Daily H2O Requirement",
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
                            Text(
                                text = if (currentLang == "fa") "مقدار محاسبه شده (محدوده)" else "Calculated Value (Range)",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
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
                    text = if (currentLang == "fa") "پایگاه داده غذاهای خشک و کنسروی گربه و سگ" else "Canned & Dry Food Database",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (currentLang == "fa") "💡 برای محاسبه خودکار مقدار مصرف روزانه، روی غذای مورد نظر کلیک کنید." else "💡 Tap on any food below to auto-populate the calorie calculator.",
                    fontSize = 11.sp,
                    color = textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2x2 Grid of buttons matching the screenshot
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: Dog Dry & Dog Canned
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isSelected = selectedFoodCategory == "Dog Dry"
                    val btnBgColor = if (isSelected) {
                        if (isDark) Color(0xFFEF5350) else Color(0xFFE53935)
                    } else {
                        themeCardBg
                    }
                    val btnBorderColor = if (isSelected) Color.Transparent else borderStrokeColor
                    val btnContentColor = if (isSelected) Color.White else textPrimary

                    Card(
                        colors = CardDefaults.cardColors(containerColor = btnBgColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(1.dp, btnBorderColor, RoundedCornerShape(12.dp))
                            .clickable { 
                                isCanineTab = true
                                selectedFoodCategory = "Dog Dry" 
                                activeClassTab = "Dry Food"
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text("🐕", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == "fa") "غذای خشک سگ" else "Dog Dry Food",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = btnContentColor
                                )
                            }
                        }
                    }

                    val isSelectedCanned = selectedFoodCategory == "Dog Canned"
                    val btnBgColorCanned = if (isSelectedCanned) {
                        if (isDark) Color(0xFFEF5350) else Color(0xFFE53935)
                    } else {
                        themeCardBg
                    }
                    val btnBorderColorCanned = if (isSelectedCanned) Color.Transparent else borderStrokeColor
                    val btnContentColorCanned = if (isSelectedCanned) Color.White else textPrimary

                    Card(
                        colors = CardDefaults.cardColors(containerColor = btnBgColorCanned),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(1.dp, btnBorderColorCanned, RoundedCornerShape(12.dp))
                            .clickable { 
                                isCanineTab = true
                                selectedFoodCategory = "Dog Canned" 
                                activeClassTab = "Canned Food"
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text("🐕", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == "fa") "غذای کنسروی سگ" else "Dog Canned Food",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = btnContentColorCanned
                                )
                            }
                        }
                    }
                }

                // Row 2: Cat Dry & Cat Canned
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isSelectedCatDry = selectedFoodCategory == "Cat Dry"
                    val btnBgColorCatDry = if (isSelectedCatDry) {
                        if (isDark) Color(0xFFEF5350) else Color(0xFFE53935)
                    } else {
                        themeCardBg
                    }
                    val btnBorderColorCatDry = if (isSelectedCatDry) Color.Transparent else borderStrokeColor
                    val btnContentColorCatDry = if (isSelectedCatDry) Color.White else textPrimary

                    Card(
                        colors = CardDefaults.cardColors(containerColor = btnBgColorCatDry),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(1.dp, btnBorderColorCatDry, RoundedCornerShape(12.dp))
                            .clickable { 
                                isCanineTab = false
                                selectedFoodCategory = "Cat Dry" 
                                activeClassTab = "Dry Food"
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text("🐈", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == "fa") "غذای خشک گربه" else "Cat Dry Food",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = btnContentColorCatDry
                                )
                            }
                        }
                    }

                    val isSelectedCatCanned = selectedFoodCategory == "Cat Canned"
                    val btnBgColorCatCanned = if (isSelectedCatCanned) {
                        if (isDark) Color(0xFFEF5350) else Color(0xFFE53935)
                    } else {
                        themeCardBg
                    }
                    val btnBorderColorCatCanned = if (isSelectedCatCanned) Color.Transparent else borderStrokeColor
                    val btnContentColorCatCanned = if (isSelectedCatCanned) Color.White else textPrimary

                    Card(
                        colors = CardDefaults.cardColors(containerColor = btnBgColorCatCanned),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(1.dp, btnBorderColorCatCanned, RoundedCornerShape(12.dp))
                            .clickable { 
                                isCanineTab = false
                                selectedFoodCategory = "Cat Canned" 
                                activeClassTab = "Canned Food"
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text("🐈", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == "fa") "غذای کنسروی گربه" else "Cat Canned Food",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = btnContentColorCatCanned
                                )
                            }
                        }
                    }
                }
            }

            if (selectedFoodCategory != null) {
                Spacer(modifier = Modifier.height(10.dp))

                // Food Data items list
                val foodIsCanine = when (selectedFoodCategory) {
                    "Dog Dry", "Dog Canned" -> true
                    else -> false
                }
                val foodIsDry = when (selectedFoodCategory) {
                    "Dog Dry", "Cat Dry" -> true
                    else -> false
                }
                val recommendedFoodsList = if (allFoodsFromDb.isNotEmpty()) {
                    allFoodsFromDb.filter { it.isCanine == foodIsCanine && it.isDry == foodIsDry }
                } else {
                    getRecommendedFoods(isCanine = foodIsCanine, isDry = foodIsDry).map {
                        FoodItem(brand = it.brand, description = it.description, descriptionFa = it.descriptionFa, calories = it.calories, isCanine = foodIsCanine, isDry = foodIsDry)
                    }
                }

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
                                        text = if (currentLang == "fa") food.descriptionFa else food.description,
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
                                    val unitLabel = if (foodIsDry) {
                                        if (currentLang == "fa") "پیمانه" else "cup"
                                    } else {
                                        if (currentLang == "fa") "قوطی" else "can"
                                    }
                                    Text(
                                        text = if (currentLang == "fa") "${food.calories} کیلوکالری/$unitLabel" else "${food.calories} kcal/$unitLabel",
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
    }

    // BCS Charts Explanatory Dialog
    if (showBcsChartDialog) {
        AlertDialog(
            onDismissRequest = { showBcsChartDialog = false },
            title = { Text(if (currentLang == "fa") "جدول امتیاز وضعیت بدنی ۹ نقطه‌ای (BCS)" else "9-Point Body Condition Score Chart (BCS)", fontWeight = FontWeight.Bold) },
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
                                Text(if (currentLang == "fa") item.titleFa else item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(if (currentLang == "fa") item.descFa else item.desc, fontSize = 11.sp, color = textSecondary, lineHeight = 14.sp)
                        }
                        HorizontalDivider(color = borderStrokeColor.copy(alpha = 0.3f))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showBcsChartDialog = false }) {
                    Text(if (currentLang == "fa") "تایید" else "OK")
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
    val descriptionFa: String,
    val calories: Double
)

fun getRecommendedFoods(isCanine: Boolean, isDry: Boolean): List<RecommendedFood> {
    return if (isCanine) {
        if (isDry) {
            listOf(
                RecommendedFood("Royal Canin Mini Adult (🐕 Dry)", "Balanced food for small breed adult dogs - 373 kcal/cup", "غذای متوازن برای سگ‌های بالغ نژاد کوچک - ۳۷۳ کیلوکالری/پیمانه", 373.0),
                RecommendedFood("Royal Canin Puppy Maxi Dry (🐕 Dry)", "Growth Support for Large Breed puppies - 343 kcal/cup", "پشتیبانی از رشد توله سگ‌های نژاد بزرگ - ۳۴۳ کیلوکالری/پیمانه", 343.0),
                RecommendedFood("Hill's Science Diet Adult Dry (🐕 Dry)", "Chicken & Barley Formula for optimal health - 363 kcal/cup", "فرمول مرغ و جو برای سلامت بهینه - ۳۶۳ کیلوکالری/پیمانه", 363.0),
                RecommendedFood("Purina Pro Plan Shredded Chicken (🐕 Dry)", "High Protein chicken & rice formula - 387 kcal/cup", "فرمول پر پروتئین مرغ و برنج - ۳۸۷ کیلوکالری/پیمانه", 387.0),
                RecommendedFood("Reflex Plus Adult Dog Salmon (🐕 Dry)", "Super Premium formula with Salmon for adult dogs - 395 kcal/cup", "فرمول سوپر پرمیوم با ماهی سالمون برای سگ‌های بالغ - ۳۹۵ کیلوکالری/پیمانه", 395.0),
                RecommendedFood("Nutri Pet Dry Dog Premium (نوتری پت 🐕)", "Iranian premium dry food with 29% protein - 355 kcal/cup", "غذای خشک پرمیوم ایرانی با ۲۹٪ پروتئین - ۳۵۵ کیلوکالری/پیمانه", 355.0),
                RecommendedFood("Josera Kids Puppy Dry (🐕 Dry)", "Premium German growth formula for medium/large puppies - 380 kcal/cup", "فرمول پرمیوم آلمانی رشد توله سگ بزرگ - ۳۸۰ کیلوکالری/پیمانه", 380.0),
                RecommendedFood("Celeb Dog Premium Dry (سلب پت 🐕)", "Premium local dry food with prebiotics - 360 kcal/cup", "غذای خشک پرمیوم ایرانی حاوی پربیوتیک - ۳۶۰ کیلوکالری/پیمانه", 360.0)
            )
        } else {
            listOf(
                RecommendedFood("Royal Canin Puppy Canned Can (🐕 Wet)", "Moist recipe for active puppy development - 335 kcal/can", "فرمول مرطوب برای رشد توله سگ‌های فعال - ۳۳۵ کیلوکالری/کنسرو", 335.0),
                RecommendedFood("Hill's Science Diet Chicken Can (🐕 Wet)", "Savoury stew with barley and meat veggies - 370 kcal/can", "خوراک لذیذ با جو، گوشت و سبزیجات - ۳۷۰ کیلوکالری/کنسرو", 370.0),
                RecommendedFood("Purina Pro Plan Beef & Rice Can (🐕 Wet)", "Classic wet high energy dog food - 408 kcal/can", "غذای مرطوب کلاسیک پر انرژی سگ - ۴۰۸ کیلوکالری/کنسرو", 408.0),
                RecommendedFood("Shayer Beef & Chicken Can (کنسرو شایر 🐕)", "100% natural meat pate for dogs, no preservatives - 310 kcal/can", "پاته گوشت صد درصد طبیعی سگ بدون مواد نگهدارنده - ۳۱۰ کیلوکالری/کنسرو", 310.0),
                RecommendedFood("Animonda GranCarno Adult Can (🐕 Wet)", "Pure beef and chicken chunks canned dog food - 390 kcal/can", "کنسرو سگ حاوی تکه‌های گوشت گاو و مرغ خالص - ۳۹۰ کیلوکالری/کنسرو", 390.0),
                RecommendedFood("Blue Buffalo Homestyle Beef Canned (🐕 Wet)", "Premium canned beef with garden veggies - 392 kcal/can", "گوشت گاو کنسرو شده پرمیوم با سبزیجات - ۳۹۲ کیلوکالری/کنسرو", 392.0)
            )
        }
    } else {
        if (isDry) {
            listOf(
                RecommendedFood("Royal Canin Feline Fit 32 (🐈 Dry)", "Balanced nutrition for moderately active cats - 315 kcal/cup", "تغذیه متوازن برای گربه‌های با فعالیت متوسط - ۳۱۵ کیلوکالری/پیمانه", 315.0),
                RecommendedFood("Royal Canin Kitten Dry (🐈 Dry)", "High energy kibble for growth phase up to 12 months - 395 kcal/cup", "غذای خشک پر انرژی برای دوره رشد تا ۱۲ ماهگی - ۳۹۵ کیلوکالری/پیمانه", 395.0),
                RecommendedFood("Royal Canin Hairball Care (🐈 Dry)", "Special dietary fiber formula to eliminate hairballs - 340.0 kcal/cup", "فرمول فیبر رژیمی مخصوص برای حذف گلوله مویی - ۳۴۰ کیلوکالری/پیمانه", 340.0),
                RecommendedFood("Hill's Science Diet Adult Cat Optimal (🐈 Dry)", "Excellent dry food for digestion and urinary tract - 502 kcal/cup", "غذای خشک عالی برای هضم و مجاری ادراری - ۵۰۲ کیلوکالری/پیمانه", 502.0),
                RecommendedFood("Purina Pro Plan Savor Salmon (🐈 Dry)", "Delicious dry cat salmon & rice formulation - 437 kcal/cup", "فرمول لذیذ خشک سالمون و برنج گربه - ۴۳۷ کیلوکالری/پیمانه", 437.0),
                RecommendedFood("Reflex Plus Kitten Chicken (🐈 Dry)", "Super premium dry food for growing kittens - 385 kcal/cup", "غذای خشک سوپر پرمیوم برای بچه گربه‌های در حال رشد - ۳۸۵ کیلوکالری/پیمانه", 385.0),
                RecommendedFood("Reflex Plus Adult Salmon (🐈 Dry)", "Super premium Omega-3 rich dry food for adult cats - 375 kcal/cup", "غذای خشک غنی از امگا ۳ برای گربه‌های بالغ - ۳۷۵ کیلوکالری/پیمانه", 375.0),
                RecommendedFood("Nutri Pet Cat Premium Dry (نوتری پت 🐈)", "Iranian premium dry cat food, balanced minerals - 340 kcal/cup", "غذای خشک پرمیوم ایرانی با مواد معدنی متوازن - ۳۴۰ کیلوکالری/پیمانه", 340.0),
                RecommendedFood("Josera Catelux Duck & Potato (🐈 Dry)", "German premium grain-free hairball controller - 410 kcal/cup", "غذای کنترل‌کننده هربال بدون غلات آلمانی - ۴۱۰ کیلوکالری/پیمانه", 410.0),
                RecommendedFood("Shoodo Cat Dry Salmon (شیدو 🐈)", "LID premium Persian formulation with salmon - 365 kcal/cup", "فرمولاسیون پرمیوم ایرانی شیدو با ماهی سالمون - ۳۶۵ کیلوکالری/پیمانه", 365.0),
                RecommendedFood("Celeb Cat Chicken & Turkey (سلب پت 🐈)", "Premium hypoallergenic turkey/poultry recipe - 360 kcal/cup", "دستور غذایی بوقلمون و مرغ ضد حساسیت - ۳۶۰ کیلوکالری/پیمانه", 360.0),
                RecommendedFood("Blue Buffalo Wilderness Cat Salmon (🐈 Dry)", "Grain-free high protein wild salmon kibbles - 443 kcal/cup", "غذای خشک پر پروتئین بدون غلات سالمون وحشی - ۴۴۳ کیلوکالری/پیمانه", 443.0)
            )
        } else {
            listOf(
                RecommendedFood("Royal Canin Intense Beauty In Gravy (🐈 Wet)", "Moist pouch with omega-3 for skin and coat beauty - 85 kcal/can", "پوچ مرطوب با امگا ۳ برای زیبایی پوست و مو - ۸۵ کیلوکالری/کنسرو", 85.0),
                RecommendedFood("Royal Canin Kitten Instinctive Gravy (🐈 Wet)", "Thin slices in gravy for baby teeth and immunity - 90 kcal/can", "برش‌های نازک در سس برای دندان‌ها و ایمنی بچه گربه - ۹۰ کیلوکالری/کنسرو", 90.0),
                RecommendedFood("Hill's Science Diet Wet Salmon (🐈 Wet)", "Seared salmon chunks in a rich wet savory glaze - 75 kcal/can", "تکه‌های ماهی سالمون تفت داده شده در سس لذیذ - ۷۵ کیلوکالری/کنسرو", 75.0),
                RecommendedFood("Purina Pro Plan Savor Salmon Can (🐈 Wet)", "Seafood delicious wet food paste for urinary tract - 95 kcal/can", "پاته غذای مرطوب لذیذ برای مجاری ادراری - ۹۵ کیلوکالری/کنسرو", 95.0),
                RecommendedFood("Shayer Chicken & Beef Canned (کنسرو شایر 🐈)", "High protein wet pate made entirely with chicken and beef - 92 kcal/can", "پاته مرطوب پر پروتئین ساخته شده از مرغ و گوساله - ۹۲ کیلوکالری/کنسرو", 92.0),
                RecommendedFood("Shayer Gourmet Turkey & Duck Can (کنسرو شایر 🐈)", "Succulent gourmet wet bits for picky adult cats - 98 kcal/can", "لقمه‌های لذیذ مرطوب بوقلمون و اردک برای گربه‌های بدغذا - ۹۸ کیلوکالری/کنسرو", 98.0),
                RecommendedFood("GimCat ShinyCat Tuna & Chicken (🐈 Wet)", "Slices of premium real tuna fillet and chicken breast - 80 kcal/can", "فیله تونا و سینه مرغ پرمیوم جی‌ام‌کت - ۸۰ کیلوکالری/کنسرو", 80.0),
                RecommendedFood("Wanpy Chicken & Crab Pouch (پوچ وانپی 🐈)", "Delicious wet jelly pouch for everyday hydration - 65 kcal/can", "پوچ ژله‌ای مرطوب و لذیذ وانپی برای هیدراتاسیون - ۶۵ کیلوکالری/کنسرو", 65.0),
                RecommendedFood("Animonda Carny Adult Beef & Cod (🐈 Wet)", "German holistic fresh meat canned pate - 110 kcal/can", "پاته گوشت تازه آلمانی آنیموندا کارنی - ۱۱۰ کیلوکالری/کنسرو", 110.0),
                RecommendedFood("Blue Buffalo Wilderness Chicken Wet (🐈 Wet)", "Pate grain-free wild chicken high protein wet meal - 120 kcal/can", "پاته بدون غلات مرغ وحشی پر پروتئین - ۱۲۰ کیلوکالری/کنسرو", 120.0)
            )
        }
    }
}

data class BcsChartItem(
    val score: Int,
    val title: String,
    val titleFa: String,
    val desc: String,
    val descFa: String
)

fun getBcsListInfo(): List<BcsChartItem> {
    return listOf(
        BcsChartItem(1, "Emaciated", "بسیار لاغر (پوست و استخوان)", "Ribs, lumbar vertebrae, pelvic bones visible. No discernible body fat. Severe loss of muscle mass.", "دنده‌ها، مهره‌های کمری و استخوان‌های لگن از دور نمایان هستند. هیچ چربی بدنی قابل لمسی وجود ندارد. تحلیل شدید توده عضلانی."),
        BcsChartItem(2, "Very Thin", "بسیار لاغر", "Ribs, lumbar vertebrae, pelvic bones easily visible. No palpable fat. Minimal muscle loss.", "دنده‌ها، مهره‌های کمری و استخوان‌های لگن به راحتی دیده می‌شوند. چربی قابل لمس نیست. تحلیل عضلانی کم."),
        BcsChartItem(3, "Thin", "لاغر", "Ribs easily palpable and may be visible. Waist easily noted. Obvious tuck.", "دنده‌ها به راحتی قابل لمس هستند و ممکن است دیده شوند. کمر به وضوح مشخص و فرورفتگی شکم بارز است."),
        BcsChartItem(4, "Underweight", "کم‌وزن", "Ribs easily palpable with minimal fat. Waist easily noted. Tuck present.", "دنده‌ها به راحتی با لایه نازکی از چربی لمس می‌شوند. کمر و تقعر شکمی مشهود است."),
        BcsChartItem(5, "Ideal", "ایده‌آل", "Ribs palpable without excess fat cover. Waist observed behind ribs. Abdominal tuck present.", "دنده‌ها بدون پوشش چربی اضافی قابل لمس هستند. دور کمر در پشت دنده‌ها دیده می‌شود و تقعر شکمی ایده‌آل است."),
        BcsChartItem(6, "Overweight", "اضافه‌وزن", "Ribs palpable with slight excess fat cover. Waist discernible but not prominent.", "دنده‌ها با لایه کمی ضخیم چربی لمس می‌شوند. دور کمر مشخص است اما برجسته نیست."),
        BcsChartItem(7, "Heavy", "سنگین‌وزن", "Ribs difficult to palpate. Thick fat cover. Waist absent. Obvious rounding of abdomen.", "لمس دنده‌ها دشوار است. لایه چربی ضخیم است. دور کمر ناپدید شده و شکم گرد شده است."),
        BcsChartItem(8, "Obese", "چاق", "Ribs not palpable under heavy fat cover. Heavy fat deposits over lumbar area & tail base. Waist absent.", "دنده‌ها زیر چربی سنگین قابل لمس نیستند. ذخایر چربی غلیظ در ناحیه کمر و پایه دم وجود دارد. دور کمر وجود ندارد."),
        BcsChartItem(9, "Severely Obese", "چاقی مفرط", "Massive fat deposits over thorax, spine, and tail base. Waist completely absent. Abdomen distended.", "ذخایر چربی عظیم روی قفسه سینه، ستون فقرات و پایه دم وجود دارد. دور کمر کاملاً ناپدید شده و شکم به شدت متورم است.")
    )
}

fun getCriteriaDisplayName(name: String, lang: String): String {
    if (lang != "fa") return name
    return when (name) {
        "Neutered Adult" -> "بالغ عقیم شده"
        "Intact Adult" -> "بالغ عقیم نشده"
        "Inactive/obese" -> "غیرفعال / مستعد چاقی"
        "Weight Loss" -> "کاهش وزن"
        "Weight Gain" -> "افزایش وزن"
        "Puppy 0-4 months" -> "توله‌سگ ۰ تا ۴ ماهه"
        "Puppy 4-12 months" -> "توله‌سگ ۴ تا ۱۲ ماهه"
        "Kitten 0-4 months" -> "بچه گربه ۰ تا ۴ ماهه"
        "Kitten 4-12 months" -> "بچه گربه ۴ تا ۱۲ ماهه"
        else -> name
    }
}
