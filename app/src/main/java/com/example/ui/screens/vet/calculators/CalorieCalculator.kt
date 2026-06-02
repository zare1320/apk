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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.example.data.database.Pet

@Composable
fun CalorieCalculatorView(
    activePet: Pet? = null,
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
                            label = { Text("Kilograms") },
                            placeholder = { Text("kgs") },
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
