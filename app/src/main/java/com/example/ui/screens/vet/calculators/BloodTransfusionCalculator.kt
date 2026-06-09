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
fun BloodTransfusionCalculator(
    activePet: Pet? = null,
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

    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true,
                            modifier = Modifier.width(150.dp)
                        )
                    }

                    // Compact customizable Weight Row
                    val currentWeightKg = weightStr.toDoubleOrNull() ?: 0.0
                    val showWeightWarning = currentWeightKg > 0.0 && ((!isDog && currentWeightKg > 12.0) || (isDog && currentWeightKg > 80.0))
                    val weightBorderColors = if (showWeightWarning) {
                        OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFFF59E0B).copy(alpha = 0.6f),
                            focusedLabelColor = Color(0xFFD97706),
                            unfocusedLabelColor = Color(0xFFD97706).copy(alpha = 0.8f)
                        )
                    } else {
                        OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = weightBorderColors,
                                singleLine = true,
                                modifier = Modifier.width(150.dp)
                            )
                        }
                        if (showWeightWarning) {
                            Text(
                                text = "⚠️ آیا از وزن وارد شده اطمینان دارید؟",
                                color = Color(0xFFD97706),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                        }
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
        colors = CardDefaults.cardColors(containerColor = if (MaterialTheme.colorScheme.background.red < 0.3f) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else Color(0xFFF9FAFB)),
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
