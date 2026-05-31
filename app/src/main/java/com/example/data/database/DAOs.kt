package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_sessions WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveSession(): Flow<UserSession?>

    @Query("SELECT * FROM user_sessions WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveSessionSync(): UserSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSession)

    @Query("UPDATE user_sessions SET isLoggedIn = 0")
    suspend fun logoutAll()
}

@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY id DESC")
    fun getAllPets(): Flow<List<Pet>>

    @Query("SELECT * FROM pets WHERE id = :id LIMIT 1")
    fun getPetById(id: Int): Flow<Pet?>

    @Query("SELECT * FROM pets WHERE ownerPhone = :phone ORDER BY id DESC")
    fun getPetsByOwnerPhone(phone: String): Flow<List<Pet>>

    @Query("SELECT recordNumber FROM pets WHERE recordNumber != '' ORDER BY id DESC LIMIT 1")
    suspend fun getLatestRecordNumber(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: Pet): Long

    @Delete
    suspend fun deletePet(pet: Pet)
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions ORDER BY id DESC")
    fun getAllPrescriptions(): Flow<List<Prescription>>

    @Query("SELECT * FROM prescriptions WHERE ownerPhone = :phone ORDER BY id DESC")
    fun getPrescriptionsByOwnerPhone(phone: String): Flow<List<Prescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: Prescription)

    @Delete
    suspend fun deletePrescription(prescription: Prescription)

    @Query("DELETE FROM prescriptions")
    suspend fun clearAll()
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY eventDate ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE petId = :petId ORDER BY eventDate ASC")
    fun getEventsByPetId(petId: Int): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)
}
