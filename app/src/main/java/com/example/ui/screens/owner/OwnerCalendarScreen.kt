package com.example.ui.screens.owner

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.CalendarEvent
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OwnerCalendarScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val allPets by viewModel.allPets.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()

    var showSchedulerForm by remember { mutableStateOf(false) }

    // Form inputs state
    var selectedPetId by remember { mutableStateOf(-1) }
    var selectedPetName by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("واکسیناسیون") } // "واکسیناسیون" / "ضد انگل" / "ویزیت چکاپ"
    var notes by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }

    val ownerPets = allPets.filter { it.ownerPhone == activeSession?.phoneNumber }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Header info node
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "📅 زمان‌بندی هوشمند سلامت پت",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "واکسیناسیون‌ها، درمان‌های دوره‌ای و قرارهای ویزیت پزشک دلبندتان را در این صفحه مدیریت کنید. پیامک‌های اطلاع‌رسانی خودکار بر همین مبنا ارسال می‌گردد.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
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
                Text(if (showSchedulerForm) "بستن فرم ثبت رویداد" else "ثبت رویداد جدید (یادآور واکسن/ضدانگل)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    horizontalAlignment = Alignment.End
                ) {
                    Text("⏰ زمان‌بندی رویداد سلامتی جدید", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                        // Pet selection drop simulation
                        if (ownerPets.isEmpty()) {
                            Text("ابتدا باید یک پت در تب داشبورد اضافه کنید.", color = Color.Gray, fontSize = 11.sp)
                        } else {
                            Text("انتخاب پت هدف:", fontSize = 11.sp, color = Color.Gray)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                ownerPets.forEach { pet ->
                                    val isChosen = selectedPetId == pet.id
                                    val bg = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bg)
                                            .clickable {
                                                selectedPetId = pet.id
                                                selectedPetName = pet.name
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(pet.name, fontSize = 10.sp, color = if (isChosen) Color.White else Color.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Type selector
                        Text("نوع رویداد:", fontSize = 11.sp, color = Color.Gray)
                        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("واکسیناسیون", "ضد انگل", "ویزیت چکاپ").forEach { type ->
                                val isChosen = eventType == type
                                val bg = if (isChosen) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickable { eventType = type }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isChosen) Color.White else Color.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("تاریخ اجرای رویداد") },
                            placeholder = { Text("مثال: ۱۴۰۵/۰۳/۱۵") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("جزئیات و توضیحات تکمیلی") },
                            placeholder = { Text("مثال: تزریق واکسن چندگانه دوره‌ای دکتر محقق") },
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
                            Text("درج و ذخیره یادآور هوشمند", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📅", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "هیچ رویدادی زمان‌بندی نشده است",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "بر روی دکمه ثبت رویداد جدید در بالا کلیک کنید تا نوبت‌های واکسیناسیون و ضدالمل حیوانات خود را ثبت نمایید.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            ownerEvents.forEach { event ->
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
                        horizontalAlignment = Alignment.End
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val markText = if (event.isCompleted) "انجام شده" else "یادآوری فعال"
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

                                Text(
                                    text = "🐾 پت: ${event.petName} | ${event.eventType}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("توضیحات: ${event.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.deleteCalendarEvent(event) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                                }
                                Text(
                                    text = "⏰ سررسید: ${event.eventDate}",
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
