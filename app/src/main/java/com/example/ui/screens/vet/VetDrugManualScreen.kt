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
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.database.DrugItem
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.staticDrugCatalog
import kotlinx.coroutines.launch

// High-fidelity clinical details data structure
data class DrugDetails(
    val contraindications: String, // منع مصرف بالینی
    val sideEffects: String,       // عوارض جانبی عمده
    val clinicalPearls: String     // نکات کلیدی بالینی
)

private fun getLocalizedCategory(category: String, lang: String): String {
    if (lang == "en") return category
    return when (category) {
        "All drugs" -> "همه داروها"
        "Anaesthetic analgesics and NSAIDs" -> "مسکن‌های بیهوشی و NSAIDها"
        "Anti-infectives" -> "ضد عفونت‌ها"
        "Anti-neoplastic" -> "ضد نئوپلاسم / ضد سرطان"
        "Behaviour modifiers" -> "اصلاح‌کننده‌های رفتار"
        "Blood and immune system" -> "خون و سیستم ایمنی"
        "Cardiovascular" -> "قلبی عروقی"
        "Dermatological" -> "پوستی"
        "Gastrointestinal and hepatic" -> "گوارشی و کبدی"
        "Genito-urinary tract" -> "مجاری ادراری تناسلی"
        "Metabolic" -> "متابولیک (سوخت و ساز)"
        "Neuromuscular system" -> "سیستم عصبی عضلانی"
        "Nutritional/fluids" -> "تغذیه و مایعات"
        "Ophthalmic" -> "چشمی"
        "Respiratory system" -> "سیستم تنفسی"
        else -> category
    }
}

private fun getLocalizedRouteRange(range: String, lang: String): String {
    if (lang == "en") return range
    return range
        .replace("mg/kg", "میلی‌گرم/کیلوگرم")
        .replace("mcg/kg", "میکروگرم/کیلوگرم")
        .replace("iv", "وریدی (IV)")
        .replace("im", "عضلانی (IM)")
        .replace("sc", "زیرپوستی (SC)")
        .replace("po", "خوراکی (PO)")
        .replace("or", "یا")
        .replace("to", "تا")
}

