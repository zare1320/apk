package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.VetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: VetRepository) : ViewModel() {

    // --- Authentication ---
    val activeSession: StateFlow<UserSession?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- UI/Language/Theme Settings (Room state-driven or in-memory) ---
    private val _themeMode = MutableStateFlow("dark") // "light" or "dark" (default dark to feel premium)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _currentLanguage = MutableStateFlow("en") // "fa", "en", "ar"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _activeSubscription = MutableStateFlow("gold") // "free", "silver", "gold", "diamond"
    val activeSubscription: StateFlow<String> = _activeSubscription.asStateFlow()

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

    // --- Drug Catalog & Custom Added Drug Store ---
    private val _customDrugs = MutableStateFlow<List<DrugItem>>(emptyList())
    val customDrugs: StateFlow<List<DrugItem>> = _customDrugs.asStateFlow()

    init {
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
        _themeMode.value = if (_themeMode.value == "light") "dark" else "light"
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun setSubscription(sub: String) {
        _activeSubscription.value = sub
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
        specialty: String = ""
    ) {
        viewModelScope.launch {
            val currentSession = repository.getActiveSessionSync()
            if (currentSession != null) {
                val updatedSession = currentSession.copy(
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    identification = identification,
                    workplaceOrUni = workplaceOrUni,
                    specialty = specialty
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
        _customDrugs.value = _customDrugs.value + newDrug
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
            _customDrugs.value = emptyList()
            physicalComplaints.value = emptyMap()
            physicalSigns.value = emptyMap()
            labResults.value = emptyMap()
        }
    }
}

// Data models for the Static Drugs catalog
data class DrugItem(
    val id: String,
    val nameGeneric: String,
    val nameScientific: String,
    val category: String, // e.g. Antibiotics, Anesthetics etc.
    val concentrationVal: Double, // in mg/ml
    val concentrationText: String, // available packaging
    val rangeAndRoute: String, // e.g. "10-20 mg/kg SC"
    val rangeMin: Double,
    val rangeMax: Double,
    val route: String,
    val defaultDosage: Double // Default drug dosage in mg/kg
)

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
