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

    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    val strokeColor = MaterialTheme.colorScheme.outlineVariant
    val primaryText = MaterialTheme.colorScheme.onSurface
    val secondaryText = MaterialTheme.colorScheme.onSurfaceVariant
    val highlightColor = MaterialTheme.colorScheme.primary
    val onHighlightColor = MaterialTheme.colorScheme.onPrimary
    val linkColor = MaterialTheme.colorScheme.primary

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
                            .background(MaterialTheme.colorScheme.secondaryContainer)
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = secondaryText,
                                lineHeight = 20.sp,
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
