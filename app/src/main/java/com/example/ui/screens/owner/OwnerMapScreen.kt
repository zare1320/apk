package com.example.ui.screens.owner

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OwnerMapScreen(viewModel: MainViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()

    val defaultFilter = if (currentLang == "en") "All Venues" else "همه مراجع"
    var selectedFilter by remember(currentLang) { mutableStateOf(defaultFilter) }
    var selectedLocationID by remember { mutableStateOf<String?>(null) }
    
    // Discussion forum modal state
    var showForumModal by remember { mutableStateOf(false) }
    var forumMessage by remember { mutableStateOf("") }
    val forumPosts = remember(currentLang) { 
        mutableStateListOf(
            ForumItem(
                if (currentLang == "en") "Ali Mohammadi" else "علی محمدی", 
                if (currentLang == "en") "Has hamster salmonellosis been spreading recently?" else "سالمونل آکواریومی همستر در شیراز شایع شده؟", 
                if (currentLang == "en") "10 min ago" else "۱۰ دقیقه پیش"
            ),
            ForumItem(
                if (currentLang == "en") "Dr. Maryam Saeedi" else "دکتر مریم سعیدی", 
                if (currentLang == "en") "No, there are no official reports from the Veterinary Organization as of yet." else "خیر، هنوز گزارش رسمی نظام دامپزشکی وجود ندارد.", 
                if (currentLang == "en") "5 min ago" else "۵ دقیقه پیش"
            )
        )
    }

    // Coordinates and details list
    val spatialPOIs = listOf(
        MapPOI("1", "اداره کل دامپزشکی تهران", "Tehran Veterinary Headquarters", "اداری", "Official", 0.35f, 0.25f, "تهران، بزرگراه آیت‌الله کاشانی", "Tehran, Kashani Hwy", "۰۲۱-۴۴۰۰۰۰۰۰"),
        MapPOI("2", "کلینیک مرکزی البرز", "Alborz Central Veterinary Clinic", "کلینیک", "Clinic", 0.62f, 0.45f, "کرج، خیابان بهار، نبش رمضانی", "Karaj, Bahar St, corner of Ramezani", "۰۲۶-۳۲۰۰۰۰۰۰"),
        MapPOI("3", "داروخانه فیاض (توزیع واکسن)", "Fayyaz Veterinary Pharmacy (Vaccine Distrib.)", "داروخانه", "Pharmacy", 0.48f, 0.65f, "تهران، میدان توحید، کوچه لادن", "Tehran, Tohid Sq, Laden Alley", "۰۲۱-۶۶۰۰۰۰۰۰"),
        MapPOI("4", "کلینیک اورژانس پارس", "Pars Veterinary Emergency Clinic", "کلینیک", "Clinic", 0.22f, 0.72f, "تهران، بزرگراه صدر، ورودی کامرانیه", "Tehran, Sadr Hwy, Kamranieh exit", "۰۲۱-۲۲۰۰۰۰۰۰")
    )

    val activePOI = spatialPOIs.find { it.id == selectedLocationID }

    val layoutDirection = if (currentLang == "en") androidx.compose.ui.unit.LayoutDirection.Ltr else androidx.compose.ui.unit.LayoutDirection.Rtl

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
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
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = if (currentLang == "en") "📍 Veterinary Services RadioCentral" else "📍 رادیوسنترال خدمات دامپزشکی",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (currentLang == "en") "Find official administrative veterinary headquarters, small animal specialized departments, and partner pharmacies selling state vaccines on this coordinate matrix." else "مراکز اداری، کلینیک‌های حیوانات کوچک، بیمارستان‌های دام بزرگ و داروخانه‌های توزیع‌کننده واکسن دولتی را بر روی نقشه پیدا کنید.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
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
                    Text(
                        text = if (currentLang == "en") "Live Chat Room with Shift Doctor (Online Telemedicine)" else "اتاق گفتگو با دامپزشک آنلاین (تله‌مدیسین دمو)", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold
                    )
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
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
                        Text(
                            text = if (currentLang == "en") "🗣️ Active Advisory Chat Board" else "🗣️ تالار گفتمان و مشاوره آنلاین", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 14.sp
                        )
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
                                    textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right,
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = forumMessage,
                            onValueChange = { forumMessage = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (currentLang == "en") "New message for shift doctor..." else "پیام جدید شما برای دکتر شیفت...") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (forumMessage.isNotEmpty()) {
                                        forumPosts.add(ForumItem(if (currentLang == "en") "You (Demo User)" else "شما (کاربر دمو)", forumMessage, if (currentLang == "en") "Now" else "اکنون"))
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

            Spacer(modifier = Modifier.height(16.dp))

            // ------------------ START OF GOOGLE MAPS LIVE SEARCH ------------------
            val context = LocalContext.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = if (currentLang == "en") "🌐 Google Maps Live Active Proximity Search" else "🌐 جستجوی زنده سراسری روی گوگل مپ واقعی",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (currentLang == "en") "Click a shortcut query below to view real-time live map results in your current geographic location instantly inside the Google Maps app:" else "با کلیک روی دکمه‌های زیر، نتایج زنده و واقعی اطراف خود را به صورت مستقیم در گوگل مپ باز کنید:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val searchItems = if (currentLang == "en") {
                            listOf(
                                Triple("🏥 Vet Hospital", "veterinary hospital", MaterialTheme.colorScheme.errorContainer),
                                Triple("🩺 Vet Clinic", "veterinary clinic", MaterialTheme.colorScheme.primaryContainer),
                                Triple("💊 Vet Pharmacy", "veterinary pharmacy", MaterialTheme.colorScheme.tertiaryContainer)
                            )
                        } else {
                            listOf(
                                Triple("🏥 بیمارستان دامپزشکی", "veterinary hospital", MaterialTheme.colorScheme.errorContainer),
                                Triple("🩺 کلینیک دامپزشکی", "veterinary clinic", MaterialTheme.colorScheme.primaryContainer),
                                Triple("💊 داروخانه دامپزشکی", "veterinary pharmacy", MaterialTheme.colorScheme.tertiaryContainer)
                            )
                        }

                        searchItems.forEach { (label, query, bgColor) ->
                            Button(
                                onClick = {
                                    try {
                                        val mapUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                                        val intent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
                                            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                                            context.startActivity(webIntent)
                                        } catch (ex: Exception) {
                                            // Ignore
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            // ------------------ END OF GOOGLE MAPS LIVE SEARCH ------------------

            // Spatial Vector Map Canvas Simulation
            Text(
                text = if (currentLang == "en") "Visual Coordinate Alignment (Double-tap or re-click active marker tag to route on external Google Maps):" else "نقشه بصری و برداری (دوبار ضربه یا کلیک مجدد بر روی تگ فعال برای مسیریابی با گوگل مپ):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
            )

            // Map filters layout
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = if (currentLang == "en") {
                    listOf("All Venues", "Official", "Clinic", "Pharmacy")
                } else {
                    listOf("همه مراجع", "اداری", "کلینیک", "داروخانه")
                }

                filters.forEach { flt ->
                    val actualDbType = when (flt) {
                        "All Venues" -> "همه مراجع"
                        "Official" -> "اداری"
                        "Clinic" -> "کلینیک"
                        "Pharmacy" -> "داروخانه"
                        else -> flt
                    }
                    val isSel = selectedFilter == actualDbType || (selectedFilter == "All Venues" && actualDbType == "همه مراجع") || (selectedFilter == "Official" && actualDbType == "اداری") || (selectedFilter == "Clinic" && actualDbType == "کلینیک") || (selectedFilter == "Pharmacy" && actualDbType == "داروخانه")
                    
                    val isActiveFilter = (flt == "All Venues" && selectedFilter == "همه مراجع") || (flt == "Official" && selectedFilter == "اداری") || (flt == "Clinic" && selectedFilter == "کلینیک") || (flt == "Pharmacy" && selectedFilter == "داروخانه") || (selectedFilter == flt)

                    val bg = if (isActiveFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val tc = if (isActiveFilter) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable { 
                                selectedFilter = actualDbType
                            }
                            .defaultMinSize(minHeight = 48.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
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
                                detectTapGestures(
                                    onTap = { offset ->
                                        // Detect which POI selected on Tap
                                        spatialPOIs.forEach { poi ->
                                            val mappedPoiFilter = if (currentLang == "en") poi.enCategory else poi.category
                                            val currentFltStandardized = when (selectedFilter) {
                                                "All Venues", "همه مراجع" -> "همه مراجع"
                                                "Official", "اداری" -> "اداری"
                                                "Clinic", "کلینیک" -> "کلینیک"
                                                "Pharmacy", "داروخانه" -> "داروخانه"
                                                else -> selectedFilter
                                            }
                                            if (currentFltStandardized == "همه مراجع" || poi.category == currentFltStandardized) {
                                                val poiX = poi.x * size.width
                                                val poiY = poi.y * size.height
                                                val dist = kotlin.math.hypot(offset.x - poiX, offset.y - poiY)
                                                if (dist < 40f) {
                                                    selectedLocationID = poi.id
                                                }
                                            }
                                        }
                                    },
                                    onDoubleTap = { offset ->
                                        // Launch Google Map for coordinates on double tap
                                        spatialPOIs.forEach { poi ->
                                            val currentFltStandardized = when (selectedFilter) {
                                                "All Venues", "همه مراجع" -> "همه مراجع"
                                                "Official", "اداری" -> "اداری"
                                                "Clinic", "کلینیک" -> "کلینیک"
                                                "Pharmacy", "داروخانه" -> "داروخانه"
                                                else -> selectedFilter
                                            }
                                            if (currentFltStandardized == "همه مراجع" || poi.category == currentFltStandardized) {
                                                val poiX = poi.x * size.width
                                                val poiY = poi.y * size.height
                                                val dist = kotlin.math.hypot(offset.x - poiX, offset.y - poiY)
                                                if (dist < 40f) {
                                                    selectedLocationID = poi.id
                                                    try {
                                                        val mapUri = Uri.parse("geo:0,0?q=${Uri.encode("${poi.name} ${poi.address}")}")
                                                        val intent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                                            setPackage("com.google.android.apps.maps")
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        try {
                                                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode("${poi.name} ${poi.address}")}")
                                                            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                                                            context.startActivity(webIntent)
                                                        } catch (ex: Exception) {
                                                            // Ignore
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
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
                            val currentFltStandardized = when (selectedFilter) {
                                "All Venues", "همه مراجع" -> "همه مراجع"
                                "Official", "اداری" -> "اداری"
                                "Clinic", "کلینیک" -> "کلینیک"
                                "Pharmacy", "داروخانه" -> "داروخانه"
                                else -> selectedFilter
                            }
                            if (currentFltStandardized == "همه مراجع" || poi.category == currentFltStandardized) {
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
                        val currentFltStandardized = when (selectedFilter) {
                            "All Venues", "همه مراجع" -> "همه مراجع"
                            "Official", "اداری" -> "اداری"
                            "Clinic", "کلینیک" -> "کلینیک"
                            "Pharmacy", "داروخانه" -> "داروخانه"
                            else -> selectedFilter
                        }
                        if (currentFltStandardized == "همه مراجع" || poi.category == currentFltStandardized) {
                            val isThisSelected = selectedLocationID == poi.id
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = (poi.x * canvasW.value).dp - 10.dp,
                                        y = (poi.y * canvasH.value).dp - 25.dp
                                    )
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isThisSelected) Color.Red else MaterialTheme.colorScheme.secondary)
                                    .clickable {
                                        if (isThisSelected) {
                                            // Tap twice or click already selected tag to launch Real Google Maps
                                            try {
                                                val mapUri = Uri.parse("geo:0,0?q=${Uri.encode("${poi.name} ${poi.address}")}")
                                                val intent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                                    setPackage("com.google.android.apps.maps")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                try {
                                                    val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode("${poi.name} ${poi.address}")}")
                                                    val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                                                    context.startActivity(webIntent)
                                                } catch (ex: Exception) {
                                                    // Ignore
                                                }
                                            }
                                        } else {
                                            selectedLocationID = poi.id
                                        }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val catLabel = if (currentLang == "en") poi.enCategory else poi.category
                                    Text(catLabel, fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    if (isThisSelected) {
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("🧭", fontSize = 7.sp, color = Color.White)
                                    }
                                }
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
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = if (currentLang == "en") "🏢 Selected Center Specifications:" else "🏢 مشخصات مرکز انتخاب شده:", 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val displayTitle = if (currentLang == "en") poi.enName else poi.name
                        val displayAddress = if (currentLang == "en") poi.enAddress else poi.address
                        val displayCategory = if (currentLang == "en") poi.enCategory else poi.category

                        Text(
                            text = if (currentLang == "en") "Clinic Title: $displayTitle" else "عنوان مرکز: $displayTitle", 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLang == "en") "Category: $displayCategory" else "دسته: $displayCategory", 
                            fontSize = 11.sp, 
                            color = Color.Gray
                        )
                        Text(
                            text = if (currentLang == "en") "Address: $displayAddress" else "نشانی: $displayAddress", 
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (currentLang == "en") "Landline: ${poi.phone}" else "تلفن ثابت: ${poi.phone}", 
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${poi.phone}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }) {
                                Text(if (currentLang == "en") "📞 Call Center" else "📞 تماس تلفنی با مرکز", fontSize = 11.sp)
                            }
                            Button(onClick = {
                                try {
                                    val mapUri = Uri.parse("geo:0,0?q=${Uri.encode("${displayTitle} ${displayAddress}")}")
                                    val intent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode("${displayTitle} ${displayAddress}")}")
                                        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                                        context.startActivity(webIntent)
                                    } catch (ex: Exception) {
                                        // Ignore
                                    }
                                }
                            }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🧭")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (currentLang == "en") "Intelligent Route" else "مسیریابی هوشمند", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Text(
                    text = if (currentLang == "en") "Tap on any of the blue/red visual pins to inspect detailed specifications, telephone lines, and addresses." else "جهت مشاهده آدرس جزئی، روی یکی از نقاط قرمز یا آبی نقشه ضربه بزنید.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

data class MapPOI(
    val id: String,
    val name: String,
    val enName: String,
    val category: String,
    val enCategory: String,
    val x: Float,
    val y: Float,
    val address: String,
    val enAddress: String,
    val phone: String
)

data class ForumItem(
    val user: String,
    val content: String,
    val time: String
)
