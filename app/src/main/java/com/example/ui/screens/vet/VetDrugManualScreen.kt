package com.example.ui.screens.vet

import androidx.compose.animation.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DrugItem
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.staticDrugCatalog
import kotlinx.coroutines.launch

// High-fidelity clinical details data structure
data class DrugDetails(
    val contraindications: String, // منع مصرف بالینی
    val sideEffects: String,       // عوارض جانبی عمده
    val clinicalPearls: String     // نکات کلیدی بالینی
)

// Dynamic medical information provider for veterinary prescription safety
private fun getDrugDetails(drugId: String): DrugDetails {
    return when (drugId) {
        "1" -> DrugDetails(
            contraindications = "بیماران با سابقه گلوکوم (فشار داخل چشم بالا)، نارسایی‌های شدید قلبی و سابقه فشار خون حاد. در گربه‌های دیابتیک یا مبتلا به نارسایی حاد کلیه با احتیاط بسیار فراوان تجویز شود.",
            sideEffects = "افزایش جریان ترشحات دهانی و بزاق کاذب، لرزش متوسط تا شدید عضلانی، ریکاوری بالینی هیجانی در غیاب آرام‌بخش اولیه، نوسان ضربان قلب.",
            clinicalPearls = "ترکیب هم‌زمان یا پیش‌دارویی با زایلازین یا دیازپام جهت پیشگیری از سفتی گربه‌سانان و تسهیل فاز ریکاوری قویاً توصیه می‌گردد."
        )
        "2" -> DrugDetails(
            contraindications = "سه‌ماهه آخر بارداری دام (ریسک بالا در ایجاد زایمان زودرس و سقط)، انسداد فیزیکی مجاری تنفسی، بیماری‌های حاد دریچه میترال قلب.",
            sideEffects = "کاهش بارز ریتم قلبی (برادی‌کاردی شدید)، فلج موقت و نفخ شکمی در نشخوارکنندگان، واکنش استفراغ کوتاه‌مدت بلافاصله پس از تزریق در گربه‌ها.",
            clinicalPearls = "داروی یوهیمبین (Yohimbine) و یا آتی‌پامزول به عنوان پادزهر و آنتاگونیست اختصاصی جهت برگرداندن اثر سداتیو زایلازین کاربرد دارند."
        )
        "3" -> DrugDetails(
            contraindications = "نارسایی کبدی پیشرفته. سرعت تزریق بسیار سریع وریدی ممکن است منجر به دپرسیون حاد تنفسی یا ترومبوفلبیت موضعی شود.",
            sideEffects = "کاهش خفیف عمق تنفس، آتاکسی موقت حرکتی (عدم تعادل عضلانی) در دقایق اولیه پس از تجویز ثانویه.",
            clinicalPearls = "دیازپام انتخاب اول درمان کلینیکی اورژانس در کنترل و خاتمه‌بخشی به تشنج‌های صرعی فعال (Status Epilepticus) در دام کوچک است."
        )
        "4" -> DrugDetails(
            contraindications = "شوک شدید سیستمیک هیپوولمیک (کم‌آبی حاد عروقی)، نارسایی قلبی احتقانی و جبران‌نشده.",
            sideEffects = "دپرسیون تنفسی موقت (آپنه گذرا) در صورت تزریق شتاب‌زده، کاهش فشار خون شریانی همراه با شل شدن کامل عضلات ارادی و اسفنکترها.",
            clinicalPearls = "تزریق حتماً باید به روش وریدی بسیار آهسته (Slow IV over 60 seconds) انجام شود تا از ایست تنفسی ناگهانی ممانعت به عمل آید."
        )
        "5" -> DrugDetails(
            contraindications = "حساسیت شناخته‌شده به آنتی‌بیوتیک‌های خانواده پنی‌سیلین و بتالاکتام‌ها. تجویز خوراکی این دارو در جوندگان کوچک (خرگوش، خوکچه هندی و همستر) به علت تخریب فلور روده کشنده است.",
            sideEffects = "اختلال گوارشی موقت، اسهال ملایم ناشی از تغییر فلور میکروبی، واکنش‌های آلرژیک پوستی و کهیر پوستی گذرا.",
            clinicalPearls = "فرمولاسیون طولانی‌اثر (LA) این آنتی‌بیوتیک، سطح درمانی فعال دارو را تا ۴۸ ساعت مداوم در جریان خون دام به حد مطلوب نگه می‌دارد."
        )
        "6" -> DrugDetails(
            contraindications = "بیماران دارای پیشینه آلرژی شدید به سفالوسپورین‌ها. تداخل شدید دارویی با داروهای آمینوگلیکوزید نفروتوکسیک.",
            sideEffects = "احساس سوزش و درد موضعی در محل تزریق عضلانی، واکنش‌های خفیف تا متوسط دستگاه گوارش نظیر حالت تهوع ملایم.",
            clinicalPearls = "به دلیل قابلیت عبور فوق‌العاده از سد خونی-مغزی (BBB)، سفتریاکسون داروی کلیدی ممتاز در درمان عفونت‌های مننژیت مغزی می‌باشد."
        )
        "7" -> DrugDetails(
            contraindications = "توله سگ‌های در حال رشد (زیر ۸ تا ۱۲ ماه) به دلیل آسیب مستقیم به غضروف مفصلی. گربه‌های با دوز بالا به علت ریسک مسمومیت شبکیه چشم و نابینایی.",
            sideEffects = "تخریب صفحات رشد و غضروف مفصلی در حیوانات در حال رشد سریع، اختلالات گوارشی گذرا شامل بی‌اشتهایی و استفراغ فردی.",
            clinicalPearls = "این دارو از آنتی‌بیوتیک‌های خط اول بسیار قدرتمند در کنترل تفصیلی عفونت‌های عمیق مجاری ادراری، غده پروستات و سیستم تنفسی است."
        )
        "8" -> DrugDetails(
            contraindications = "بیماری‌های حاد نارسایی کلیوی، دهیدراتاسیون شدید بدن حیوان بیمار، استفاده هم‌زمان با سایر داروهای سمی برای بافت کلیه.",
            sideEffects = "سمیت شدید کلیوی در اثر انباشت دارو (Nephrotoxicity) و آسیب دائم به عصب دهلیزی-حلزونی شنوایی در گوش داخلی (Ototoxicity).",
            clinicalPearls = "هیدراتاسیون یا آبرسانی دقیق بدن بیمار و پایش غلظت کراتینین خون در طول دوره درمان با آمینوگلیکوزیدها الزامی و حیاتی است."
        )
        "9" -> DrugDetails(
            contraindications = "بیماری‌های حاد و پیشرفته بافت کبد (هپاتوپاتی شدید)، نارسایی احتقانی قلب سگ‌سانان.",
            sideEffects = "کاهش اشتهای ملایم، تهوع گذرا، افزایش غلظت آنزیم‌های جگر (ALT , AST) وابسته به مقدار دوزاژ مصرفی.",
            clinicalPearls = "تجویز خوراکی ایتراکونازول در حضور وعده غذایی حاوی چربی به مقدار چشمگیری فراهمی زیستی و جذب روده را بالا می‌برد."
        )
        "10" -> DrugDetails(
            contraindications = "اختلال شدید عملکرد کبد، سگ‌های مبتلا به نارسایی غده فوق کلیه (Hypoadrenocorticism).",
            sideEffects = "ریزش موی موضعی، خارش، تهوع، مهار موقت ساخت هورمون تستوسترون و کورتیزول بدن دام.",
            clinicalPearls = "به عنوان یک مهارکننده قوی آنزیم سیتوکروم P450 کبد، کوفاکتورهای دارویی هم‌زمان باید با دوز تعدیل‌شده تجویز شوند."
        )
        "11" -> DrugDetails(
            contraindications = "سگ‌های حساس نژاد کالی (Collies)، شتلند، ژرمن شپرد با جهش ژنتیکی مستند در ژن MDR1 به دلیل مسمومیت و مرگ مغزی حاد.",
            sideEffects = "گشاد شدن شدید مردمک چشم (میدریاز)، عدم تعادل حرکتی شدید، تشنج‌های منقطع مغزی در صورت مصرف دوزهای غیراستاندارد بالینی.",
            clinicalPearls = "دوزهای پیشگیری‌کننده کرم قلب بسیار ناچیز و ایمن است، اما دوزهای درمانی جرب پوستی (Demodex) در نژاد کالی کاملاً ممنوع است."
        )
        "12" -> DrugDetails(
            contraindications = "سه‌ماهه اول بارداری کلیه گونه‌های دام، اختلالات پویای عصبی و صرع فعال.",
            sideEffects = "عوارض شدید اعصاب مرکزی نظیر نیستاگموس (لرزش افقی چشم)، بی‌آشتهایی مطلق، و احساس طعم نامطبوع فلزی در دهان.",
            clinicalPearls = "در صورت مشاهده کوچک‌ترین آتاکسی، سرگیجه یا چرخش سر دام، مصرف دارو را فوراً قطع کرده و درمان حمایتی با دیازپام را شروع کنید."
        )
        "13" -> DrugDetails(
            contraindications = "عوارض منع مصرف قطعی ثبت نشده است. این دارو از حیث دارویی دارای حاشیه ایمنی بسیار بالایی در کلیه پستانداران اهلی حیوانات خانگی است.",
            sideEffects = "کمی تهوع گوارشی، بی‌اشتهایی موقت و ملایم پس از اولین دوز مصرفی.",
            clinicalPearls = "دوره استاندارد پاکسازی انگل‌های داخلی (کرم‌های لوله‌ای) معمولاً در ۳ روز متوالی با دوز ثابت روزانه انجام می‌پذیرد."
        )
        "14" -> DrugDetails(
            contraindications = "شوک شدید قلبی عروقی، دهیدراتاسیون، سگ‌های نژاد باکسر (Boxer) به علت حساسیت ژنتیکی بیولوژیک مفرط به سنکوپ و ایست قلبی.",
            sideEffects = "کاهش ناگهانی و شدید فشار خون شریانی، رخوت عمیق عضلانی، بیرون‌زدگی یا پرولاپس پلک سوم چشم دام به صورت موقت.",
            clinicalPearls = "آسپرومایزین صرفاً آرام‌بخش حرکتی است و به تنهایی فاقد هرگونه خواص ضد دردی (Analgesic) جهت جراحی‌ها می‌باشد."
        )
        "15" -> DrugDetails(
            contraindications = "نارسایی کبدی حاد، آسیب‌های مغزی ریوی ناشی از خفگی یا انسداد تنفسی متوسط.",
            sideEffects = "پلی‌یوریا و پلی‌دیپسیا (پرخوری و پرنوشی مفرط)، رخوت موقتی حیوان در دو هفته ابتدایی شروع درمان کنترل تشنج صرع.",
            clinicalPearls = "پایش سیستماتیک سطح سرمی فنوپاربیتال خون هر ۶ ماه یکبار جهت تنظیم دقیق دوز ضد تشنج در درمان‌های صرعی الزامی است."
        )
        "16" -> DrugDetails(
            contraindications = "شوک غیرقلبی، نارسایی بی‌آوری شدید آب بدن، آنوری (قطع خروجی ادرار به علت خرابی کامل نفرون‌های کلیه).",
            sideEffects = "کاهش پوتاسیوم خون (هیپوکالمی)، دهیدراتاسیون شدید، افزایش ثانویه غلظت اوره و کراتینین خون دام در اثر کم‌آبی.",
            clinicalPearls = "فوروزماید قوی‌ترین دیورتیک برای کاهش ادم حاد ریوی گربه‌ها و سگ‌ها در نارسایی احتقانی قلب شمرده می‌شود."
        )
        "17" -> DrugDetails(
            contraindications = "کاردیومیوپاتی هیپرتروفیک (HCM) در گربه‌ها، انسداد خروجی جریان بطن چپ و تنگی آئورت قلب.",
            sideEffects = "افزایش ملایم نرخ ضربان قلب، اسهال دوره‌ای بسیار سبک و گذرا در موارد حاد مصرف بالینی.",
            clinicalPearls = "پیموبندان با افزایش قدرت انقباض ماهیچه قلب و گشاد کردن عروق، کارآمدی چشمگیری در بهبود نارسایی احتقانی قلب (CHF) دارد."
        )
        "18" -> DrugDetails(
            contraindications = "سابقه آلرژی و پاسخ آنافیلاکتیک به کمپلکس ویتامین‌های خانواده ب.",
            sideEffects = "احساس سوزش و ناآرامی موضعی و کوتاه پس از تزریق عضلانی عمیق دارو.",
            clinicalPearls = "تزریق به صورت زیرجلدی آهسته (SQ) جهت کاهش اثر سوزش موضعی در کلینیک توصیه می‌گردد."
        )
        else -> DrugDetails(
            contraindications = "در دام‌های مبتلا به افت شدید عملکردهای فیلتراسیون کلیه و تصفیه جگر با احتیاط و ارزیابی دقیق دوزاژ مصرف شود.",
            sideEffects = "اختلالات گذرا و ملایم در دستگاه گوارش حیوان یا التهاب جزئی و برگشت‌پذیر در موضع تزریق دارو.",
            clinicalPearls = "اندازه‌گیری دقیق دوز تجویزی صرفاً بر حسب وزن بدن (کیلوگرم) و ویژگی‌های اختصاصی هر بیمار انجام شود."
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VetDrugManualScreen(viewModel: MainViewModel) {
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()
    val customCreatedDrugs by viewModel.customDrugs.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("همه داروها") }
    var isCategoriesExpanded by remember { mutableStateOf(false) }

    // Custom Drug Add Form state
    var showAddDrugForm by remember { mutableStateOf(false) }
    var newGeneric by remember { mutableStateOf("") }
    var newScientific by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("داروهای ضدعفونت (ضدمیکروبی)") }
    var newConcentrationVal by remember { mutableStateOf("") }
    var newConcentrationTxt by remember { mutableStateOf("") }
    var newRangeMin by remember { mutableStateOf("") }
    var newRangeMax by remember { mutableStateOf("") }
    var newRoute by remember { mutableStateOf("PO") }
    var newDefaultDosage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Combine static catalog and custom created drugs
    val fullCatalog = staticDrugCatalog + customCreatedDrugs

    // Premium categories with custom medical icons and emojis
    val categoriesWithIcons = listOf(
        Pair("همه داروها", "🩺"),
        Pair("داروهای بیهوشی، ضددرد و ضدالتهاب (NSAIDs)", "💤"),
        Pair("داروهای ضدعفونت (ضدمیکروبی)", "🦠"),
        Pair("داروهای ضدسرطان (ضدنئوپلاستیک)", "🧬"),
        Pair("اصلاح‌کننده‌های رفتار", "🧠"),
        Pair("خون و سیستم ایمنی", "🩸"),
        Pair("قلبی و عروقی", "❤️"),
        Pair("پوست و مو (درماتولوژیک)", "🩹"),
        Pair("گوارش و کبد", "🧪"),
        Pair("دستگاه تناسلی و ادراری", "💦"),
        Pair("متابولیک و غدد درون‌ریز", "🔥"),
        Pair("سیستم عصبی و عضلانی", "⚡"),
        Pair("مکمل‌های تغذیه‌ای و مایع‌درمانی", "🥤"),
        Pair("داروهای چشم‌پزشکی (چشمی)", "👁️"),
        Pair("سیستم تنفسی", "🫁")
    )
    val categoriesList = categoriesWithIcons.map { it.first }

    // Filtered drugs, sorted alphabetically by Persian generic name (nameGeneric)
    val filteredDrugs = fullCatalog.filter { drug ->
        val matchesCategory = selectedCategory == "همه داروها" || drug.category == selectedCategory
        val matchesSearch = drug.nameGeneric.contains(searchQuery, ignoreCase = true) ||
                drug.nameScientific.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }.sortedBy { it.nameGeneric }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Patient Summary Banner at Top
            activeExaminedPet?.let { pet ->
                CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🐾 محاسبات دارویی برای: ${pet.name}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "گونه: ${pet.species} | نژاد: ${pet.breed} | وزن بیمار: ${pet.weight} کیلوگرم",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )
                            }
                        }
                    }
                }
            } ?: run {
                CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "⚠️ بیمار فعالی انتخاب نشده است",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "محاسبات دارویی بر مبنای وزن پیش‌فرض ۱ کیلوگرم انجام می‌شود. لطفاً برای محاسبه دقیق ابتدا در تب داشبورد پرونده مراجعه‌کننده را فعال کنید.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }

            // Quick search bar
            CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("جستجوی سریع نام یا عنوان علمی دارو") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories Area
            CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "دسته‌بندی‌های دارویی:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Elegant Expand / Collapse Toggle Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .clickable { isCategoriesExpanded = !isCategoriesExpanded }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCategoriesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "تغییر نمای دسته‌بندی",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isCategoriesExpanded) "نمای فشرده (افقی)" else "مشاهده همه دسته‌ها (۱۵)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // 1. COMPACT SCROLLABLE ROW (Default / Non-expanded view)
                    AnimatedVisibility(
                        visible = !isCategoriesExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(categoriesWithIcons) { (catName, emoji) ->
                                    val isChosen = selectedCategory == catName
                                    val bgCol = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    val textCol = if (isChosen) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    val borderCol = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(bgCol)
                                            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
                                            .clickable { selectedCategory = catName }
                                            .defaultMinSize(minHeight = 40.dp)
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(emoji, fontSize = 14.sp)
                                            Text(
                                                text = catName,
                                                fontSize = 11.sp,
                                                color = textCol,
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick add short chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                    .clickable { showAddDrugForm = !showAddDrugForm }
                                    .defaultMinSize(minHeight = 40.dp)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("➕", fontSize = 11.sp)
                                    Text(
                                        text = "داروی جدید",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // 2. EXPANDED PREMIUM USER-FRIENDLY SELECTION GRID (Prevention of selection errors)
                    AnimatedVisibility(
                        visible = isCategoriesExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "جهت فیلتر دقیق‌تر و جلوگیری از خطا، گروه مورد نظر را انتخاب کنید:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            val rows = categoriesWithIcons.chunked(2)
                            rows.forEach { rowPairs ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowPairs.forEach { (catName, emoji) ->
                                        val isChosen = selectedCategory == catName
                                        val bgCol = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                        val borderCol = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        val textCol = if (isChosen) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(bgCol)
                                                .border(1.2.dp, borderCol, RoundedCornerShape(12.dp))
                                                .clickable { selectedCategory = catName }
                                                .defaultMinSize(minHeight = 48.dp)
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(
                                                            if (isChosen) Color.White.copy(alpha = 0.2f)
                                                            else MaterialTheme.colorScheme.surfaceVariant,
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(emoji, fontSize = 13.sp)
                                                }

                                                Text(
                                                    text = catName,
                                                    fontSize = 11.sp,
                                                    color = textCol,
                                                    fontWeight = if (isChosen) FontWeight.ExtraBold else FontWeight.Medium,
                                                    lineHeight = 14.sp,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                if (isChosen) {
                                                    Text(
                                                        text = "✓",
                                                        fontSize = 14.sp,
                                                        color = textCol,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Placeholder if odd number
                                    if (rowPairs.size == 1) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Large interactive Add Custom Drug Button
                            Button(
                                onClick = { showAddDrugForm = !showAddDrugForm },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("➕", fontSize = 12.sp)
                                    Text(
                                        text = "افزودن داروی جدید به دارونامه +",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Add Custom Drug Form
            AnimatedVisibility(visible = showAddDrugForm) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("➕ ثبت داروی جدید در نسخه دمو", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))

                        CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
                            OutlinedTextField(
                                value = newGeneric,
                                onValueChange = { newGeneric = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("نام ژنریک (فارسی)") },
                                placeholder = { Text("مثال: سفالکسین") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newScientific,
                                onValueChange = { newScientific = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("عنوان علمی (لاتین)") },
                                placeholder = { Text("مثال: Cephalexin") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Category Select
                            Text("دسته‌بندی دارو:", fontSize = 11.sp)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                categoriesList.filter { it != "همه داروها" }.forEach { name ->
                                    val isSel = newCategory == name
                                    val col = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(col)
                                            .clickable { newCategory = name }
                                            .defaultMinSize(minHeight = 48.dp)
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(name, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newConcentrationVal,
                                    onValueChange = { newConcentrationVal = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("غلظت (mg/ml)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newConcentrationTxt,
                                    onValueChange = { newConcentrationTxt = it },
                                    modifier = Modifier.weight(1.5f),
                                    label = { Text("واحد موجود") },
                                    placeholder = { Text("مثال: 50 mg/ml") },
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newRangeMin,
                                    onValueChange = { newRangeMin = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("حداقل دوز (mg/kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newRangeMax,
                                    onValueChange = { newRangeMax = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("حداکثر دوز") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newRoute,
                                    onValueChange = { newRoute = it },
                                    modifier = Modifier.weight(1.2f),
                                    label = { Text("روش تجویز") },
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newDefaultDosage,
                                onValueChange = { newDefaultDosage = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("دوز پیش‌فرض میانگین (mg/kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val concVal = newConcentrationVal.toDoubleOrNull() ?: 1.0
                                    val minDos = newRangeMin.toDoubleOrNull() ?: 0.0
                                    val maxDos = newRangeMax.toDoubleOrNull() ?: 0.0
                                    val defDos = newDefaultDosage.toDoubleOrNull() ?: minDos

                                    if (newGeneric.isEmpty() || newConcentrationTxt.isEmpty()) {
                                        return@Button
                                    }

                                    viewModel.addCustomDrug(
                                        nameGeneric = newGeneric,
                                        nameScientific = newScientific.ifEmpty { "Scientific Name" },
                                        category = newCategory,
                                        concentrationValue = concVal,
                                        concentrationText = newConcentrationTxt,
                                        rangeMin = minDos,
                                        rangeMax = maxDos,
                                        route = newRoute,
                                        defaultDosage = defDos
                                    )

                                    // Clear form
                                    newGeneric = ""
                                    newScientific = ""
                                    newConcentrationVal = ""
                                    newConcentrationTxt = ""
                                    newRangeMin = ""
                                    newRangeMax = ""
                                    newDefaultDosage = ""
                                    showAddDrugForm = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ثبت و درج در فارماکوپه")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Drug Table & Dosage Calculation Calculator
            Text(
                text = "دارونامه و تجویز هوشمند:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Right
            )

            if (filteredDrugs.isEmpty()) {
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
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💊", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "دارویی یافت نشد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "هیچ داروی پیش‌فرض یا دست‌سازی با جستجوی شما مطابقت ندارد. می‌توانید غلظت و دوزهای اختصاصی را در قالب فرمول جدید به سیستم اضافه کنید.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                showAddDrugForm = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("افزودن فرمول داروی جدید", fontSize = 11.sp)
                        }
                    }
                }
            } else {
                filteredDrugs.forEach { drug ->
                    // State trackers per-drug
                    var dosageCoeff by remember(drug.id) { mutableStateOf(drug.defaultDosage) }
                    val weightOfPet = activeExaminedPet?.weight ?: 1.0

                    // Dynamic open-expander state tracker for each drug card's contraindications/warnings panel
                    var isGuideExpanded by remember { mutableStateOf(false) }

                    // Auto Calculations
                    val calculatedDose = weightOfPet * dosageCoeff
                    val calculatedVolume = if (drug.concentrationVal > 0) calculatedDose / drug.concentrationVal else 0.0

                    // Left-to-Right layout direction for drug details in English format as per image
                    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            // 1. Header Row (Clickable Area for English + Persian names, concentration, and expander)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isGuideExpanded = !isGuideExpanded }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit or Click for Clinical Details",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(10.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = getEnglishName(drug.nameGeneric),
                                        fontWeight = FontWeight.ExtraBold, // FONT-EXTRABOLD: Maximum focal weight
                                        fontSize = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "ژنریک: ${getPersianName(drug.nameGeneric)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) // Distinctive Persian Theme
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = drug.concentrationText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand Clinical Advice",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(if (isGuideExpanded) 180f else 0f)
                                )
                            }

                            // 1.1 Clinical Guidance & Contraindications (Dynamic Dropdown Sheet)
                            AnimatedVisibility(
                                visible = isGuideExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                val details = getDrugDetails(drug.id)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp, top = 8.dp, bottom = 12.dp)
                                        .border(
                                            width = 1.3.dp,
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.45f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // 1.1.1 Farsi Section: Title (Right-to-Left, Right-aligned)
                                        CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Start
                                            ) {
                                                Text("⚠️", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "راهنمای عوارض و منع مصرف تخصصی دامپزشکی",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        // 1.1.2 English Section: Scientific Formulation & Name (Left-to-Right, Left-aligned)
                                        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Scientific Formula:",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                Text(
                                                    text = drug.nameScientific,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.Start
                                                )
                                            }
                                        }

                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        )

                                        // 1.1.3 Farsi Section: Contraindications, Side Effects, Pearls (Right-to-Left, Right-aligned)
                                        CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Contraindications (منع مصرف)
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = "🚫 موارد منع مصرف بالینی قطعی:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFC2410C) // Tailwind Orange 700
                                                    )
                                                    Text(
                                                        text = details.contraindications,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                                                        textAlign = TextAlign.Right
                                                    )
                                                }

                                                // Side Effects (عوارض جانبی عمده)
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = "🚨 عوارض جانبی گزارش شده:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFB91C1C) // Tailwind Red 700
                                                    )
                                                    Text(
                                                        text = details.sideEffects,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                                                        textAlign = TextAlign.Right
                                                    )
                                                }

                                                // Clinical Pearl (نکات کلینییکال)
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = "💡 نکته مهارتی و کلیدی دامپزشک:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF0369A1) // Tailwind Sky 700
                                                    )
                                                    Text(
                                                        text = details.clinicalPearls,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                                                        textAlign = TextAlign.Right
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Intelligence Color-Coded Route & Range Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, top = 2.dp, bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = "بازه دوز مرجع: ${drug.rangeAndRoute.substringBeforeLast(" ")}",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                RouteBadges(routeStr = drug.route)
                            }

                            // 3. Three-column input/calculations row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Dosage Column
                                Column(
                                    modifier = Modifier.width(130.dp)
                                ) {
                                    Text(
                                        text = if (drug.rangeAndRoute.contains("mcg", ignoreCase = true)) "Dosage (mcg)" else "Dosage",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    var dosageTextInput by remember(drug.id) { mutableStateOf(dosageCoeff.toString()) }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(38.dp)
                                            .background(
                                                color = if (MaterialTheme.colorScheme.surface == Color.White) Color.White else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        androidx.compose.foundation.text.BasicTextField(
                                            value = dosageTextInput,
                                            onValueChange = { newValue ->
                                                val sanitized = newValue.filter { it.isDigit() || it == '.' }
                                                dosageTextInput = sanitized
                                                val parsed = sanitized.toDoubleOrNull()
                                                if (parsed != null) {
                                                    dosageCoeff = parsed
                                                }
                                            },
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            singleLine = true,
                                            maxLines = 1,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                // Dose Column
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "Dose",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFE2EDFD))
                                            .padding(horizontal = 14.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (drug.rangeAndRoute.contains("mcg", ignoreCase = true)) {
                                                "${String.format("%.1f", calculatedDose)} mcg"
                                            } else {
                                                "${String.format("%.2f", calculatedDose)} mg"
                                            },
                                            color = Color(0xFF1E56B6),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Volume Column
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "Volume",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFE2F7EB))
                                            .padding(horizontal = 14.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${String.format("%.2f", calculatedVolume)} ml",
                                            color = Color(0xFF137333),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // 4. Action Row (Save Prescription Button)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, top = 8.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.savePrescription(
                                            drug = drug,
                                            dosageVal = dosageCoeff,
                                            calculatedDose = calculatedDose,
                                            calculatedVolume = calculatedVolume
                                        )
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("داروی ${drug.nameGeneric} به نسخه بیمار اضافه شد!")
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "افزودن به نسخه",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Subcomponent: Auto-Color Coded Route of Administration Badges
@Composable
private fun RouteBadges(routeStr: String) {
    // Splitting routes by slash, comma, or spaces
    val routes = routeStr.split(Regex("[/,\\s]+")).map { it.trim() }.filter { it.isNotEmpty() }
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        routes.forEach { r ->
            val cleanR = r.uppercase()
            val (bg, fg, label) = when {
                cleanR.contains("IV") -> Triple(
                    Color(0xFFE0F2FE), // Blue/Sky background
                    Color(0xFF0284C7), // Blue/Sky foreground
                    "IV (وریدی)"
                )
                cleanR.contains("IM") -> Triple(
                    Color(0xFFF3E8FF), // Purple background
                    Color(0xFF7C3AED), // Purple foreground
                    "IM (عضلانی)"
                )
                cleanR.contains("SC") || cleanR.contains("SQ") -> Triple(
                    Color(0xFFD1FAE5), // Mint/Emerald background
                    Color(0xFF059669), // Mint/Emerald foreground
                    "SC (زیرجلدی)"
                )
                cleanR.contains("PO") -> Triple(
                    Color(0xFFFEF3C7), // Amber background
                    Color(0xFFD97706), // Amber foreground
                    "PO (خوراکی)"
                )
                else -> Triple(
                    Color(0xFFF3F4F6), 
                    Color(0xFF374151), 
                    r
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg)
                    .border(0.5.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = fg,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getEnglishName(name: String): String {
    val regex = Regex("\\(([^)]+)\\)")
    val match = regex.find(name)
    val extracted = match?.groupValues?.get(1) ?: name
    return extracted.trim()
}

private fun getPersianName(name: String): String {
    val index = name.indexOf('(')
    return if (index != -1) {
        name.substring(0, index).trim()
    } else {
        name
    }
}
