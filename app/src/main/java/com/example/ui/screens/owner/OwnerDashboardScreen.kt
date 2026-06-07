package com.example.ui.screens.owner

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.DogVectorIcon
import com.example.ui.screens.CatVectorIcon
import com.example.ui.screens.ExoticVectorIcon
import com.example.data.database.Pet
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OwnerDashboardScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val allPets by viewModel.allPets.collectAsState()

    // Filter pets belonging to this logged-in owner
    val ownerPhoneNum = activeSession?.phoneNumber ?: "empty"
    val ownerPetsList = allPets.filter { it.ownerPhone == ownerPhoneNum }

    var showAddPetForm by remember { mutableStateOf(false) }

    // Add Pet Form state
    var newPetName by remember { mutableStateOf("") }
    var newPetBreed by remember { mutableStateOf("") }
    var newPetWeight by remember { mutableStateOf("") }
    var newPetAge by remember { mutableStateOf("") }
    var newPetGender by remember { mutableStateOf("نر") }
    var newPetSpecies by remember { mutableStateOf("dog") } // "dog", "cat", "exotic"

    // Populate standard lists of breed based on species in Persian and English for Owner registration
    val ownerBreedOptions = when (newPetSpecies) {
        "dog" -> listOf(
            "شیتزو (Shih Tzu)",
            "ژرمن شپرد (German Shepherd)",
            "هاسکی سیبرین (Siberian Husky)",
            "پودل (Poodle)",
            "پمرانین (Pomeranian)",
            "گلدن رتریور (Golden Retriever)",
            "پاگ (Pug)",
            "بولداگ (Bulldog)",
            "روتوایلر (Rottweiler)",
            "دوبرمن (Doberman)",
            "پیتبول (Pitbull)",
            "سرابی (Sarabi Mastiff)",
            "تریر (Terrier)",
            "داکسهوند (Dachshund)",
            "ساموید (Samoyed)",
            "گریت دین (Great Dane)",
            "باکسر (Boxer)",
            "بیگل (Beagle)",
            "چاو چاو (Chow Chow)",
            "کوکر اسپنیل (Cocker Spaniel)",
            "بومی / دورگه (Mixed Breed)"
        )
        "cat" -> listOf(
            "پرشین (Persian)",
            "دی‌اس‌اچ (DSH)",
            "اسکاتیش فولد (Scottish Fold)",
            "بریتیش فولد (British Fold)",
            "بریتیش شورت‌هر (British Shorthair)",
            "دی‌ال‌اچ (DLH)",
            "سیامی (Siamese)",
            "راگدول (Ragdoll)",
            "مین کون (Maine Coon)",
            "اسفینکس (Sphynx)",
            "راشن بلو (Russian Blue)",
            "بنگال (Bengal)",
            "آنگورای ترکی (Turkish Angora)",
            "بیرمن (Birman)",
            "بومی / دورگه (Mixed Breed)"
        )
        "exotic" -> listOf(
            "عروس هلندی (Cockatiel)",
            "مرغ عشق (Budgerigar)",
            "کاسکو (Grey Parrot)",
            "همستر روسی (Russian Hamster)",
            "خوکچه هندی (Guinea Pig)",
            "خرگوش لوپ (Lop Rabbit)",
            "ماهی قرمز (Goldfish)",
            "گوپی (Guppy)",
            "لاک‌پشت گوش‌قرمز (Red-eared Slider)",
            "آنجل (Angel Fish)"
        )
        else -> emptyList()
    }

    var isBreedDropdownExpanded by remember { mutableStateOf(false) }
    var breedTextFieldFocused by remember { mutableStateOf(false) }

    val filteredBreeds = remember(newPetBreed, ownerBreedOptions) {
        if (newPetBreed.isEmpty()) {
            ownerBreedOptions
        } else {
            ownerBreedOptions.filter {
                it.contains(newPetBreed, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcoming card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "سلام، ${activeSession?.fullName ?: "صاحب عزیز پت"} 👋",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "به پورتال مدیریت پرونده‌های سلامت حیوانات خانگی خوش آمدید. اطلاعات کلینیکی همگام‌سازی شده با دامپزشک فورا در تب نسخه قابل بازرسی است.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions Bar
        Button(
            onClick = { showAddPetForm = !showAddPetForm },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("➕", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (showAddPetForm) "بستن فرم ورود" else "ثبت و افزودن حیوان خانگی جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Add Pet Form
        AnimatedVisibility(visible = showAddPetForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("🐾 مشخصات حیوان دلبند شما:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
                        OutlinedTextField(
                            value = newPetName,
                            onValueChange = { newPetName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("نام پت (فارسی)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("نوع حیوان (گونه زیستی):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("dog", "سگ", Color(0xFFC084FC)),
                                Triple("cat", "گربه", Color(0xFF60A5FA)),
                                Triple("exotic", "پرنده/اگزوتیک", Color(0xFF34D399))
                            ).forEach { (code, label, color) ->
                                val isSel = newPetSpecies == code
                                val bg = if (isSel) color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bg)
                                        .border(
                                            width = if (isSel) 0.dp else 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { newPetSpecies = code }
                                        .defaultMinSize(minHeight = 48.dp)
                                        .padding(horizontal = 4.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        when (code) {
                                            "dog" -> DogVectorIcon(modifier = Modifier.size(18.dp), tint = if (isSel) Color.White else color)
                                            "cat" -> CatVectorIcon(modifier = Modifier.size(18.dp), tint = if (isSel) Color.White else color)
                                            else -> ExoticVectorIcon(modifier = Modifier.size(18.dp), tint = if (isSel) Color.White else color)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            label,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newPetWeight,
                                onValueChange = { newPetWeight = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("وزن (کیلوگرم)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = newPetAge,
                                onValueChange = { newPetAge = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("سن تخمینی (سال)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Breed selection with quick pill filters and intelligent autocomplete
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newPetBreed,
                                onValueChange = { newValue ->
                                    newPetBreed = newValue
                                    isBreedDropdownExpanded = newValue.isNotEmpty()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        breedTextFieldFocused = focusState.isFocused
                                        if (focusState.isFocused && newPetBreed.isNotEmpty()) {
                                            isBreedDropdownExpanded = true
                                        }
                                    },
                                label = { Text("نژاد حیوان") },
                                placeholder = { Text("پرشین / هاسکی") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            DropdownMenu(
                                expanded = isBreedDropdownExpanded && filteredBreeds.isNotEmpty() && filteredBreeds.any { it != newPetBreed },
                                onDismissRequest = { isBreedDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                            ) {
                                filteredBreeds.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, fontSize = 13.sp) },
                                        onClick = {
                                            newPetBreed = option
                                            isBreedDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (filteredBreeds.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("پیشنهادهای نژاد بر اساس گونه:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                filteredBreeds.take(12).forEach { option ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                            .clickable { 
                                                newPetBreed = option 
                                                isBreedDropdownExpanded = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(option, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Gender Switch
                        Text("جنسیت حیوان:", fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("نر", "ماده").forEach { gen ->
                                val isSel = newPetGender == gen
                                val bg = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickable { newPetGender = gen }
                                        .defaultMinSize(minHeight = 48.dp)
                                        .padding(horizontal = 4.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(gen, fontSize = 11.sp, color = if (isSel) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (newPetName.isEmpty()) return@Button
                                val intAge = newPetAge.toIntOrNull() ?: 1
                                val dblWeight = newPetWeight.toDoubleOrNull() ?: 1.0

                                val newlyCreatedPet = Pet(
                                    name = newPetName,
                                    species = newPetSpecies,
                                    breed = newPetBreed.ifEmpty { "ناپیدا" },
                                    weight = dblWeight,
                                    age = newPetAge,
                                    gender = newPetGender,
                                    isNeutered = false,
                                    ownerName = activeSession?.fullName ?: "Unknown",
                                    ownerPhone = activeSession?.phoneNumber ?: "Unknown",
                                    recordNumber = "PR-" + (allPets.size + 10041)
                                )

                                viewModel.addNewPatient(newlyCreatedPet)

                                // Reset form fields
                                newPetName = ""
                                newPetBreed = ""
                                newPetWeight = ""
                                newPetAge = ""
                                showAddPetForm = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("درج نهایی پرونده پت")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // History list of pets
        Text(
            text = "🐾 حیوانات ثبت‌شده شما:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Right
        )

        if (ownerPetsList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐾", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "هنوز هیچ حیوان خانگی ثبت نشده است",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "برای ثبت پرونده سلامت، نوبت‌های درمان دوره‌ای و دسترسی به اطلاعات کلینیکی، ابتدا مشخصات حیوان خانگی خود را وارد کنید.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddPetForm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت و افزودن اولین پت", fontSize = 11.sp)
                    }
                }
            }
        } else {
            ownerPetsList.forEach { pet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🐾", fontSize = 24.sp)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("گونه: " + if (pet.species == "dog") "سگ‌سان" else if (pet.species == "cat") "گربه‌سان" else "پرنده/اگزوتیک زیستی", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("نژاد حیوان:", fontSize = 10.sp, color = Color.Gray)
                                    Text(pet.breed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("جنسیت و سن:", fontSize = 10.sp, color = Color.Gray)
                                    Text("${pet.gender} | ${pet.age} ساله", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("وزن بالینی:", fontSize = 10.sp, color = Color.Gray)
                                    Text("${pet.weight} کیلوگرم", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("شناسه پرونده: ${pet.recordNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
