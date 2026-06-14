package com.example.ui.screens.vet

// Force clean recompilation of VetDashboardScreen to resolve classloading NoClassDefFoundError at runtime
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.SharedSpeciesCircleButton
import com.example.ui.screens.DogVectorIcon
import com.example.ui.screens.CatVectorIcon
import com.example.ui.screens.ExoticVectorIcon
import com.example.ui.screens.ReptileVectorIcon
import com.example.ui.screens.RodentVectorIcon
import com.example.ui.screens.AquaticVectorIcon
import com.example.ui.screens.AmphibianVectorIcon
import com.example.data.database.Pet
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.glassmorphic
import com.example.ui.theme.GlassBackgroundBox

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

    val reptilesList = listOf("ایگوانا", "آفتاب‌پرست", "مار", "lizard", "iguana", "chameleon", "snake")
    val isReptileBreed = remember(petBreed) {
        reptilesList.any { petBreed.lowercase().contains(it) }
    }
    var localSpeciesSelection by remember(activeSpecies, activeExotic, petBreed) {
        mutableStateOf(
            when {
                activeSpecies == "dog" -> "dog"
                activeSpecies == "cat" -> "cat"
                activeSpecies == "exotic" && activeExotic == "bird" -> "bird"
                activeSpecies == "exotic" && activeExotic == "rodent" -> "rodent"
                activeSpecies == "exotic" && activeExotic == "aquatic" -> "aquatic"
                activeSpecies == "exotic" && activeExotic == "amphibian" -> if (isReptileBreed) "reptile" else "amphibian"
                else -> null
            }
        )
    }
    var petWeight by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petIsNeutered by remember { mutableStateOf(false) }
    var petGender by remember { mutableStateOf("نر") }

    // Owner fields
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }

    var isAddingNewRecord by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showRecordNumberHelp by remember { mutableStateOf(false) }

    // Populate standard lists of breed based on species in Persian and English
    val breedOptions = when (activeSpecies) {
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
        "exotic" -> when (activeExotic) {
            "bird" -> listOf(
                "عروس هلندی (Cockatiel)",
                "مرغ عشق (Budgerigar)",
                "کاسکو (Grey Parrot)",
                "کانور (Conure)",
                "طوطی برزیلی (Lovebird)",
                "قناری (Canary)",
                "فنچ (Finch)",
                "طوطی ملنگو (Ringneck)",
                "کاکادو (Cockatoo)",
                "ماکائو (Macaw)"
            )
            "rodent" -> listOf(
                "همستر روسی (Russian Hamster)",
                "خوکچه هندی (Guinea Pig)",
                "خرگوش لوپ (Lop Rabbit)",
                "سنجاب (Squirrel)",
                "خرگوش هلندی (Dutch Rabbit)",
                "همستر سوری (Syrian Hamster)",
                "چینچیلا (Chinchilla)",
                "جوجه تیغی (Hedgehog)"
            )
            "aquatic" -> listOf(
                "ماهی قرمز (Goldfish)",
                "گوپی (Guppy)",
                "فایتر (Betta)",
                "دیسکس (Discus)",
                "آنجل (Angel Fish)",
                "مولی (Molly)",
                "کوی (Koi)"
            )
            "amphibian" -> listOf(
                "لاک‌پشت لاک‌نرم (Softshell Turtle)",
                "سمندر لرستانی (Lorestan Newt)",
                "قورباغه درختی سبز (Green Tree Frog)",
                "لاک‌پشت گوش‌قرمز (Red-eared Slider)",
                "ایگوانا (Iguana)",
                "آفتاب‌پرست (Chameleon)",
                "مار ذرت (Corn Snake)"
            )
            else -> listOf("نامشخص", "دوزیست (Amphibian)")
        }
        else -> emptyList()
    }

    var isBreedDropdownExpanded by remember { mutableStateOf(false) }
    var breedTextFieldFocused by remember { mutableStateOf(false) }
    var showBreedGuidelines by remember { mutableStateOf(false) }

    val filteredBreeds = remember(petBreed, breedOptions) {
        if (petBreed.isEmpty()) {
            breedOptions
        } else {
            breedOptions.filter {
                it.contains(petBreed, ignoreCase = true)
            }
        }
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
        // Core Species Select Grid matching user's visual reference
        Text(
            text = "گونه مورد نظر خود را انتخاب کنید",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 8.dp),
            textAlign = TextAlign.Right
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main row of 4 columns: سگ, گربه, پرنده, اگزوتیک
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // سگ (Dog)
                WhiteSpeciesCircleButton(
                    speciesKey = "dog",
                    label = "سگ",
                    isSelected = activeSpecies == "dog",
                    onClick = {
                        viewModel.selectSpecies("dog")
                        petBreed = ""
                    }
                )

                // گربه (Cat)
                WhiteSpeciesCircleButton(
                    speciesKey = "cat",
                    label = "گربه",
                    isSelected = activeSpecies == "cat",
                    onClick = {
                        viewModel.selectSpecies("cat")
                        petBreed = ""
                    }
                )

                // پرنده (Bird)
                WhiteSpeciesCircleButton(
                    speciesKey = "bird",
                    label = "پرنده",
                    isSelected = activeSpecies == "exotic" && activeExotic == "bird",
                    onClick = {
                        viewModel.selectSpecies("exotic")
                        viewModel.selectExoticOption("bird")
                        petBreed = ""
                    }
                )

                // اگزوتیک (Exotic)
                val isExoticSelected = activeSpecies == "exotic" && activeExotic != "bird"
                WhiteSpeciesCircleButton(
                    speciesKey = "exotic",
                    label = "اگزوتیک",
                    isSelected = isExoticSelected,
                    onClick = {
                        viewModel.selectSpecies("exotic")
                        if (activeExotic == "bird") {
                            viewModel.selectExoticOption(null)
                        }
                        petBreed = ""
                    }
                )
            }

            // Sub-row of 4 columns if "اگزوتیک" is selected: جونده, خزنده, دوزیست, ماهی
            AnimatedVisibility(
                visible = activeSpecies == "exotic" && activeExotic != "bird",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نوع حیوان اگزوتیک:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, end = 4.dp),
                        textAlign = TextAlign.Right
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // جونده (Rodent)
                        WhiteSpeciesCircleButton(
                            speciesKey = "rodent",
                            label = "جونده",
                            isSelected = activeExotic == "rodent",
                            onClick = {
                                viewModel.selectExoticOption("rodent")
                                petBreed = ""
                            }
                        )

                        // خزنده (Reptile)
                        val isReptileActive = activeExotic == "amphibian" && isReptileBreed
                        WhiteSpeciesCircleButton(
                            speciesKey = "reptile",
                            label = "خزنده",
                            isSelected = isReptileActive,
                            onClick = {
                                viewModel.selectExoticOption("amphibian")
                                petBreed = "مار" // Triggers isReptileBreed
                            }
                        )

                        // دوزیست (Amphibian)
                        val isAmphibianActive = activeExotic == "amphibian" && !isReptileBreed
                        WhiteSpeciesCircleButton(
                            speciesKey = "amphibian",
                            label = "دوزیست",
                            isSelected = isAmphibianActive,
                            onClick = {
                                viewModel.selectExoticOption("amphibian")
                                petBreed = ""
                            }
                        )

                        // ماهی (Fish)
                        WhiteSpeciesCircleButton(
                            speciesKey = "aquatic",
                            label = "ماهی",
                            isSelected = activeExotic == "aquatic",
                            onClick = {
                                viewModel.selectExoticOption("aquatic")
                                petBreed = ""
                            }
                        )
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        // Determine if Species choice is made
        val isSpeciesChosen = activeSpecies != null && (activeSpecies != "exotic" || activeExotic != null)

        if (!isSpeciesChosen) {
            // High-Contrast Premium Info/Warning Card containing health-themed colors
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp, 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, 
                            contentDescription = "قفل", 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "پذیرش بیمار غیرفعال است. جهت شروع معاینه و فعالسازی بخش‌های دارو، تشخیص و درمان ابتدا گونه حیوان (سگ/گربه/اگزوتیک) را در بالا انتخاب کنید.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary, // #1F6F5F - Highly readable and perfectly matches the aesthetic!
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Right,
                        lineHeight = 20.sp
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
        AnimatedVisibility(
            visible = isSpeciesChosen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Right
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. اطلاعات صاحب حیوان خانگی شامل شماره موبایل
                        Text(
                            text = "📞 اطلاعات صاحب حیوان خانگی:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ownerPhone,
                            onValueChange = { ownerPhone = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("شماره موبایل صاحب پت") },
                            placeholder = { Text("مثال: 09121234567") },
                            enabled = isSpeciesChosen,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        AnimatedVisibility(
                            visible = ownerPhone.trim().isNotEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                OutlinedTextField(
                                    value = ownerName,
                                    onValueChange = { ownerName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("نام صاحب پت (اختیاری)") },
                                    placeholder = { Text("مثال: مسعود زارع") },
                                    enabled = isSpeciesChosen,
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. شماره پرونده
                        OutlinedTextField(
                            value = recordNumber,
                            onValueChange = { recordNumber = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("شماره پرونده") },
                            placeholder = { Text("مثال: 10042") },
                            enabled = isSpeciesChosen,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { showRecordNumberHelp = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "راهنما",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )

                        if (showRecordNumberHelp) {
                            AlertDialog(
                                onDismissRequest = { showRecordNumberHelp = false },
                                confirmButton = {
                                    Button(
                                        onClick = { showRecordNumberHelp = false }
                                    ) {
                                        Text("متوجه شدم")
                                    }
                                },
                                title = {
                                    Text(
                                        text = "💡 راهنمای ثبت شماره پرونده",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                text = {
                                    Text(
                                        text = "همکار گرامی، به منظور ثبت و بایگانی منظم‌تر پرونده مراجعه‌کنندگان خود، می‌توانید شماره پرونده فیزیکی یا ثبت‌شده در سیستم داخلی کلینیک یا بیمارستان خود را در این بخش وارد نمایید. این امر دسترسی سریع‌تر به مشخصات و سابقه درمانی حیوان را در مراجعات بعدی تسهیل می‌نماید.",
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. نام پت اختیاری
                        OutlinedTextField(
                            value = petName,
                            onValueChange = { petName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pet_name_input"),
                            label = { Text("نام پت (اختیاری)") },
                            placeholder = { Text("مثال: جسیکا") },
                            enabled = isSpeciesChosen,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. وزن پت الزامی
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

                        // 5. نژاد پت الزامی
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = petBreed,
                                onValueChange = { newValue ->
                                    petBreed = newValue
                                    isBreedDropdownExpanded = isSpeciesChosen && newValue.isNotEmpty()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        breedTextFieldFocused = focusState.isFocused
                                        if (focusState.isFocused && isSpeciesChosen && petBreed.isNotEmpty()) {
                                            isBreedDropdownExpanded = true
                                        }
                                    },
                                label = { Text("نژاد پت * (الزامی)") },
                                placeholder = { Text("مثال: شیتزو") },
                                enabled = isSpeciesChosen,
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            DropdownMenu(
                                expanded = isBreedDropdownExpanded && filteredBreeds.isNotEmpty() && filteredBreeds.any { it != petBreed },
                                onDismissRequest = { isBreedDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                            ) {
                                filteredBreeds.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, fontSize = 13.sp) },
                                        onClick = {
                                            petBreed = option
                                            isBreedDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (isSpeciesChosen && filteredBreeds.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showBreedGuidelines = !showBreedGuidelines }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showBreedGuidelines) "🏷️ پنهان کردن لیست نژادهای پیشنهادی" else "💡 مشاهده نژادهای پیشنهادی این گونه",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            AnimatedVisibility(visible = showBreedGuidelines) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Text(
                                            text = "پیشنهادهای نژاد برای کمک به ثبت سریع‌تر:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            filteredBreeds.take(24).forEach { option ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                                                        .clickable { 
                                                            petBreed = option 
                                                            isBreedDropdownExpanded = false
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(option, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 6. سن تقریبی اختیاری & 7. جنسیت اختیاری
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = petAge,
                                onValueChange = { petAge = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("سن تقریبی (اختیاری)") },
                                placeholder = { Text("مثال: ۲ سال") },
                                enabled = isSpeciesChosen,
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "جنسیت پت (اختیاری)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // "نر" option button
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (petGender == "نر" && isSpeciesChosen) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = if (petGender == "نر" && isSpeciesChosen) 1.5.dp else 0.dp,
                                                color = if (petGender == "نر" && isSpeciesChosen) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(enabled = isSpeciesChosen) { petGender = "نر" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            RadioButton(
                                                selected = petGender == "نر",
                                                onClick = null,
                                                enabled = isSpeciesChosen,
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "نر",
                                                fontSize = 11.sp,
                                                fontWeight = if (petGender == "نر") FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    // "ماده" option button
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (petGender == "ماده" && isSpeciesChosen) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = if (petGender == "ماده" && isSpeciesChosen) 1.5.dp else 0.dp,
                                                color = if (petGender == "ماده" && isSpeciesChosen) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(enabled = isSpeciesChosen) { petGender = "ماده" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            RadioButton(
                                                selected = petGender == "ماده",
                                                onClick = null,
                                                enabled = isSpeciesChosen,
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "ماده",
                                                fontSize = 11.sp,
                                                fontWeight = if (petGender == "ماده") FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 8. حیوان عقیم شده است؟
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = isSpeciesChosen) { petIsNeutered = !petIsNeutered }
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (petIsNeutered) Icons.Default.Check else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (petIsNeutered && isSpeciesChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "حیوان عقیم شده است؟",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = petIsNeutered,
                                onCheckedChange = { petIsNeutered = it },
                                enabled = isSpeciesChosen,
                                thumbContent = if (petIsNeutered) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                } else null
                            )
                        }

                        if (errorMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 9. ثبت و تایید پرونده بیمار
                        Button(
                            onClick = {
                                if (petBreed.trim().isEmpty() || petWeight.trim().isEmpty()) {
                                    errorMessage = "وارد کردن نژاد و وزن پت الزامی است."
                                    return@Button
                                }
                                val weightVal = petWeight.toDoubleOrNull()
                                if (weightVal == null || weightVal <= 0) {
                                    errorMessage = "لطفاً وزن عددی معتبر وارد کنید."
                                    return@Button
                                }

                                errorMessage = ""
                                viewModel.saveExaminedPet(
                                    name = petName.trim().ifEmpty { "بدون نام" },
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
fun WhiteSpeciesCircleButton(
    speciesKey: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outlineCol = if (isSelected) Color(0xFF2DD4BF) else Color(0xFFE2E8F0)
    val outlineWidth = if (isSelected) 3.dp else 1.dp
    val textCol = if (isSelected) Color(0xFF2DD4BF) else MaterialTheme.colorScheme.onBackground

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(outlineWidth, outlineCol, CircleShape)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            val iconColor = when (speciesKey) {
                "dog" -> Color(0xFF38BDF8)
                "cat" -> Color(0xFF0D9488)
                "bird" -> Color(0xFF60A5FA)
                "rodent" -> Color(0xFF34D399)
                "aquatic" -> Color(0xFF38BDF8)
                "amphibian" -> Color(0xFF4ADE80)
                else -> Color(0xFFFBBF24) // reptile / exotic
            }

            when (speciesKey) {
                "dog" -> DogVectorIcon(modifier = Modifier.fillMaxSize(), tint = iconColor)
                "cat" -> CatVectorIcon(modifier = Modifier.fillMaxSize(), tint = iconColor)
                "bird" -> ExoticVectorIcon(modifier = Modifier.fillMaxSize(), tint = iconColor)
                "rodent" -> RodentVectorIcon(modifier = Modifier.fillMaxSize(), tint = iconColor)
                "aquatic" -> AquaticVectorIcon(modifier = Modifier.fillMaxSize(), tint = iconColor)
                "amphibian" -> AmphibianVectorIcon(modifier = Modifier.fillMaxSize(), tint = iconColor)
                else -> ReptileVectorIcon(modifier = Modifier.fillMaxSize(), tint = iconColor)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textCol,
            textAlign = TextAlign.Center
        )
    }
}
