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
            contraindications = "Hypertrophic cardiomyopathy (HCM) in felines, severe heart failure, uncontrolled hypertension. Use with extreme caution in diabetic pets or patients with acute renal insufficiency.",
            sideEffects = "Sialorrhea (hypersalivation), moderate to severe muscle tremors, rough emergence delirium in the absence of proper pre-anesthetic sedation, fluctuating heart rate.",
            clinicalPearls = "Co-administration or pre-medication with diazepam or xylazine is highly recommended to prevent muscle rigidity and facilitate a smooth recovery phase."
        )
        "2" -> DrugDetails(
            contraindications = "Last trimester of pregnancy (high risk of premature labor or abortion), upper airway obstruction, severe mitral valve disease.",
            sideEffects = "Severe bradycardia, transient rumen stasis/bloat in ruminants, early emesis immediately following injection in felines.",
            clinicalPearls = "Yohimbine or atipamezole serve as specific antagonists to quickly reverse the sedative and physiological effects of xylazine."
        )
        "3" -> DrugDetails(
            contraindications = "Advanced hepatic insufficiency. Rapid IV bolus may trigger acute respiratory depression or local thrombophlebitis.",
            sideEffects = "Mild respiratory depression, transient muscle ataxia in the initial minutes following intravenous administration.",
            clinicalPearls = "Diazepam is the primary emergency drug of choice for controlling active epileptic seizures (Status Epilepticus) in veterinary medicine."
        )
        "4" -> DrugDetails(
            contraindications = "Severe hypovolemic shock, uncompensated congestive heart failure, extreme systemic dehydration.",
            sideEffects = "Transient respiratory apnea if injected too rapidly, arterial hypotension accompanied by complete skeletal muscle relaxation.",
            clinicalPearls = "Must be administered via slow IV route (over 60 seconds) to prevent sudden apnea or acute respiratory arrest."
        )
        "5" -> DrugDetails(
            contraindications = "Known hypersensitivity to penicillins or beta-lactams. Oral administration is fatal to small hindgut fermenting rodents (rabbits, guinea pigs, hamsters).",
            sideEffects = "Transient GI upset, companion microflora diarrhea, skin rashes, and mild hypersensitivity reactions.",
            clinicalPearls = "Long-acting (LA) formulations maintain active therapeutic blood and tissue levels for up to 48 consecutive hours."
        )
        "6" -> DrugDetails(
            contraindications = "Known hypersensitivity or allergy to cephalosporins. Significant risk of nephrotoxicity when co-administered with aminoglycosides.",
            sideEffects = "Temporary localized pain or sting at deep IM injection sites, mild gastrointestinal distress.",
            clinicalPearls = "Due to exceptional blood-brain barrier (BBB) penetration, ceftriaxone is excellent for treating bacterial meningitis."
        )
        "7" -> DrugDetails(
            contraindications = "Rapidly growing puppies (under 8 to 12 months) due to joint cartilage damage. High doses in felines pose risk of retinal toxicity and blindness.",
            sideEffects = "Articular cartilage degeneration in juvenile patients, temporary anorexia or emesis.",
            clinicalPearls = "Highly effective first-line fluoroquinolone for deep urinary tract infections, prostatitis, and severe respiratory tract infections."
        )
        "8" -> DrugDetails(
            contraindications = "Pre-existing acute renal insufficiency, severe dehydration, concurrent use of other nephrotoxic medications.",
            sideEffects = "Accumulative nephrotoxicity and permanent auditory/vestibular ototoxicity from eighth cranial nerve damage.",
            clinicalPearls = "Ensure the patient is well-hydrated and monitor blood creatinine levels closely during aminoglycoside therapy."
        )
        "9" -> DrugDetails(
            contraindications = "Pre-existing advanced hepatic dysfunction, congestive heart failure.",
            sideEffects = "Mild anorexia, transient emesis, dose-dependent hepatic enzyme elevations (ALT/AST).",
            clinicalPearls = "Oral itraconazole absorption and clinical bioavailability are significantly enhanced if given with a fatty meal."
        )
        "10" -> DrugDetails(
            contraindications = "Severe hepatic impairment, patients with hypoadrenocorticism (Addison's disease).",
            sideEffects = "Temporary alopecia, pruritus, emesis, temporary inhibition of testosterone and cortisol synthesis.",
            clinicalPearls = "Potent cytochrome P450 inhibitor. Co-administered drugs cleared via this pathway require dose downward adjustments."
        )
        "11" -> DrugDetails(
            contraindications = "Herding breeds (Collies, Shetland Sheepdogs, German Shepherds) with MDR1 mutations due to neurotoxicity.",
            sideEffects = "Mydriasis, severe ataxia, depression, tremors, seizures if standard dosage limits are exceeded.",
            clinicalPearls = "Heartworm preventative doses are extremely low and safe, but demographic MDR1 precaution remains mandatory."
        )
        "12" -> DrugDetails(
            contraindications = "First trimester of pregnancy, pre-existing active central nervous system or neurological disorder.",
            sideEffects = "Neurotoxic symptoms (ataxia, horizontal nystagmus, head tilt), profound lethargy, metallic taste anorexia.",
            clinicalPearls = "If vestibular signs or ataxia manifest, stop therapy immediately and supportively treat with diazepam."
        )
        "13" -> DrugDetails(
            contraindications = "No absolute contraindications reported. Extremely wide margin of safety in companion animals and livestock.",
            sideEffects = "Very rare mild GI distress, transient nausea on the first dosing instance.",
            clinicalPearls = "Standard therapeutic course for internal parasites (roundworms, whipworms) requires 3 consecutive daily doses."
        )
        "14" -> DrugDetails(
            contraindications = "Hypovolemia, shock, patients with a genotype predisposition to cardiovascular syncope (e.g. Boxer breed).",
            sideEffects = "Risk of severe hypotension, profound sedation, transient protrusion of the third eyelid (nictitating membrane).",
            clinicalPearls = "Acepromazine provides tranquilization/sedation only; it has zero analgesic properties for surgical pain management."
        )
        "15" -> DrugDetails(
            contraindications = "Acute hepatic failure, respiratory depression, severe pre-existing pulmonary disease.",
            sideEffects = "Polyuria, polydipsia, polyphagia, transient sedation/lethargy in the first two weeks of epilepsy therapy.",
            clinicalPearls = "Monitor therapeutic serum concentrations every 6 months to adjust seizure control dosage effectively."
        )
        "16" -> DrugDetails(
            contraindications = "Anuria, progressive renal failure, severe pre-existing dehydration or electrolyte depletion.",
            sideEffects = "Hypokalemia, systemic dehydration, secondary prerenal azotemia.",
            clinicalPearls = "Primary diuretic of choice for rapid treatment of cardiogenic pulmonary edema in cats and dogs."
        )
        "17" -> DrugDetails(
            contraindications = "Hypertrophic cardiomyopathy (HCM) in cats, left ventricular outflow tract obstruction, aortic stenosis.",
            sideEffects = "Mild tachycardia, transient loose stools or GI irritation.",
            clinicalPearls = "Acts as a positive inotrope and vasodilator, greatly improving survival in dogs with CHF secondary to DCM or MMVD."
        )
        "18" -> DrugDetails(
            contraindications = "History of hypersensitivity or anaphylactoid reactions to vitamin B components.",
            sideEffects = "Temporary localized stinging or burning sensation after injection.",
            clinicalPearls = "Administer slow subcutaneous (SQ) injection to minimize transient pain associated with deeper muscular dosing."
        )
        else -> DrugDetails(
            contraindications = "Use with caution in patients with renal or hepatic compromise. Adjust dosage carefully.",
            sideEffects = "Potential transient gastrointestinal irritation or mild localized reaction at injection site.",
            clinicalPearls = "Dose calculation should be strictly verified based on specific lean body weight and patient parameters."
        )
    }
}

