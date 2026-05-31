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
fun VetDiagnosisTreatmentScreen(viewModel: MainViewModel) {
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()
    val activeSpecies by viewModel.selectedSpecies.collectAsState()

    var activeSubTab by remember { mutableStateOf("تشخیص") } // "تشخیص" or "درمان"

    // Diagnosis section state variables (Inputs)
    var chiefComplaintCategory by remember { mutableStateOf("علائم عمومی") }
    var chiefComplaintText by remember { mutableStateOf("") }
    
    var physicalSignCategory by remember { mutableStateOf("علائم عمومی") }
    var physicalSignText by remember { mutableStateOf("") }

    // Lab results comparison simulator (WBC count, Creatinine)
    var labWbcInput by remember { mutableStateOf("") }
    var labCreatinineInput by remember { mutableStateOf("") }

    val currentComplaints by viewModel.physicalComplaints.collectAsState()
    val currentSigns by viewModel.physicalSigns.collectAsState()

    // Diseases based on Species Chosen
    val diseaseCatalog = when (activeSpecies) {
        "dog" -> listOf(
            DiseaseItem(
                name = "پاروویروس سگ‌سانان (CPV)",
                symptoms = "اسهال خونی جهنده و بسیار بدبو، استفراغ شدید، بی‌اشتهایی کامل، تب بالا، دهیدراتاسیون حاد.",
                diffDiagnosis = "کوروناویروس، ژیاردیازیس، مسمومیت غذایی، انسدادهای مکانیکی گوارشی.",
                protocol = "مایع‌درمانی تهاجمی رینگرلاکتات، آنتی‌بیوتیک محافظت ثانویه (آمپی‌سیلین/ماربوفلوکساسین)، داروهای ضد استفراغ (ماروپیتانت)، تغذیه تزریقی."
            ),
            DiseaseItem(
                name = "دیستمپر سگ (CDV)",
                symptoms = "ترشحات چرکی دوطرفه چشم و بینی، پد هیپرکراتوز پنجه پا، علائم عصبی تشنج و تیک چانه‌ای.",
                diffDiagnosis = "هاری، هپاتیت عفونی سگ‌سانان، مننژیت قارچی.",
                protocol = "حمایتی ضدتشنج (فنوپاربیتال)، رطوبت‌بخشی راه‌های هوایی، قطره‌های چشمی چرک‌زدا، مولتی ویتامین B کمپلکس، ایزولاسیون شدید."
            ),
            DiseaseItem(
                name = "سرفه کنل (سیاه‌سرفه سگ)",
                symptoms = "سرفه خشک و بوقی شکل تشدید شونده پس از فعالیت یا لمس نای، استفراغ کفی به دنبال سرفه.",
                diffDiagnosis = "کلاپس نای، نارسایی قلبی و ادم ریه، ورود جسم خارجی به نای.",
                protocol = "بخور آب گرم، داکسی‌سایکیلین ۱۰ میلی‌گرم بر کیلوگرم، شربت‌های ضد سرفه کدوئین، پرهیز از اعمال فشار قلاده گردنی."
            )
        )
        "cat" -> listOf(
            DiseaseItem(
                name = "کلسی‌ویروس گربه‌سانان (FCV)",
                symptoms = "زخم‌های دردناک روی سطح زبان و لثه، بی‌اشتهایی شدید به علت سوزش دهان، ترشحات بینی، تب مالامال.",
                diffDiagnosis = "هرپس ویروس گربه‌سانان، لنفوم دهانی، زخم‌های ناشی از اورمی کلیوی.",
                protocol = "ضددرد دهانی (ملوکسیکام)، کلیندامایسین هیدروکلراید جهت کاهش باکتری ثانویه، داروهای محرک اشتها، غذای نرم مرطوب گرم شده."
            ),
            DiseaseItem(
                name = "پنلوکوپنی گربه‌ها (Panleukopenia)",
                symptoms = "مشابه پاروو ویروس； گربه چانه بر روی ظرف آب گذاشته اما مایعات نمی‌نوشد، تب نوسانی حاد، کاهش شدید گلبول‌های سفید.",
                diffDiagnosis = "سالمونلوز شدید، پریتونیت عفونی گربه‌ها (FIP).",
                protocol = "انتقال پلاسما یا کلوییدهای تغذیه‌ای، کواموکسی کلاو، پنتوکسی فیلین، مایع‌درمانی وریدی دقیق، مراقبت‌های ویژه دمایی."
            ),
            DiseaseItem(
                name = "راینوتراکئیت ویروسی گربه (FHV-1)",
                symptoms = "زخم‌های قرنیه منشعب (دندریتیک)، عطسه‌های مکرر و تخلیه ترشحات غلیظ بینی، چشم درد و بلفارواسپاسم شدید.",
                diffDiagnosis = "کلامیدیاوفیلیس، مایکوپلاسما گربه‌سانان.",
                protocol = "آمینو اسید ال-لیزین ۵۰۰ میلی‌گرم، قطره‌های ضد ویروس چشم (پنوکسی کوریدین)، داروهای سدکننده مخاط بینی."
            )
        )
        else -> listOf(
            DiseaseItem(
                name = "بند آمدن تخم در پرندگان (Egg Binding)",
                symptoms = "نشستن پرنده در کف قفس با بال‌های باز شده، تنگی نفس شدید، کرنش‌های مشهود شکم، بزرگی خلفی شکمی.",
                diffDiagnosis = "تومورهای تخمدانی، چاقی مفرط، فتق شکمی.",
                protocol = "تامین دمای گرم و رطوبت بالا، تزریق کلسیم گلوکونات وریدی/عضلانی، مالش روغن معدنی استریل در کلوآک."
            ),
            DiseaseItem(
                name = "دم خیس در همسترها (Wet Tail)",
                symptoms = "اسهال آبکی بسیار شدید و مرطوب بودن کل ناحیه دم و مقعد همستر، رخوت مرگبار، پشت خمیده.",
                diffDiagnosis = "اسهال غذایی تفریحی ساده، انگل‌های تک یاخته ژیاردیا.",
                protocol = "مایع‌درمانی زیرپوستی گرم، داکسی‌سایکلین مناسب جوندگان، پروبیوتیک‌های بازساز گوارشی."
            ),
            DiseaseItem(
                name = "پوسیدگی باله در آبزیان (Fin Rot)",
                symptoms = "رنگ پریدگی، تکه‌تکه شدن و ساییدگی لبه باله‌ها و دم ماهی که با نوارهای قرمز یا سفید چرکی همراه‌اند.",
                diffDiagnosis = "سوختگی ناشی از آمونیاک بالا، انگل پوست و آبشش.",
                protocol = "سیفون ۵۰ درصد آب آکواریوم، مصرف حمام‌های دارویی سولفات مس یا آنتی‌بیوتیک‌های وسیع‌الطیف متیلن بلو."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Patient Summary at the Top
        activeExaminedPet?.let { pet ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🩺 پرونده معاینه فعال: ${pet.name}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "نژاد: ${pet.breed} | وزن بیمار: ${pet.weight} کیلوگرم | پرونده: ${pet.recordNumber}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } ?: run {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "⚠️ بیمار فعالی انتخاب نشده است",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "بخش تشخیص و درمان بر مبنای حیوان دمو کار می‌کند. برای دسته‌بندی بیماری‌ها جهت راهنمایی بالینی، ابتدا گونه حیوان را در تب داشبورد مشخص کنید.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Right
                    )
                }
            }
        }

        // Subtabs selection: "تشخیص" or "درمان"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { activeSubTab = "درمان" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "درمان") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("درمان و دستورالعمل‌ها", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { activeSubTab = "تشخیص" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "تشخیص") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔬", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تشخیص و آزمایشگاه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Context Display
        Crossfade(targetState = activeSubTab) { screen ->
            when (screen) {
                "تشخیص" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "۱. مشخصات بالینی و تاریخچه مراجعه:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Grid of Chief Complaints Categories
                                Text("دسته‌بندی‌های تاریخچه علائم:", fontSize = 11.sp, color = Color.Gray)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                    listOf(
                                        "علائم عمومی", "قلبی‌ریوی", "گوارشی", "عصبی",
                                        "ارتوپدیک", "یوروجنیتال", "چشمی", "پوستی"
                                    ).forEach { cat ->
                                        val isSel = chiefComplaintCategory == cat
                                        val col = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                        val tc = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(col)
                                                .clickable { chiefComplaintCategory = cat }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(cat, fontSize = 10.sp, color = tc, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                                    OutlinedTextField(
                                        value = chiefComplaintText,
                                        onValueChange = { chiefComplaintText = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("ثبت شرح حال و علائم فیزیکال در دسته ($chiefComplaintCategory)") },
                                        placeholder = { Text("مثال: از سه روز پیش سرفه‌های بوقی مکرر بدون تغییر رژیم غذایی شروع شده...") },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (chiefComplaintText.isNotEmpty()) {
                                            viewModel.physicalComplaints.value = viewModel.physicalComplaints.value + (chiefComplaintCategory to chiefComplaintText)
                                            chiefComplaintText = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("ذخیره در خلاصه علائم معاینه")
                                }
                            }
                        }

                        // Display saved diagnostic descriptors
                        if (currentComplaints.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                                    Text("خلاصه علائم عمومی ثبت شده:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    currentComplaints.forEach { (cat, desc) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(desc, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
                                            Text("[$cat]: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Biochemistry Comparison Table
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "۲. تحلیل آنلاین مقادیر گلبولی و آزمایشگاهی:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "مقادیر دریافتی با چارت رفرنس سگ/گربه مطابقت داده می‌شوند.",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = labWbcInput,
                                            onValueChange = { labWbcInput = it },
                                            modifier = Modifier.weight(1f),
                                            label = { Text("سنجش WBC (K/µL)") },
                                            placeholder = { Text("رنج: 6 - 17") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        OutlinedTextField(
                                            value = labCreatinineInput,
                                            onValueChange = { labCreatinineInput = it },
                                            modifier = Modifier.weight(1f),
                                            label = { Text("کراتینین (mg/dL)") },
                                            placeholder = { Text("رنج: 0.5 - 1.8") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Comparison results
                                val wbcVal = labWbcInput.toDoubleOrNull()
                                val crVal = labCreatinineInput.toDoubleOrNull()

                                if (wbcVal != null || crVal != null) {
                                    Text("تحلیل مقادیر با چارت مرجع کلینیکال:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    wbcVal?.let { w ->
                                        val status = when {
                                            w < 6.0 -> "Low ⬇️ (احتمال لکوپنی ناشی از عفونت حاد ویروسی یا فقر استخوان)"
                                            w > 17.0 -> "High ⬆️ (فرآیند التهابی یا عفونت لوکال باکتریایی)"
                                            else -> "Normal ✅ (حدود طبیعی گلبول سفید)"
                                        }
                                        Text("گلبول سفید (WBC) با مقدار $w: $status", fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                                    }

                                    crVal?.let { c ->
                                        val status = when {
                                            c < 0.5 -> "Low ⬇️ (کاهش توده عضلانی حیوان)"
                                            c > 1.8 -> "High ⬆️ (نارسایی حاد/مزمن کلیوی یا پره‌رنال آزوتمی دهیدراتاسیون)"
                                            else -> "Normal ✅ (عملکرد کلیوی متعارف)"
                                        }
                                        Text("کراتینین خون با مقدار $c: $status", fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                "درمان" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                val specFarsi = when(activeSpecies) {
                                    "dog" -> "سگ‌سانان"
                                    "cat" -> "گربه‌سانان"
                                    else -> "پرندگان و اگزوتیک‌پت"
                                }
                                Text(
                                    text = "📋 پروتکل‌های جامع درمان بیماری‌ها ($specFarsi):",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "مرجع دسترسی سریع برای دوزها و پیش‌آگاهی بالینی:",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                diseaseCatalog.forEach { disease ->
                                    var isExpanded by remember { mutableStateOf(false) }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clickable { isExpanded = !isExpanded }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = disease.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.secondary,
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("علائم بالینی: " + disease.symptoms, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), maxLines = if (isExpanded) Int.MAX_VALUE else 1)

                                            if (isExpanded) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "تشخیص افتراقی (Differential Diagnosis):",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Text(disease.diffDiagnosis, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)

                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "پروتکل درمانی تجویزی (Treatment Protocol):",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Text(
                                                    text = disease.protocol,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF15803D), // green description
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Right
                                                )
                                            } else {
                                                Text("جهت باز کردن جزئیات ضربه بزنید...", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class DiseaseItem(
    val name: String,
    val symptoms: String,
    val diffDiagnosis: String,
    val protocol: String
)
