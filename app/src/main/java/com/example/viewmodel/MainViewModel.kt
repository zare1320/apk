package com.example.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.VetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: VetRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    // --- Authentication ---
    val activeSession: StateFlow<UserSession?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- UI/Language/Theme Settings (SharedPreferences-backed) ---
    private val _themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "dark") ?: "dark")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _currentLanguage = MutableStateFlow(sharedPrefs.getString("current_language", "fa") ?: "fa") // Default "fa" as seen on the login page
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _activeSubscription = MutableStateFlow("gold") // "free", "silver", "gold", "diamond"
    val activeSubscription: StateFlow<String> = _activeSubscription.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(sharedPrefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // --- Vet Mode: Examined Pet State (Dashboard) ---
    private val _selectedSpecies = MutableStateFlow<String?>(null) // "dog", "cat", "exotic"
    val selectedSpecies: StateFlow<String?> = _selectedSpecies.asStateFlow()

    private val _selectedExoticOption = MutableStateFlow<String?>(null) // "bird", "rodent", "aquatic", "amphibian"
    val selectedExoticOption: StateFlow<String?> = _selectedExoticOption.asStateFlow()

    // Active examined patient (null until a patient profile is saved in current session)
    private val _activeExaminedPet = MutableStateFlow<Pet?>(null)
    val activeExaminedPet: StateFlow<Pet?> = _activeExaminedPet.asStateFlow()

    // In-memory list of pets registered in the database
    val allPets: StateFlow<List<Pet>> = repository.allPets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All presets and saved prescriptions
    val allPrescriptions: StateFlow<List<Prescription>> = repository.allPrescriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calendar schedules
    val allEvents: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Medical Diagnosis Logs ---
    val physicalComplaints = MutableStateFlow(mapOf<String, String>())
    val physicalSigns = MutableStateFlow(mapOf<String, String>())
    val labResults = MutableStateFlow(mapOf<String, String>())

    // --- Drug Catalog & Custom Added Drug Store (Room database-backed) ---
    val customDrugs: StateFlow<List<DrugItem>> = repository.allDrugs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Treatment Guidelines Store (Room database-backed) ---
    val allGuidelines: StateFlow<List<TreatmentGuideline>> = repository.allGuidelines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Food Database Store (Room database-backed) ---
    val allFoods: StateFlow<List<FoodItem>> = repository.allFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed database with default drug catalog if empty
        viewModelScope.launch {
            repository.seedDrugs(staticDrugCatalog)
        }

        // Seed database with default treatment guidelines if empty
        viewModelScope.launch {
            repository.seedGuidelines(staticGuidelinesCatalog)
        }

        // Seed database with default recommended foods if empty
        viewModelScope.launch {
            repository.seedFoods(staticFoodCatalog)
        }

        // Pre-create a default vet session so the user starts with something if they haven't registered
        viewModelScope.launch {
            val session = repository.getActiveSessionSync()
            if (session == null) {
                // Prepopulate standard database session or let them login
            }
        }
    }

    // --- Functions ---
    fun toggleTheme() {
        val newTheme = if (_themeMode.value == "light") "dark" else "light"
        _themeMode.value = newTheme
        sharedPrefs.edit().putString("theme_mode", newTheme).apply()
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        sharedPrefs.edit().putString("current_language", lang).apply()
    }

    fun setSubscription(sub: String) {
        _activeSubscription.value = sub
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        sharedPrefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun selectSpecies(species: String?) {
        _selectedSpecies.value = species
        if (species != "exotic") {
            _selectedExoticOption.value = null
        }
    }

    fun selectExoticOption(option: String?) {
        _selectedExoticOption.value = option
    }

    fun clearExaminedPet() {
        _activeExaminedPet.value = null
        _selectedSpecies.value = null
        _selectedExoticOption.value = null
        physicalComplaints.value = emptyMap()
        physicalSigns.value = emptyMap()
        labResults.value = emptyMap()
    }

    fun saveExaminedPet(
        name: String,
        breed: String,
        weight: Double,
        age: String,
        gender: String,
        isNeutered: Boolean,
        ownerName: String,
        ownerPhone: String,
        recordNumber: String
    ) {
        viewModelScope.launch {
            val speciesValue = _selectedSpecies.value ?: "dog"
            val speciesString = if (speciesValue == "exotic") {
                _selectedExoticOption.value ?: "exotic"
            } else {
                speciesValue
            }

            val pet = Pet(
                name = name,
                species = speciesString,
                breed = breed,
                weight = weight,
                age = age,
                gender = gender,
                isNeutered = isNeutered,
                ownerName = ownerName,
                ownerPhone = ownerPhone,
                healthStatus = "Under Treatment",
                recordNumber = recordNumber
            )

            val id = repository.insertPet(pet)
            val insertedPet = pet.copy(id = id.toInt())
            _activeExaminedPet.value = insertedPet
        }
    }

    fun selectExistingPet(pet: Pet) {
        _activeExaminedPet.value = pet
        // Sync species select representation
        if (pet.species == "dog" || pet.species == "cat") {
            _selectedSpecies.value = pet.species
            _selectedExoticOption.value = null
        } else {
            _selectedSpecies.value = "exotic"
            _selectedExoticOption.value = pet.species
        }
    }

    fun addNewPatient(pet: Pet) {
        viewModelScope.launch {
            repository.insertPet(pet)
        }
    }

    fun simulateRegistration(
        fullName: String,
        phoneNumber: String,
        userType: String,
        licenseNum: String,
        specOrUni: String,
        gender: String = "آقا"
    ) {
        simulateRegister(
            phone = phoneNumber,
            userType = userType,
            fullName = fullName,
            idNumber = licenseNum,
            workplace = specOrUni,
            specialty = specOrUni,
            petsData = emptyList(),
            gender = gender
        )
    }

    fun simulateRegister(
        phone: String,
        userType: String,
        fullName: String,
        idNumber: String,
        workplace: String,
        specialty: String,
        petsData: List<Pet> = emptyList(),
        gender: String = "آقا"
    ) {
        viewModelScope.launch {
            val session = UserSession(
                phoneNumber = phone,
                userType = userType,
                fullName = fullName,
                identification = idNumber,
                workplaceOrUni = workplace,
                specialty = specialty,
                isLoggedIn = true,
                coins = 100,
                gender = gender
            )
            repository.login(session)

            if (userType == "owner") {
                // If they are a pet owner, save their pets registered in DB automatically
                petsData.forEach { pet ->
                    repository.insertPet(pet.copy(ownerPhone = phone, ownerName = fullName))
                }
            }
        }
    }

    fun simulateSocialAuth(
        emailOrId: String,
        fullName: String,
        userType: String,
        provider: String,
        gender: String = "آقا"
    ) {
        viewModelScope.launch {
            val session = UserSession(
                phoneNumber = emailOrId,
                userType = userType,
                fullName = fullName,
                identification = "سریع با $provider",
                workplaceOrUni = provider,
                specialty = "تایید هویت سریع مستقل",
                isLoggedIn = true,
                coins = 100,
                gender = gender
            )
            repository.login(session)
        }
    }

    fun simulateLogin(phone: String): Flow<Boolean> {
        val flow = MutableSharedFlow<Boolean>()
        viewModelScope.launch {
            // Check if user session exists in db (even if logged out)
            val existing = repository.getSessionByPhone(phone)
            val session = existing?.copy(isLoggedIn = true) ?: UserSession(
                phoneNumber = phone,
                userType = if (phone.contains("98") || phone.startsWith("09")) "vet" else "owner",
                fullName = "Dr. Jane Smith",
                identification = "9901123",
                workplaceOrUni = "Tehran Medical Vet",
                specialty = "Small Animals Medicine",
                isLoggedIn = true,
                coins = 100
            )
            repository.login(session)
        }
        return flow { emit(true) }
    }

    suspend fun checkUserExists(phone: String): UserSession? {
        return repository.getSessionByPhone(phone)
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _activeExaminedPet.value = null
        }
    }

    fun updateSession(
        fullName: String,
        phoneNumber: String,
        identification: String = "",
        workplaceOrUni: String = "",
        specialty: String = "",
        gender: String = "آقا"
    ) {
        viewModelScope.launch {
            val currentSession = repository.getActiveSessionSync()
            if (currentSession != null) {
                val updatedSession = currentSession.copy(
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    identification = identification,
                    workplaceOrUni = workplaceOrUni,
                    specialty = specialty,
                    gender = gender
                )
                if (currentSession.phoneNumber != phoneNumber) {
                    repository.logout()
                }
                repository.insertSession(updatedSession.copy(isLoggedIn = true))
            }
        }
    }

    fun savePrescription(
        drug: DrugItem,
        dosageVal: Double,
        calculatedDose: Double,
        calculatedVolume: Double
    ) {
        viewModelScope.launch {
            val pet = _activeExaminedPet.value
            val docSession = repository.getActiveSessionSync()
            val prescription = Prescription(
                petId = pet?.id ?: 0,
                petName = pet?.name ?: "Unknown Patient",
                ownerPhone = pet?.ownerPhone ?: "",
                doctorName = docSession?.fullName ?: "Dr. Veterinarian",
                drugName = drug.nameGeneric + " (" + drug.nameScientific + ")",
                concentration = drug.concentrationText,
                rangeRoute = drug.rangeAndRoute,
                dosageUsed = dosageVal,
                calculatedDose = calculatedDose,
                calculatedVolume = calculatedVolume
            )
            repository.insertPrescription(prescription)
        }
    }

    fun deletePrescription(prescription: Prescription) {
        viewModelScope.launch {
            repository.deletePrescription(prescription)
        }
    }

    fun addCustomDrug(
        nameGeneric: String,
        nameScientific: String,
        category: String,
        concentrationValue: Double,
        concentrationText: String,
        rangeMin: Double,
        rangeMax: Double,
        route: String,
        defaultDosage: Double
    ) {
        val newDrug = DrugItem(
            id = "custom_" + System.currentTimeMillis(),
            nameGeneric = nameGeneric,
            nameScientific = nameScientific,
            category = category,
            concentrationVal = concentrationValue,
            concentrationText = concentrationText,
            rangeAndRoute = "$rangeMin-$rangeMax mg/kg $route",
            rangeMin = rangeMin,
            rangeMax = rangeMax,
            route = route,
            defaultDosage = defaultDosage
        )
        viewModelScope.launch {
            repository.insertDrug(newDrug)
        }
    }

    fun deleteDrug(drug: DrugItem) {
        viewModelScope.launch {
            repository.deleteDrug(drug)
        }
    }

    fun insertGuideline(species: String, name: String, symptoms: String, diffDiagnosis: String, protocol: String) {
        val guidId = "custom_g_" + System.currentTimeMillis()
        val newGuideline = TreatmentGuideline(
            id = guidId,
            species = species,
            name = name,
            symptoms = symptoms,
            diffDiagnosis = diffDiagnosis,
            protocol = protocol
        )
        viewModelScope.launch {
            repository.insertGuideline(newGuideline)
        }
    }

    fun deleteGuideline(guideline: TreatmentGuideline) {
        viewModelScope.launch {
            repository.deleteGuideline(guideline)
        }
    }

    fun addCalendarEvent(
        petId: Int,
        petName: String,
        eventType: String,
        eventDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            val event = CalendarEvent(
                petId = petId,
                petName = petName,
                eventType = eventType,
                eventDate = eventDate,
                notes = notes,
                isCompleted = false
            )
            repository.insertEvent(event)
        }
    }

    fun deleteCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun toggleCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.insertEvent(event.copy(isCompleted = !event.isCompleted))
        }
    }

    // Reset database cache for client-side cleanliness
    fun resetAllData() {
        viewModelScope.launch {
            repository.logout()
            repository.clearAllPrescriptions()
            _activeExaminedPet.value = null
            _selectedSpecies.value = null
            _selectedExoticOption.value = null
            repository.deleteCustomDrugs()
            repository.deleteCustomGuidelines()
            physicalComplaints.value = emptyMap()
            physicalSigns.value = emptyMap()
            labResults.value = emptyMap()
        }
    }
}

