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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

                    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                        OutlinedTextField(
                            value = newPetName,
                            onValueChange = { newPetName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("نام پت (فارسی)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("نوع حیوان (گونه زیستی):", fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("dog", "🐕 سگ", Color(0xFFC084FC)),
                                Triple("cat", "🐈 گربه", Color(0xFF60A5FA)),
                                Triple("exotic", "🦜 پرنده/اگزوتیک", Color(0xFF34D399))
                            ).forEach { (code, label, color) ->
                                val isSel = newPetSpecies == code
                                val bg = if (isSel) color else MaterialTheme.colorScheme.surface
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickable { newPetSpecies = code }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        color = if (isSel) Color.White else Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
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

                        OutlinedTextField(
                            value = newPetBreed,
                            onValueChange = { newPetBreed = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("نژاد حیوان") },
                            placeholder = { Text("پرشین / هاسکی") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

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
                                        .padding(vertical = 8.dp),
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
            Text(
                text = "هنوز هیچ حیوان خانگی در اکانت شما ثبت نشده است.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
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

                        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
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
