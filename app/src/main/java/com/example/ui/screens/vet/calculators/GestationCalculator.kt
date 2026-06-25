package com.example.ui.screens.vet.calculators

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.example.viewmodel.MainViewModel

data class LocalPatient(
    val id: String,
    val petName: String,
    val ownerName: String,
    val age: String,
    val species: String,
    val sex: String,
    val notes: String
)

fun getFormattedDateWithOffset(baseDate: java.util.Date?, daysSelected: Int, currentLang: String = "en"): String {
    if (baseDate == null) return "-"
    val cal = java.util.Calendar.getInstance()
    cal.time = baseDate
    cal.add(java.util.Calendar.DAY_OF_YEAR, daysSelected)
    
    if (currentLang == "fa") {
        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.ENGLISH)
        return sdf.format(cal.time)
    }
    
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
    
    val currentLang by viewModel.currentLanguage.collectAsState()
    
    val formattedToday = if (currentLang == "en") {
        "Today's Date: ${todayFormat.format(today)}$todaySuffix ${yearFormat.format(today)}"
    } else {
        "تاریخ امروز: ${todayFormat.format(today)}$todaySuffix ${yearFormat.format(today)}"
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    val strokeColor = MaterialTheme.colorScheme.outlineVariant
    val primaryText = MaterialTheme.colorScheme.onSurface
    val secondaryText = MaterialTheme.colorScheme.onSurfaceVariant
    val highlightColor = MaterialTheme.colorScheme.primary
    val onHighlightColor = MaterialTheme.colorScheme.onPrimary
    val linkColor = MaterialTheme.colorScheme.primary

    val layoutDirection = if (currentLang == "en") androidx.compose.ui.unit.LayoutDirection.Ltr else androidx.compose.ui.unit.LayoutDirection.Rtl

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
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
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentLang == "en") "Patients / Patient Info" else "مشخصات بیمار",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                    
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Warning Box
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                                Text(
                                    text = if (currentLang == "en") {
                                        "To automatically populate the fields, please activate a patient's record in the Admissions tab. Otherwise, you can enter the details manually."
                                    } else {
                                        "کاربر گرامی، جهت تکمیل خودکار اطلاعات، لطفا ابتدا پرونده یک بیمار را در بخش پذیرش فعال کنید. در غیر این صورت، باید فیلدها را به صورت دستی پر نمایید."
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

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
                                Text(
                                    text = if (currentLang == "en") "View Patients" else "مشاهده بیماران",
                                    fontSize = 11.sp,
                                    color = primaryText,
                                    fontWeight = FontWeight.SemiBold
                                )
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
                                Text(
                                    text = if (currentLang == "en") "Save New" else "ذخیره جدید",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Form Row 1: ID, Pet Name, Owner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CustomField(
                                label = if (currentLang == "en") "ID #" else "شناسه #",
                                value = patientId,
                                onValueChange = { patientId = it },
                                placeholder = if (currentLang == "en") "ID" else "شناسه",
                                modifier = Modifier.weight(1f)
                            )
                            CustomField(
                                label = if (currentLang == "en") "Pet Name" else "نام پت",
                                value = petName,
                                onValueChange = { petName = it },
                                placeholder = if (currentLang == "en") "Pet Name" else "نام پت",
                                modifier = Modifier.weight(1f)
                            )
                            CustomField(
                                label = if (currentLang == "en") "Owner" else "صاحب حیوان",
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                placeholder = if (currentLang == "en") "Owner Name" else "نام صاحب",
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
                                label = if (currentLang == "en") "Age" else "سن",
                                value = age,
                                onValueChange = { age = it },
                                placeholder = if (currentLang == "en") "Age" else "سن",
                                modifier = Modifier.weight(1f)
                            )
                            
                            val displaySpecies = if (species == "Canine") {
                                if (currentLang == "en") "Canine" else "سگ (Canine)"
                            } else {
                                if (currentLang == "en") "Feline" else "گربه (Feline)"
                            }
                            val speciesOptions = if (currentLang == "en") listOf("Canine", "Feline") else listOf("سگ (Canine)", "گربه (Feline)")
                            
                            CustomDropdownField(
                                label = if (currentLang == "en") "Species" else "گونه",
                                selectedValue = displaySpecies,
                                options = speciesOptions,
                                onSelect = { selectedDisplay ->
                                    val actualSpec = if (selectedDisplay == "سگ (Canine)" || selectedDisplay == "Canine") "Canine" else "Feline"
                                    species = actualSpec
                                    isCanine = (actualSpec == "Canine")
                                },
                                modifier = Modifier.weight(1.2f)
                            )
                            
                            val displaySex = if (sex == "Male") {
                                if (currentLang == "en") "Male" else "نر (Male)"
                            } else {
                                if (currentLang == "en") "Female" else "ماده (Female)"
                            }
                            val sexOptions = if (currentLang == "en") listOf("Male", "Female") else listOf("نر (Male)", "ماده (Female)")
                            
                            CustomDropdownField(
                                label = if (currentLang == "en") "Sex" else "جنسیت",
                                selectedValue = displaySex,
                                options = sexOptions,
                                onSelect = { selectedDisplay ->
                                    sex = if (selectedDisplay == "نر (Male)" || selectedDisplay == "Male") "Male" else "Female"
                                },
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
                            Text(
                                text = if (currentLang == "en") "Add Patient Notes" else "افزودن یادداشت بیمار",
                                fontSize = 11.sp,
                                color = primaryText,
                                fontWeight = FontWeight.Medium
                            )
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
                                            Text(
                                                text = if (currentLang == "en") "Enter patient notes here..." else "یادداشت‌های بیمار را اینجا وارد کنید...",
                                                color = secondaryText.copy(alpha = 0.5f),
                                                fontSize = 11.sp
                                            )
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
                            Text(
                                text = if (currentLang == "en") "Patient Records:" else "سوابق بیماران ثبت شده:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
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
                                        val recSpec = if (record.species == "Canine") (if (currentLang == "en") "Canine" else "سگ") else (if (currentLang == "en") "Feline" else "گربه")
                                        Text(
                                            text = "${record.petName} ($recSpec) - ID: ${record.id}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryText
                                        )
                                        val lblOwner = if (currentLang == "en") "Owner" else "صاحب"
                                        val lblAge = if (currentLang == "en") "Age" else "سن"
                                        val lblSex = if (currentLang == "en") "Sex" else "جنسیت"
                                        val valSex = if (record.sex == "Male") (if (currentLang == "en") "Male" else "نر") else (if (currentLang == "en") "Female" else "ماده")
                                        Text(
                                            text = "$lblOwner: ${record.ownerName} | $lblAge: ${record.age} | $lblSex: $valSex",
                                            fontSize = 10.sp,
                                            color = secondaryText
                                        )
                                        if (record.notes.isNotEmpty()) {
                                            val lblNotes = if (currentLang == "en") "Notes" else "یادداشت‌ها"
                                            Text(
                                                text = "$lblNotes: ${record.notes}",
                                                fontSize = 10.sp,
                                                color = secondaryText
                                            )
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
                            val cardTitleText = if (currentLang == "en") {
                                if (petName.isNotEmpty()) "Due Date for $petName" else "Due Date"
                            } else {
                                if (petName.isNotEmpty()) "تاریخ احتمالی زایمان برای $petName" else "تاریخ احتمالی زایمان"
                            }
                            Text(
                                text = cardTitleText,
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
                    
                    val chooseDateLabel = if (currentLang == "en") {
                        if (petName.isNotEmpty()) "Choose Conception Date for $petName - ${if (isCanine) "Canine" else "Feline"}" else "Choose Conception Date - ${if (isCanine) "Canine" else "Feline"}"
                    } else {
                        if (petName.isNotEmpty()) "انتخاب تاریخ جفت‌گیری (لقاح) برای $petName - ${if (isCanine) "سگ" else "گربه"}" else "انتخاب تاریخ جفت‌گیری (لقاح) - ${if (isCanine) "سگ" else "گربه"}"
                    }
                    Text(
                        text = chooseDateLabel,
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
                        val dateBtnText = if (conceptionDate == null) {
                            if (currentLang == "en") "Select date" else "انتخاب تاریخ"
                        } else {
                            sdfDisplay.format(conceptionDate!!)
                        }
                        
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
                            Text(
                                text = if (currentLang == "en") "Days since Conception: " else "روزهای سپری شده از لقاح: ",
                                fontSize = 11.sp,
                                color = secondaryText
                            )
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
                        text = if (currentLang == "en") "Canine: Gestation range: 57-65 days, Average: 63 days." else "سگ: محدوده بارداری: ۵۷ تا ۶۵ روز، میانگین: ۶۳ روز.",
                        fontSize = 11.sp,
                        color = secondaryText,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLang == "en") "Feline: Gestation range: 60-67 days, Average: 63-65 days." else "گربه: محدوده بارداری: ۶۰ تا ۶۷ روز، میانگین: ۶۳-۶۵ روز.",
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
                        val likelyDueDateLabel = if (currentLang == "en") {
                            if (petName.isNotEmpty()) "Likely Due Date for $petName" else "Likely Due Date"
                        } else {
                            if (petName.isNotEmpty()) "تاریخ تخمینی زایمان برای $petName" else "تاریخ تخمینی زایمان"
                        }
                        Text(
                            text = likelyDueDateLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val avgGestation = if (isCanine) 63 else 64
                        val likelyDueDateText = getFormattedDateWithOffset(conceptionDate, avgGestation, currentLang)
                        
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
                            val earlyDueDateLabel = if (currentLang == "en") {
                                if (petName.isNotEmpty()) "Early Due Date ($petName)" else "Early Due Date"
                            } else {
                                if (petName.isNotEmpty()) "حداقل زمان بارداری ($petName)" else "حداقل زمان بارداری"
                            }
                            Text(
                                text = earlyDueDateLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val earlyOffset = if (isCanine) 57 else 60
                            val earlyDateText = getFormattedDateWithOffset(conceptionDate, earlyOffset, currentLang)
                            
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
                            val lateDueDateLabel = if (currentLang == "en") {
                                if (petName.isNotEmpty()) "Late Due Date ($petName)" else "Late Due Date"
                            } else {
                                if (petName.isNotEmpty()) "حداکثر زمان بارداری ($petName)" else "حداکثر زمان بارداری"
                            }
                            Text(
                                text = lateDueDateLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val lateOffset = if (isCanine) 65 else 67
                            val lateDateText = getFormattedDateWithOffset(conceptionDate, lateOffset, currentLang)
                            
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
                                text = if (currentLang == "en") "Pregnancy Info" else "دانستنی‌های دوران بارداری",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val bulletPoints = if (currentLang == "en") {
                        listOf(
                            "Fertilization occurs in the oviducts of both dogs and cats. Embryo implantation in the uterus happens around day 18 in dogs and day 14 in cats.",
                            "Embryos are palpable around day 21, and the diameter of the uterine swellings roughly doubles every 7 days. After days 35 to 38, the swellings become indistinct, making palpation difficult until late pregnancy.",
                            "The fetal skeleton starts to calcify around day 28 but is not detectable by standard radiography before days 42-45. The most routine method to count puppies/kittens is radiography after day 55 of gestation (which is safe during this period).",
                            "Ultrasonography is most accurate for initial diagnosis during the first 25 to 35 days. Prior to this, lacks of differentiation may lead to false-negative results.",
                            "Doppler-type devices allow hearing fetal heartbeats to confirm viability, which beat 2 to 3 times faster than the mother's heart rate.",
                            "Ultrasonography is especially indicated to differentiate normal pregnancy from other causes of uterine enlargement (such as pyometra or hydrometra)."
                        )
                    } else {
                        listOf(
                            "لقاح در لوله‌های رحم هر دو حیوان سگ و گربه رخ می‌دهد. لانه‌گزینی رویان در رحم حدوداً در روز ۱۸ در سگ‌ها و روز ۱۴ در گربه‌ها انجام می‌پذیرد.",
                            "جنین در روز ۲۱ قابل لمس است و قطر توده‌ها تقریباً هر ۷ روز دو برابر می‌شود. پس از روز ۳۵ الی ۳۸، توده‌ها غیر قابل تشخیص و نامشخص می‌شوند و معاینه تا روزهای پایانی بارداری دشوار خواهد بود.",
                            "استخوان‌بندی جنین از روز ۲۸ شروع به کلسیمی شدن می‌کند ولی توسط رادیوگرافی استاندارد قبل از روزهای ۴۲-۴۵ شناسایی نمی‌شود. روتین‌ترین روش شمارش تعداد توله‌ها رادیوگرافی بعد از روز ۵۵ بارداری است (طی این دوره بی‌خطر است).",
                            "سونوگرافی در ۲۵ الی ۳۵ روز اول دقیق‌ترین زمان برای تشخیص اولیه می‌باشد. قبل از آن به دلیل نبود تمایز کافی ممکن است نتایج منفی کاذب نمایان شود.",
                            "دستگاه‌های نوع داپلر اجازه شنیدن ضربان قلب جنینی را برای تایید حیات فراهم می‌کنند که با سرعتی ۲ تا ۳ برابر سریع‌تر از ضربان مادر می‌زند.",
                            "انجام سونوگرافی خصوصاً جهت افتراق بارداری طبیعی از سایر علل بزرگی مرضی یا آب دور زهدان (مانند پیرومتر عفونی یا هیدرومتر مایع) تجویز می‌گردد."
                        )
                    }
                    
                    bulletPoints.forEach { point ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("•", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = highlightColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = point,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = secondaryText,
                                lineHeight = 20.sp,
                                textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    Text(
                        text = if (currentLang == "en") "*Source: Merck Veterinary Manual" else "*منبع: مرجع دامپزشکی مرک (Merck Veterinary Manual)",
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
