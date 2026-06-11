package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow

class VetRepository(private val db: AppDatabase) {

    private val userSessionDao = db.userSessionDao()
    private val petDao = db.petDao()
    private val prescriptionDao = db.prescriptionDao()
    private val calendarEventDao = db.calendarEventDao()

    // --- Authentication / User Session ---
    val activeSession: Flow<UserSession?> = userSessionDao.getActiveSession()
    suspend fun getActiveSessionSync(): UserSession? = userSessionDao.getActiveSessionSync()
    suspend fun getSessionByPhone(phone: String): UserSession? = userSessionDao.getSessionByPhone(phone)

    suspend fun insertSession(session: UserSession) {
        userSessionDao.insertSession(session)
    }

    suspend fun login(session: UserSession) {
        userSessionDao.logoutAll()
        userSessionDao.insertSession(session.copy(isLoggedIn = true))
    }

    suspend fun logout() {
        userSessionDao.logoutAll()
    }

    // --- Pets ---
    val allPets: Flow<List<Pet>> = petDao.getAllPets()

    fun getPetById(id: Int): Flow<Pet?> = petDao.getPetById(id)

    fun getPetsByOwnerPhone(phone: String): Flow<List<Pet>> = petDao.getPetsByOwnerPhone(phone)

    suspend fun getLatestRecordNumber(): String? = petDao.getLatestRecordNumber()

    suspend fun insertPet(pet: Pet): Long {
        var finalRecord = pet.recordNumber
        if (finalRecord.isEmpty() && pet.recordNumber.isEmpty()) {
            val latest = getLatestRecordNumber()
            val latestInt = latest?.toIntOrNull() ?: 10000
            finalRecord = (latestInt + 1).toString()
        }
        return petDao.insertPet(pet.copy(recordNumber = finalRecord))
    }

    suspend fun deletePet(pet: Pet) {
        petDao.deletePet(pet)
    }

    // --- Prescriptions ---
    val allPrescriptions: Flow<List<Prescription>> = prescriptionDao.getAllPrescriptions()

    fun getPrescriptionsByOwnerPhone(phone: String): Flow<List<Prescription>> =
        prescriptionDao.getPrescriptionsByOwnerPhone(phone)

    suspend fun insertPrescription(prescription: Prescription) {
        prescriptionDao.insertPrescription(prescription)
    }

    suspend fun deletePrescription(prescription: Prescription) {
        prescriptionDao.deletePrescription(prescription)
    }

    suspend fun clearAllPrescriptions() {
        prescriptionDao.clearAll()
    }

    // --- Calendar Events ---
    val allEvents: Flow<List<CalendarEvent>> = calendarEventDao.getAllEvents()

    fun getEventsByPetId(petId: Int): Flow<List<CalendarEvent>> = calendarEventDao.getEventsByPetId(petId)

    suspend fun insertEvent(event: CalendarEvent) {
        calendarEventDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: CalendarEvent) {
        calendarEventDao.deleteEvent(event)
    }
}
