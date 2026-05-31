package com.example.ui.screens.owner

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OwnerMapScreen(viewModel: MainViewModel) {
    var selectedFilter by remember { mutableStateOf("همه مراجع") } // "همه مراجع", "اداری", "کلینیک", "داروخانه", "کاربران"
    var selectedLocationID by remember { mutableStateOf<String?>(null) }
    
    // Discussion forum modal state
    var showForumModal by remember { mutableStateOf(false) }
    var forumMessage by remember { mutableStateOf("") }
    val forumPosts = remember { 
        mutableStateListOf(
            ForumItem("علی محمدی", "سالمونل آکواریومی همستر در شیراز شایع شده؟", "۱۰ دقیقه پیش"),
            ForumItem("دکتر مریم سعیدی", "خیر، هنوز گزارش رسمی نظام دامپزشکی وجود ندارد.", "۵ دقیقه پیش")
        )
    }

    // Coordinates and details list
    val spatialPOIs = listOf(
        MapPOI("1", "اداره کل دامپزشکی تهران", "اداری", 0.35f, 0.25f, "تهران، بزرگراه آیت‌الله کاشانی", "۰۲۱-۴۴۰۰۰۰۰۰"),
        MapPOI("2", "کلینیک مرکزی البرز", "کلینیک", 0.62f, 0.45f, "کرج، خیابان بهار، نبش رمضانی", "۰۲۶-۳۲۰۰۰۰۰۰"),
        MapPOI("3", "داروخانه فیاض (توزیع واکسن)", "داروخانه", 0.48f, 0.65f, "تهران، میدان توحید، کوچه لادن", "۰۲۱-۶۶۰۰۰۰۰۰"),
        MapPOI("4", "کلینیک اورژانس پارس", "کلینیک", 0.22f, 0.72f, "تهران، بزرگراه صدر، ورودی کامرانیه", "۰۲۱-۲۲۰۰۰۰۰۰")
    )

    val activePOI = spatialPOIs.find { it.id == selectedLocationID }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Intro Banner
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
                    text = "📍 رادیوسنترال خدمات دامپزشکی",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "مراکز اداری، کلینیک‌های حیوانات کوچک، بیمارستان‌های دام بزرگ و داروخانه‌های توزیع‌کننده واکسن دولتی را بر روی نقشه پیدا کنید.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Right
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trigger discussion forum button
        Button(
            onClick = { showForumModal = !showForumModal },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💬", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("اتاق گفتگو با دامپزشک آنلاین (تله‌مدیسین دمو)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Discussion forum view
        AnimatedVisibility(visible = showForumModal) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                    Text("🗣️ تالار گفتمان و مشاوره آنلاین", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    forumPosts.forEach { pst ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pst.time, fontSize = 9.sp, color = Color.Gray)
                            Text(
                                text = "👤 ${pst.user}: ${pst.content}",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                        OutlinedTextField(
                            value = forumMessage,
                            onValueChange = { forumMessage = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("پیام جدید شما برای دکتر شیفت...") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (forumMessage.isNotEmpty()) {
                                        forumPosts.add(ForumItem("شما (کاربر دمو)", forumMessage, "اکنون"))
                                        forumMessage = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Send, contentDescription = "")
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Spatial Vector Map Canvas Simulation
        Text(
            text = "نقشه بصری و برداری مراکز درمانی شما:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Right
        )

        // Map filters layout
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("همه مراجع", "اداری", "کلینیک", "داروخانه").forEach { flt ->
                val isSel = selectedFilter == flt
                val bg = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val tc = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .clickable { selectedFilter = flt }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(flt, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tc)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val canvasW = maxWidth
                val canvasH = maxHeight

                // Custom vector canvas coordinates renderer
                val gridBgCol = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                val dotCol = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // Detect which POI selected on Tap
                                spatialPOIs.forEach { poi ->
                                    val poiX = poi.x * size.width
                                    val poiY = poi.y * size.height
                                    val dist = kotlin.math.hypot(offset.x - poiX, offset.y - poiY)
                                    if (dist < 40f) {
                                        selectedLocationID = poi.id
                                    }
                                }
                            }
                        }
                ) {
                    // Draw secondary grid lines to simulate GPS map matrix
                    for (x in 0..size.width.toInt() step 60) {
                        drawLine(color = gridBgCol, start = Offset(x.toFloat(), 0f), end = Offset(x.toFloat(), size.height), strokeWidth = 1f)
                    }
                    for (y in 0..size.height.toInt() step 60) {
                        drawLine(color = gridBgCol, start = Offset(0f, y.toFloat()), end = Offset(size.width, y.toFloat()), strokeWidth = 1f)
                    }

                    // Render locations
                    spatialPOIs.forEach { poi ->
                        if (selectedFilter == "همه مراجع" || poi.category == selectedFilter) {
                            val px = poi.x * size.width
                            val py = poi.y * size.height

                            val isThisSelected = selectedLocationID == poi.id
                            val col = if (isThisSelected) Color.Red else dotCol

                            drawCircle(
                                color = col,
                                radius = if (isThisSelected) 14f else 8f,
                                center = Offset(px, py)
                            )
                        }
                    }
                }

                // Standard marker tags
                spatialPOIs.forEach { poi ->
                    if (selectedFilter == "همه مراجع" || poi.category == selectedFilter) {
                        val isThisSelected = selectedLocationID == poi.id
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (poi.x * canvasW.value).dp - 10.dp,
                                    y = (poi.y * canvasH.value).dp - 25.dp
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isThisSelected) Color.Red else MaterialTheme.colorScheme.secondary)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(poi.category, fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected marker details row container
        activePOI?.let { poi ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("🏢 مشخصات مرکز انتخاب شده:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                        Text("عنوان مرکز: ${poi.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("دسته: ${poi.category}", fontSize = 11.sp, color = Color.Gray)
                        Text("نشانی: ${poi.address}", fontSize = 11.sp)
                        Text("تلفن ثابت: ${poi.phone}", fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(onClick = { /* Call handle */ }) {
                                Text("📞 تماس تلفنی با مرکز")
                            }
                            Button(onClick = { /* Nav handle */ }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🧭")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مسیریابی هوشمند")
                                }
                            }
                        }
                    }
                }
            }
        } ?: run {
            Text(
                text = "جهت مشاهده آدرس جزئی، روی یکی از نقاط قرمز یا آبی نقشه ضربه بزنید.",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

data class MapPOI(
    val id: String,
    val name: String,
    val category: String,
    val x: Float,
    val y: Float,
    val address: String,
    val phone: String
)

data class ForumItem(
    val user: String,
    val content: String,
    val time: String
)