fun getCategoryDisp(cat: String, lang: String): String {
    if (lang == "en") return cat
    return when(cat) {
        "All drugs" -> "همه داروها"
        "Anaesthetic analgesics and NSAIDs" -> "ضددردها، ملوکسیکام و NSAIDها"
        "Anti-infectives" -> "ضد عفونت و آنتی‌بیوتیک‌ها"
        "Anti-neoplastic" -> "ضد سرطان و انکولوژی"
        "Behaviour modifiers" -> "اصلاح‌کننده‌های رفتار"
        "Blood and immune system" -> "خون و سیستم ایمنی"
        "Cardiovascular" -> "سیستم قلبی عروقی"
        "Dermatological" -> "داروهای پوست و مو"
        "Gastrointestinal and hepatic" -> "گوارش و کبد"
        "Genito-urinary tract" -> "سیستم تناسلی ادراری"
        "Metabolic" -> "داروهای متابولیک"
        "Neuromuscular system" -> "سیستم عصبی عضلانی"
        "Nutritional/fluids" -> "تغذیه و مایع‌درمانی"
        "Ophthalmic" -> "داروهای چشم‌پزشکی"
        "Respiratory system" -> "سیستم تنفسی"
        else -> cat
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VetDrugManualScreen(viewModel: MainViewModel) {
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()
    val customCreatedDrugs by viewModel.customDrugs.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

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

    // Combine static catalog and custom created drugs
    val fullCatalog = staticDrugCatalog + customCreatedDrugs

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
                                    text = "🐾 Dosage Calculations for: ${pet.name}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Left
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Species: ${pet.species} | Breed: ${pet.breed} | Weight: ${pet.weight} kg",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Left
                                )
                            }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = if (currentLanguage == "en") Alignment.Start else Alignment.End
                    ) {
                        Text(
                            text = if (currentLanguage == "en") "⚠️ No Active Patient Selected" else "⚠️ هیچ بیمار فعالی انتخاب نشده است",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentLanguage == "en") {
                                "Dosage calculations are based on a default weight of 1 kg. Please activate a patient record under the dashboard first for precise calculations."
                            } else {
                                "محاسبات دوز مصرفی بر مبنای وزن پیش‌فرض ۱ کیلوگرم انجام می‌شود. لطفاً ابتدا از داشبورد یک پرونده بیمار فعال کنید تا محاسبات دقیق انجام شود."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (currentLanguage == "en") "Search generic name or scientific title..." else "جستجوی نام ژنریک یا نام علمی دارو...") },
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
                        text = if (currentLanguage == "en") "Drug Classifications:" else "دسته‌بندی‌های دارویی:",
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
                                contentDescription = if (currentLanguage == "en") "Toggle category view" else "تغییر نمای دسته‌بندی",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (currentLanguage == "en") {
                                    if (isCategoriesExpanded) "Compact View (Horizontal)" else "Show All Categories (15)"
                                } else {
                                    if (isCategoriesExpanded) "نمای فشرده (افقی)" else "مشاهده تمام دسته‌ها (۱۵)"
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
                                                text = getCategoryDisp(catName, currentLanguage),
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
                                        text = if (currentLanguage == "en") "New Drug" else "جدید",
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
                                text = if (currentLanguage == "en") "Select sterile class or category group:" else "یک گروه دارویی را جهت محدودسازی دوزها فیلتر کنید:",
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
                                                    text = getCategoryDisp(catName, currentLanguage),
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
                                        text = "Add New Custom Drug +",
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
                        Text("➕ Add Custom Drug - Demo Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))

                        CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
                            OutlinedTextField(
                                value = newGeneric,
                                onValueChange = { newGeneric = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Generic Name") },
                                placeholder = { Text("e.g. Cephalexin") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newScientific,
                                onValueChange = { newScientific = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Scientific Name") },
                                placeholder = { Text("e.g. Cephalexin Monohydrate") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Category Select
                            Text("Drug Classification:", fontSize = 11.sp)
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
                                    label = { Text("Concentration (mg/ml)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newConcentrationTxt,
                                    onValueChange = { newConcentrationTxt = it },
                                    modifier = Modifier.weight(1.5f),
                                    label = { Text("Concentration Label") },
                                    placeholder = { Text("e.g. 50 mg/ml") },
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newRangeMin,
                                    onValueChange = { newRangeMin = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Min Dose (mg/kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newRangeMax,
                                    onValueChange = { newRangeMax = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Max Dose (mg/kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = newRoute,
                                    onValueChange = { newRoute = it },
                                    modifier = Modifier.weight(1.2f),
                                    label = { Text("Route") },
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newDefaultDosage,
                                onValueChange = { newDefaultDosage = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Default Avg Dose (mg/kg)") },
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
                                Text("Save & Add to Pharmacopoeia")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Drug Table & Dosage Calculation Calculator
            Text(
                text = "Drug Manual & Smart Prescription:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Left
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
                            text = "No Drugs Found",
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
                                        text = drug.nameGeneric,
                                        fontWeight = FontWeight.ExtraBold, // FONT-EXTRABOLD: Maximum focal weight
                                        fontSize = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Scientific: ${drug.nameScientific}",
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
                                        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Start
                                            ) {
                                                Text("⚠️", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Veterinary Contraindications & Side Effects Guide",
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
                                        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Contraindications (منع مصرف)
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = "🚫 Absolute Contraindications:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFC2410C) // Tailwind Orange 700
                                                    )
                                                    Text(
                                                        text = details.contraindications,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                                                        textAlign = TextAlign.Left
                                                    )
                                                }

                                                // Side Effects (عوارض جانبی عمده)
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = "🚨 Reported Side Effects & Adverse Events:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFB91C1C) // Tailwind Red 700
                                                    )
                                                    Text(
                                                        text = details.sideEffects,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                                                        textAlign = TextAlign.Left
                                                    )
                                                }

                                                // Clinical Pearl (نکات کلینییکال)
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = "💡 Expert Clinical Pearl & Tip:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF0369A1) // Tailwind Sky 700
                                                    )
                                                    Text(
                                                        text = details.clinicalPearls,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                                                        textAlign = TextAlign.Left
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
                                    text = "Ref Dose Range: ${drug.rangeAndRoute.substringBeforeLast(" ")}",
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
                                            snackbarHostState.showSnackbar("Drug ${drug.nameGeneric} has been added to the prescription!")
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
                                            text = "Add to Prescription",
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
                    "IV (Intravenous)"
                )
                cleanR.contains("IM") -> Triple(
                    Color(0xFFF3E8FF), // Purple background
                    Color(0xFF7C3AED), // Purple foreground
                    "IM (Intramuscular)"
                )
                cleanR.contains("SC") || cleanR.contains("SQ") -> Triple(
                    Color(0xFFD1FAE5), // Mint/Emerald background
                    Color(0xFF059669), // Mint/Emerald foreground
                    "SC (Subcutaneous)"
                )
                cleanR.contains("PO") -> Triple(
                    Color(0xFFFEF3C7), // Amber background
                    Color(0xFFD97706), // Amber foreground
                    "PO (Oral)"
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
