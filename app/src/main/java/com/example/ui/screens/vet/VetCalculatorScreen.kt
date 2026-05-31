package com.example.ui.screens.vet

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VetCalculatorScreen(viewModel: MainViewModel) {
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()

    var activeCalculator by remember { mutableStateOf("مایع‌درمانی") }

    // Forms States
    var weightInput by remember { mutableStateOf("") }

    // Init fields from active pet if available
    LaunchedEffect(activeExaminedPet) {
        activeExaminedPet?.let { pet ->
            weightInput = pet.weight.toString()
        }
    }

    val calculatorsList = listOf(
        "مایع‌درمانی", "انتقال خون", "محاسبه کالری غذا", "زمان زایمان", "سن معادل انسان", "تریاژ تروما", "کاهش میزان درد"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Clinical calculators titles
        Text(
            text = "🧮 ابزارهای سنجش و محاسبه‌گرهای کلینیکال:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Right
        )

        // Horizontal selections
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            calculatorsList.forEach { cal ->
                val isSel = activeCalculator == cal
                val bgCol = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val textCol = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgCol)
                        .clickable { activeCalculator = cal }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(cal, fontSize = 11.sp, color = textCol, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weight helper info
        Text(
            text = "وزن مبنا جهت اجرای فرمول‌ها: ${weightInput.ifEmpty { "1.0" }} کیلوگرم",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            // Interactive Display
            when (activeCalculator) {
                "مایع‌درمانی" -> {
                    FluidTherapyCalculator(initWeight = weightInput)
                }
                "انتقال خون" -> {
                    BloodTransfusionCalculator(initWeight = weightInput)
                }
                "محاسبه کالری غذا" -> {
                    CalorieCalculatorView(initWeight = weightInput)
                }
                "زمان زایمان" -> {
                    GestationCalculatorView()
                }
                "سن معادل انسان" -> {
                    HumanAgeCalculatorView(initWeight = weightInput)
                }
                "تریاژ تروما" -> {
                    TraumaTriageView()
                }
                "کاهش میزان درد" -> {
                    PainScoreView()
                }
            }
        }
    }
}

// 1. Fluid Therapy Calculator implementation
@Composable
fun FluidTherapyCalculator(initWeight: String) {
    var weightStr by remember(initWeight) { mutableStateOf(initWeight) }
    var dehydrationPct by remember { mutableStateOf("5") } // Default 5% dehydration
    var ongoingLosses by remember { mutableStateOf("100") } // ml/day

    val weight = weightStr.toDoubleOrNull() ?: 5.0
    val dehydration = dehydrationPct.toDoubleOrNull() ?: 5.0
    val losses = ongoingLosses.toDoubleOrNull() ?: 100.0

    // Calculations: Deficit = weight * dehydration% * 10, Maintenance = weight * 50
    val deficit = weight * dehydration * 10
    val maintenance = weight * 50
    val total24h = deficit + maintenance + losses
    val dripRate15 = (total24h * 15) / (24 * 60) // 15 drops/ml IV sets
    val dripRate20 = (total24h * 20) / (24 * 60) // 20 drops/ml IV sets

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("💧 محاسبه مایع‌درمانی و قطرات سرم ریپ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it },
                label = { Text("وزن حیوان (کیلوگرم)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dehydrationPct,
                onValueChange = { dehydrationPct = it },
                label = { Text("درصد دهیدراتاسیون یا کم‌آبی (٪)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = ongoingLosses,
                onValueChange = { ongoingLosses = it },
                label = { Text("تخمین مایعات تلف‌شده مداوم (ml/day)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("نتایج محاسبات مایع‌درمانی ۲۴ ساعته:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text("کمبود مایعات دهیدراتاسیون (Deficit): ${String.format("%.1f", deficit)} میلی‌لیتر", fontSize = 12.sp)
            Text("مایع نگهداری پایه (Maintenance): ${String.format("%.1f", maintenance)} میلی‌لیتر در روز", fontSize = 12.sp)
            Text("حجم کل مایعات ۲۴ ساعت حاد: ${String.format("%.1f", total24h)} میلی‌لیتر در ۲۴ ساعت", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(10.dp))
            Text("نرخ جریان قطرات سرم (ست استاندارد ۲۰): ${String.format("%.1f", dripRate20)} قطره در دقیقه", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Text("نرخ جریان با ست خون (ست استاندارد ۱۵): ${String.format("%.1f", dripRate15)} قطره در دقیقه", fontSize = 12.sp)
        }
    }
}

// 2. Blood Transfusion Calculator implementation
@Composable
fun BloodTransfusionCalculator(initWeight: String) {
    var weightStr by remember(initWeight) { mutableStateOf(initWeight) }
    var currentPcv by remember { mutableStateOf("15") } // Patient's current Hematocrit %
    var targetPcv by remember { mutableStateOf("25") } // Target Hematocrit %
    var donorPcv by remember { mutableStateOf("40") } // Donor's standard Hematocrit %

    val weight = weightStr.toDoubleOrNull() ?: 5.0
    val recipientPcv = currentPcv.toDoubleOrNull() ?: 15.0
    val target = targetPcv.toDoubleOrNull() ?: 25.0
    val donor = donorPcv.toDoubleOrNull() ?: 40.0

    // Volume of whole blood required = Weight * 80 * (Target - Recipient) / Donor (for dog)
    val volumeDog = weight * 80 * (target - recipientPcv) / donor
    val volumeCat = weight * 60 * (target - recipientPcv) / donor

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🩸 دستگاه تخمین حجم خون انتقالی (Transfusion)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currentPcv,
                onValueChange = { currentPcv = it },
                label = { Text("حجم گلبولی بیمار (PCV ٪)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = targetPcv,
                onValueChange = { targetPcv = it },
                label = { Text("رنج هماتوکریت ایده‌آل قرنیه هدف (٪)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = donorPcv,
                onValueChange = { donorPcv = it },
                label = { Text("هماتوکریت گداخته خون اهداکننده (٪)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("حجم نهایی کل خون انتقالی (Whole Blood):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text("حجم الزامی برای سگ‌سانان (ضریب ۸۰): ${String.format("%.1f", volumeDog)} میلی‌لیتر کل", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("حجم الزامی برای گربه‌سانان (ضریب ۶۰): ${String.format("%.1f", volumeCat)} میلی‌لیتر کل", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("احتیاط: حداکثر نرخ انفوزیون نباید از ۱۰-۱۵ میلی‌لیتر بر کیلوگرم بر ساعت فراتر رود.", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

// 3. Calorie Food limits
@Composable
fun CalorieCalculatorView(initWeight: String) {
    var weightStr by remember(initWeight) { mutableStateOf(initWeight) }
    var selectedFactor by remember { mutableStateOf(1.6) } // Active multiplier

    val weight = weightStr.toDoubleOrNull() ?: 5.0
    // RER = 70 * (weight)^0.75
    val rer = 70 * Math.pow(weight, 0.75)
    val mer = rer * selectedFactor

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🍗 تخمین انرژی نگهداری و کالری روزانه (MER/RER)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it },
                label = { Text("وزن جهت برآورد خوراک") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("وضعیت زیستی و ضریب انرژی حیوان خانگی:", fontSize = 11.sp, color = Color.Gray)

            listOf(
                "بالغ عقیم‌ شده (۱.۶)" to 1.6,
                "بالغ عقیم‌ نشده (۱.۸)" to 1.8,
                "توله یا جوان در حال رشد (۳.۰)" to 3.0,
                "مسن یا مایل به کاهش وزن (۱.۲)" to 1.2
            ).forEach { (label, coeff) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedFactor = coeff }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedFactor == coeff, onClick = { selectedFactor = coeff })
                    Text(label, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("نیازهای حرارتی استراحت و سوخت‌وساز:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text("متابولیسم استراحت‌پایه (RER): ${String.format("%.1f", rer)} کیلوکالری در روز", fontSize = 12.sp)
            Text("متابولیسم نگهداری نهایی (MER): ${String.format("%.1f", mer)} کیلوکالری در روز", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// 4. Mating & Gestation Clock
@Composable
fun GestationCalculatorView() {
    var daysSinceMating by remember { mutableStateOf("10") }
    val days = daysSinceMating.toIntOrNull() ?: 10
    val totalGestation = 63 // average gestation for dogs/cats
    val remaining = totalGestation - days

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📅 برآورد زمان زایمان و تقویم بارداری (Gestation)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = daysSinceMating,
                onValueChange = { daysSinceMating = it },
                label = { Text("روزهای سپری شده از جفت‌گیری") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("وضعیت تقویمی جنین و آمادگی زایمان:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text("طول بارداری متعارف سگ/گربه: ۶۳ روز", fontSize = 12.sp)
            Text("زمان مانده تا زایمان: $remaining روز کل", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

            // Diagnostic indicators
            val stageDesc = when {
                days < 21 -> "مرحله لقاح و جایگزینی رویان در دیواره رحم (رژیم غذایی معمولی)"
                days < 45 -> "مرحله استخوان‌سازی جنین و ضربان قلب (ضرورت افزایش دریافت پروتئین)"
                else -> "تکمیل نهایی جنین و آمادگی زایمان (تامین جعبه زایمان گرم و مراقبت دمای مداوم)"
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("راهنمای گام کنونی: $stageDesc", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

// 5. Human equivalent age in years
@Composable
fun HumanAgeCalculatorView(initWeight: String) {
    var ageStr by remember { mutableStateOf("2") }
    var selectedAnimal by remember { mutableStateOf("dog") }

    val age = ageStr.toIntOrNull() ?: 2

    val humanAge = if (selectedAnimal == "cat") {
        if (age == 1) 15 else if (age == 2) 24 else 24 + ((age - 2) * 4)
    } else {
        // Dog average based on weight
        if (age == 1) 15 else if (age == 2) 24 else 24 + ((age - 2) * 5)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🐹 تخمین سن معادل انسان (Human Age Checker)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedAnimal == "dog") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedAnimal = "dog" }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("سگ‌سانان", color = if (selectedAnimal == "dog") Color.White else Color.Black, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedAnimal == "cat") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedAnimal = "cat" }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("گربه‌سانان", color = if (selectedAnimal == "cat") Color.White else Color.Black, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = ageStr,
                onValueChange = { ageStr = it },
                label = { Text("سن تقویمی پت (سالیانه)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("نتایج معادل‌سازی سنی:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text("سن فیزیکی پت: $age سال کامل", fontSize = 12.sp)
            Text("سن معادل در انسان: حدود $humanAge ساله!", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// 6. Trauma Triage system
@Composable
fun TraumaTriageView() {
    var scoreMotor by remember { mutableStateOf(6) } // MGCS motor score: 1 to 6
    var scoreBrainstem by remember { mutableStateOf(6) } // MGCS brainstem score: 1 to 6
    var scoreConscience by remember { mutableStateOf(6) } // MGCS conscience: 1 to 6

    val totalMgcs = scoreMotor + scoreBrainstem + scoreConscience

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🧠 غربالگری مغزی MGCS (Modified Glassgow Score)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("غربالگری سطح هوشیاری پس از تصادف و تروما:", fontSize = 10.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            // Score lists selectors
            Text("۱. مهارت‌های ارادی حرکتی (موتور):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1, 3, 5, 6).forEach { score ->
                    val isSel = scoreMotor == score
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { scoreMotor = score }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("امتیاز $score", fontSize = 10.sp, color = if (isSel) Color.White else Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("۲. تست هم‌گرایی مردمک و رفلکس مغز غضروفی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1, 3, 5, 6).forEach { score ->
                    val isSel = scoreBrainstem == score
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { scoreBrainstem = score }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("امتیاز $score", fontSize = 10.sp, color = if (isSel) Color.White else Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val triageRecommendation = when {
                totalMgcs <= 8 -> "⚠️ وخیم (بحران قرمز) - احتمال کما و خون‌ریزی جمجمه، نیاز اضطراری به مانیتور تنفس دایم و مسکن‌های کاهنده فشار درون کله."
                totalMgcs <= 14 -> "⚠️ پایدار فرعی (زرد) - ضرورت مانیتورینگ متمرکز ۱۲ ساعته، احتمال ضربه کله متوسط."
                else -> "✅ کاملاً پایدار (سبز) - مانیتور علائم حیاتی و ترخیص موقتی با استراحت خانگی."
            }

            Text("امتیاز نهایی تروما MGCS: $totalMgcs از ۱۸", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("مراقبت تجویزی: $triageRecommendation", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        }
    }
}

// 7. CSU Pain estimation
@Composable
fun PainScoreView() {
    var vocalBy by remember { mutableStateOf(false) }
    var postureBy by remember { mutableStateOf(false) }
    var touchBy by remember { mutableStateOf(false) }

    val painScore = (if (vocalBy) 1 else 0) + (if (postureBy) 1 else 0) + (if (touchBy) 2 else 0)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("😿 سنجش درد بر اساس معیار جامعه درد کانادا (CSU)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = vocalBy, onCheckedChange = { vocalBy = it })
                Text("نالیدن حین جابجایی یا میاو/پارس‌های دردناک", fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = postureBy, onCheckedChange = { postureBy = it })
                Text("کمر قوز کرده متمایل به خواب شکم روی زمین (Prayer position)", fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = touchBy, onCheckedChange = { touchBy = it })
                Text("فرار یا تلاش به گاز گرفتگی حین لمس موضعی زخم (امتیاز ۲)", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val painRating = when (painScore) {
                0 -> "فاقد درد (بدون بیقراری)"
                1 -> "درد خفیف (سفارش ملوکسیکام موضعی)"
                2 -> "درد متوسط (سفارش مخدرهای خوراکی ترامادول تضعیف شده)"
                else -> "درد شدید ⚠️ (احتمال شوک قلبی، سفارش دگزامتازون به پیوست پتدین مسکن فوری)"
            }

            Text("شاخص تخمینی شدت درد: $painScore از ۴", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("توصیه دندان‌پزشکی تگ: $painRating", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
