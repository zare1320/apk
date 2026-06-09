package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey
    val phoneNumber: String, // Or email
    val userType: String, // "vet" or "owner"
    val fullName: String,
    val identification: String = "", // Professional ID / Student ID
    val workplaceOrUni: String = "", // Workplace or University
    val specialty: String = "", // Vet specialty
    val isLoggedIn: Boolean = false,
    val coins: Int = 100,
    val gender: String = "آقا" // "آقا" or "خانم"
) {
    fun getFullTitle(): String {
        val cleanName = fullName.replace("دکتر ", "").replace("دکتر", "").trim()
        return if (userType == "vet") {
            val isStudent = workplaceOrUni.contains("دانشگاه")
            if (isStudent) {
                if (gender == "خانم") "خانم دکتر دانشجوی گرامی $cleanName" else "جناب آقای دکتر دانشجوی گرامی $cleanName"
            } else {
                if (gender == "خانم") "خانم دکتر $cleanName" else "آقای دکتر $cleanName"
            }
        } else {
            if (gender == "خانم") "خانم $cleanName" else "جناب آقای $cleanName"
        }
    }
}

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val species: String, // "dog", "cat", "bird", "rodent", "aquatic", "amphibian"
    val breed: String,
    val weight: Double, // in kg
    val age: String = "",
    val gender: String = "نر", // "نر" (Male) or "ماده" (Female)
    val birthdate: String = "",
    val healthStatus: String = "سالم", // "سالم" (Healthy) or "بیمار" (Sick)
    val isNeutered: Boolean = false, // Spayed/Neutered
    val ownerName: String = "",
    val ownerPhone: String = "",
    val lastVaccination: String = "",
    val lastParasiteTherapy: String = "",
    val recordNumber: String = "" // Optional record numbering, starting at 10001
)

@Entity(tableName = "prescriptions")
data class Prescription(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val petId: Int = 0,
    val petName: String,
    val ownerPhone: String,
    val doctorName: String = "",
    val drugName: String,
    val concentration: String,
    val rangeRoute: String,
    val dosageUsed: Double, // Default dosage selected by user (mg/kg)
    val calculatedDose: Double, // calculated mg
    val calculatedVolume: Double, // calculated volume (ml)
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val petId: Int = 0,
    val petName: String,
    val eventType: String, // "واکسیناسیون", "ضدانگل", "معاینه", "گرومینگ"
    val eventDate: String, // Solat / Jalali / Gregorian formatted date
    val notes: String = "",
    val isCompleted: Boolean = false
)
