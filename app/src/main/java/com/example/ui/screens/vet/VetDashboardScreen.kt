package com.example.ui.screens.vet

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Pet
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VetDashboardScreen(viewModel: MainViewModel) {
    val activeSpecies by viewModel.selectedSpecies.collectAsState()
    val activeExotic by viewModel.selectedExoticOption.collectAsState()
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()
    val allPets by viewModel.allPets.collectAsState()

    var recordNumber by remember { mutableStateOf("") }
    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petWeight by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petIsNeutered by remember { mutableStateOf(false) }
    var petGender by remember { mutableStateOf("نر") }

    // Owner fields
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }

    var isAddingNewRecord by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Populate standard lists of breed based on species
    val breedOptions = when (activeSpecies) {
        "dog" -> listOf("شیتزو (Shih Tzu)", "ژرمن شپرد (German Shepherd)", "هاسکی (Husky)", "پودل (Poodle)", "بومی / دورگه")
        "cat" -> listOf("پرشین (Persian)", "بریتیش فولد (British Fold)", "دی‌اس‌اچ (DSH)", "سیامی (Siamese)")
        "exotic" -> when (activeExotic) {
            "bird" -> listOf("عروس هلندی (Cockatiel)", "مرغ عشق (Budgerigar)", "کاسکو (Grey Parrot)", "کانور")
            "rodent" -> listOf("همستر روسی", "خوکچه هندی", "خرگوش لوپ", "سنجاب")
            "aquatic" -> listOf("ماهی قرمز (Goldfish)", "گوپی", "فایتر (Betta)", "دیسکس")
            "amphibian" -> listOf("لاک‌پشت لاک‌نرم", "سمندر لرستانی", "قورباغه درختی")
            else -> listOf("نامشخص")
        }
        else -> emptyList()
    }

    // Prefill form if activeExaminedPet changes
    LaunchedEffect(activeExaminedPet) {
        activeExaminedPet?.let { pet ->
            recordNumber = pet.recordNumber
            petName = pet.name
            petBreed = pet.breed
            petWeight = pet.weight.toString()
            petAge = pet.age
            petIsNeutered = pet.isNeutered
            petGender = pet.gender
            ownerName = pet.ownerName
            ownerPhone = pet.ownerPhone
            isAddingNewRecord = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Core Species Select Row (Circular items)
        Text(
            text = "انتخاب گونه حیوان خانگی مورد معاینه (الزامی):",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Right
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Dog circle
            SpeciesCircleButton(
                emoji = "🐕",
                label = "سگ",
                isSelected = activeSpecies == "dog",
                onClick = {
                    viewModel.selectSpecies("dog")
                    petBreed = ""
                }
            )

            // Cat circle
            SpeciesCircleButton(
                emoji = "🐈",
                label = "گربه",
                isSelected = activeSpecies == "cat",
                onClick = {
                    viewModel.selectSpecies("cat")
                    petBreed = ""
                }
            )

            // Exotic circle
            SpeciesCircleButton(
                emoji = "🦎",
                label = "اگزوتیک پت",
                isSelected = activeSpecies == "exotic",
                onClick = {
                    viewModel.selectSpecies("exotic")
                    petBreed = ""
                }
            )
        }

        // Suboptions for Exotic Pet
        AnimatedVisibility(
            visible = activeSpecies == "exotic",
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نوع گروه اگزوتیک را انتخاب کنید:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(
                            Triple("bird", "پرندگان 🦜", "پرنده"),
                            Triple("rodent", "جوندگان 🐹", "جونده"),
                            Triple("aquatic", "آبزیان 🐠", "آبزیان"),
                            Triple("amphibian", "دوزیستان 🦎", "دوزیستان")
                        ).forEach { (key, display, speciesName) ->
                            val isSel = activeExotic == key
                            val col = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            val textCol = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(col)
                                    .clickable { viewModel.selectExoticOption(key) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(display, fontSize = 11.sp, color = textCol, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        // Determine if Species choice is made
        val isSpeciesChosen = activeSpecies != null && (activeSpecies != "exotic" || activeExotic != null)

        if (!isSpeciesChosen) {
            // Disabled Callout
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "قفل", tint = MaterialTheme.colorScheme.error)
                    Text(
                        text = "پذیرش بیمار غیرفعال است. جهت شروع معاینه و فعالسازی بخش‌های دارو، تشخیص و درمان ابتدا گونه حیوان (سگ/گربه/اگزوتیک) را در بالا انتخاب کنید.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Right
                    )
                }
            }
        }

        // Active Patient Summary Banner
        activeExaminedPet?.let { pet ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "بیمار فعال در حال معاینه: ${pet.name}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "گونه: ${pet.species} | نژاد: ${pet.breed} | وزن: ${pet.weight} کیلوگرم | پرونده: ${pet.recordNumber}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = { viewModel.clearExaminedPet() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("خاتمه معاینه", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        // Client Form Card (RTL input layout)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSpeciesChosen) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 فرم ثبت اطلاعات حیوان مورد معاینه",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSpeciesChosen) MaterialTheme.colorScheme.primary else Color.Gray,
                            textAlign = TextAlign.Right
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    // Record Number
                    OutlinedTextField(
                        value = recordNumber,
                        onValueChange = { recordNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("شماره پرونده (اختیاری - پایه ۱۰۰۰۱)") },
                        placeholder = { Text("مثال: 10042") },
                        enabled = isSpeciesChosen,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pet Name
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pet_name_input"),
                        label = { Text("نام پت * (الزامی)") },
                        placeholder = { Text("مثال: جسیکا") },
                        enabled = isSpeciesChosen,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pet Weight
                    OutlinedTextField(
                        value = petWeight,
                        onValueChange = { petWeight = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pet_weight_input"),
                        label = { Text("وزن به کیلوگرم * (الزامی)") },
                        placeholder = { Text("مثال: 12.5") },
                        enabled = isSpeciesChosen,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Breed selection with quick pill filters
                    OutlinedTextField(
                        value = petBreed,
                        onValueChange = { petBreed = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("نژاد پت * (الزامی)") },
                        placeholder = { Text("مثال: شیتزو") },
                        enabled = isSpeciesChosen,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (isSpeciesChosen && breedOptions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("پیشنهادهای نژاد بر اساس گونه:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            breedOptions.forEach { option ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                        .clickable { petBreed = option }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(option, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Age & Gender
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = petAge,
                            onValueChange = { petAge = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("سن تقریبی") },
                            placeholder = { Text("مثال: ۲ سال") },
                            enabled = isSpeciesChosen,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Text("جنسیت", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(enabled = isSpeciesChosen) { petGender = "نر" }
                                        .defaultMinSize(minHeight = 48.dp)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    RadioButton(
                                        selected = petGender == "نر",
                                        onClick = null,
                                        enabled = isSpeciesChosen
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("نر", fontSize = 11.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(enabled = isSpeciesChosen) { petGender = "ماده" }
                                        .defaultMinSize(minHeight = 48.dp)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    RadioButton(
                                        selected = petGender == "ماده",
                                        onClick = null,
                                        enabled = isSpeciesChosen
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ماده", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Neutered / Spayed status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("عقیم شده است؟", fontSize = 13.sp, color = if (isSpeciesChosen) MaterialTheme.colorScheme.onSurface else Color.Gray)
                        Switch(
                            checked = petIsNeutered,
                            onCheckedChange = { petIsNeutered = it },
                            enabled = isSpeciesChosen
                        )
                    }

                    // Owner fields (Always adding owner credentials for high connectivity)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "📞 اطلاعات صاحب حیوان خانگی:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpeciesChosen) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("نام صاحب پت") },
                        placeholder = { Text("مثال: مسعود زارع") },
                        enabled = isSpeciesChosen,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = ownerPhone,
                        onValueChange = { ownerPhone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("تلفن صاحب پت") },
                        placeholder = { Text("مثال: 09121234567") },
                        enabled = isSpeciesChosen,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (petName.trim().isEmpty() || petBreed.trim().isEmpty() || petWeight.trim().isEmpty()) {
                                errorMessage = "وارد کردن نام پت، نژاد و وزن الزامی است."
                                return@Button
                            }
                            val weightVal = petWeight.toDoubleOrNull()
                            if (weightVal == null || weightVal <= 0) {
                                errorMessage = "لطفاً وزن عددی معتبر وارد کنید."
                                return@Button
                            }

                            errorMessage = ""
                            viewModel.saveExaminedPet(
                                name = petName,
                                breed = petBreed,
                                weight = weightVal,
                                age = petAge,
                                gender = petGender,
                                isNeutered = petIsNeutered,
                                ownerName = ownerName,
                                ownerPhone = ownerPhone,
                                recordNumber = recordNumber
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_examined_pet"),
                        enabled = isSpeciesChosen,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ثبت و تایید پرونده بیمار", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Historial clients list
        if (allPets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📚 پرونده‌های اخیر کلینیک",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text("⏳", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            allPets.take(5).forEach { pet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.selectExistingPet(pet) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { viewModel.selectExistingPet(pet) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("انتخاب جهت معاینه", fontSize = 10.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "گونه: ${pet.species} | نژاد: ${pet.breed} | وزن: ${pet.weight}kg",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
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
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🩺", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "هیچ پرونده بیماری ثبت نشده است",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "هم‌اکنون می‌توانید ویژگی‌های بالینی، دارویی و سوابق رکوردهای مراجع جدید را ثبت کنید تا تاریخچه کلینیکی او به صورت یکپارچه ذخیره شود.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.selectSpecies("dog")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت و معاینه یک بیمار جدید", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SpeciesCircleButton(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgCol by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
    val textCol by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
    val outlineCol by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(75.dp)
                .clip(CircleShape)
                .background(bgCol)
                .border(3.dp, outlineCol, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textCol
        )
    }
}
