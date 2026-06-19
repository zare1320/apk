package com.example.ui.screens.vet

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.staticGuidelinesCatalog
import com.example.data.database.TreatmentGuideline
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.BorderStroke
import com.example.R

// Simple data model for signs and their matching diagnoses
data class DiagnosticSign(
    val id: String,
    val nameEn: String,
    val nameFa: String,
    val category: String, // "general", "cardio", "gastro", "neuro"
    val diseases: List<String>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VetDiagnosisTreatmentScreen(viewModel: MainViewModel) {
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()
    val activeSpecies by viewModel.selectedSpecies.collectAsState()
    val customGuidelines by viewModel.allGuidelines.collectAsState()

    val specFarsi = when (activeSpecies) {
        "dog" -> "سگ‌سانان"
        "cat" -> "گربه‌سانان"
        else -> "پرندگان و اگزوتیک‌پت"
    }
    val speciesKey = when (activeSpecies) {
        "dog" -> "dog"
        "cat" -> "cat"
        else -> "exotic"
    }

    var activeSubTab by remember { mutableStateOf("تشخیص") } // "تشخیص" or "درمان"
    var currentSubScreen by remember { mutableStateOf("home") } // "home", "historical", "physical", "lab", "combine"
    var selectedCategoryDetail by remember { mutableStateOf<String?>(null) } // e.g. "general"
    var selectedHistoricalSign by remember { mutableStateOf<String?>(null) }
    var selectedPhysicalCategoryDetail by remember { mutableStateOf<String?>(null) }
    var selectedPhysicalSign by remember { mutableStateOf<String?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    var showProDialog by remember { mutableStateOf(false) }

    // Dialog for adding custom SQLite-backed guidelines
    var showAddGuidelineDialog by remember { mutableStateOf(false) }
    var newGuidelineName by remember { mutableStateOf("") }
    var newGuidelineSymptoms by remember { mutableStateOf("") }
    var newGuidelineDiffDiagnosis by remember { mutableStateOf("") }
    var newGuidelineProtocol by remember { mutableStateOf("") }

    // State of selected symptomatology
    val selectedSigns = remember { mutableStateListOf<String>() }

    // Laboratory Inputs State
    var labWbcInput by remember { mutableStateOf("") }
    var labCreatinineInput by remember { mutableStateOf("") }

    // Interactive signs knowledge base
    val signsList = remember {
        listOf(
            DiagnosticSign("fever", "Fever", "تب بالا", "general", listOf("کلسی‌ویروس گربه‌سانان (FCV)", "پنلوکوپنی گربه‌ها", "پاروویروس سگ‌سانان (CPV)", "دیستمپر سگ")),
            DiagnosticSign("lethargy", "Lethargy", "بی‌حالی شدید", "general", listOf("پاروویروس سگ‌سانان (CPV)", "پنلوکوپنی گربه‌ها", "دیستمپر سگ")),
            DiagnosticSign("anorexia", "Anorexia", "بی‌اشتهایی حاد", "general", listOf("کلسی‌ویروس گربه‌سانان (FCV)", "پاروویروس سگ‌سانان (CPV)")),
            DiagnosticSign("weight_loss", "Weight Loss", "کاهش وزن مفرط", "general", listOf("راینوتراکئیت ویروسی گربه", "دم خیس")),
            
            // Cardiorespiratory signs (7 items - Screenshot 1)
            DiagnosticSign("collapse", "Collapse / Syncope", "سنکوپ / کلاپس عروقی", "cardio", listOf("شوک و افت فشار شریانی", "بیماری‌های دریچه‌ای قلبی", "آریتمی شدید قلبی")),
            DiagnosticSign("cough_cats", "Cough in cats", "سرفه در گربه‌ها", "cardio", listOf("آسم گربه‌سانان (Feline Asthma)", "برونشیت مزمن ریوی", "عفونت‌های ویروسی تنفسی")),
            DiagnosticSign("cough_dogs", "Cough in dogs", "سرفه در سگ‌ها", "cardio", listOf("سرفه کنل (Kennel Cough)", "کلاپس نای (Tracheal Collapse)", "نارسایی احتقانی قلب (CHF)")),
            DiagnosticSign("epistaxis", "Epistaxis", "خون‌دماغ شدن (اپیستاکسی)", "cardio", listOf("سندرم‌های انعقادی خون", "مسمومیت با جونده‌کش ضد انعقاد", "انگل‌های خونی Ehrlichia")),
            DiagnosticSign("hemoptysis", "Hemoptysis", "خون‌ریزی ریوی", "cardio", listOf("پنومونی نکروزه شریانی", "آمبولی و ترومبوز ریه", "عفونت کرم قلب پیشرفته")),
            DiagnosticSign("reverse_sneezing", "Reverse sneezing", "عطسه معکوس", "cardio", listOf("سندرم راه هوایی براکی‌سفالیک", "تحریک ناشی از گرد و خاک محیطی")),
            DiagnosticSign("sneezing_nasal", "Sneezing / Nasal discharge", "عطسه و ترشحات بینی", "cardio", listOf("راینوتراکئیت ویروسی گربه", "کلسی‌ویروس گربه‌سانان", "آسپرژیلوزیس سینوسی")),
            
            // Gastrointestinal signs (19 items - Screenshot 2 & 3)
            DiagnosticSign("abdominal_distension", "Abdominal distension", "اتساع شکمی", "gastro", listOf("پیچش و اتساع معده (GDV)", "آسیت یا آب‌آوردگی شکم", "خونریزی داخلی ناشی از پارگی طحال")),
            DiagnosticSign("constipation_obstipation", "Constipation / Obstipation", "یبوست و انسداد مجرای کولون", "gastro", listOf("سندرم مگاکولون گربه‌ها", "بلع جسم خارجی انسدادی", "انسداد فتق پرینه در سگ نر")),
            DiagnosticSign("diarrhea", "Diarrhea", "اسهال عمومی", "gastro", listOf("تهوع و اسهال معمولی", "بروز انگل‌های گوارشی", "گاستروآنتریت خودمختار")),
            DiagnosticSign("diarrhea_acute", "Diarrhea (acute)", "اسهال حاد", "gastro", listOf("پاروویروس سگ‌سانان (CPV)", "پنلوکوپنی گربه (FPV)", "مسمومیت قارچی یا غذایی حاد")),
            DiagnosticSign("diarrhea_chronic", "Diarrhea (chronic)", "اسهال مزمن", "gastro", listOf("بیماری التهاب روده (IBD)", "سوءجذب روده باریک", "انگل ژیاردیا یا کریپتوسپوریدیوم")),
            DiagnosticSign("dysphagia", "Dysphagia", "سختی در بلع غذا", "gastro", listOf("اجسام خارجی حلقی مری", "استوماتیت لنفوسیتیک گربه‌ها", "آسیب عصب زبانی حلقی")),
            DiagnosticSign("fecal_incontinence", "Fecal incontinence", "بی‌اختیاری در دفع مدفوع", "gastro", listOf("بیماری تخریب دیسک بین مهره‌ای", "فیستول مقعدی در نژاد ژرمن", "فلجی اسفنکتر خارجی")),
            DiagnosticSign("flatulence", "Flatulence", "باد شکم و گوارش سخت", "gastro", listOf("تخمیر باکتریایی جیره پر کربوهیدرات", "رشد بیش از حد باکتری روده (SIBO)")),
            DiagnosticSign("gagging_retching", "Gagging / Retching", "عق زدن بیهوده و سخت", "gastro", listOf("انسداد مری با استخوان یا شئی", "کلاپس نای ضربانی")),
            DiagnosticSign("halitosis", "Halitosis", "بوی بد دهان", "gastro", listOf("پریودنتیت حاد و پوسیدگی ریشه دندان", "اورمی ناشی از نارسایی کلیوی")),
            DiagnosticSign("hematemesis", "Hematemesis", "استفراغ خونی", "gastro", listOf("زخم معده عمیق ناشی از NSAID", "مسمومیت با حلال‌های اسیدی")),
            DiagnosticSign("hematochezia", "Hematochezia", "دفع خون روشن در مدفوع", "gastro", listOf("کولیت کولون نزولی", "آلودگی شدید به کرم شلاقی")),
            DiagnosticSign("melena", "Melena", "مدفوع تیره و هضم شده", "gastro", listOf("خونریزی دوازدهه یا معده بیمار", "مصرف کورتون همراه مسکن ممنوعه")),
            DiagnosticSign("ptyalism", "Ptyalism / Pseudoptyalism", "بزاق جاری و آب دهان زیاد", "gastro", listOf("مسمومیت با سموم ارگانوفسفره", "تهوع شدید مسافرتی", "سوختگی شیمیایی لثه")),
            DiagnosticSign("regurgitation", "Regurgitation", "رگورژیتاسیون (برگشت غیر فعال)", "gastro", listOf("مگاازوفاگوس مادرزادی یا اکتسابی", "التهاب ازوفاژیت مری")),
            DiagnosticSign("tenesmus_dyschezia", "Tenesmus / Dyschezia", "تلاش دردناک بی‌ثمر جهت دفع", "gastro", listOf("بزرگ‌شدگی پروستات سگ (BPH)", "فتق لترال پرینه", "آبسه غدد مقعدی")),
            DiagnosticSign("vomiting", "Vomiting", "استفراغ عمومی", "gastro", listOf("پانکراتیت حاد", "تورم معده خودمختار", "نارسایی ترشحی کبد")),
            DiagnosticSign("vomiting_acute", "Vomiting (acute)", "استفراغ حاد", "gastro", listOf("انسداد روده با اسباب بازی یا جوراب", "پاروویروس گوارشی")),
            DiagnosticSign("vomiting_chronic", "Vomiting (chronic)", "استفراغ مزمن", "gastro", listOf("نارسایی ثانویه کلیه بیمار", "هایپرپلازی سست پیلور معده")),
            
            DiagnosticSign("deafness", "Deafness", "ناشنوایی حاد یا مزمن بیمار", "neuro", listOf("عفونت شدید مجرای گوش داخلی", "مسمومیت دارویی با جنتامایسین")),
            DiagnosticSign("head_pressing", "Head pressing", "فشار دادن سر به سطوح (هدپرسینگ)", "neuro", listOf("انسفالوپاتی کبدی پیشرفته", "تومور یا التهاب مغزی مننژیت")),
            DiagnosticSign("myoclonus", "Myoclonus", "میوکلونوس عمیق عضلاتی", "neuro", listOf("ویروس دیستمپر حاد سگ‌سانان (CDV)", "پاسخ تحریک رفلکسی نخاع")),
            DiagnosticSign("myotonia", "Myotonia", "سفتی پایدار عضلانی (میوتونی)", "neuro", listOf("اختلال کانال کلسیم موروثی اورگانیک", "انقباض مداوم عصب محرک")),
            DiagnosticSign("nystagmus", "Nystagmus", "انحراف و لرزش مردمک چشم (نیستاگموس)", "neuro", listOf("سندرم وستیبولار گرانشی", "سایتوتوکسیسیتی مترونیدازول")),
            DiagnosticSign("seizures", "Seizures", "تشنج شدید و صرع", "neuro", listOf("صرع ایدیوپاتیک و ارگانیک گربه و سگ", "هیپوگلیسمی بحرانی خون")),
            DiagnosticSign("stupor_coma", "Stupor / Coma", "بهت‌زدگی عمیق یا کما", "neuro", listOf("تروما شدید و ضربه ضرباتی مغزی", "شوک بحرانی پایپلاین عروقی")),
            DiagnosticSign("tremors", "Tremors", "لرزش عمومی یا موضعی شیورینگ", "neuro", listOf("مسمومیت ارگانوفسفره کشاورزی", "کاهش بحرانی کلسیم سرم (اکلامپسی)")),

            DiagnosticSign("lameness", "Lameness / Limping", "لنگش و دشواری حرکت", "ortho", listOf("شکستگی استخوان", "آرتریت مزمن")),
            DiagnosticSign("bone_pain", "Joint / Bone Pain", "درد شدید مفصل و استخوان", "ortho", listOf("آرتریت مزمن")),

            DiagnosticSign("dysuria", "Dysuria / Straining", "سختی در دفع ادرار", "uro", listOf("سنگ مثانه", "عفونت مجاری ادراری (UTI)")),
            DiagnosticSign("hematuria", "Hematuria / Blood in urine", "خون در ادرار بیمار", "uro", listOf("سنگ مثانه", "عفونت مجاری ادراری (UTI)")),

            DiagnosticSign("epiphora", "Epiphora / Eye Discharge", "ترشحات چشمی مداوم", "ophthalm", listOf("کلسی‌ویروس گربه‌سانان (FCV)", "راینوتراکئیت ویروسی گربه")),
            DiagnosticSign("conjunctivitis", "Conjunctivitis / Red eyes", "قرمزی و التهاب ملتحمه چشم", "ophthalm", listOf("راینوتراکئیت ویروسی گربه")),

            DiagnosticSign("alopecia", "Alopecia / Hair Loss", "ریزش موی موضعی یا عمومی", "derm", listOf("جرب دمودکس", "قارچ پوستی (درماتوفیتوز)")),
            DiagnosticSign("pruritus", "Pruritus / Severe Itching", "خارش و التهاب پوستی شدید", "derm", listOf("جرب شوره سر", "حساسیت پوستی کک"))
        )
    }

    // Colors adaptation for both dark & light modes
    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val bgColor = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFFAFBFB)
    val cardBgColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else Color.White
    val strokeColor = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)
    val primaryText = if (isDark) Color.White else Color(0xFF1E293B)
    val secondaryText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val accentTeal = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F766E)
    val accentTealContainer = if (isDark) Color(0xFF134E4A) else Color(0xFFF0FDFA)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Patient Summary Banner at top
            activeExaminedPet?.let { pet ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "🩺 پرونده فعال: ${pet.name} (${if (activeSpecies == "dog") "سگ" else "گربه"})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "نژاد: ${pet.breed} | وزن: ${pet.weight}kg | پرونده: ${pet.recordNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Top Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { activeSubTab = "درمان" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == "درمان") accentTeal else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("درمان و دستورالعمل‌ها", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeSubTab == "درمان") Color.White else primaryText)
                }

                Button(
                    onClick = { activeSubTab = "تشخیص" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == "تشخیص") accentTeal else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تشخیص و آزمایشگاه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeSubTab == "تشخیص") Color.White else primaryText)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Crossfade(targetState = activeSubTab) { subTab ->
                if (subTab == "تشخیص") {
                    // MAIN DIAGNOSIS SUB-SYSTEM
                    when (currentSubScreen) {
                        "home" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Search Input Area (Screenshot 1 Top)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Search, contentDescription = "Search", tint = secondaryText)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            modifier = Modifier.weight(1f),
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = primaryText),
                                            decorationBox = { innerTextField ->
                                                if (searchQuery.isEmpty()) {
                                                    Text(
                                                        "Enter a clinical sign or a laboratory fi...",
                                                        color = secondaryText.copy(alpha = 0.7f),
                                                        fontSize = 13.sp
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.Mic, contentDescription = "Voice search", tint = secondaryText)
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                if (searchQuery.isNotEmpty()) {
                                    // Live signs results from searching
                                    Text("نتایج جستجوی علائم بالینی:", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End), color = accentTeal)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val filtered = signsList.filter { it.nameEn.contains(searchQuery, ignoreCase = true) || it.nameFa.contains(searchQuery) }
                                    if (filtered.isEmpty()) {
                                        Text("موردی یافت نشد", fontSize = 11.sp, color = secondaryText, modifier = Modifier.padding(16.dp))
                                    } else {
                                        filtered.forEach { s ->
                                            val isSel = selectedSigns.contains(s.id)
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        if (isSel) selectedSigns.remove(s.id) else selectedSigns.add(s.id)
                                                    },
                                                colors = CardDefaults.cardColors(containerColor = if (isSel) accentTealContainer else cardBgColor),
                                                border = BorderStroke(1.dp, if (isSel) accentTeal else strokeColor)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(s.nameEn, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = primaryText)
                                                    Text(s.nameFa, fontSize = 11.sp, color = secondaryText)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                } else {
                                    // Primary Headline matching Screenshot 1 exactly
                                    Text(
                                        text = "DIFFERENTIAL DIAGNOSES AND TRAINING IN FELINE AND CANINE MEDICINE.",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentTeal,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))
                                }

                                // Responsive Grid of Main Categories
                                val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
                                val isTablet = screenWidth >= 600

                                if (isTablet) {
                                    // Single elegant horizontal row of 4 cards on tablet for optimal widescreen appearance!
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        GridActionCard(
                                            title = "Historical\nsigns",
                                            bgColor = cardBgColor,
                                            strokeColor = strokeColor,
                                            textColor = primaryText,
                                            icon = { HistoricalSignsIcon(modifier = Modifier.fillMaxSize(), color = accentTeal) },
                                            badgeContainer = accentTealContainer,
                                            onClick = { currentSubScreen = "historical" },
                                            modifier = Modifier.weight(1f)
                                        )

                                        GridActionCard(
                                            title = "Physical\nsigns",
                                            bgColor = cardBgColor,
                                            strokeColor = strokeColor,
                                            textColor = primaryText,
                                            icon = { PhysicalSignsIcon(modifier = Modifier.fillMaxSize(), color = accentTeal) },
                                            badgeContainer = accentTealContainer,
                                            onClick = { currentSubScreen = "physical" },
                                            modifier = Modifier.weight(1f)
                                        )

                                        GridActionCard(
                                            title = "Laboratory\nfindings",
                                            bgColor = cardBgColor,
                                            strokeColor = strokeColor,
                                            textColor = primaryText,
                                            icon = { LaboratoryFindingsIcon(modifier = Modifier.fillMaxSize(), color = accentTeal) },
                                            badgeContainer = accentTealContainer,
                                            onClick = { currentSubScreen = "lab" },
                                            modifier = Modifier.weight(1f)
                                        )

                                        GridActionCard(
                                            title = "Combine\n(pro)",
                                            bgColor = cardBgColor,
                                            strokeColor = strokeColor,
                                            textColor = primaryText,
                                            icon = { CombineIcon(modifier = Modifier.fillMaxSize(), color = accentTeal) },
                                            badgeContainer = accentTealContainer,
                                            onClick = { currentSubScreen = "combine" },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                } else {
                                    // Standard 2x2 Grid of Main Categories on mobile matching visual specs
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // 1. Historical Signs Card
                                            GridActionCard(
                                                title = "Historical\nsigns",
                                                bgColor = cardBgColor,
                                                strokeColor = strokeColor,
                                                textColor = primaryText,
                                                icon = { HistoricalSignsIcon(modifier = Modifier.fillMaxSize(), color = accentTeal) },
                                                badgeContainer = accentTealContainer,
                                                onClick = { currentSubScreen = "historical" },
                                                modifier = Modifier.weight(1f)
                                            )

                                            // 2. Physical Signs Card
                                            GridActionCard(
                                                title = "Physical\nsigns",
                                                bgColor = cardBgColor,
                                                strokeColor = strokeColor,
                                                textColor = primaryText,
                                                icon = { PhysicalSignsIcon(modifier = Modifier.fillMaxSize(), color = accentTeal) },
                                                badgeContainer = accentTealContainer,
                                                onClick = { currentSubScreen = "physical" },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // 3. Laboratory Findings Card
                                            GridActionCard(
                                                title = "Laboratory\nfindings",
                                                bgColor = cardBgColor,
                                                strokeColor = strokeColor,
                                                textColor = primaryText,
                                                icon = { LaboratoryFindingsIcon(modifier = Modifier.fillMaxSize(), color = accentTeal) },
                                                badgeContainer = accentTealContainer,
                                                onClick = { currentSubScreen = "lab" },
                                                modifier = Modifier.weight(1f)
                                            )

                                            // 4. Combine (pro) Card
                                            GridActionCard(
                                                title = "Combine\n(pro)",
                                                bgColor = cardBgColor,
                                                strokeColor = strokeColor,
                                                textColor = primaryText,
                                                icon = { CombineIcon(modifier = Modifier.fillMaxSize(), color = accentTeal) },
                                                badgeContainer = accentTealContainer,
                                                onClick = { currentSubScreen = "combine" },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                // Dynamic computed differential diagnosis list from selected symptoms
                                if (selectedSigns.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = accentTealContainer),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(onClick = { selectedSigns.clear() }) {
                                                    Text("صاف کردن", color = Color.Red, fontSize = 12.sp)
                                                }
                                                Text("نتایج انطباق آزمایشگاهی و علائم:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentTeal)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            // Compute matching illnesses based on checked items
                                            val selectedSignObj = signsList.filter { selectedSigns.contains(it.id) }
                                            val diseaseFreq = mutableMapOf<String, Int>()
                                            selectedSignObj.forEach { sign ->
                                                sign.diseases.forEach { d ->
                                                    diseaseFreq[d] = (diseaseFreq[d] ?: 0) + 1
                                                }
                                            }
                                            
                                            val totalChecked = selectedSigns.size
                                            val sortedMatches = diseaseFreq.toList().sortedByDescending { it.second }
                                            
                                            sortedMatches.forEach { (disease, occurrences) ->
                                                val pct = (occurrences.toFloat() / totalChecked * 100).toInt()
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("$pct% Match", color = accentTeal, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Text(disease, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = primaryText)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "historical" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                val categories = listOf(
                                    Triple("General signs", "general", "علائم عمومی"),
                                    Triple("Cardiorespiratory signs", "cardio", "علائم قلبی تنفسی"),
                                    Triple("Gastrointestinal signs", "gastro", "علائم گوارشی"),
                                    Triple("Neurological signs", "neuro", "علائم عصبی"),
                                    Triple("Orthopaedic signs", "ortho", "علائم ارتوپدی"),
                                    Triple("Urogenital signs", "uro", "علائم ادراری تناسلی"),
                                    Triple("Ophthalmological signs", "ophthalm", "علائم چشم‌پزشکی"),
                                    Triple("Dermatological signs", "derm", "علائم پوست و مو")
                                )

                                if (selectedCategoryDetail == null) {
                                    // 1. Show Categories list
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { currentSubScreen = "home" }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentTeal)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "HISTORICAL SIGNS",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentTeal
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        categories.forEach { (label, tag, labelFa) ->
                                            CategoryRowItem(
                                                title = label,
                                                subtitle = labelFa,
                                                tag = tag,
                                                bgColor = cardBgColor,
                                                strokeColor = strokeColor,
                                                textColor = primaryText,
                                                linkColor = accentTeal,
                                                secondaryTextColor = secondaryText,
                                                onClick = {
                                                    selectedCategoryDetail = tag
                                                    selectedHistoricalSign = null
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    // We are inside a selected category detail (e.g. general, cardio, etc.)
                                    val currentCatName = categories.firstOrNull { it.second == selectedCategoryDetail }?.first ?: ""
                                    
                                    if (selectedHistoricalSign == null) {
                                        // 2. Show List of signs in this category (Screenshot 1 & 2)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = { selectedCategoryDetail = null }) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentTeal)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    "HISTORICAL SIGNS",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentTeal
                                                )
                                                Text(
                                                    "($currentCatName)",
                                                    fontSize = 12.sp,
                                                    color = secondaryText
                                                )
                                            }
                                        }

                                        // Information Instruction card matching design
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
                                                .padding(bottom = 16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    "Historical signs",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = primaryText
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Select a historical sign to obtain the corresponding differential diagnosis.",
                                                    fontSize = 12.sp,
                                                    color = secondaryText
                                                )
                                            }
                                        }

                                        // Get the list of signs to display
                                        // If cat == general, we display the 12 items from historicalSignsDb
                                        // Otherwise, we get them from signsList (or hardcoded db)
                                        val displaySigns = if (selectedCategoryDetail == "general") {
                                            historicalSignsDb.map { Triple(it.id, it.name, it.name) }
                                        } else {
                                            signsList.filter { it.category == selectedCategoryDetail }.map { Triple(it.id, it.nameEn, it.nameFa) }
                                        }

                                        // Master Card layout encompassing Contents and the list items (Screenshot 1 & 2)
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, strokeColor, RoundedCornerShape(16.dp))
                                                .padding(bottom = 16.dp),
                                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "Contents",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = primaryText
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 36.dp, height = 28.dp)
                                                            .background(accentTeal, RoundedCornerShape(14.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = displaySigns.size.toString(),
                                                            color = Color.White,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    displaySigns.forEach { (id, nameEn, nameFa) ->
                                                        Card(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .border(1.dp, strokeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                                .clickable { selectedHistoricalSign = id },
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.3f) else Color.White
                                                            ),
                                                            shape = RoundedCornerShape(12.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(36.dp)
                                                                            .background(accentTeal.copy(alpha = 0.11f), CircleShape)
                                                                            .border(1.dp, accentTeal.copy(alpha = 0.3f), CircleShape),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Description,
                                                                            contentDescription = null,
                                                                            tint = accentTeal,
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    }
                                                                    Spacer(modifier = Modifier.width(14.dp))
                                                                    Text(
                                                                        text = nameEn,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 14.sp,
                                                                        color = primaryText
                                                                    )
                                                                }
                                                                Icon(
                                                                    imageVector = Icons.Default.ChevronRight,
                                                                    contentDescription = "Open Details",
                                                                    tint = secondaryText,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // 3. Show Details of the selected sign (Screenshot 3 & 4 & 5)
                                        val currentSignId = selectedHistoricalSign ?: ""
                                        // Fallback sign detail if not found in db
                                        val fallbackName = if (selectedCategoryDetail == "general") {
                                            historicalSignsDb.firstOrNull { it.id == currentSignId }?.name ?: ""
                                        } else {
                                            signsList.firstOrNull { it.id == currentSignId }?.nameEn ?: ""
                                        }
                                        val signDetail = getHistoricalSignDetail(currentSignId, fallbackName)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = { selectedHistoricalSign = null }) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentTeal)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    "HISTORICAL SIGNS",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentTeal
                                                )
                                                Text(
                                                    "($currentCatName)",
                                                    fontSize = 12.sp,
                                                    color = secondaryText
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Prominent Sign Name Banner matching Screenshot 3
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, strokeColor, RoundedCornerShape(4.dp)),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isDark) Color(0xFF2C2523) else Color(0xFFEFEBE9)
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = signDetail.name.uppercase(),
                                                    color = if (isDark) Color(0xFFD7CCC8) else Color(0xFF4E342E),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    letterSpacing = 1.2.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            ExpandableDetailCard(
                                                title = "DEFINITION",
                                                content = signDetail.definition,
                                                primaryText = primaryText,
                                                secondaryText = secondaryText,
                                                cardBgColor = cardBgColor,
                                                strokeColor = strokeColor
                                            )

                                            ExpandableDetailCard(
                                                title = "FREQUENT CAUSES",
                                                content = signDetail.frequentCauses,
                                                primaryText = primaryText,
                                                secondaryText = secondaryText,
                                                cardBgColor = cardBgColor,
                                                strokeColor = strokeColor
                                            )

                                            ExpandableDetailCard(
                                                title = "OTHER CAUSES",
                                                content = signDetail.otherCauses,
                                                primaryText = primaryText,
                                                secondaryText = secondaryText,
                                                cardBgColor = cardBgColor,
                                                strokeColor = strokeColor
                                            )

                                            ExpandableDetailCard(
                                                title = "BIBLIOGRAPHIC REFERENCES",
                                                content = signDetail.bibliographicReferences,
                                                primaryText = primaryText,
                                                secondaryText = secondaryText,
                                                cardBgColor = cardBgColor,
                                                strokeColor = strokeColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "physical" -> {
                            // Elegant design-focused Physical Signs Screen matching screenshots perfectly
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                val categories = listOf(
                                    Triple("General signs", "general", "علائم عمومی بالینی"),
                                    Triple("Cardiorespiratory signs", "cardio", "علائم قلبی تنفسی و عروقی"),
                                    Triple("Gastrointestinal signs", "gastro", "علائم مرتبط با دستگاه گوارش"),
                                    Triple("Neurological signs", "neuro", "آسیب‌ها و علائم سیستم عصبی"),
                                    Triple("Orthopaedic signs", "ortho", "مشکلات حرکتی، استخوان و مفاصل"),
                                    Triple("Urogenital signs", "uro", "نابهنجاری‌های سیستم ادراری تناسلی"),
                                    Triple("Ophthalmological signs", "ophthalm", "تظاهرات بالینی و علائم چشم"),
                                    Triple("Dermatological signs", "derm", "آسیب‌های پوست، مو و لایه‌های خارجی")
                                )

                                if (selectedPhysicalCategoryDetail == null) {
                                    // 1. Show List of Physical Signs categories with Persian descriptions
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { currentSubScreen = "home" }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentTeal)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "PHYSICAL SIGNS",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentTeal
                                        )
                                    }

                                    // Primary descriptive card matching design
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
                                            .padding(bottom = 16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                "Physical signs",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = primaryText
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Select a category to obtain the corresponding physical signs.",
                                                fontSize = 12.sp,
                                                color = secondaryText
                                            )
                                        }
                                    }

                                    // Draw categories in 1-column list matching historical layout
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        categories.forEach { (label, tag, labelFa) ->
                                            CategoryRowItem(
                                                title = label,
                                                subtitle = labelFa,
                                                tag = tag,
                                                bgColor = cardBgColor,
                                                strokeColor = strokeColor,
                                                textColor = primaryText,
                                                linkColor = accentTeal,
                                                secondaryTextColor = secondaryText,
                                                onClick = {
                                                    selectedPhysicalCategoryDetail = tag
                                                    selectedPhysicalSign = null
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    // Categories detail inside select category
                                    val currentCatName = categories.firstOrNull { it.second == selectedPhysicalCategoryDetail }?.first ?: ""
                                    
                                    if (selectedPhysicalSign == null) {
                                        // 2. Show List of physical signs in this category
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = { selectedPhysicalCategoryDetail = null }) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentTeal)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    "PHYSICAL SIGNS",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentTeal
                                                )
                                                Text(
                                                    "($currentCatName)",
                                                    fontSize = 12.sp,
                                                    color = secondaryText
                                                )
                                            }
                                        }

                                        // Information Instruction card matching design
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
                                                .padding(bottom = 16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    "Physical signs",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = primaryText
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Select a physical sign to obtain the corresponding differential diagnosis.",
                                                    fontSize = 12.sp,
                                                    color = secondaryText
                                                )
                                            }
                                        }

                                        // Set display physical signs
                                        val displaySigns = when (selectedPhysicalCategoryDetail) {
                                            "general" -> listOf(
                                                Triple("cyanosis", "Cyanosis", "کبودی پوست و مخاطات عروقی"),
                                                Triple("failure_to_grow", "Failure to grow", "عدم رشد یا کندی رشد بالینی"),
                                                Triple("fever", "Fever", "تب (دمای فوق فیزیولوژیک پاسخ به پیروژن)"),
                                                Triple("hyperemia", "Hyperemia", "پرخونی فعال مخاطات یا پوست"),
                                                Triple("hypertension", "Hypertension, systemic", "افزایش سنکوپ‌زای فشار خون سیستمیک"),
                                                Triple("hypotension", "Hypotension, systemic", "کاهش بحرانی فشار خون سیستمیک"),
                                                Triple("hypothermia", "Hypothermia", "کاهش دمای مرکزی بدن بیمار"),
                                                Triple("jaundice", "Jaundice / Icterus", "زردی صلب چشم و پوست بدن حیوان"),
                                                Triple("lymphadenopathy", "Lymphadenopathy", "تورم غدد لنفاوی سطحی"),
                                                Triple("pallor", "Pallor", "رنگ‌پریدگی لثه و مخاطات"),
                                                Triple("edema", "Peripheral edema", "ادم پیرامونی و تجمع مایع میان‌بافتی"),
                                                Triple("petechiae", "Petechiae / Ecchymoses", "پورپورا و خونریزی ریز جلدی"),
                                                Triple("pruritus", "Pruritus", "خارش و مالش مداوم پوست"),
                                                Triple("restlessness", "Restlessness", "بی‌قراری و کلافگی بیمار"),
                                                Triple("shock", "Shock", "کلاپس عروقی و عدم پرفیوژن بافتی"),
                                                Triple("voice_change", "Voice change", "تغییر تن صدا بر اثر فلجی تارهای صوتی"),
                                                Triple("obesity", "Weight gain / Obesity", "چاقی مفرط و انباشت چربی عروقی"),
                                                Triple("weight_loss", "Weight loss", "لاغری مفرط و کاهش وزن مزمن بیمار")
                                            )
                                            "cardio" -> listOf(
                                                Triple("bradycardia", "Bradycardia", "ضربان قلب بسیار کند و ضعیف"),
                                                Triple("cardiac_arrhythmias", "Cardiac arrhythmias", "آریتمی شدید قلبی"),
                                                Triple("congestive_heart_failure", "Congestive heart failure", "نارسایی احتقانی حاد قلب"),
                                                Triple("cough_cats", "Cough in cats", "سرفه در گله گربه‌سانان"),
                                                Triple("cough_dogs", "Cough in dogs", "سرفه در سگ‌سانان"),
                                                Triple("crackles", "Crackles", "صداهای ترق‌ترق ریوی"),
                                                Triple("dyspnea_tachypnea", "Dyspnea / Tachypnea", "تنگی نفس و تندنفسی حاد"),
                                                Triple("epistaxis", "Epistaxis", "خون‌دماغ شدن (اپیستاکسی)"),
                                                Triple("hemoptysis", "Hemoptysis", "خون‌ریزی ریوی (سرفه خونی)"),
                                                Triple("jugular_pulse", "Jugular pulse (abnormal)", "پالس غیرطبیعی ورید ژوگولار"),
                                                Triple("murmurs_cardiac", "Murmurs (cardiac)", "سوفل قلبی غیر طبیعی"),
                                                Triple("pulse_abnormalities", "Pulse abnormalities", "ناهنجاری‌های پالس ضربانی نای"),
                                                Triple("reverse_sneezing", "Reverse sneezing", "عطسه معکوس"),
                                                Triple("sneezing_nasal", "Sneezing / Nasal discharge", "عطسه و ترشحات بینی"),
                                                Triple("stertor", "Stertor", "خرخر تنفسی (خواب یا بیداری)"),
                                                Triple("stridor", "Stridor", "صوت تنفسی زیر و خشن"),
                                                Triple("tachycardia", "Tachycardia", "تاکی‌کاردی و ضربان قلب سریع"),
                                                Triple("wheezes", "Wheezes", "صبت ویز تنفسی بازدمی ریوی")
                                            )
                                            "gastro" -> listOf(
                                                Triple("abdominal_distension", "Abdominal distension", "اتساع و بادکردگی شکم"),
                                                Triple("acute_abdomen", "Acute abdomen", "شکم حاد و درد ارگانیک شدید"),
                                                Triple("halitosis", "Halitosis", "بوی بد دهان"),
                                                Triple("hepatomegaly", "Hepatomegaly", "بزرگ‌شدگی حاد کبد"),
                                                Triple("oral_masses", "Oral masses", "توده‌ها و لزیون‌های دهانی"),
                                                Triple("oral_ulceration", "Oral ulceration", "زخم‌های حاد مخاط دهانی"),
                                                Triple("ptyalism_pseudoptyalism", "Ptyalism / Pseudoptyalism", "جاری شدن بزاق بیش از حد"),
                                                Triple("salivary_gland_enlargement", "Salivary gland enlargement", "تورم غدد بزاق دهانی"),
                                                Triple("stomatitis", "Stomatitis", "التهاب و زخم لثه و دهان")
                                            )
                                            "neuro" -> listOf(
                                                Triple("ataxia", "Ataxia", "عدم تعادل و ناهماهنگی حرکتی"),
                                                Triple("cervical_lesions", "Cervical lesions (C1-C5)", "ضایعات نخاعی سگمان گردنی ۱ تا ۵"),
                                                Triple("cervicothoracic_lesions", "Cervicothoracic lesions (C6-T2)", "ضایعات نخاعی سگمان گردنی ۶ تا سینه‌ای ۲"),
                                                Triple("circling", "Circling", "چرخش ترجیحی به یک سمت"),
                                                Triple("deafness", "Deafness", "ناشنوایی حاد یا مزمن"),
                                                Triple("dyskinesia", "Dyskinesia", "حرکات غیر ارادی عضلانی دیسکینزی"),
                                                Triple("head_pressing", "Head pressing", "فشار دادن سر به اجسام"),
                                                Triple("head_tilt", "Head tilt", "کج کردن سر به یک سمت بدنی"),
                                                Triple("lumbosacral_lesions", "Lumbosacral lesions (L4-S3)", "ضایعات کمری خاجی سگمان کمر ۴ تا خاجی ۳"),
                                                Triple("monoparesis", "Monoparesis", "فلجی موضعی یک اندام حرکتی"),
                                                Triple("myoclonus", "Myoclonus", "انقباضات ناگهانی و ریتمیک میوکلونوس"),
                                                Triple("myotonia", "Myotonia", "سفتی پایدار عضلاتی (میوتونی)"),
                                                Triple("nystagmus", "Nystagmus", "لرزش نوسانی مردمک چشم (نیستاگموس)"),
                                                Triple("paraparesis", "Paraparesis", "فلجی ناقص اندام‌های حرکتی خلفی"),
                                                Triple("seizures", "Seizures", "حملات صرع و غش رفتگی"),
                                                Triple("stupor_coma", "Stupor / Coma", "بهت‌زدگی عمیق یا حالت کما"),
                                                Triple("tetanus", "Tetanus", "سفتی شدید کلی کزاز"),
                                                Triple("tetany", "Tetany", "اسپاسم و انقباضات پیاپی تشنج تپه‌ای"),
                                                Triple("tetraparesis", "Tetraparesis", "ضعف حرکتی هر چهار اندام حرکتی"),
                                                Triple("thoracolumbar_lesions", "Thoracolumbar lesions (T3-L3)", "ضایعات نخاعی سگمان سینه‌ای ۳ تا کمری ۳"),
                                                Triple("tremors", "Tremors", "لرزش عمومی یا منطقه‌ای عضلات"),
                                                Triple("vestibular_disease", "Vestibular disease", "بیماری وستیبولار"),
                                                Triple("vestibular_disease_central", "Vestibular disease (central)", "بیماری وستیبولار منشأ مرکزی"),
                                                Triple("vestibular_disease_peripheral", "Vestibular disease (peripheral)", "بیماری وستیبولار منشأ محیطی")
                                            )
                                            "ortho" -> listOf(
                                                Triple("lameness", "Lameness", "لنگش خفیف و ملایم اندام حرکتی جفت"),
                                                Triple("joint_swelling", "Joint swelling", "تورم مفصلی و تاندونیت")
                                            )
                                            "uro" -> listOf(
                                                Triple("anuria", "Anuria", "قطع ادرار کامل یا کم ادراری شدید"),
                                                Triple("hematuria", "Hematuria", "وجود گلبول قرمز و خون در ادرار")
                                            )
                                            "ophthalm" -> listOf(
                                                Triple("miosis", "Miosis", "انقباض حداکثری مردمک چشم بیمار"),
                                                Triple("mydriasis", "Mydriasis", "گشادی پایدار مردمک یک یا هر دو چشم")
                                            )
                                            "derm" -> listOf(
                                                Triple("pruritus", "Pruritus", "خارش، مالش و خراشیدگی پوست اندام‌ها"),
                                                Triple("petechiae", "Petechiae / Ecchymoses", "پورپورا و خونریزی ریز جلدی"),
                                                Triple("edema", "Peripheral edema", "ادم پیرامونی و تجمع مایعات"),
                                                Triple("hyperemia", "Hyperemia", "قرمزی و پرفشار شدن سطح مویرگ پوست")
                                            )
                                            else -> listOf()
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Contents",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = primaryText
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .height(24.dp)
                                                    .width(36.dp)
                                                    .background(accentTeal, RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = displaySigns.size.toString(),
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            displaySigns.forEach { (id, nameEn, nameFa) ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
                                                        .clickable { selectedPhysicalSign = id },
                                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 14.dp, vertical = 14.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(36.dp)
                                                                    .background(accentTeal.copy(alpha = 0.11f), CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Description,
                                                                    contentDescription = null,
                                                                    tint = accentTeal,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(14.dp))
                                                            Column {
                                                                Text(
                                                                    text = nameEn,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 14.sp,
                                                                    color = primaryText
                                                                )
                                                                Text(
                                                                    text = nameFa,
                                                                    fontSize = 11.sp,
                                                                    color = secondaryText
                                                                )
                                                            }
                                                        }
                                                        Icon(
                                                            imageVector = Icons.Default.ChevronRight,
                                                            contentDescription = "Open Details",
                                                            tint = secondaryText,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // 3. Show Details of the selected physical sign (alike historical)
                                        val currentSignId = selectedPhysicalSign ?: ""
                                        val fallbackName = physicalSignsDb.firstOrNull { it.id.equals(currentSignId, ignoreCase = true) }?.name 
                                            ?: currentSignId.replace("_", " ").replaceFirstChar { it.uppercase() }
                                        val signDetail = getPhysicalSignDetail(currentSignId, fallbackName)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = { selectedPhysicalSign = null }) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentTeal)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    "PHYSICAL SIGNS",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentTeal
                                                )
                                                Text(
                                                    "($currentCatName)",
                                                    fontSize = 12.sp,
                                                    color = secondaryText
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Prominent Sign Name Banner matching original design
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, strokeColor, RoundedCornerShape(4.dp)),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isDark) Color(0xFF2C2523) else Color(0xFFEFEBE9)
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = signDetail.name.uppercase(),
                                                    color = if (isDark) Color(0xFFD7CCC8) else Color(0xFF4E342E),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    letterSpacing = 1.2.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            ExpandableDetailCard(
                                                title = "DEFINITION",
                                                content = signDetail.definition,
                                                primaryText = primaryText,
                                                secondaryText = secondaryText,
                                                cardBgColor = cardBgColor,
                                                strokeColor = strokeColor
                                            )

                                            ExpandableDetailCard(
                                                title = "FREQUENT CAUSES",
                                                content = signDetail.frequentCauses,
                                                primaryText = primaryText,
                                                secondaryText = secondaryText,
                                                cardBgColor = cardBgColor,
                                                strokeColor = strokeColor
                                            )

                                            ExpandableDetailCard(
                                                title = "OTHER CAUSES",
                                                content = signDetail.otherCauses,
                                                primaryText = primaryText,
                                                secondaryText = secondaryText,
                                                cardBgColor = cardBgColor,
                                                strokeColor = strokeColor
                                            )

                                            ExpandableDetailCard(
                                                title = "BIBLIOGRAPHIC REFERENCES",
                                                content = signDetail.bibliographicReferences,
                                                primaryText = primaryText,
                                                secondaryText = secondaryText,
                                                cardBgColor = cardBgColor,
                                                strokeColor = strokeColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "lab" -> {
                            // Elegant design-focused Laboratory Entries Screen
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { currentSubScreen = "home" }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentTeal)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("LABORATORY RESULTS", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accentTeal)
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, strokeColor, RoundedCornerShape(12.dp)).padding(bottom = 16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("۲. تحلیل مقادیر گلبولی و بیوشیمی آزمایشگاه:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accentTeal)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        OutlinedTextField(
                                            value = labWbcInput,
                                            onValueChange = { labWbcInput = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("گلبول سفید خون WBC (range: 6-17 K/µL)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedTextField(
                                            value = labCreatinineInput,
                                            onValueChange = { labCreatinineInput = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("کراتینین خون (range: 0.5-1.8 mg/dL)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        
                                        // WBC and Creatinine analyzer interpretation
                                        val wVal = labWbcInput.toDoubleOrNull()
                                        val cVal = labCreatinineInput.toDoubleOrNull()
                                        if (wVal != null || cVal != null) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text("تفسیر هوشمند پارامترها:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryText)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            wVal?.let { w ->
                                                val status = when {
                                                    w < 6.0 -> "لکوپنی شدید ⬇️ (خطر پنلوکوپنی یا مسمومیت عفونی)"
                                                    w > 17.0 -> "لوکوسیتوز غلیظ ⬆️ (فرآیند التهابی یا عفونت باکتریال)"
                                                    else -> "طبیعی ✅ (گلبول‌های سفید در مرز هنجار)"
                                                }
                                                Text("WBC با مقدار $w: $status", fontSize = 11.sp, color = primaryText)
                                            }
                                            cVal?.let { c ->
                                                val status = when {
                                                    c < 0.5 -> "کاهش آزوتوز ⬇️ (ضعف عضلانی)"
                                                    c > 1.8 -> "آزوتمی پیش‌کلیوی/کلیوی ⬆️ (نارسایی کلیوی یا دهیدراتاسیون)"
                                                    else -> "طبیعی ✅ (سنجش اورمی در رنج امن)"
                                                }
                                                Text("کراتینین با مقدار $c: $status", fontSize = 11.sp, color = primaryText)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "combine" -> {
                            // Combine (pro) Venn Diagram screen
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { currentSubScreen = "home" }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = accentTeal)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("COMBINE (PRO)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accentTeal)
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, strokeColor, RoundedCornerShape(12.dp)).padding(bottom = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("ادغام هوشمند فاکتورهای بالینی (Venn Model)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = primaryText, textAlign = TextAlign.Center)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Venn diagram built dynamically
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(180.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(modifier = Modifier.size(100.dp).offset(x = (-25).dp, y = (-15).dp).background(Color(0xFF0F766E).copy(alpha = 0.15f), CircleShape).border(1.5.dp, Color(0xFF0F766E), CircleShape))
                                            Box(modifier = Modifier.size(100.dp).offset(x = 25.dp, y = (-15).dp).background(Color(0xFF2563EB).copy(alpha = 0.15f), CircleShape).border(1.5.dp, Color(0xFF2563EB), CircleShape))
                                            Box(modifier = Modifier.size(100.dp).offset(y = 25.dp).background(Color(0xFFD97706).copy(alpha = 0.15f), CircleShape).border(1.5.dp, Color(0xFFD97706), CircleShape))
                                            Box(modifier = Modifier.size(40.dp).background(Color.White, CircleShape).border(1.dp, strokeColor, CircleShape), contentAlignment = Alignment.Center) {
                                                Text("PRO", fontSize = 10.sp, color = accentTeal, fontWeight = FontWeight.Black)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("این قابلیت ویژه به شما امکان می‌دهد تا شرح حال شرح داده شده، نشانه‌های فیزیکی معاینه و سنجه‌های خونی بیمار را به صورت تمام خودکار تطابق داده و خطا را به زیر ۱٪ برسانید.", fontSize = 11.sp, color = secondaryText, textAlign = TextAlign.Center)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { showProDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = accentTeal)) {
                                            Text("خرید دسترسی حرفه‌ای (PRO)", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TREATMENTS & PROTOCOLS SCREEN
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Display guidelines dynamically from the SQLite local database (Room)
                                val diseaseCatalog = customGuidelines.filter { it.species == speciesKey }.ifEmpty {
                                    // Preloaded fallback lists
                                    staticGuidelinesCatalog.filter { it.species == speciesKey }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { showAddGuidelineDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add therapy protocol",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("پروتکل جدید", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Text(
                                        text = "📋 پروتکل‌های درمان بیماری‌ها ($specFarsi):",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentTeal,
                                        textAlign = TextAlign.Right
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "جهت مطالعه درمان‌های دارویی حاد، या مدیریت پروتکل‌های محلی ذخیره شده در پایگاه‌داده SQLite ضربه بزنید:",
                                    fontSize = 10.sp,
                                    color = secondaryText,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                diseaseCatalog.forEach { disease_item ->
                                    var isExpanded by remember { mutableStateOf(false) }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { isExpanded = !isExpanded },
                                        border = BorderStroke(0.5.dp, strokeColor)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (disease_item.id.startsWith("custom_g_")) {
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.deleteGuideline(disease_item)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete Local Guidelines",
                                                            tint = Color(0xFFEF4444),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.width(1.dp))
                                                }

                                                Text(
                                                    text = disease_item.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = accentTeal,
                                                    textAlign = TextAlign.Right
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("علائم بالینی: " + disease_item.symptoms, fontSize = 11.sp, color = primaryText, maxLines = if (isExpanded) 10 else 1, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())

                                            if (isExpanded) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("تشخیص‌های افتراقی:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = primaryText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                                Text(disease_item.diffDiagnosis, fontSize = 11.sp, color = secondaryText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("دستورالعمل و پروتکل درمانی پزشک:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = accentTeal, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                                Text(disease_item.protocol, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF15803D), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                            } else {
                                                Text("جهت باز کردن جزئیات ضربه بزنید...", fontSize = 9.sp, color = secondaryText, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
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

    // Professional Subscription Pro Modal popup
    if (showProDialog) {
        AlertDialog(
            onDismissRequest = { showProDialog = false },
            confirmButton = {
                Button(
                    onClick = { showProDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                ) {
                    Text("خرید دسترسی PRO", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProDialog = false }) {
                    Text("انصراف", color = accentTeal)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = accentTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("امکانات نسخه حرفه‌ای (Pro Features)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "با خرید نسخه طلایی برنامه، به تمام ۱۲۰+ علامت ارتوپدی، تناسلی ادراری، چشمی، پوستی و لرزش ماهیچه‌ها منحصراً دسترسی دائم داشته باشید و پروتکل‌ها را ادغام کنید.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Right
                )
            }
        )
    }

    if (showAddGuidelineDialog) {
        AlertDialog(
            onDismissRequest = { showAddGuidelineDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGuidelineName.isNotBlank() && newGuidelineProtocol.isNotBlank()) {
                            viewModel.insertGuideline(
                                species = speciesKey,
                                name = newGuidelineName,
                                symptoms = newGuidelineSymptoms.ifBlank { "بدون علامت ثبت شده" },
                                diffDiagnosis = newGuidelineDiffDiagnosis.ifBlank { "نامشخص/بررسی بالینی" },
                                protocol = newGuidelineProtocol
                            )
                            // Reset state
                            newGuidelineName = ""
                            newGuidelineSymptoms = ""
                            newGuidelineDiffDiagnosis = ""
                            newGuidelineProtocol = ""
                            showAddGuidelineDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                    enabled = newGuidelineName.isNotBlank() && newGuidelineProtocol.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ذخیره در پایگاه‌داده", color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGuidelineDialog = false }) {
                    Text("انصراف", color = accentTeal, fontSize = 12.sp)
                }
            },
            title = {
                Text(
                    text = "➕ افزودن دستورالعمل و پروتکل جدید ($specFarsi)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentTeal,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "مشخصات پروتکل درمانی محلی را جهت ذخیره در حافظه پایدار SQLite وارد نمایید:",
                        fontSize = 11.sp,
                        color = secondaryText,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newGuidelineName,
                        onValueChange = { newGuidelineName = it },
                        label = { Text("نام بیماری / عارضه (الزامی)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentTeal,
                            unfocusedBorderColor = strokeColor
                        )
                    )

                    OutlinedTextField(
                        value = newGuidelineSymptoms,
                        onValueChange = { newGuidelineSymptoms = it },
                        label = { Text("علائم بالینی شایع", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentTeal,
                            unfocusedBorderColor = strokeColor
                        )
                    )

                    OutlinedTextField(
                        value = newGuidelineDiffDiagnosis,
                        onValueChange = { newGuidelineDiffDiagnosis = it },
                        label = { Text("تشخیص‌های افتراقی تفکیکی", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentTeal,
                            unfocusedBorderColor = strokeColor
                        )
                    )

                    OutlinedTextField(
                        value = newGuidelineProtocol,
                        onValueChange = { newGuidelineProtocol = it },
                        label = { Text("دستورالعمل و پروتکل درمانی پزشک (الزامی)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentTeal,
                            unfocusedBorderColor = strokeColor
                        )
                    )
                }
            }
        )
    }
}

// 2x2 Grid card layout structure matching Screenshot 1 exactly
@Composable
fun GridActionCard(
    title: String,
    bgColor: Color,
    strokeColor: Color,
    textColor: Color,
    icon: @Composable () -> Unit,
    badgeContainer: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTablet = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier
            .run {
                if (isTablet) {
                    this.aspectRatio(1.35f)
                } else {
                    this.aspectRatio(1.1f)
                }
            }
            .border(1.dp, strokeColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(badgeContainer, CircleShape)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun CategoryRowItem(
    title: String,
    subtitle: String,
    tag: String,
    bgColor: Color,
    strokeColor: Color,
    textColor: Color,
    linkColor: Color,
    secondaryTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category cute drawn Icon based on tag, embedded in a circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(linkColor.copy(alpha = 0.1f), CircleShape)
                        .padding(9.dp)
                ) {
                    CategoryIcon(name = tag, color = linkColor)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Subcategory grid card matching Screenshot 2 & 3 lists
@Composable
fun CategoryGridItem(
    title: String,
    tag: String,
    isPro: Boolean,
    bgColor: Color,
    strokeColor: Color,
    textColor: Color,
    linkColor: Color,
    secondaryTextColor: Color,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1.15f)
            .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isPro) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color(0xFFE0F2FE), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("(PRO)", fontSize = 8.sp, color = Color(0xFF0369A1), fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Category cute drawn Icon based on tag
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    CategoryIcon(name = tag, color = linkColor)
                }

                Text(
                    text = title,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPro) "Pro Access" else "Open",
                        fontSize = 10.sp,
                        color = if (isPro) secondaryTextColor else linkColor,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isPro) Icons.Default.Lock else Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (isPro) secondaryTextColor else linkColor
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryIcon(name: String, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        when (name) {
            "general", "neuro_pro" -> {
                drawCircle(color = color, radius = w * 0.45f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = w * 0.08f))
                drawLine(color = color, start = Offset(w * 0.5f, h * 0.05f), end = Offset(w * 0.5f, h * 0.95f), strokeWidth = w * 0.08f)
                drawLine(color = color, start = Offset(w * 0.05f, h * 0.5f), end = Offset(w * 0.95f, h * 0.5f), strokeWidth = w * 0.08f)
            }
            "cardio" -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.85f)
                    cubicTo(w * 0.15f, h * 0.5f, w * 0.05f, h * 0.15f, w * 0.5f, h * 0.22f)
                    cubicTo(w * 0.95f, h * 0.15f, w * 0.85f, h * 0.5f, w * 0.5f, h * 0.85f)
                }
                drawPath(path = path, color = color, style = Stroke(width = w * 0.08f, join = StrokeJoin.Round))
            }
            "gastro" -> {
                val path = Path().apply {
                    moveTo(w * 0.35f, h * 0.15f)
                    lineTo(w * 0.65f, h * 0.15f)
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.5f, h * 0.35f)
                    cubicTo(w * 0.1f, h * 0.5f, w * 0.1f, h * 0.85f, w * 0.5f, h * 0.85f)
                    cubicTo(w * 0.9f, h * 0.85f, w * 0.9f, h * 0.5f, w * 0.5f, h * 0.35f)
                }
                drawPath(path = path, color = color, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
            }
            "neuro" -> {
                val cx = w * 0.5f
                val cy = h * 0.5f
                drawCircle(color = color, radius = w * 0.12f, center = Offset(cx, cy))
                val angles = listOf(0f, 60f, 120f, 180f, 240f, 300f)
                angles.forEach { angle ->
                    val rad = Math.toRadians(angle.toDouble())
                    val endX = cx + Math.cos(rad).toFloat() * w * 0.35f
                    val endY = cy + Math.sin(rad).toFloat() * h * 0.35f
                    drawLine(color = color, start = Offset(cx, cy), end = Offset(endX, endY), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
                }
            }
            "ortho" -> {
                drawLine(color = color, start = Offset(w * 0.3f, h * 0.7f), end = Offset(w * 0.7f, h * 0.3f), strokeWidth = w * 0.09f, cap = StrokeCap.Round)
                drawCircle(color = color, radius = w * 0.08f, center = Offset(w * 0.3f, h * 0.65f))
                drawCircle(color = color, radius = w * 0.08f, center = Offset(w * 0.7f, h * 0.35f))
            }
            "uro", "palp_pro" -> {
                drawOval(color = color, topLeft = Offset(w * 0.1f, h * 0.25f), size = Size(w * 0.35f, h * 0.5f), style = Stroke(width = w * 0.08f))
                drawOval(color = color, topLeft = Offset(w * 0.55f, h * 0.25f), size = Size(w * 0.35f, h * 0.5f), style = Stroke(width = w * 0.08f))
            }
            "ophthalm", "mucous" -> {
                val p = Path().apply {
                    moveTo(w * 0.1f, h * 0.5f)
                    quadraticBezierTo(w * 0.5f, h * 0.15f, w * 0.9f, h * 0.5f)
                    quadraticBezierTo(w * 0.5f, h * 0.85f, w * 0.1f, h * 0.5f)
                }
                drawPath(path = p, color = color, style = Stroke(width = w * 0.08f, join = StrokeJoin.Round))
                drawCircle(color = color, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.5f))
            }
            "derm", "vital" -> {
                val p = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    cubicTo(w * 0.15f, h * 0.45f, w * 0.15f, h * 0.85f, w * 0.5f, h * 0.85f)
                    cubicTo(w * 0.85f, h * 0.85f, w * 0.85f, h * 0.45f, w * 0.5f, h * 0.15f)
                }
                drawPath(path = p, color = color, style = Stroke(width = w * 0.08f))
            }
        }
    }
}

// Canvas-drawn Vector Icons matching Screenshots inside grid
@Composable
fun HistoricalSignsIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.7f, h * 0.75f),
            cornerRadius = CornerRadius(w * 0.08f),
            style = Stroke(width = w * 0.06f)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.35f, h * 0.06f),
            size = Size(w * 0.3f, h * 0.12f),
            cornerRadius = CornerRadius(w * 0.03f),
            style = Stroke(width = w * 0.06f)
        )
        for (i in 0..2) {
            drawLine(
                color = color.copy(alpha = 0.8f),
                start = Offset(w * 0.28f, h * 0.35f + i * h * 0.12f),
                end = Offset(w * 0.73f, h * 0.35f + i * h * 0.12f),
                strokeWidth = w * 0.05f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun PhysicalSignsIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.2f)
            lineTo(w * 0.25f, h * 0.45f)
            cubicTo(w * 0.25f, h * 0.75f, w * 0.75f, h * 0.75f, w * 0.75f, h * 0.45f)
            lineTo(w * 0.75f, h * 0.22f)
        }
        drawPath(path = path, color = color, style = Stroke(width = w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color = color, radius = w * 0.05f, center = Offset(w * 0.25f, h * 0.16f))
        drawCircle(color = color, radius = w * 0.05f, center = Offset(w * 0.75f, h * 0.16f))

        val subPath = Path().apply {
            moveTo(w * 0.5f, h * 0.65f)
            cubicTo(w * 0.5f, h * 0.85f, w * 0.82f, h * 0.8f, w * 0.82f, h * 0.55f)
        }
        drawPath(path = subPath, color = color, style = Stroke(width = w * 0.06f, cap = StrokeCap.Round))
        drawCircle(color = color, radius = w * 0.09f, center = Offset(w * 0.82f, h * 0.47f), style = Stroke(width = w * 0.06f))
    }
}

@Composable
fun LaboratoryFindingsIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(color = color, start = Offset(w * 0.2f, h * 0.85f), end = Offset(w * 0.8f, h * 0.85f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
        val standPath = Path().apply {
            moveTo(w * 0.65f, h * 0.85f)
            lineTo(w * 0.65f, h * 0.45f)
            cubicTo(w * 0.65f, h * 0.2f, w * 0.45f, h * 0.2f, w * 0.45f, h * 0.25f)
        }
        drawPath(path = standPath, color = color, style = Stroke(width = w * 0.06f, cap = StrokeCap.Round))
        val lensPath = Path().apply {
            moveTo(w * 0.32f, h * 0.28f)
            lineTo(w * 0.52f, h * 0.55f)
        }
        drawPath(path = lensPath, color = color, style = Stroke(width = w * 0.09f, cap = StrokeCap.Round))
        drawLine(color = color, start = Offset(w * 0.3f, h * 0.68f), end = Offset(w * 0.6f, h * 0.68f), strokeWidth = w * 0.05f)
    }
}

@Composable
fun CombineIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = w * 0.26f
        val cy = h * 0.5f
        drawCircle(color = color, radius = r, center = Offset(w * 0.38f, cy), style = Stroke(width = w * 0.05f))
        drawCircle(color = color, radius = r, center = Offset(w * 0.62f, cy), style = Stroke(width = w * 0.05f))
        drawLine(color = color, start = Offset(w * 0.38f, cy), end = Offset(w * 0.62f, cy), strokeWidth = w * 0.04f)
        drawCircle(color = color, radius = w * 0.04f, center = Offset(w * 0.38f, cy))
        drawCircle(color = color, radius = w * 0.04f, center = Offset(w * 0.62f, cy))
    }
}

// Static database of treatment protocols
data class CompactDisease(
    val name: String,
    val symptoms: String,
    val diffDiagnosis: String,
    val protocol: String
)

private fun getDiseaseCatalogFor(species: String?): List<CompactDisease> {
    return when (species) {
        "dog" -> listOf(
            CompactDisease("پاروویروس سگ‌سانان (CPV)", "اسهال خونی بسیار شدید و بدبو، استفراغ مداوم، بی‌اشتهایی کامل، تب بالا، دهیدراتاسیون بسیار سریع.", "کوروناویروس، ژیاردیازیس، انسداد مکانیکی گوارشی.", "مایع‌درمانی وریدی تهاجمی رینگرلاکتات، آنتی‌بیوتیک محافظتی ثانویه آمپی‌سیلین، ماروپیتانت ضد استفراغ."),
            CompactDisease("دیستمپر سگ (CDV)", "ترشحات غلیظ چرکی چشم و بینی، افزایش ضخامت پد کف پنجه پا، پرش عضلانی عصبی، تب نوسانی.", "هاری، هپاتیت عفونی سگ‌سانان، مننژیت قارچی.", "مراقبت‌های ویژه حمایتی، فنوپاربیتال ضدتشنج، داکسی‌سایکلین، مرطوب‌ساز مجرای تنفسی."),
            CompactDisease("سرفه کنل (سیاه‌سرفه)", "سرفه‌های عمیق بوقی خشک و مکرر پس از فعالیت بدنی، ترشح کف دهان.", "کلاپس نای، نارسایی احتقانی قلبی، بلع جسم خارجی.", "بخور ملایم آب گرم، داکسی‌سایکلین مناسب (۱۰mg/kg)، پرهیز از اعمال فشار قلاده بر مجرای نای.")
        )
        "cat" -> listOf(
            CompactDisease("کلسی‌ویروس گربه‌سانان (FCV)", "بافتهای زخمی دهان و دندان، ریزش بزاق، تب، بی‌اشتهایی شدید به دلیل درد دهان.", "هرپس‌ویروس گربه، لنفوم دهانی، زخم‌های ناشی از اورمی کلیه.", "ملوکسیکام ضددرد، کلیندامایسین برای برطرف کردن باکتری، تیتانیوم دهانی، تغذیه نرم و ولرم."),
            CompactDisease("پنلوکوپنی گربه‌ها (Panleukopenia)", "تب کشنده نوسانی، اسهال بدبو، کاهش شدید ناگهانی سطح گلبول‌های سفید خون.", "سالمونلوز شدید حاد، عفونت پریتونیت FIP روده.", "پنتوکسی‌فیلین، کواموکسی‌کلاو، سرم‌تراپی وریدی بسیار دقیق گرم شده، مراقبت‌های ایزوله حرارتی."),
            CompactDisease("راینوتراکئیت ویروسی گربه (FHV-1)", "زخم‌های قرنیه چشمی شاخه‌دار، ترشحات زیاد چشم و بینی، عطسه‌های دردآور.", "کلامیدیا فلیس، مایکوپلاسما عفونی گربه‌ها.", "اسید آمینه ال‌لایزین، قطره ضد ویروس چشمی مکرر، بخارساز ملایم اتاق.")
        )
        else -> listOf(
            CompactDisease("بند آمدن تخم در پرندگان", "کرنش شکم، نشستن کف قفس با بال‌های گشاده، تنفسی نامنظم حاد.", "تومور تخمدان، چاقی مفرط پرندگان.", "تامین محیط گرم و با رطوبت بسیار بالا، تزریق کلسیم گلوکونات مناسب."),
            CompactDisease("دم خیس در همسترها", "اسهال آبکی مداوم، خیسی و آلودگی مخرج، سستی کشنده.", "اسهال باکتریایی سبک، ژیاردیازیس.", "مایع‌درمانی زیرپوستی گرم، داکسی‌سایکلین مناسب جوندگان.")
        )
    }
}

// Historical signs details data structure & database
data class HistoricalSignDetail(
    val id: String,
    val name: String,
    val definition: String,
    val frequentCauses: String,
    val otherCauses: String,
    val bibliographicReferences: String
)

@Composable
fun ExpandableDetailCard(
    title: String,
    content: String,
    primaryText: Color,
    secondaryText: Color,
    cardBgColor: Color,
    strokeColor: Color
) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = primaryText,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = primaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = content,
                    fontSize = 12.sp,
                    color = secondaryText,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

fun getHistoricalSignDetail(id: String, fallbackName: String): HistoricalSignDetail {
    val existing = historicalSignsDb.firstOrNull { it.id.equals(id, ignoreCase = true) }
    if (existing != null) return existing
    
    return HistoricalSignDetail(
        id = id,
        name = fallbackName,
        definition = "Clinical manifestation of $fallbackName observed during patient examination. This symptom requires immediate investigative differential diagnosis.",
        frequentCauses = "• Primary Pathogen / Trauma\nInfectious agents complicating system function\nPrimary localized physical injury or dysfunction\n\n• Secondary Inflammation\nImmunological defense activation",
        otherCauses = "• Systemic Disorders\nAge-related degenerative changes\nMetabolic complications\nIdiosyncratic drug reactions\n\n• Environmental factors\nAcute anxiety or stress",
        bibliographicReferences = "1. Textbook of Veterinary Internal Medicine.\n2. Muller & Kirk's Small Animal Dermatology."
    )
}

val historicalSignsDb = listOf(
    HistoricalSignDetail(
        id = "aggression",
        name = "Aggression",
        definition = "A threat, challenge or attack directed towards one or more individuals. It can be intraspecific (between one species) or interspecific (between different species).",
        frequentCauses = "• Behavioral\nFear\nLack of socialization\nPlay behavior\n\n• Organic cause\nPain",
        otherCauses = "• Behavioral\nAnxiety disorder\nImpulse-control aggression (dogs. Formerly: dominance aggression)\nMaternal protective instinct (dogs)\nPredatory behavior\n\n• Iatrogenic/toxic\nBenzodiazepines\nGlucocorticoids\nKetamine\nLead poisoning\n\n• Organic cause\nEncephalopathy\nEpilepsy\nHepatic encephalopathy\nHyperthyroidism (cats)\nOsteoarthritis\nRabies\nToxoplasmosis",
        bibliographicReferences = "1. BSAVA Manual of Canine and Feline Behavioral Medicine.\n2. Handbook of Veterinary Pain Management."
    ),
    HistoricalSignDetail(
        id = "anorexia",
        name = "Anorexia / Hyporexia",
        definition = "A complete lack of appetite (anorexia) or a decrease in food intake (hyporexia). It is a common non-specific sign of disease in dogs and cats.",
        frequentCauses = "• Infection / Inflammation\nSystemic viral/bacterial infections\nPyometra\n\n• Organ Dysfunction\nChronic Kidney Disease (CKD)\nHepatic disease\nGastrointestinal diseases",
        otherCauses = "• Pain / Discomfort\nDental disease / Stomatitis\nPost-operative discomfort\n\n• Behavioral\nStress\nDietary aversion\nEnvironmental change",
        bibliographicReferences = "1. Small Animal Clinical Nutrition, 5th Edition.\n2. Textbook of Veterinary Internal Medicine."
    ),
    HistoricalSignDetail(
        id = "collapse",
        name = "Collapse / Syncope",
        definition = "A sudden loss of strength causing the animal to fall, with (syncope) or without (collapse) transient loss of consciousness.",
        frequentCauses = "• Cardiovascular cause\nCardiomyopathy\nValvular disease (e.g., MMVD)\nArrhythmias\n\n• Neuromuscular cause\nSeizure disorders\nMyasthenia gravis",
        otherCauses = "• Respiratory cause\nTracheal collapse\nSevere laryngeal paralysis\n\n• Metabolic / Endocrine\nHypoglycemia\nAddisonian crisis (Hypoadrenocorticism)",
        bibliographicReferences = "1. Manual of Canine and Feline Cardiology.\n2. Rapid Practical Guide to Veterinary Emergency Medicines."
    ),
    HistoricalSignDetail(
        id = "failure_to_grow",
        name = "Failure to grow",
        definition = "Inadequate physical development, failure to gain weight properly, or stunting of growth in pediatric/young animals.",
        frequentCauses = "• Nutritional\nInadequate diet quality/quantity\nMalnutrition\n\n• Parasitic infestation\nHeavy intestinal parasite load (roundworms, hookworms)",
        otherCauses = "• Congenital / Genetic disorders\nPortosystemic shunt (PSS)\nPituitary dwarfism\nCongenital heart defects\n\n• Gastrointestinal\nExocrine Pancreatic Insufficiency (EPI)\nInflammatory bowel conditions",
        bibliographicReferences = "1. Pediatrics of the Dog and Cat.\n2. Clinical Canine and Feline Reproduction and Pediatrics."
    ),
    HistoricalSignDetail(
        id = "malodor",
        name = "Malodor",
        definition = "An unpleasant or foul odor emanating from the pet's body, mouth, ears, skin, or perineum.",
        frequentCauses = "• Dental Disease\nPeriodontitis / Gingivitis\nOral necrotizing lesions\n\n• Integumentary Infections\nDeep pyoderma\nSeborrhea with secondary yeast",
        otherCauses = "• Otitis Externa\nPseudomonas or Malassezia ear infection\n\n• Anal Sacculitis\nImpacted or ruptured anal glands\n\n• Metabolic system\nUremic breath (ESRD)",
        bibliographicReferences = "1. Müller & Kirk's Small Animal Dermatology.\n2. Manual of Small Animal Dentistry."
    ),
    HistoricalSignDetail(
        id = "polyphagia",
        name = "Polyphagia",
        definition = "An abnormally increased appetite or consumption of food, often accompanied by weight loss or weight gain depending on the etiology.",
        frequentCauses = "• Endocrine Disorders\nDiabetes Mellitus\nHyperthyroidism (especially cats)\nHyperadrenocorticism (Cushing's)",
        otherCauses = "• Gastrointestinal Malabsorption\nPortosystemic Shunts (PSS)\nExocrine Pancreatic Insufficiency (EPI)\nSevere Intestinal Parasitism\n\n• Drug-Induced\nExogenous corticosteroids",
        bibliographicReferences = "1. Canine & Feline Endocrinology.\n2. BSAVA Manual of Canine and Feline Gastroenterology."
    ),
    HistoricalSignDetail(
        id = "pruritus",
        name = "Pruritus",
        definition = "An unpleasant sensation that provokes the desire to scratch, rub, lick, or bite the skin. One of the most common dermatological complaints.",
        frequentCauses = "• Parasitic Hypersensitivity\nFlea bite allergy\nDemodicosis / Sarcoptic mange\n\n• Allergies\nAtopic dermatitis\nFood allergy",
        otherCauses = "• Infectious\nBacterial pyoderma\nMalassezia dermatitis\nDermatophytosis (Ringworm)\n\n• Immune-mediated\nPemphigus foliaceus",
        bibliographicReferences = "1. Small Animal Dermatology, 7th Edition.\n2. BSAVA Manual of Canine and Feline Dermatology."
    ),
    HistoricalSignDetail(
        id = "restlessness",
        name = "Restlessness",
        definition = "An inability to relax, settle or remain still. Often indicates underlying pain, anxiety, or systemic discomfort.",
        frequentCauses = "• Pain and Discomfort\nOsteoarthritis flare-ups\nVisceral pain (GDV/colic)\nSevere pruritus\n\n• Cognitive Decline\nCognitive Dysfunction Syndrome (CDS)",
        otherCauses = "• Anxiety and Phobias\nSeparation anxiety\nNoise phobia (thunder/fireworks)\n\n• Systemic / Endocrine\nHyperthyroidism\nEncephalopathy",
        bibliographicReferences = "1. Canine and Feline Behavior for Veterinary Technicians and Nurses.\n2. Pain Management in Small Animal Clinicians."
    ),
    HistoricalSignDetail(
        id = "voice_change",
        name = "Voice change",
        definition = "An alteration in the pitch, tone, or ability to produce vocalizations (dysphonia or aphonia) in dogs or cats.",
        frequentCauses = "• Laryngeal Disease\nLaryngeal paralysis (GOLPP)\nLaryngitis (infectious/traumatic)",
        otherCauses = "• Neoplasia / Masses\nLaryngeal polyps / carcinomas\nThyroid carcinoma compressing recurrent laryngeal nerve\n\n• Systemic conditions\nMyasthenia gravis",
        bibliographicReferences = "1. Small Animal Surgical Emergencies.\n2. Textbook of Veterinary Internal Medicine."
    ),
    HistoricalSignDetail(
        id = "weakness",
        name = "Weakness/exercise intolerance",
        definition = "A general reduction in bodily strength or an inability to sustain physical exertional activity.",
        frequentCauses = "• Cardiovascular problems\nCongestive Heart Failure (CHF)\nDilated Cardiomyopathy (DCM)\nMitral valve disease\n\n• Hematological\nSevere anemia (IMHA, blood loss)",
        otherCauses = "• Orthopaedic conditions\nSevere osteoarthritis\nCranial cruciate ligament rupture\n\n• Endocrine / Neurological\nHypothyroidism / Addison's\nMyasthenia gravis",
        bibliographicReferences = "1. Cardiovascular Diseases of the Dog and Cat.\n2. Veterinary Neuroanatomy and Clinical Neurology."
    ),
    HistoricalSignDetail(
        id = "obesity",
        name = "Weight gain / Obesity",
        definition = "An excessive accumulation of body fat or increase in body mass, leading to healthy weight range deviation.",
        frequentCauses = "• Dietary / Lifestyle\nOverfeeding (excess portion size)\nLack of routine physical exercise",
        otherCauses = "• Endocrine disease\nHypothyroidism (dogs)\nHyperadrenocorticism (Cushing's)\n\n• Behavioral / Idiopathic\nSpaying / Neutering changes",
        bibliographicReferences = "1. Small Animal Clinical Nutrition.\n2. Canine and Feline Endocrinology and Reproduction."
    ),
    HistoricalSignDetail(
        id = "weight_loss_general",
        name = "Weight loss",
        definition = "An involuntary decrease in body weight, representing negative energy balance due to low intake, malabsorption, or chronic illnesses.",
        frequentCauses = "• Chronic Systemic Diseases\nChronic Kidney Disease (CKD)\nDiabetes Mellitus\nHyperthyroidism\nNeoplasia",
        otherCauses = "• Gastrointestinal\nInflammatory Bowel Disease (IBD)\nMalabsorption / Maldigestion\n\n• Dental / Masticatory pain\nAdvanced periodontal disease",
        bibliographicReferences = "1. Small Animal Gastroenterology.\n2. Textbook of Veterinary Internal Medicine."
    )
)

data class PhysicalSignDetail(
    val id: String,
    val name: String,
    val definition: String,
    val frequentCauses: String,
    val otherCauses: String,
    val bibliographicReferences: String
)

fun getPhysicalSignDetail(id: String, fallbackName: String): PhysicalSignDetail {
    val existing = physicalSignsDb.firstOrNull { it.id.equals(id, ignoreCase = true) }
    if (existing != null) return existing
    
    return PhysicalSignDetail(
        id = id,
        name = fallbackName,
        definition = "Clinical physical examination finding of $fallbackName observed during patient inspection, palpation, percussion, or auscultation.",
        frequentCauses = "• Primary Dysfunction\nSpecific localized tissue injury or clinical pathology\n\n• Systemic Reaction\nCompromised physiological feedback loop",
        otherCauses = "• Congenital / Genetic Factors\nRare underlying breed predisposition\n\n• Idiopathic causes\nAsymptomatic or transient abnormality",
        bibliographicReferences = "1. Textbook of Veterinary Internal Medicine.\n2. Clinical Veterinary Advisor."
    )
}

val physicalSignsDb = listOf(
    PhysicalSignDetail(
        id = "cyanosis",
        name = "Cyanosis",
        definition = "A bluish or purplish discoloration of the skin or mucous membranes due to high levels of deoxygenated hemoglobin in the blood.",
        frequentCauses = "• Respiratory conditions\nPneumonia\nUpper airway obstruction\nTracheal collapse\n\n• Cardiovascular conditions\nCongestive heart failure\nCongenital heart defects",
        otherCauses = "• Toxic exposures\nMethemoglobinemia (e.g., acetaminophen toxicity)\n\n• Environmental factors\nSevere hypothermia",
        bibliographicReferences = "1. Small Animal Critical Care Medicine.\n2. Manual of Canine and Feline Cardiology."
    ),
    PhysicalSignDetail(
        id = "failure_to_grow",
        name = "Failure to grow",
        definition = "Inadequate gains in body weight and physical development in pediatric/young animals compared to breed and age expectations.",
        frequentCauses = "• Nutritional deficiencies\nInadequate quantity or quality of food\n\n• Intoxication & Parasitism\nIntestinal worms (roundworms/hookworms)",
        otherCauses = "• Congenital shunts\nPortosystemic Shunts (PSS)\n\n• Endocrine conditions\nPituitary Dwarfism",
        bibliographicReferences = "1. Pediatrics of the Dog and Cat.\n2. Small Animal Clinical Nutrition."
    ),
    PhysicalSignDetail(
        id = "fever",
        name = "Fever",
        definition = "An elevation of the core body temperature above the normal range established for dogs (37.5-39.2°C) or cats (38.0-39.2°C), typically as a response to pyrogens.",
        frequentCauses = "• Systemic Infection\nViral (e.g., FIP, Canine Distemper)\nBacterial (e.g., Pyometra, Abscesses)",
        otherCauses = "• Immune-mediated\nImmune-mediated hemolytic anemia (IMHA)\n\n• Neoplastic conditions\nLymphoma",
        bibliographicReferences = "1. Greene's Infectious Diseases of the Dog and Cat.\n2. Textbook of Veterinary Internal Medicine."
    ),
    PhysicalSignDetail(
        id = "hyperemia",
        name = "Hyperemia",
        definition = "An excess of blood in the vessels supplying an organ or other part of the body, manifested as bright red mucous membranes or skin redness.",
        frequentCauses = "• Local Inflammation\nAllergic reactions\nLocal infections / trauma\n\n• Systemic conditions\nSepsis / Septic shock (hyperdynamic phase)",
        otherCauses = "• Heatstroke / Hyperthermia\nExtreme core temperature elevation\n\n• Toxicity\nCarbon monoxide inhalation",
        bibliographicReferences = "1. Textbook of Veterinary Internal Medicine, 8th Edition.\n2. Manual of Veterinary Skin Diseases."
    ),
    PhysicalSignDetail(
        id = "hypertension",
        name = "Hypertension, systemic",
        definition = "A sustained elevation of systemic arterial blood pressure, commonly diagnosed secondary to organ pathologies in older companion animals.",
        frequentCauses = "• Chronic Kidney Disease (CKD)\nGlomerular or tubulointerstitial diseases\n\n• Endocrine diseases\nHyperadrenocorticism (Cushing's)\nHyperthyroidism (especially cats)",
        otherCauses = "• Primary / Idiopathic hypertension\nDiagnosis of exclusion\n\n• Pheochromocytoma\nCatecholamine-secreting tumor",
        bibliographicReferences = "1. ACVIM Consensus Statement on Systemic Hypertension.\n2. Canine and Feline Nephrology and Urology."
    ),
    PhysicalSignDetail(
        id = "hypotension",
        name = "Hypotension, systemic",
        definition = "An abnormally low systemic arterial blood pressure, leading to compromised tissue perfusion and potential organ failure.",
        frequentCauses = "• Shock states\nHypovolemic (hemorrhage/dehydration)\nDistributive (sepsis, anaphylaxis)\n\n• Anesthetic agents\nOverdosage or cardiorespiratory depression",
        otherCauses = "• Cardiac arrest / Failure\nEnd-stage cardiomyopathy or valvular diseases",
        bibliographicReferences = "1. Small Animal Critical Care Medicine.\n2. Veterinary Emergency and Critical Care Manual."
    ),
    PhysicalSignDetail(
        id = "hypothermia",
        name = "Hypothermia",
        definition = "An abnormal decrease in core body temperature below normal ranges, causing metabolic depression and cardiac arrhythmias in severe cases.",
        frequentCauses = "• Environmental exposure\nCold climates / lack of shelter\n\n• Under anesthesia\nAnesthetic-induced thermal dysregulation",
        otherCauses = "• Endocrine disorders\nHypothyroidism (dogs)\nHypoadrenocorticism\n\n• Multi-organ Failure\nUremic crisis",
        bibliographicReferences = "1. Textbook of Veterinary Internal Medicine.\n2. Veterinary Emergency Medicine Guide."
    ),
    PhysicalSignDetail(
        id = "jaundice",
        name = "Jaundice / Icterus",
        definition = "Yellow discoloration of the sclera, mucous membranes, or skin resulting from hyperbilirubinemia.",
        frequentCauses = "• Pre-hepatic (Hemolysis)\nImmune-Mediated Hemolytic Anemia (IMHA)\nBlood parasites (e.g., Babesia)\n\n• Hepatic disease\nFeline Hepatic Lipidosis\nCholangiohepatitis\nToxic hepatopathy",
        otherCauses = "• Post-hepatic obstruction\nPancreatitis obstructing Common Bile Duct\nCholelithiasis / Bile duct carcinoma\nDuodenal foreign body",
        bibliographicReferences = "1. BSAVA Manual of Canine and Feline Gastroenterology.\n2. Textbook of Veterinary Internal Medicine."
    ),
    PhysicalSignDetail(
        id = "lymphadenopathy",
        name = "Lymphadenopathy",
        definition = "An enlargement of one or more lymph nodes, which can be localized or generalized, and painful or painless on palpation.",
        frequentCauses = "• Local Lymphadenitis\nLocalized infections (bacterial or fungal)\n\n• Generalized Lymphadenopathy\nSystemic infectious diseases (Leishmania, Ehrlichia)\nLymphosarcoma / Lymphoma",
        otherCauses = "• Metastatic neoplasia\nSpread from primary solid tumors\n\n• Immune-mediated triggers\nReactive hyperplasia",
        bibliographicReferences = "1. Small Animal Clinical Oncology, 6th Edition.\n2. Pathologic Basis of Veterinary Disease."
    ),
    PhysicalSignDetail(
        id = "pallor",
        name = "Pallor",
        definition = "An unusual paleness of the mucous membranes (e.g., gums), indicating decreased red blood cells or reduced peripheral blood flow.",
        frequentCauses = "• Anemia\nBlood loss (internal/external)\nHemolysis (IMHA)\nUnderproduction (CKD, Bone marrow failure)\n\n• Poor Perfusion\nHypovolemic or cardiogenic shock",
        otherCauses = "• Hypothermia\nSystemic peripheral vasoconstriction",
        bibliographicReferences = "1. Small Animal Internal Medicine.\n2. Manual of Veterinary Hematology."
    ),
    PhysicalSignDetail(
        id = "edema",
        name = "Peripheral edema",
        definition = "An accumulation of fluid in the interstitial space of peripheral tissues, primarily affecting limbs, ventrum, or submandibular regions.",
        frequentCauses = "• Hypoalbuminemia\nProtein-Losing Enteropathy (PLE)\nProtein-Losing Nephropathy (PLN)\nSevere hepatic insufficiency",
        otherCauses = "• Increased Hydrostatic Pressure\nRight-sided congestive heart failure\nLocal lymphatic or venous obstruction",
        bibliographicReferences = "1. Textbook of Veterinary Internal Medicine.\n2. Fluid, Electrolyte, and Acid-Base Disorders in Small Animal Practice."
    ),
    PhysicalSignDetail(
        id = "petechiae",
        name = "Petechiae / Ecchymoses",
        definition = "Pinpoint-sized (petechiae) or larger (ecchymoses) purpuric spots on mucous membranes or skin, indicating capillary hemorrhage.",
        frequentCauses = "• Thrombocytopenia\nImmune-Mediated Thrombocytopenia (IMTP)\nVector-borne diseases (Anaplasma, Ehrlichia)\nBone marrow suppression",
        otherCauses = "• Platelet Dysfunction (Thrombocytopathia)\nVon Willebrand disease\n\n• Vasculitis\nInfectious agents or drug reactions",
        bibliographicReferences = "1. Veterinary Hematology and Clinical Chemistry.\n2. Small Animal Emergency and Critical Care Medicine."
    ),
    PhysicalSignDetail(
        id = "pruritus",
        name = "Pruritus",
        definition = "An unpleasant sensation on the skin that provokes the urge to scratch, lick, bite, or rub, leading to secondary excoriations.",
        frequentCauses = "• Parasites\nFlea bite hypersensitivity\nSarcoptic mange (scabies)\nOtodectic mange\n\n• Allergic Skin Diseases\nAtopic dermatitis\nAdverse food reactions (food allergy)",
        otherCauses = "• Secondary Infections\nBacterial pyoderma\nMalassezia dermatitis",
        bibliographicReferences = "1. Muller and Kirk's Small Animal Dermatology.\n2. BSAVA Manual of Canine and Feline Dermatology."
    ),
    PhysicalSignDetail(
        id = "restlessness",
        name = "Restlessness",
        definition = "An inability to remain at rest, lie down quietly, or settle down, often caused by severe distress, pain, or systemic discomfort.",
        frequentCauses = "• Pain\nVisceral pain (Gastrointestinal distress/GDV)\nOrthopedic/Neurologic pain\n\n• Dyspnea\nHypoxia or laboring to breathe",
        otherCauses = "• Toxic ingestions\nMethylxanthines (chocolate, caffeine)\n\n• Neuromuscular triggers\nHypocalcemia (eclampsia)",
        bibliographicReferences = "1. BSAVA Manual of Canine and Feline Behavioral Medicine.\n2. Handbook of Veterinary Pain Management."
    ),
    PhysicalSignDetail(
        id = "shock",
        name = "Shock",
        definition = "A clinical state characterized by inadequate cellular energy production, typically due to low oxygen delivery or poor tissue perfusion.",
        frequentCauses = "• Hypovolemic Shock\nSevere blood loss / Dehydration\n\n• Cardiogenic Shock\nAdvanced dilated cardiomyopathy (DCM) or valve failure",
        otherCauses = "• Distributive / Septic Shock\nAnaphylactic reaction\nSevere systemic inflammatory response (SIRS)",
        bibliographicReferences = "1. Small Animal Critical Care Medicine.\n2. Veterinary Emergency and Critical Care Journal."
    ),
    PhysicalSignDetail(
        id = "voice_change",
        name = "Voice change",
        definition = "Changes in the sound, pitch, or amplitude of an animal's bark, meow, or whine, indicating laryngeal or vocal cord anomalies.",
        frequentCauses = "• Laryngeal Disease\nLaryngeal paralysis (e.g., GOLPP in older Labradors)\nDirect laryngitis (infectious or mechanical)",
        otherCauses = "• Nerve dysfunction or pressure\nThyroid carcinoma invading recurrent laryngeal nerve\n\n• Trauma\nTight collar neck injury",
        bibliographicReferences = "1. Textbook of Veterinary Internal Medicine.\n2. Small Animal Surgical Emergencies."
    ),
    PhysicalSignDetail(
        id = "obesity",
        name = "Weight gain / Obesity",
        definition = "The accumulation of excess body fat resulting in a state where body weight exceeds optimal ranges by more than 15-20%.",
        frequentCauses = "• Excess Caloric Intake\nOverfeeding, high-fat table scraps\nLack of regular exercise",
        otherCauses = "• Endocrine Disorders\nHypothyroidism (dogs)\nHyperadrenocorticism (Cushing's)",
        bibliographicReferences = "1. Small Animal Clinical Nutrition, 5th Edition.\n2. Canine and Feline Endocrinology."
    ),
    PhysicalSignDetail(
        id = "weight_loss",
        name = "Weight loss",
        definition = "An involuntary decrease in body weight over time, representing a chronic negative energy balance.",
        frequentCauses = "• Metabolic and Systemic Pathology\nDiabetes Mellitus\nChronic Kidney Disease (CKD)\nHyperthyroidism (cats)\nNeoplasia",
        otherCauses = "• Gastrointestinal diseases\nExocrine Pancreatic Insufficiency (EPI)\nInflammatory Bowel Disease (IBD)",
        bibliographicReferences = "1. Small Animal Gastroenterology.\n2. Textbook of Veterinary Internal Medicine."
    )
)
