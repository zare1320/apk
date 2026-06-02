package com.example.ui.screens.vet.calculators

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
                        
                        HorizontalDivider(color = strokeColor, thickness = 1.dp)

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
                        
                        HorizontalDivider(color = strokeColor, thickness = 1.dp)

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
                                Text(
                                    text = rowData[5],
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