// Data models for the Static Drugs catalog

val staticDrugCatalog = listOf(
    // Anesthetics
    DrugItem("1", "Ketamine", "Ketamine Hydrocholoride", "Anaesthetic analgesics and NSAIDs", 100.0, "100 mg/ml (10%)", "5-15 mg/kg IM/IV", 5.0, 15.0, "IM/IV", 10.0),
    DrugItem("2", "Xylazine", "Xylazine 2%", "Anaesthetic analgesics and NSAIDs", 20.0, "20 mg/ml (2%)", "1-2 mg/kg IM/SC", 1.0, 2.0, "IM/SC", 1.5),
    DrugItem("3", "Diazepam", "Diazepam 5mg/ml", "Anaesthetic analgesics and NSAIDs", 5.0, "5 mg/ml", "0.2-0.5 mg/kg IV", 0.2, 0.5, "IV", 0.3),
    DrugItem("4", "Propofol", "Propofol 1%", "Anaesthetic analgesics and NSAIDs", 10.0, "10 mg/ml", "4-6 mg/kg IV", 4.0, 6.0, "IV", 5.0),
    
    // Antibiotics
    DrugItem("5", "Amoxicillin", "Amoxicillin LA", "Anti-infectives", 150.0, "150 mg/ml (15%)", "10-20 mg/kg SC/IM", 10.0, 20.0, "SC/IM", 15.0),
    DrugItem("6", "Ceftriaxone", "Ceftriaxone 1g Sodium", "Anti-infectives", 100.0, "100 mg/ml (reconstituted powder)", "15-30 mg/kg IV/IM", 15.0, 30.0, "IV/IM", 20.0),
    DrugItem("7", "Enrofloxacin", "Enrofloxacin 10%", "Anti-infectives", 100.0, "100 mg/ml (10%)", "5-10 mg/kg SC/PO", 5.0, 10.0, "SC/PO", 7.5),
    DrugItem("8", "Gentamicin", "Gentamicin 5% Injection", "Anti-infectives", 50.0, "50 mg/ml (5%)", "2-4 mg/kg IV/IM/SC", 2.0, 4.0, "IV/IM/SC", 3.0),
    
    // Antifungal
    DrugItem("9", "Itraconazole", "Itraconazole 100mg", "Anti-infectives", 10.0, "10 mg/ml (oral suspension)", "5-10 mg/kg PO", 5.0, 10.0, "PO", 7.5),
    DrugItem("10", "Ketoconazole", "Ketoconazole 200mg", "Anti-infectives", 20.0, "20 mg/ml", "5-10 mg/kg PO", 5.0, 10.0, "PO", 8.0),
    
    // Antiparasitics
    DrugItem("11", "Ivermectin", "Ivermectin 1%", "Anti-infectives", 10.0, "10 mg/ml (1%)", "0.2-0.4 mg/kg SC", 0.2, 0.4, "SC", 0.25),
    DrugItem("12", "Metronidazole", "Metronidazole Intravenous Infusion", "Anti-infectives", 5.0, "5 mg/ml (0.5%)", "15-25 mg/kg IV", 15.0, 25.0, "IV", 20.0),
    DrugItem("13", "Fenbendazole", "Fenbendazole Oral", "Anti-infectives", 100.0, "100 mg/ml (10%)", "50 mg/kg PO", 50.0, 50.0, "PO", 50.0),
    
    // Psychotropics
    DrugItem("14", "Acepromazine", "Acepromazine 10mg/ml", "Behaviour modifiers", 10.0, "10 mg/ml", "0.05-0.1 mg/kg IM/SC", 0.05, 0.1, "IM/SC", 0.05),
    DrugItem("15", "Phenobarbital", "Phenobarbital Sodium", "Neuromuscular system", 30.0, "30 mg/ml", "2-5 mg/kg IV", 2.0, 5.0, "IV", 3.0),
 
    // Cardiovasculars
    DrugItem("16", "Furosemide", "Lasix 5% Injection", "Cardiovascular", 50.0, "50 mg/ml (5%)", "1-4 mg/kg IV/IM", 1.0, 4.0, "IV/IM", 2.0),
    DrugItem("17", "Pimobendan", "Pimobendan Vetmedin", "Cardiovascular", 5.0, "5 mg/ml", "0.2-0.6 mg/kg PO (divided in two doses)", 0.2, 0.6, "PO", 0.3),
 
    // Multivitamins
    DrugItem("18", "B-Complex", "Vitamin B Complex", "Nutritional/fluids", 50.0, "50 mg/ml Combined", "0.1-0.2 ml/kg SC/IM", 0.1, 0.2, "SC/IM", 0.15)
)