// Dynamic medical information provider for veterinary prescription safety
private fun getDrugDetails(drugId: String, lang: String): DrugDetails {
    return when (drugId) {
        "1" -> if (lang == "en") DrugDetails(
            contraindications = "Hypertrophic cardiomyopathy (HCM) in felines, severe heart failure, uncontrolled hypertension. Use with extreme caution in diabetic pets or patients with acute renal insufficiency.",
            sideEffects = "Sialorrhea (hypersalivation), moderate to severe muscle tremors, rough emergence delirium in the absence of proper pre-anesthetic sedation, fluctuating heart rate.",
            clinicalPearls = "Co-administration or pre-medication with diazepam or xylazine is highly recommended to prevent muscle rigidity and facilitate a smooth recovery phase."
        ) else DrugDetails(
            contraindications = "کاردیومیوپاتی هیپرتروفیک (HCM) در گربه‌سانان، نارسایی شدید قلبی، فشار خون کنترل‌نشده. با احتیاط فراوان در حیوانات مبتلا به دیابت یا نارسایی حاد کلیوی استفاده شود.",
            sideEffects = "ترشح بیش از حد بزاق (سیالوره)، لرزش خفیف تا شدید عضلانی، هذیان و بیداری ناآرام در صورت عدم استفاده از پیش‌بیهوشی مناسب، نوسان ضربان قلب.",
            clinicalPearls = "تجویز هم‌زمان یا پیش‌دارو با دیازپام یا زایلازین برای جلوگیری از سفتی عضلانی و کمک به ریکاوری آرام و بدون مشکل به شدت توصیه می‌شود."
        )
        "2" -> if (lang == "en") DrugDetails(
            contraindications = "Last trimester of pregnancy (high risk of premature labor or abortion), upper airway obstruction, severe mitral valve disease.",
            sideEffects = "Severe bradycardia, transient rumen stasis/bloat in ruminants, early emesis immediately following injection in felines.",
            clinicalPearls = "Yohimbine or atipamezole serve as specific antagonists to quickly reverse the sedative and physiological effects of xylazine."
        ) else DrugDetails(
            contraindications = "سه ماهه آخر بارداری (خطر بالای زایمان زودرس یا سقط جنین)، انسداد مجاری تنفسی فوقانی، بیماری شدید دریچه میترال.",
            sideEffects = "برادی‌کاردی (کاهش ضربان قلب) شدید، ایست موقت شکمبه/نفخ در نشخوارکنندگان، استفراغ زودهنگام بلافاصله بعد از تزریق در گربه‌ها.",
            clinicalPearls = "یوهیمبین یا آتی‌پامزول به عنوان آنتاگونیست‌های اختصاصی عمل می‌کنند تا اثرات آرام‌بخش و فیزیولوژیک زایلازین را به سرعت معکوس کنند."
        )
        "3" -> if (lang == "en") DrugDetails(
            contraindications = "Advanced hepatic insufficiency. Rapid IV bolus may trigger acute respiratory depression or local thrombophlebitis.",
            sideEffects = "Mild respiratory depression, transient muscle ataxia in the initial minutes following intravenous administration.",
            clinicalPearls = "Diazepam is the primary emergency drug of choice for controlling active epileptic seizures (Status Epilepticus) in veterinary medicine."
        ) else DrugDetails(
            contraindications = "نارسایی پیشرفته کبدی. تزریق سریع داخل وریدی (بولوس) ممکن است موجب ضعف حاد تنفسی یا ترومبوفلبیت موضعی شود.",
            sideEffects = "تضعیف خفیف تنفس، آتاکسی (ناهماهنگی حرکتی) موقت عضلانی در دقایق اولیه پس از تزریق داخل وریدی.",
            clinicalPearls = "دیازپام اولین داروی انتخابی و اورژانسی جهت کنترل حملات صرع فعال (استاتوس اپیلپتیکوس) در طب دامپزشکی است."
        )
        "4" -> if (lang == "en") DrugDetails(
            contraindications = "Severe hypovolemic shock, uncompensated congestive heart failure, extreme systemic dehydration.",
            sideEffects = "Transient respiratory apnea if injected too rapidly, arterial hypotension accompanied by complete skeletal muscle relaxation.",
            clinicalPearls = "Must be administered via slow IV route (over 60 seconds) to prevent sudden apnea or acute respiratory arrest."
        ) else DrugDetails(
            contraindications = "شوک کاهش حجم خون (هیپوولمیک) شدید، نارسایی احتقانی قلب جبران‌نشده، کم‌آبی شدید سیستمیک بدن.",
            sideEffects = "آپنه (قطع موقت تنفس) گذرا در صورت تزریق بیش از حد سریع، افت فشار خون شریانی همراه با شل شدن کامل عضلات اسکلتی.",
            clinicalPearls = "باید از مسیر وریدی بسیار آرام (بیش از ۶۰ ثانیه) تجویز شود تا از بروز آپنه ناگهانی یا ایست حاد تنفسی جلوگیری گردد."
        )
        "5" -> if (lang == "en") DrugDetails(
            contraindications = "Known hypersensitivity to penicillins or beta-lactams. Oral administration is fatal to small hindgut fermenting rodents (rabbits, guinea pigs, hamsters).",
            sideEffects = "Transient GI upset, companion microflora diarrhea, skin rashes, and mild hypersensitivity reactions.",
            clinicalPearls = "Long-acting (LA) formulations maintain active therapeutic blood and tissue levels for up to 48 consecutive hours."
        ) else DrugDetails(
            contraindications = "حساسیت شناخته‌شده به پنی‌سیلین‌ها یا بتالاکتام‌ها. تجویز خوراکی برای جوندگان کوچک با تخمیر سکومی (مانند خرگوش، خوکچه هندی و همستر) کشنده است.",
            sideEffects = "ناراحتی‌های گوارشی گذرا، اسهال ناشی از تغییر فلور میکروبی روده، بثورات پوستی و واکنش‌های حساسیت خفیف.",
            clinicalPearls = "فرمولاسیون‌های طولانی‌اثر (LA) غلظت درمانی فعال خو‌ن و بافت را تا ۴۸ ساعت مداوم حفظ می‌کنند."
        )
        "6" -> if (lang == "en") DrugDetails(
            contraindications = "Known hypersensitivity or allergy to cephalosporins. Significant risk of nephrotoxicity when co-administered with aminoglycosides.",
            sideEffects = "Temporary localized pain or sting at deep IM injection sites, mild gastrointestinal distress.",
            clinicalPearls = "Due to exceptional blood-brain barrier (BBB) penetration, ceftriaxone is excellent for treating bacterial meningitis."
        ) else DrugDetails(
            contraindications = "حساسیت شناخته‌شده یا آلرژی به سفالوسپورین‌ها. ریسک قابل توجه مسمومیت کلیوی (نفروتوکسیسیته) در صورت تجویز همزمان با آمینوگلیکوزیدها.",
            sideEffects = "درد یا سوزش موقت موضعی در محل تزریق‌های عضلانی عمیق، ناراحتی خفیف گوارشی.",
            clinicalPearls = "به دلیل نفوذ فوق‌العاده از سد خونی-مغزی (BBB)، سفتریاکسون برای درمان مننژیت باکتریایی گزینه‌ای عالی است."
        )
        "7" -> if (lang == "en") DrugDetails(
            contraindications = "Rapidly growing puppies (under 8 to 12 months) due to joint cartilage damage. High doses in felines pose risk of retinal toxicity and blindness.",
            sideEffects = "Articular cartilage degeneration in juvenile patients, temporary anorexia or emesis.",
            clinicalPearls = "Highly effective first-line fluoroquinolone for deep urinary tract infections, prostatitis, and severe respiratory tract infections."
        ) else DrugDetails(
            contraindications = "توله‌سگ‌های در حال رشد سریع (زیر ۸ تا ۱۲ ماه) به دلیل آسیب به غغضروف‌های مفصلی. دوزهای بالا در گربه‌ها خطر سمیت شبکیه و نابینایی دارد.",
            sideEffects = "تحلیل غضروف مفصلی در حیوانات جوان، بی‌اشتهایی یا استفراغ موقتی.",
            clinicalPearls = "فلوئوروکینولون خط اول بسیار موثر برای عفونت‌های عمیق مجاری اادراری، پروستاتیت و عفونت‌های شدید دستگاه تنفسی."
        )
        "8" -> if (lang == "en") DrugDetails(
            contraindications = "Pre-existing acute renal insufficiency, severe dehydration, concurrent use of other nephrotoxic medications.",
            sideEffects = "Accumulative nephrotoxicity and permanent auditory/vestibular ototoxicity from eighth cranial nerve damage.",
            clinicalPearls = "Ensure the patient is well-hydrated and monitor blood creatinine levels closely during aminoglycoside therapy."
        ) else DrugDetails(
            contraindications = "نارسایی حاد کلیوی از قبل موجود، کم‌آبی شدید بدن، استفاده همزمان از سایر داروهای مسموم‌کننده کلیه.",
            sideEffects = "مسمومیت کلیوی تجمعی و سمیت شنوایی/دهلیزی دائمی ناشی از آسیب به زوج هشتم اعصاب مغزی.",
            clinicalPearls = "مطمئن شوید که بدن بیمار به حد کافی هیدراته است و سطح کراتینین خون را طی درمان با آمینوگلیکوزیدها به دقت ارزیابی کنید."
        )
        "9" -> if (lang == "en") DrugDetails(
            contraindications = "Pre-existing advanced hepatic dysfunction, congestive heart failure.",
            sideEffects = "Mild anorexia, transient emesis, dose-dependent hepatic enzyme elevations (ALT/AST).",
            clinicalPearls = "Oral itraconazole absorption and clinical bioavailability are significantly enhanced if given with a fatty meal."
        ) else DrugDetails(
            contraindications = "اختلال پیشرفته و از قبل موجود کبدی، نارسایی احتقانی قلب.",
            sideEffects = "بی‌اشتهایی خفیف، استفراغ گذرا، افزایش وابسته به دوز آنزیم‌های کبدی (ALT/AST).",
            clinicalPearls = "جذب خوراکی و زیست‌فراهمی بالینی ایتراکونازول در صورت همراهی با یک وعده غذایی چرب به طور قابل توجهی افزایش می‌یابد."
        )
        "10" -> if (lang == "en") DrugDetails(
            contraindications = "Severe hepatic impairment, patients with hypoadrenocorticism (Addison's disease).",
            sideEffects = "Temporary alopecia, pruritus, emesis, temporary inhibition of testosterone and cortisol synthesis.",
            clinicalPearls = "Potent cytochrome P450 inhibitor. Co-administered drugs cleared via this pathway require dose downward adjustments."
        ) else DrugDetails(
            contraindications = "نقص شدید کارکرد کبد، بیماران مبتلا به کم‌کاری غده فوق کلیوی (بیماری آدیسون).",
            sideEffects = "آلوپسی (ریزش مو) موقت، خارش، استفراغ، مهار موقت سنتز تستوسترون و کورتیزول.",
            clinicalPearls = "مهارکننده قوی سیتوکروم P450. مصرف همزمان داروهای دیگر که از این مسیر کلیر می‌شوند به دوزهای پایین‌تری نیاز دارند."
        )
        "11" -> if (lang == "en") DrugDetails(
            contraindications = "Herding breeds (Collies, Shetland Sheepdogs, German Shepherds) with MDR1 mutations due to neurotoxicity.",
            sideEffects = "Mydriasis, severe ataxia, depression, tremors, seizures if standard dosage limits are exceeded.",
            clinicalPearls = "Heartworm preventative doses are extremely low and safe, but demographic MDR1 precaution remains mandatory."
        ) else DrugDetails(
            contraindications = "نژادهای گله (کولی، شتلند شیپ‌داگ، ژرمن شپرد) دارای جهش ژنتیکی MDR1 به علت بروز سمیت شدید عصبی.",
            sideEffects = "گشاد شدن مردمک چشم (میدریازیس)، آتاکسی شدید، افسردگی و سستی، لرزش عضلانی، تشنج در صورت بیش‌بود دوزهای استاندارد.",
            clinicalPearls = "دوزهای پیشگیری از کرم قلب بسیار پایین و ایمن هستند، اما مراقبت‌های جمعیتی ترجیحی MDR1 همواره الزامی است."
        )
        "12" -> if (lang == "en") DrugDetails(
            contraindications = "First trimester of pregnancy, pre-existing active central nervous system or neurological disorder.",
            sideEffects = "Neurotoxic symptoms (ataxia, horizontal nystagmus, head tilt), profound lethargy, metallic taste anorexia.",
            clinicalPearls = "If vestibular signs or ataxia manifest, stop therapy immediately and supportively treat with diazepam."
        ) else DrugDetails(
            contraindications = "سه ماهه اول بارداری، اختلالات فعال سیستم عصبی مرکزی یا مغز و اعصاب پیشین.",
            sideEffects = "علائم سمیت عصبی (آتاکسی، نیستاگموس افقی، کج شدن سر)، سستی و بی‌حالی عمیق، بی‌اشتهایی ناشی از طعم فلزی دهان.",
            clinicalPearls = "در صورت بروز علائم وستیبولار یا آتاکسی، درمان را فورا متوقف کرده و با دیازپام درمان حمایتی را آغاز کنید."
        )
        "13" -> if (lang == "en") DrugDetails(
            contraindications = "No absolute contraindications reported. Extremely wide margin of safety in companion animals and livestock.",
            sideEffects = "Very rare mild GI distress, transient nausea on the first dosing instance.",
            clinicalPearls = "Standard therapeutic course for internal parasites (roundworms, whipworms) requires 3 consecutive daily doses."
        ) else DrugDetails(
            contraindications = "هیچ منبع منع مصرف مطلقی گزارش نشده است. حاشیه ایمنی فوق‌العاده بالا در حیوانات همراه و دام‌ها.",
            sideEffects = "بسیار نادر، ناراحتی‌های خفیف در دستگاه گوارش، تهوع گذرا در اولین نوبت مصرف.",
            clinicalPearls = "دوره درمانی استاندارد برای انگل‌های داخلی (کرم‌های گرد، کج‌دم) نیازمند ۳ دوز متوالی روزانه است."
        )
        "14" -> if (lang == "en") DrugDetails(
            contraindications = "Hypovolemia, shock, patients with a genotype predisposition to cardiovascular syncope (e.g. Boxer breed).",
            sideEffects = "Risk of severe hypotension, profound sedation, transient protrusion of the third eyelid (nictitating membrane).",
            clinicalPearls = "Acepromazine provides tranquilization/sedation only; it has zero analgesic properties for surgical pain management."
        ) else DrugDetails(
            contraindications = "کاهش حجم خون (هیپوولمی)، شوک، بیماران با پیش‌زمینه ژنتیکی سنکوپ قلبی عروقی (نظیر نژاد باکسر).",
            sideEffects = "خطر افت شدید فشار خون، آرام‌بخشی عمیق، بیرون‌زدگی گذرا از پلک سوم (غشای چشمک‌زن).",
            clinicalPearls = "آسپرومایزین صرفا اثرات آرام‌بخش و تسلیم‌کننده دارد و هیچ‌گونه خاصیت ضددرد برای مدیریت دردهای جراحی ندارد."
        )
        "15" -> if (lang == "en") DrugDetails(
            contraindications = "Acute hepatic failure, respiratory depression, severe pre-existing pulmonary disease.",
            sideEffects = "Polyuria, polydipsia, polyphagia, transient sedation/lethargy in the first two weeks of epilepsy therapy.",
            clinicalPearls = "Monitor therapeutic serum concentrations every 6 months to adjust seizure control dosage effectively."
        ) else DrugDetails(
            contraindications = "نارسایی حاد کبدی، تضعیف شدید تنفس، بیماری‌های شدید ریوی از قبل موجود.",
            sideEffects = "پرادراری، پرنوشی، پرخوری، بی‌حالی/خواب‌آلودگی گذرا در دو هفته ابتدایی درمان صرع.",
            clinicalPearls = "هر ۶ ماه یک‌بار غلظت سرمی درمانی را نظارت کنید تا میزان دوز کنترل تشنج را به طور موثر تنظیم نمایید."
        )
        "16" -> if (lang == "en") DrugDetails(
            contraindications = "Anuria, progressive renal failure, severe pre-existing dehydration or electrolyte depletion.",
            sideEffects = "Hypokalemia, systemic dehydration, secondary prerenal azotemia.",
            clinicalPearls = "Primary diuretic of choice for rapid treatment of cardiogenic pulmonary edema in cats and dogs."
        ) else DrugDetails(
            contraindications = "آنوری (قطع ترشح ادرار)، نارسایی پیشرونده کلیوی، کم‌آبی شدید بدن یا تخلیه الکترولیت از قبل موجود.",
            sideEffects = "هیپوکالمی (کاهش پتاسیم خون)، کم‌آبی سیستمیک، ازوتمی پیش‌کلیوی ثانویه.",
            clinicalPearls = "مدر انتخابی و اول برای درمان سریع ادم ریوی کاردیوژنیک در سگ‌ها و گربه‌ها."
        )
        "17" -> if (lang == "en") DrugDetails(
            contraindications = "Hypertrophic cardiomyopathy (HCM) in cats, left ventricular outflow tract obstruction, aortic stenosis.",
            sideEffects = "Mild tachycardia, transient loose stools or GI irritation.",
            clinicalPearls = "Acts as a positive inotrope and vasodilator, greatly improving survival in dogs with CHF secondary to DCM or MMVD."
        ) else DrugDetails(
            contraindications = "کاردیومیوپاتی هیپرتروفیک (HCM) در گربه‌ها، انسداد جریان خروجی بطن چپ، تنگی آئورت.",
            sideEffects = "تاکی‌کاردی (افزایش ضربان قلب) خفیف، شل شدن گذرا مدفوع یا تحریک گوارشی.",
            clinicalPearls = "به عنوان اینوتروپ مثبت و گشادکننده عروق عمل می‌کند و بقای سگ‌های مبتلا به نارسایی احتقانی قلب ثانویه به DCM یا MMVD را افزایش می‌دهد."
        )
        "18" -> if (lang == "en") DrugDetails(
            contraindications = "History of hypersensitivity or anaphylactoid reactions to vitamin B components.",
            sideEffects = "Temporary localized stinging or burning sensation after injection.",
            clinicalPearls = "Administer slow subcutaneous (SQ) injection to minimize transient pain associated with deeper muscular dosing."
        ) else DrugDetails(
            contraindications = "سابقه واکنش‌های حساسیتی و آنافیلاکتوئیدی به اجزای ویتامین ب.",
            sideEffects = "احساس سوزش موقت موضعی پس از تزریق.",
            clinicalPearls = "سوزن تزریق را به صورت پوستی (SQ) بسیار آرام وارد کنید تا درد موقت ناشی از عضلانی عمیق به حداقل برسد."
        )
        else -> if (lang == "en") DrugDetails(
            contraindications = "Use with caution in patients with renal or hepatic compromise. Adjust dosage carefully.",
            sideEffects = "Potential transient gastrointestinal irritation or mild localized reaction at injection site.",
            clinicalPearls = "Dose calculation should be strictly verified based on specific lean body weight and patient parameters."
        ) else DrugDetails(
            contraindications = "در بیماران مبتلا به نارسایی کلیوی یا کبدی با احتیاط مصرف شود. دوز مصرفی را به دقت تنظیم کنید.",
            sideEffects = "احتمال تحریک گوارشی گذرا یا واکنش موضعی خفیف در محل تزریق.",
            clinicalPearls = "محاسبه دوز باید بر اساس وزن دقیق و بدون چربی بدن و سایر شاخص‌های تخصصی حیوان و بیمار بررسی و تایید شود."
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VetDrugManualScreen(viewModel: MainViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()
    val customCreatedDrugs by viewModel.customDrugs.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All drugs") }
    var isCategoriesExpanded by remember { mutableStateOf(false) }

    // Custom Drug Add Form state
    var showAddDrugForm by remember { mutableStateOf(false) }
    var newGeneric by remember { mutableStateOf("") }
    var newScientific by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Anti-infectives") }
    var newConcentrationVal by remember { mutableStateOf("") }
    var newConcentrationTxt by remember { mutableStateOf("") }
    var newRangeMin by remember { mutableStateOf("") }
    var newRangeMax by remember { mutableStateOf("") }
    var newRoute by remember { mutableStateOf("PO") }
    var newDefaultDosage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Combine static catalog and custom created drugs (all managed robustly via Room Database)
    val fullCatalog = customCreatedDrugs.ifEmpty { staticDrugCatalog }

    // Premium categories with custom medical icons and emojis
    val categoriesWithIcons = listOf(
        Pair("All drugs", "🩺"),
        Pair("Anaesthetic analgesics and NSAIDs", "💤"),
        Pair("Anti-infectives", "🦠"),
        Pair("Anti-neoplastic", "🧬"),
        Pair("Behaviour modifiers", "🧠"),
        Pair("Blood and immune system", "🩸"),
        Pair("Cardiovascular", "❤️"),
        Pair("Dermatological", "🩹"),
        Pair("Gastrointestinal and hepatic", "🧪"),
        Pair("Genito-urinary tract", "💦"),
        Pair("Metabolic", "🔥"),
        Pair("Neuromuscular system", "⚡"),
        Pair("Nutritional/fluids", "🥤"),
        Pair("Ophthalmic", "👁️"),
        Pair("Respiratory system", "🫁")
    )
    val categoriesList = categoriesWithIcons.map { it.first }

    // Filtered drugs, sorted alphabetically by Generic name
    val filteredDrugs = fullCatalog.filter { drug ->
        val matchesCategory = selectedCategory == "All drugs" || drug.category == selectedCategory
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
                val bannerLayoutDir = if (currentLang == "en") androidx.compose.ui.unit.LayoutDirection.Ltr else androidx.compose.ui.unit.LayoutDirection.Rtl
                CompositionLocalProvider(LocalLayoutDirection provides bannerLayoutDir) {
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
                                    text = if (currentLang == "en") "🐾 Dosage Calculations for: ${pet.name}" else "🐾 محاسبات دوز برای: ${pet.name}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currentLang == "en") "Species: ${pet.species} | Breed: ${pet.breed} | Weight: ${pet.weight} kg" else "گونه: ${pet.species} | نژاد: ${pet.breed} | وزن: ${pet.weight} کیلوگرم",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
                                )
                            }
                        }
                    }
                }
            } ?: run {
                val bannerLayoutDir = if (currentLang == "en") androidx.compose.ui.unit.LayoutDirection.Ltr else androidx.compose.ui.unit.LayoutDirection.Rtl
                CompositionLocalProvider(LocalLayoutDirection provides bannerLayoutDir) {
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
                                text = if (currentLang == "en") "⚠️ No Active Patient Selected" else "⚠️ هیچ بیمار فعالی انتخاب نشده است",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentLang == "en") "Dosage calculations are based on a default weight of 1 kg. Please activate a patient record under the dashboard first for precise calculations." else "محاسبات دوز بر اساس وزن پیش‌فرض ۱ کیلوگرم انجام می‌شود. لطفا ابتدا یک پرونده بیمار را در پیشخوان فعال کنید تا محاسبات دقیق انجام شود.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (currentLang == "en") "Search generic name or scientific title..." else "جستجوی نام ژنریک یا نام علمی...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categories Area
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLang == "en") "Drug Classifications:" else "دسته‌بندی‌های دارویی:",
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
                                contentDescription = "Toggle category view",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (currentLang == "en") {
                                    if (isCategoriesExpanded) "Compact View (Horizontal)" else "Show All Categories (15)"
                                } else {
                                    if (isCategoriesExpanded) "نمای فشرده (افقی)" else "نمایش همه دسته‌بندی‌ها (۱۵)"
                                },
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
                                                text = getLocalizedCategory(catName, currentLang),
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
                                        text = if (currentLang == "en") "New Drug" else "داروی جدید",
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
                                text = if (currentLang == "en") "Select a group to filter and prevent errors:" else "یک گروه را جهت فیلتر و جلوگیری از خطا انتخاب کنید:",
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
                                                    text = getLocalizedCategory(catName, currentLang),
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
                                        text = if (currentLang == "en") "Add New Custom Drug +" else "افزودن داروی جدید سفارشی +",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
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
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = if (currentLang == "en") "➕ Add Custom Drug - Demo Mode" else "➕ افزودن داروی سفارشی - حالت دمو",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
                            OutlinedTextField(
                                value = newGeneric,
                                onValueChange = { newGeneric = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(if (currentLang == "en") "Generic Name" else "نام ژنریک") },
                                placeholder = { Text(if (currentLang == "en") "e.g. Cephalexin" else "مثال: سفالکسین") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newScientific,
                                onValueChange = { newScientific = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(if (currentLang == "en") "Scientific Name" else "نام علمی") },
                                placeholder = { Text(if (currentLang == "en") "e.g. Cephalexin Monohydrate" else "مثال: سفالکسین مونوهیدرات") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Category Select
                            Text(
                                text = if (currentLang == "en") "Drug Classification:" else "دسته‌بندی دارویی:",
                                fontSize = 11.sp
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                categoriesList.filter { it != "All drugs" }.forEach { name ->
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
                                        Text(
                                            text = getLocalizedCategory(name, currentLang),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newConcentrationVal,
                                    onValueChange = { newConcentrationVal = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text(if (currentLang == "en") "Concentration (mg/ml)" else "غلظت (mg/ml)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newConcentrationTxt,
                                    onValueChange = { newConcentrationTxt = it },
                                    modifier = Modifier.weight(1.5f),
                                    label = { Text(if (currentLang == "en") "Concentration Label" else "برچسب غلظت") },
                                    placeholder = { Text(if (currentLang == "en") "e.g. 50 mg/ml" else "مثال: ۵۰ میلی‌گرم/میلی‌لیتر") },
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newRangeMin,
                                    onValueChange = { newRangeMin = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text(if (currentLang == "en") "Min Dose (mg/kg)" else "حداقل دوز (mg/kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newRangeMax,
                                    onValueChange = { newRangeMax = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text(if (currentLang == "en") "Max Dose (mg/kg)" else "حداکثر دوز (mg/kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newRoute,
                                    onValueChange = { newRoute = it },
                                    modifier = Modifier.weight(1.2f),
                                    label = { Text(if (currentLang == "en") "Route" else "روش مصرف") },
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newDefaultDosage,
                                onValueChange = { newDefaultDosage = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(if (currentLang == "en") "Default Avg Dose (mg/kg)" else "دوز متوسط پیش‌فرض (mg/kg)") },
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
                                Text(if (currentLang == "en") "Save & Add to Pharmacopoeia" else "ذخیره و افزودن به فارماکوپه")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Drug Table & Dosage Calculation Calculator
            Text(
                text = if (currentLang == "en") "Drug Manual & Smart Prescription:" else "دارونامه و نسخه هوشمند:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
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
                            text = if (currentLang == "en") "No Drugs Found" else "هیچ دارویی یافت نشد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No default or custom drugs match your search query. You can add unique concentrations and custom dosages.",
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
                            Text("Add Custom Drug Formula", fontSize = 11.sp)
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

                    // Dynamic layout direction for drug details based on current language
                    val itemLayoutDir = if (currentLang == "en") androidx.compose.ui.unit.LayoutDirection.Ltr else androidx.compose.ui.unit.LayoutDirection.Rtl
                    CompositionLocalProvider(LocalLayoutDirection provides itemLayoutDir) {
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
                                        text = drug.nameGeneric,
                                        fontWeight = FontWeight.ExtraBold, // FONT-EXTRABOLD: Maximum focal weight
                                        fontSize = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (currentLang == "en") "Scientific: ${drug.nameScientific}" else "نام علمی: ${drug.nameScientific}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
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
                                
                                if (drug.id.startsWith("custom_")) {
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteDrug(drug)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Drug Formula",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                
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
                                val details = getDrugDetails(drug.id, currentLang)
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
                                        // 1.1.1 Title Action
                                        CompositionLocalProvider(LocalLayoutDirection provides itemLayoutDir) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Start
                                            ) {
                                                Text("⚠️", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (currentLang == "en") "Veterinary Contraindications & Side Effects Guide" else "راهنمای موارد منع مصرف و عوارض جانبی دامپزشکی",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        // 1.1.2 Scientific Formula Display
                                        CompositionLocalProvider(LocalLayoutDirection provides itemLayoutDir) {
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
                                                    text = if (currentLang == "en") "Scientific Formula:" else "فرمول علمی:",
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

                                        // 1.1.3 Contraindications, Side Effects, Pearls
                                        CompositionLocalProvider(LocalLayoutDirection provides itemLayoutDir) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Contraindications (منع مصرف)
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = if (currentLang == "en") "🚫 Absolute Contraindications:" else "🚫 موارد منع مصرف مطلق:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFC2410C) // Tailwind Orange 700
                                                    )
                                                    Text(
                                                        text = details.contraindications,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                                                        textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
                                                    )
                                                }

                                                // Side Effects (عوارض جانبی عمده)
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = if (currentLang == "en") "🚨 Reported Side Effects & Adverse Events:" else "🚨 عوارض جانبی گزارش‌شده و نامطلوب:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFB91C1C) // Tailwind Red 700
                                                    )
                                                    Text(
                                                        text = details.sideEffects,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                                                        textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
                                                    )
                                                }

                                                // Clinical Pearl (نکات کلینییکال)
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = if (currentLang == "en") "💡 Expert Clinical Pearl & Tip:" else "💡 نکته و توصیه کلیدی کارشناس:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF0369A1) // Tailwind Sky 700
                                                    )
                                                    Text(
                                                        text = details.clinicalPearls,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                                                        textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
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
                                    text = if (currentLang == "en") "Ref Dose Range: ${drug.rangeAndRoute.substringBeforeLast(" ")}" else "محدوده دوز مرجع: ${drug.rangeAndRoute.substringBeforeLast(" ")}",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                RouteBadges(routeStr = drug.route, currentLang)
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
                                        text = if (currentLang == "en") {
                                            if (drug.rangeAndRoute.contains("mcg", ignoreCase = true)) "Dosage (mcg)" else "Dosage"
                                        } else {
                                            if (drug.rangeAndRoute.contains("mcg", ignoreCase = true)) "دوز محاسبه (mcg)" else "دوز محاسبه"
                                        },
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
                                        text = if (currentLang == "en") "Dose" else "دوز واقعی",
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
                                        text = if (currentLang == "en") "Volume" else "حجم مصرف",
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
                                            val message = if (currentLang == "en") "Drug ${drug.nameGeneric} has been added to the prescription!" else "داروی ${drug.nameGeneric} به نسخه اضافه شد!"
                                            snackbarHostState.showSnackbar(message)
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
                                            text = if (currentLang == "en") "Add to Prescription" else "افزودن به نسخه",
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
private fun RouteBadges(routeStr: String, currentLang: String) {
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
                    if (currentLang == "en") "IV (Intravenous)" else "IV (داخل وریدی)"
                )
                cleanR.contains("IM") -> Triple(
                    Color(0xFFF3E8FF), // Purple background
                    Color(0xFF7C3AED), // Purple foreground
                    if (currentLang == "en") "IM (Intramuscular)" else "IM (داخل عضلانی)"
                )
                cleanR.contains("SC") || cleanR.contains("SQ") -> Triple(
                    Color(0xFFD1FAE5), // Mint/Emerald background
                    Color(0xFF059669), // Mint/Emerald foreground
                    if (currentLang == "en") "SC (Subcutaneous)" else "SC (زیر پوستی)"
                )
                cleanR.contains("PO") -> Triple(
                    Color(0xFFFEF3C7), // Amber background
                    Color(0xFFD97706), // Amber foreground
                    if (currentLang == "en") "PO (Oral)" else "PO (خوراکی)"
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
