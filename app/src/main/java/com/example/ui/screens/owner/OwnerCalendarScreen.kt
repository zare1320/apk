package com.example.ui.screens.owner

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.CalendarEvent
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.bounceClick
import com.example.ui.theme.StaggeredFadeInItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OwnerCalendarScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val allPets by viewModel.allPets.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var showSchedulerForm by remember { mutableStateOf(false) }

    // Form inputs state
    var selectedPetId by remember { mutableStateOf(-1) }
    var selectedPetName by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("واکسیناسیون") } // "واکسیناسیون" / "ضد انگل" / "ویزیت چکاپ"
    var notes by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }

    val ownerPets = allPets.filter { it.ownerPhone == activeSession?.phoneNumber }

    // Reactive selection of the active pet for roadmap and form
    LaunchedEffect(ownerPets) {
        if (selectedPetId == -1 && ownerPets.isNotEmpty()) {
            selectedPetId = ownerPets[0].id
            selectedPetName = ownerPets[0].name
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Header info node
        StaggeredFadeInItem(index = 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = if (currentLang == "en") Alignment.Start else Alignment.End
                ) {
                    Text(
                        text = if (currentLang == "en") "📅 Smart Pet Health Scheduler" else "📅 زمان‌بندی هوشمند سلامت پت",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (currentLang == "en") {
                            "Track vaccinations, core physical checks, parasite treatments, and clinical schedules for your beloved pet. Automated reminders and SMS alerts will be triggered."
                        } else {
                            "واکسیناسیون‌ها، درمان‌های دوره‌ای و قرارهای ویزیت پزشک دلبندتان را در این صفحه مدیریت کنید. پیامک‌های اطلاع‌رسانی خودکار بر همین مبنا ارسال می‌گردد."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ------------------ START OF GAMIFIED HEALTH ROAD ------------------
        val selectedPet = ownerPets.find { it.id == selectedPetId } ?: ownerPets.firstOrNull()
        if (selectedPet != null) {
            val isCat = selectedPet.species.lowercase() == "cat" || selectedPet.species.contains("گربه") || selectedPet.species.contains("cat")
            val milestones = if (isCat) {
                listOf(
                    VaccineMilestone(
                        if (currentLang == "en") "Core FVRCP 1" else "سه‌گانه نوبت اول",
                        "FVRCP Dose 1",
                        if (currentLang == "en") "Protection against herpesvirus, calicivirus & panleukopenia" else "محافظت در برابر هرپس‌ویروس، کلسی‌ویروس و پن‌لوکوپنی",
                        if (currentLang == "en") "8 weeks" else "۸ هفتگی",
                        "اول"
                    ),
                    VaccineMilestone(
                        if (currentLang == "en") "Core FVRCP 2" else "سه‌گانه نوبت دوم",
                        "FVRCP Dose 2",
                        if (currentLang == "en") "Feline triple vaccine booster to strengthen immunity" else "تقویت ایمنی و یادآور سه‌گانه واکسن گربه‌ها",
                        if (currentLang == "en") "12 weeks" else "۱۲ هفتگی",
                        "دوم"
                    ),
                    VaccineMilestone(
                        if (currentLang == "en") "Rabies Vaccine" else "واکسن هاری",
                        "Rabies Vaccine",
                        if (currentLang == "en") "Mandatory vaccination for prevention of Rabies in cats" else "واکسیناسیون الزامی برای پیشگیری از هاری گربه‌ها",
                        if (currentLang == "en") "16 weeks" else "۱۶ هفتگی",
                        "هاری"
                    ),
                    VaccineMilestone(
                        if (currentLang == "en") "Annual Booster" else "یادآور سالانه",
                        "Annual Booster",
                        if (currentLang == "en") "Annual booster to maintain active protective immunity" else "تقویت سیستم ایمنی به صورت سالانه",
                        if (currentLang == "en") "Every Year" else "هر سال",
                        "سالانه"
                    )
                )
            } else {
                listOf(
                    VaccineMilestone(
                        if (currentLang == "en") "Core DHPPi+L 1" else "چندگانه نوبت اول",
                        "DHPPi+L Dose 1",
                        if (currentLang == "en") "Protects against Distemper, Hepatitis, Parvovirus, Parainfluenza" else "دیستمپر، هپاتیت، پاروویروس، پاراآنفولانزا",
                        if (currentLang == "en") "6 weeks" else "۶ هفتگی",
                        "اول"
                    ),
                    VaccineMilestone(
                        if (currentLang == "en") "Core DHPPi+L 2" else "چندگانه نوبت دوم",
                        "DHPPi+L Dose 2",
                        if (currentLang == "en") "Five-fold immunity booster and first scheduled reminder" else "تقویت ایمنی پنج‌گانه و یادآور اول",
                        if (currentLang == "en") "9 weeks" else "۹ هفتگی",
                        "دوم"
                    ),
                    VaccineMilestone(
                        if (currentLang == "en") "DHPPi+L 3 + Rabies" else "چندگانه سوم + هاری",
                        "DHPPi+L + Rabies",
                        if (currentLang == "en") "Complete active immunity shield plus first Rabies vaccine" else "کامل کردن سپر ایمنی بدن به همراه واکسن هاری",
                        if (currentLang == "en") "12 weeks" else "۱۲ هفتگی",
                        "هاری"
                    ),
                    VaccineMilestone(
                        if (currentLang == "en") "Annual Booster" else "یادآور سالانه",
                        "Annual Booster",
                        if (currentLang == "en") "Annual core vaccine booster for adult canine" else "تقویت سالانه سیستم ایمنی سگ بالغ",
                        if (currentLang == "en") "Every Year" else "هر سال",
                        "سالانه"
                    )
                )
            }

            val petVaccines = allEvents.filter { it.petName == selectedPet.name && (it.eventType == "واکسیناسیون" || it.eventType == "Vaccination") }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
                            Text(
                                text = "🛡️ نقشه راه سلامت و ایمنی پت (Status Roadmap)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (ownerPets.size > 1) {
                            Text(
                                text = "تغییر پت 🔄",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                    .bounceClick {
                                        val currentIndex = ownerPets.indexOfFirst { it.id == selectedPetId }
                                        val nextIndex = (currentIndex + 1) % ownerPets.size
                                        selectedPetId = ownerPets[nextIndex].id
                                        selectedPetName = ownerPets[nextIndex].name
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLang == "en") "Active immunization road for \"${selectedPet.name}\": ${if (isCat) "🐱 Cat" else "🐶 Dog"}" else "طرح ایمن‌سازی بدنی فعال برای «${selectedPet.name}»: ${if (isCat) "🐱 گربه" else "🐶 سگ"}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        milestones.forEachIndexed { index, milestone ->
                            val isDone = petVaccines.any { it.isCompleted && (it.notes.contains(milestone.keyKeyword) || it.notes.contains(milestone.title)) }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(95.dp).bounceClick {
                                    if (!isDone) {
                                        viewModel.addCalendarEvent(
                                            petId = selectedPet.id,
                                            petName = selectedPet.name,
                                            eventType = "واکسیناسیون",
                                            eventDate = if (currentLang == "en") "2026/" else "۱۴۰۵/",
                                            notes = "واکسن ${milestone.title} - ${milestone.desc}"
                                        )
                                    }
                                }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    if (isDone) {
                                        // Green check translucency (تیک سبز نیمه‌شفاف)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(Color(0x3310B981)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("✔️", fontSize = 16.sp, color = Color(0xFF10B981))
                                        }
                                    } else {
                                        // Dotted outline circle (دایره نقطه‌چین ملایم)
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawCircle(
                                                color = Color.Gray.copy(alpha = 0.5f),
                                                style = Stroke(
                                                    width = 1.5.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                                )
                                            )
                                        }
                                        Text("🛡️", fontSize = 14.sp, color = Color.Gray.copy(alpha = 0.5f))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = milestone.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDone) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = milestone.weekText,
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (index < milestones.size - 1) {
                                Text(if (currentLang == "en") "➡️" else "⬅️", fontSize = 10.sp, color = Color.Gray.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }

        Spacer(modifier = Modifier.height(12.dp))

                    // Create new scheduler toggle button
                    Button(
                        onClick = {
                            showSchedulerForm = !showSchedulerForm
                            if (ownerPets.isNotEmpty() && selectedPetId == -1) {
                                selectedPetId = ownerPets[0].id
                                selectedPetName = ownerPets[0].name
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("➕", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLang == "en") {
                                    if (showSchedulerForm) "Close Event Planner" else "Schedule New Event (Vaccine/Parasite)"
                                } else {
                                    if (showSchedulerForm) "بستن فرم ثبت رویداد" else "ثبت رویداد جدید (یادآور واکسن/ضدانگل)"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
        // Animated scheduler input Form
        AnimatedVisibility(visible = showSchedulerForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = if (currentLang == "en") Alignment.Start else Alignment.End
                ) {
                    Text(
                        text = if (currentLang == "en") "⏰ Schedule New Wellness Event" else "⏰ زمان‌بندی رویداد سلامتی جدید",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides (if (currentLang == "en") androidx.compose.ui.unit.LayoutDirection.Ltr else androidx.compose.ui.unit.LayoutDirection.Rtl)) {
                        // Pet selection drop simulation
                        if (ownerPets.isEmpty()) {
                            Text(text = if (currentLang == "en") "You must add a pet in the Dashboard first." else "ابتدا باید یک پت در تب داشبورد اضافه کنید.", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            Text(text = if (currentLang == "en") "Select Target Pet:" else "انتخاب پت هدف:", fontSize = 11.sp, color = Color.Gray)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                ownerPets.forEach { pet ->
                                    val isChosen = selectedPetId == pet.id
                                    val bg = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bg)
                                            .bounceClick {
                                                selectedPetId = pet.id
                                                selectedPetName = pet.name
                                            }
                                            .defaultMinSize(minHeight = 48.dp)
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(pet.name, fontSize = 10.sp, color = if (isChosen) Color.White else Color.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Type selector
                        Text(text = if (currentLang == "en") "Event Category:" else "نوع رویداد:", fontSize = 11.sp, color = Color.Gray)
                        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("واکسیناسیون", "ضد انگل", "ویزیت چکاپ").forEach { type ->
                                val isChosen = eventType == type
                                val bg = if (isChosen) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                                val dispType = when (type) {
                                    "واکسیناسیون" -> if (currentLang == "en") "Vaccine" else "واکسیناسیون"
                                    "ضد انگل" -> if (currentLang == "en") "Parasite" else "ضد انگل"
                                    else -> if (currentLang == "en") "Checkup" else "ویزیت چکاپ"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .bounceClick { eventType = type }
                                        .defaultMinSize(minHeight = 48.dp)
                                        .padding(horizontal = 4.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(dispType, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isChosen) Color.White else Color.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (currentLang == "en") "Event Date" else "تاریخ اجرای رویداد") },
                            placeholder = { Text(if (currentLang == "en") "e.g., 2026/06/15" else "مثال: ۱۴۰۵/۰۳/۱۵") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (currentLang == "en") "Details and Notes" else "جزئیات و توضیحات تکمیلی") },
                            placeholder = { Text(if (currentLang == "en") "e.g., Core multi-dose periodic vaccine" else "مثال: تزریق واکسن چندگانه دوره‌ای دکتر محقق") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (selectedPetName.isEmpty() || eventDate.isEmpty() || notes.isEmpty()) return@Button

                                val matchedPet = ownerPets.find { it.name == selectedPetName }
                                viewModel.addCalendarEvent(
                                    petId = matchedPet?.id ?: 0,
                                    petName = selectedPetName,
                                    eventType = eventType,
                                    eventDate = eventDate,
                                    notes = notes
                                )

                                // Clear forms
                                notes = ""
                                eventDate = ""
                                showSchedulerForm = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(if (currentLang == "en") "Save Smart Reminder" else "درج و ذخیره یادآور هوشمند", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Calendar Events
        val ownerEvents = allEvents.filter { evt -> ownerPets.any { it.name == evt.petName } }

        if (ownerEvents.isEmpty()) {
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
                        Text("📅", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (currentLang == "en") "No Scheduled Events Yet" else "هیچ رویدادی زمان‌بندی نشده است",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentLang == "en") {
                            "Organize vaccinations, parasite treatments, and clinic visits here for your pets so you get alerted when due."
                        } else {
                            "نوبت‌های واکسیناسیون، داروهای انگل‌زدایی دوره‌ای و قرارهای معاینه کلینیک را برای حیوان خود تنظیم کنید تا یادآوری به موقع انجام شود."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showSchedulerForm = true
                            if (ownerPets.isNotEmpty() && selectedPetId == -1) {
                                selectedPetId = ownerPets[0].id
                                selectedPetName = ownerPets[0].name
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentLang == "en") "Schedule First Event" else "ثبت اولین رویداد سلامت", fontSize = 11.sp)
                    }
                }
            }
        } else {
            ownerEvents.forEachIndexed { i, event ->
                StaggeredFadeInItem(index = i + 1) {
                    Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = if (currentLang == "en") Alignment.Start else Alignment.End
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides (if (currentLang == "en") androidx.compose.ui.unit.LayoutDirection.Ltr else androidx.compose.ui.unit.LayoutDirection.Rtl)) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val markText = if (event.isCompleted) {
                                            if (currentLang == "en") "Completed" else "انجام شده"
                                        } else {
                                            if (currentLang == "en") "Active Alert" else "یادآوری فعال"
                                        }
                                        val bgCol = if (event.isCompleted) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                        val textCol = if (event.isCompleted) Color(0xFF15803D) else Color(0xFFB45309)

                                        if (!event.isCompleted) {
                                            IconButton(onClick = { viewModel.toggleCalendarEvent(event) }) {
                                                Icon(Icons.Default.Check, contentDescription = "", tint = Color.Gray)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(bgCol)
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(markText, color = textCol, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    val dispEvtType = when (event.eventType) {
                                        "واکسیناسیون", "Vaccination" -> if (currentLang == "en") "Vaccination" else "واکسیناسیون"
                                        "ضد انگل", "Deworming" -> if (currentLang == "en") "Parasite Control" else "ضد انگل"
                                        else -> if (currentLang == "en") "Clinic Checkup" else "ویزیت چکاپ"
                                    }

                                    Text(
                                        text = if (currentLang == "en") "🐾 ${event.petName} | $dispEvtType" else "🐾 پت: ${event.petName} | $dispEvtType",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (currentLang == "en") "Details: ${event.notes}" else "توضیحات: ${event.notes}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
                                )

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.deleteCalendarEvent(event) }) {
                                            Icon(Icons.Default.Delete, contentDescription = if (currentLang == "en") "Delete" else "حذف", tint = Color.Red)
                                        }
                                        
                                        val context = LocalContext.current
                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                                        data = CalendarContract.Events.CONTENT_URI
                                                        putExtra(CalendarContract.Events.TITLE, "🐾 ${event.eventType} - ${event.petName}")
                                                        putExtra(CalendarContract.Events.DESCRIPTION, event.notes)
                                                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, System.currentTimeMillis())
                                                        putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PUBLIC)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    // Fallback
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            elevation = null,
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("📅", fontSize = 10.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (currentLang == "en") "Sync to Google Calendar" else "ثبت در تقویم گوگل", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (currentLang == "en") "⏰ Due: ${event.eventDate}" else "⏰ سررسید: ${event.eventDate}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class VaccineMilestone(
    val title: String,
    val enTitle: String,
    val desc: String,
    val weekText: String,
    val keyKeyword: String
)
