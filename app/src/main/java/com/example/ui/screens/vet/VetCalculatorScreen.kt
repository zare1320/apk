package com.example.ui.screens.vet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import com.example.ui.screens.vet.calculators.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VetCalculatorScreen(viewModel: MainViewModel) {
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var activeCalculator by remember(currentLang) { mutableStateOf<String?>(null) }

    val standardCalName = activeCalculator?.let {
        when (it) {
            "Fluid Therapy", "مایع‌درمانی" -> "مایع‌درمانی"
            "Blood Transfusion", "انتقال خون" -> "انتقال خون"
            "Calorie Calculator", "محاسبه کالری غذا" -> "محاسبه کالری غذا"
            "Gestation Calendar", "زمان زایمان" -> "زمان زایمان"
            "Human Age Equiv.", "سن معادل انسان" -> "سن معادل انسان"
            "Trauma Triage", "تریاژ تروما" -> "تریاژ تروما"
            else -> it
        }
    }

    // Forms States
    var weightInput by remember { mutableStateOf("") }

    // Init fields from active pet if available
    LaunchedEffect(activeExaminedPet) {
        activeExaminedPet?.let { pet ->
            weightInput = pet.weight.toString()
        }
    }

    val calculatorsList = if (currentLang == "en") {
        listOf("Fluid Therapy", "Blood Transfusion", "Calorie Calculator", "Gestation Calendar", "Human Age Equiv.", "Trauma Triage")
    } else {
        listOf("مایع‌درمانی", "انتقال خون", "محاسبه کالری غذا", "زمان زایمان", "سن معادل انسان", "تریاژ تروما")
    }

    val layoutDirection = if (currentLang == "en") androidx.compose.ui.unit.LayoutDirection.Ltr else androidx.compose.ui.unit.LayoutDirection.Rtl

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Clinical calculators titles
            Text(
                text = if (currentLang == "en") "🧮 Clinical Diagnostics & Assessment Tools:" else "🧮 ابزارهای سنجش و محاسبه‌گرهای کلینیکال:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = if (currentLang == "en") TextAlign.Left else TextAlign.Right
            )

            // Horizontal selections
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                calculatorsList.forEach { cal ->
                    val isSel = activeCalculator == cal
                    val bgCol = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val textCol = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgCol)
                            .clickable { activeCalculator = cal }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(cal, fontSize = 11.sp, color = textCol, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (activeExaminedPet == null && standardCalName != "انتقال خون") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) Color(0xFF7F1D1D) else Color(0xFFFEE2E2),
                        contentColor = if (isSystemInDarkTheme()) Color(0xFFFECACA) else Color(0xFF991B1B)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (currentLang == "en") "⚠️ No active patient selected." else "⚠️ بیمار فعالی انتخاب نشده است.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLang == "en") {
                                "Calculations are performed based on a default weight of 1 kg. Please activate a patient record at the front desk first for accurate calculations."
                            } else {
                                "محاسبات بر اساس وزن پیش‌فرض ۱ کیلوگرم انجام می‌شوند. لطفاً ابتدا پرونده یک بیمار را در پذیرش فعال کنید تا محاسبات دقیق‌تری داشته باشید."
                            },
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Display
            if (standardCalName != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeCalculator ?: "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    TextButton(onClick = { activeCalculator = null }) {
                        Text(
                            text = if (currentLang == "en") "❌ Close Tool" else "❌ بستن ابزار",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                when (standardCalName) {
                    "مایع‌درمانی" -> {
                        FluidTherapyCalculator(activePet = activeExaminedPet, selectedSpecies = selectedSpecies, currentLang = currentLang)
                    }
                    "انتقال خون" -> {
                        BloodTransfusionCalculator(activePet = activeExaminedPet, initWeight = weightInput, selectedSpecies = selectedSpecies, currentLang = currentLang)
                    }
                    "محاسبه کالری غذا" -> {
                        CalorieCalculatorView(activePet = activeExaminedPet, initWeight = weightInput, selectedSpecies = selectedSpecies, currentLang = currentLang)
                    }
                    "زمان زایمان" -> {
                        GestationCalculatorView(viewModel)
                    }
                    "سن معادل انسان" -> {
                        HumanAgeCalculatorView(initWeight = weightInput)
                    }
                    "تریاژ تروما" -> {
                        TraumaTriageView()
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLang == "en") {
                            "Select a calculator tool from the options above to view its interactive controls."
                        } else {
                            "یکی از ابزارهای محاسبه‌گر بالا را انتخاب کنید تا کنترل‌های تعاملی آن نمایش داده شود."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
