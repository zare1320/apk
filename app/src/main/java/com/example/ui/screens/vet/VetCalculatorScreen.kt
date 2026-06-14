package com.example.ui.screens.vet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val activeExaminedPet by viewModel.activeExaminedPet.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()

    var activeCalculator by remember { mutableStateOf("مایع‌درمانی") }

    // Forms States
    var weightInput by remember { mutableStateOf("") }

    // Init fields from active pet if available
    LaunchedEffect(activeExaminedPet) {
        activeExaminedPet?.let { pet ->
            weightInput = pet.weight.toString()
        }
    }

    val calculatorsList = listOf(
        "مایع‌درمانی", "انتقال خون", "محاسبه کالی غذا", "زمان زایمان", "سن معادل انسان", "تریاژ تروما"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Clinical calculators titles
        Text(
            text = if (currentLanguage == "en") "🧮 Clinical Assessment & Calculators:" else "🧮 ابزارهای سنجش و محاسبه‌گرهای کلینیکال:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = if (currentLanguage == "en") TextAlign.Left else TextAlign.Right
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
                val label = remember(cal, currentLanguage) {
                    if (currentLanguage != "en") cal
                    else when (cal) {
                        "مایع‌درمانی" -> "Fluid Therapy"
                        "انتقال خون" -> "Blood Transfusion"
                        "محاسبه کالی غذا" -> "Calorie Needs"
                        "زمان زایمان" -> "Gestation"
                        "سن معادل انسان" -> "Human-Age Equiv."
                        "تریاژ تروما" -> "Trauma Triage"
                        else -> cal
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgCol)
                        .clickable { activeCalculator = cal }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(label, fontSize = 11.sp, color = textCol, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))



        CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current) {
            // Interactive Display
            when (activeCalculator) {
                "مایع‌درمانی" -> {
                    FluidTherapyCalculator(activePet = activeExaminedPet, selectedSpecies = selectedSpecies)
                }
                "انتقال خون" -> {
                    BloodTransfusionCalculator(activePet = activeExaminedPet, initWeight = weightInput, selectedSpecies = selectedSpecies)
                }
                "محاسبه کالری غذا" -> {
                    CalorieCalculatorView(activePet = activeExaminedPet, initWeight = weightInput, selectedSpecies = selectedSpecies)
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
        }
    }
}