val staticGuidelinesCatalog = listOf(
    TreatmentGuideline(
        id = "g_dog_cpv",
        species = "dog",
        name = "پاروویروس سگ‌سانان (CPV)",
        symptoms = "اسهال خونی بسیار شدید و بدبو، استفراغ مداوم، بی‌اشتهایی کامل، تب بالا، دهیدراتاسیون بسیار سریع.",
        diffDiagnosis = "کوروناویروس، ژیاردیازیس، انسداد مکانیکی گوارشی.",
        protocol = "مایع‌درمانی وریدی تهاجمی رینگرلاکتات، آنتی‌بیوتیک محافظتی ثانویه آمپی‌سیلین، ماروپیتانت ضد استفراغ."
    ),
    TreatmentGuideline(
        id = "g_dog_cdv",
        species = "dog",
        name = "دیستمپر سگ (CDV)",
        symptoms = "ترشحات غلیظ چرکی چشم و بینی، افزایش ضخامت پد کف پنجه پا، پرش عضلانی عصبی، تب نوسانی.",
        diffDiagnosis = "هاری، هپاتیت عفونی سگ‌سانان، مننژیت قارچی.",
        protocol = "مراقبت‌های ویژه حمایتی، فنوپاربیتال ضدتشنج، داکسی‌سایکلین، مرطوب‌ساز مجرای تنفسی."
    ),
    TreatmentGuideline(
        id = "g_dog_kc",
        species = "dog",
        name = "سرفه کنل (سیاه‌سرفه)",
        symptoms = "سرفه‌های عمیق بوقی خشک و مکرر پس از فعالیت بدنی، ترشح کف دهان.",
        diffDiagnosis = "کلاپس نای، نارسایی احتقانی قلبی، بلع جسم خارجی.",
        protocol = "بخور ملایم آب گرم، داکسی‌سایکلین مناسب (۱۰mg/kg)، پرهیز از اعمال فشار قلاده بر مجرای نای."
    ),
    TreatmentGuideline(
        id = "g_cat_fcv",
        species = "cat",
        name = "کلسی‌ویروس گربه‌سانان (FCV)",
        symptoms = "بافتهای زخمی دهان و دندان، ریزش بزاق، تب، بی‌اشتهایی شدید به دلیل درد دهان.",
        diffDiagnosis = "هرپس‌ویروس گربه، لنفوم دهانی، زخم‌های ناشی از اورمی کلیه.",
        protocol = "ملوکسیکام ضددرد، کلیندامایسین برای برطرف کردن باکتری، تیتانیوم دهانی، تغذیه نرم و ولرم."
    ),
    TreatmentGuideline(
        id = "g_cat_pan",
        species = "cat",
        name = "پنلوکوپنی گربه‌ها (Panleukopenia)",
        symptoms = "تب کشنده نوسانی، اسهال بدبو، کاهش شدید ناگهانی سطح گلبول‌های سفید خون.",
        diffDiagnosis = "سالمونلوز شدید حاد، عفونت پریتونیت FIP روده.",
        protocol = "پنتوکسی‌فیلین، کواموکسی‌کلاو، سرم‌تراپی وریدی بسیار دقیق گرم شده، مراقبت‌های ایزوله حرارتی."
    ),
    TreatmentGuideline(
        id = "g_cat_fvp",
        species = "cat",
        name = "راینوتراکئیت ویروسی گربه (FHV-1)",
        symptoms = "زخم‌های قرنیه چشمی شاخه‌دار، ترشحات زیاد چشم و بینی، عطسه‌های دردآور.",
        diffDiagnosis = "کلامیدیا فلیس، مایکوپلاسما عفونی گربه‌ها.",
        protocol = "اسید آمینه ال‌لایزین، قطره ضد ویروس چشمی مکرر، بخارساز ملایم اتاق."
    ),
    TreatmentGuideline(
        id = "g_exotic_egg",
        species = "exotic",
        name = "بند آمدن تخم در پرندگان",
        symptoms = "کرنش شکم، نشستن کف قفس با بال‌های گشاده، تنفسی نامنظم حاد.",
        diffDiagnosis = "تومور تخمدان، چاقی مفرط پرندگان.",
        protocol = "تامین محیط گرم و با رطوبت بسیار بالا، تزریق کلسیم گلوکونات مناسب."
    ),
    TreatmentGuideline(
        id = "g_exotic_wet",
        species = "exotic",
        name = "دم خیس در همسترها",
        symptoms = "اسهال آبکی مداوم، خیسی و آلودگی مخرج، سستی کشنده.",
        diffDiagnosis = "اسهال باکتریایی سبک، ژیاردیازیس.",
        protocol = "مایع‌درمانی زیرپوستی گرم، داکسی‌سایکلین مناسب جوندگان."
    )
)

