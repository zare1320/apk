package com.example.ui.screens.vet.calculators

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
 
                        HorizontalDivider(color = strokeColor)
 
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
 
                        HorizontalDivider(color = strokeColor)
 
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
 
                        HorizontalDivider(color = strokeColor)
 
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
 
                        HorizontalDivider(color = strokeColor)
 
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
 
                        HorizontalDivider(color = strokeColor)
 
                        // 5. Skeletal Score Category
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Bone Icon", fontSize = 14.sp)
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
 
                        HorizontalDivider(color = strokeColor)
 
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
 
                        HorizontalDivider(color = strokeColor)
 
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
 
                        HorizontalDivider(color = strokeColor)
 
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
                                Text("🧪", fontSize = 14.sp)
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
 
                        HorizontalDivider(color = strokeColor)
 
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
 
                        HorizontalDivider(color = strokeColor)
 
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
 
                        HorizontalDivider(color = strokeColor.copy(alpha = 0.4f))
 
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
 
                        HorizontalDivider(color = strokeColor.copy(alpha = 0.4f))
 
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
 
                        HorizontalDivider(color = strokeColor.copy(alpha = 0.4f))
 
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
 
                        HorizontalDivider(color = strokeColor.copy(alpha = 0.4f))
 
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
 
                        HorizontalDivider(color = strokeColor)
 
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