val staticFoodCatalog = listOf(
    // Dog Dry (🐕 Dry)
    FoodItem(
        brand = "Royal Canin Mini Adult (🐕 Dry)",
        description = "Balanced food for small breed adult dogs - 373 kcal/cup",
        descriptionFa = "غذای متوازن برای سگ‌های بالغ نژاد کوچک - ۳۷۳ کیلوکالری/پیمانه",
        calories = 373.0,
        isCanine = true,
        isDry = true
    ),
    FoodItem(
        brand = "Royal Canin Puppy Maxi Dry (🐕 Dry)",
        description = "Growth Support for Large Breed puppies - 343 kcal/cup",
        descriptionFa = "پشتیبانی از رشد توله سگ‌های نژاد بزرگ - ۳۴۳ کیلوکالری/پیمانه",
        calories = 343.0,
        isCanine = true,
        isDry = true
    ),
    FoodItem(
        brand = "Hill's Science Diet Adult Dry (🐕 Dry)",
        description = "Chicken & Barley Formula for optimal health - 363 kcal/cup",
        descriptionFa = "فرمول مرغ و جو برای سلامت بهینه - ۳۶۳ کیلوکالری/پیمانه",
        calories = 363.0,
        isCanine = true,
        isDry = true
    ),
    FoodItem(
        brand = "Purina Pro Plan Shredded Chicken (🐕 Dry)",
        description = "High Protein chicken & rice formula - 387 kcal/cup",
        descriptionFa = "فرمول پر پروتئین مرغ و برنج - ۳۸۷ کیلوکالری/پیمانه",
        calories = 387.0,
        isCanine = true,
        isDry = true
    ),
    FoodItem(
        brand = "Reflex Plus Adult Dog Salmon (🐕 Dry)",
        description = "Super Premium formula with Salmon for adult dogs - 395 kcal/cup",
        descriptionFa = "فرمول سوپر پرمیوم با ماهی سالمون برای سگ‌های بالغ - ۳۹۵ کیلوکالری/پیمانه",
        calories = 395.0,
        isCanine = true,
        isDry = true
    ),
    FoodItem(
        brand = "Nutri Pet Dry Dog Premium (نوتری پت 🐕)",
        description = "Iranian premium dry food with 29% protein - 355 kcal/cup",
        descriptionFa = "غذای خشک پرمیوم ایرانی با ۲۹٪ پروتئین - ۳۵۵ کیلوکالری/پیمانه",
        calories = 355.0,
        isCanine = true,
        isDry = true
    ),
    FoodItem(
        brand = "Josera Kids Puppy Dry (🐕 Dry)",
        description = "Premium German growth formula for medium/large puppies - 380 kcal/cup",
        descriptionFa = "فرمول پرمیوم آلمانی رشد توله سگ بزرگ - ۳۸۰ کیلوکالری/پیمانه",
        calories = 380.0,
        isCanine = true,
        isDry = true
    ),
    FoodItem(
        brand = "Celeb Dog Premium Dry (سلب پت 🐕)",
        description = "Premium local dry food with prebiotics - 360 kcal/cup",
        descriptionFa = "غذای خشک پرمیوم ایرانی حاوی پربیوتیک - ۳۶۰ کیلوکالری/پیمانه",
        calories = 360.0,
        isCanine = true,
        isDry = true
    ),

    // Dog Canned (🐕 Wet)
    FoodItem(
        brand = "Royal Canin Puppy Canned Can (🐕 Wet)",
        description = "Moist recipe for active puppy development - 335 kcal/can",
        descriptionFa = "فرمول مرطوب برای رشد توله سگ‌های فعال - ۳۳۵ کیلوکالری/کنسرو",
        calories = 335.0,
        isCanine = true,
        isDry = false
    ),
    FoodItem(
        brand = "Hill's Science Diet Chicken Can (🐕 Wet)",
        description = "Savoury stew with barley and meat veggies - 370 kcal/can",
        descriptionFa = "خوراک لذیذ با جو، گوشت و سبزیجات - ۳۷۰ کیلوکالری/کنسرو",
        calories = 370.0,
        isCanine = true,
        isDry = false
    ),
    FoodItem(
        brand = "Purina Pro Plan Beef & Rice Can (🐕 Wet)",
        description = "Classic wet high energy dog food - 408 kcal/can",
        descriptionFa = "غذای مرطوب کلاسیک پر انرژی سگ - ۴۰۸ کیلوکالری/کنسرو",
        calories = 408.0,
        isCanine = true,
        isDry = false
    ),
    FoodItem(
        brand = "Shayer Beef & Chicken Can (کنسرو شایر 🐕)",
        description = "100% natural meat pate for dogs, no preservatives - 310 kcal/can",
        descriptionFa = "پاته گوشت صد درصد طبیعی سگ بدون مواد نگهدارنده - ۳۱۰ کیلوکالری/کنسرو",
        calories = 310.0,
        isCanine = true,
        isDry = false
    ),
    FoodItem(
        brand = "Animonda GranCarno Adult Can (🐕 Wet)",
        description = "Pure beef and chicken chunks canned dog food - 390 kcal/can",
        descriptionFa = "کنسرو سگ حاوی تکه‌های گوشت گاو و مرغ خالص - ۳۹۰ کیلوکالری/کنسرو",
        calories = 390.0,
        isCanine = true,
        isDry = false
    ),
    FoodItem(
        brand = "Blue Buffalo Homestyle Beef Canned (🐕 Wet)",
        description = "Premium canned beef with garden veggies - 392 kcal/can",
        descriptionFa = "گوشت گاو کنسرو شده پرمیوم با سبزیجات - ۳۹۲ کیلوکالری/کنسرو",
        calories = 392.0,
        isCanine = true,
        isDry = false
    ),

    // Cat Dry (🐈 Dry)
    FoodItem(
        brand = "Royal Canin Feline Fit 32 (🐈 Dry)",
        description = "Balanced nutrition for moderately active cats - 315 kcal/cup",
        descriptionFa = "تغذیه متوازن برای گربه‌های با فعالیت متوسط - ۳۱۵ کیلوکالری/پیمانه",
        calories = 315.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Royal Canin Kitten Dry (🐈 Dry)",
        description = "High energy kibble for growth phase up to 12 months - 395 kcal/cup",
        descriptionFa = "غذای خشک پر انرژی برای دوره رشد تا ۱۲ ماهگی - ۳۹۵ کیلوکالری/پیمانه",
        calories = 395.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Royal Canin Hairball Care (🐈 Dry)",
        description = "Special dietary fiber formula to eliminate hairballs - 340.0 kcal/cup",
        descriptionFa = "فرمول فیبر رژیمی مخصوص برای حذف گلوله مویی - ۳۴۰ کیلوکالری/پیمانه",
        calories = 340.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Hill's Science Diet Adult Cat Optimal (🐈 Dry)",
        description = "Excellent dry food for digestion and urinary tract - 502 kcal/cup",
        descriptionFa = "غذای خشک عالی برای هضم و مجاری ادراری - ۵۰۲ کیلوکالری/پیمانه",
        calories = 502.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Purina Pro Plan Savor Salmon (🐈 Dry)",
        description = "Delicious dry cat salmon & rice formulation - 437 kcal/cup",
        descriptionFa = "فرمول لذیذ خشک سالمون و برنج گربه - ۴۳۷ کیلوکالری/پیمانه",
        calories = 437.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Reflex Plus Kitten Chicken (🐈 Dry)",
        description = "Super premium dry food for growing kittens - 385 kcal/cup",
        descriptionFa = "غذای خشک سوپر پرمیوم برای بچه گربه‌های در حال رشد - ۳۸۵ کیلوکالری/پیمانه",
        calories = 385.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Reflex Plus Adult Salmon (🐈 Dry)",
        description = "Super premium Omega-3 rich dry food for adult cats - 375 kcal/cup",
        descriptionFa = "غذای خشک غنی از امگا ۳ برای گربه‌های بالغ - ۳۷۵ کیلوکالری/پیمانه",
        calories = 375.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Nutri Pet Cat Premium Dry (نوتری پت 🐈)",
        description = "Iranian premium dry cat food, balanced minerals - 340 kcal/cup",
        descriptionFa = "غذای خشک پرمیوم ایرانی با مواد معدنی متوازن - ۳۴۰ کیلوکالری/پیمانه",
        calories = 340.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Josera Catelux Duck & Potato (🐈 Dry)",
        description = "German premium grain-free hairball controller - 410 kcal/cup",
        descriptionFa = "غذای کنترل‌کننده هربال بدون غلات آلمانی - ۴۱۰ کیلوکالری/پیمانه",
        calories = 410.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Shoodo Cat Dry Salmon (شیدو 🐈)",
        description = "LID premium Persian formulation with salmon - 365 kcal/cup",
        descriptionFa = "فرمولاسیون پرمیوم ایرانی شیدو با ماهی سالمون - ۳۶۵ کیلوکالری/پیمانه",
        calories = 365.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Celeb Cat Chicken & Turkey (سلب پت 🐈)",
        description = "Premium hypoallergenic turkey/poultry recipe - 360 kcal/cup",
        descriptionFa = "دستور غذایی بوقلمون و مرغ ضد حساسیت - ۳۶۰ کیلوکالری/پیمانه",
        calories = 360.0,
        isCanine = false,
        isDry = true
    ),
    FoodItem(
        brand = "Blue Buffalo Wilderness Cat Salmon (🐈 Dry)",
        description = "Grain-free high protein wild salmon kibbles - 443 kcal/cup",
        descriptionFa = "غذای خشک پر پروتئین بدون غلات سالمون وحشی - ۴۴۳ کیلوکالری/پیمانه",
        calories = 443.0,
        isCanine = false,
        isDry = true
    ),

    // Cat Canned (🐈 Wet)
    FoodItem(
        brand = "Royal Canin Intense Beauty In Gravy (🐈 Wet)",
        description = "Moist pouch with omega-3 for skin and coat beauty - 85 kcal/can",
        descriptionFa = "پوچ مرطوب با امگا ۳ برای زیبایی پوست و مو - ۸۵ کیلوکالری/کنسرو",
        calories = 85.0,
        isCanine = false,
        isDry = false
    ),
    FoodItem(
        brand = "Royal Canin Kitten Instinctive Gravy (🐈 Wet)",
        description = "Thin slices in gravy for baby teeth and immunity - 90 kcal/can",
        descriptionFa = "برش‌های نازک در سس برای دندان‌ها و ایمنی بچه گربه - ۹۰ کیلوکالری/کنسرو",
        calories = 90.0,
        isCanine = false,
        isDry = false
    ),
    FoodItem(
        brand = "Hill's Science Diet Wet Salmon (🐈 Wet)",
        description = "Seared salmon chunks in a rich wet savory glaze - 75 kcal/can",
        descriptionFa = "تکه‌های ماهی سالمون تفت داده شده در سس لذیذ - ۷۵ کیلوکالری/کنسرو",
        calories = 75.0,
        isCanine = false,
        isDry = false
    ),
    FoodItem(
        brand = "Purina Pro Plan Savor Salmon Can (🐈 Wet)",
        description = "Seafood delicious wet food paste for urinary tract - 95 kcal/can",
        descriptionFa = "پاته غذای مرطوب لذیذ برای مجاری ادراری - ۹۵ کیلوکالری/کنسرو",
        calories = 95.0,
        isCanine = false,
        isDry = false
    ),
    FoodItem(
        brand = "Shayer Chicken & Beef Canned (کنسرو شایر 🐈)",
        description = "High protein wet pate made entirely with chicken and beef - 92 kcal/can",
        descriptionFa = "پاته مرطوب پر پروتئین ساخته شده از مرغ و گوساله - ۹۲ کیلوکالری/کنسرو",
        calories = 92.0,
        isCanine = false,
        isDry = false
    ),
    FoodItem(
        brand = "Shayer Gourmet Turkey & Duck Can (کنسرو شایر 🐈)",
        description = "Succulent gourmet wet bits for picky adult cats - 98 kcal/can",
        descriptionFa = "لقمه‌های لذیذ مرطوب بوقلمون و اردک برای گربه‌های بدغذا - ۹۸ کیلوکالری/کنسرو",
        calories = 98.0,
        isCanine = false,
        isDry = false
    ),
    FoodItem(
        brand = "GimCat ShinyCat Tuna & Chicken (🐈 Wet)",
        description = "Slices of premium real tuna fillet and chicken breast - 80 kcal/can",
        descriptionFa = "فیله تونا و سینه مرغ پرمیوم جی‌ام‌کت - ۸۰ کیلوکالری/کنسرو",
        calories = 80.0,
        isCanine = false,
        isDry = false
    ),
    FoodItem(
        brand = "Wanpy Chicken & Crab Pouch (پوچ وانپی 🐈)",
        description = "Delicious wet jelly pouch for everyday hydration - 65 kcal/can",
        descriptionFa = "پوچ ژله‌ای مرطوب و لذیذ وانپی برای هیدراتاسیون - ۶۵ کیلوکالری/کنسرو",
        calories = 65.0,
        isCanine = false,
        isDry = false
    ),
    FoodItem(
        brand = "Animonda Carny Adult Beef & Cod (🐈 Wet)",
        description = "German holistic fresh meat canned pate - 110 kcal/can",
        descriptionFa = "پاته گوشت تازه آلمانی آنیموندا کارنی - ۱۱۰ کیلوکالری/کنسرو",
        calories = 110.0,
        isCanine = false,
        isDry = false
    ),
    FoodItem(
        brand = "Blue Buffalo Wilderness Chicken Wet (🐈 Wet)",
        description = "Pate grain-free wild chicken high protein wet meal - 120 kcal/can",
        descriptionFa = "پاته بدون غلات مرغ وحشی پر پروتئین - ۱۲۰ کیلوکالری/کنسرو",
        calories = 120.0,
        isCanine = false,
        isDry = false
    )
)

