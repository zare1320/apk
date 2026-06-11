package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Map
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.VetRepository
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.owner.OwnerCalendarScreen
import com.example.ui.screens.owner.OwnerDashboardScreen
import com.example.ui.screens.owner.OwnerMapScreen
import com.example.ui.screens.owner.OwnerPrescriptionsScreen
import com.example.ui.screens.owner.OwnerProfileScreen
import com.example.ui.screens.vet.VetCalculatorScreen
import com.example.ui.screens.vet.VetDashboardScreen
import com.example.ui.screens.vet.VetDiagnosisTreatmentScreen
import com.example.ui.screens.vet.VetDrugManualScreen
import com.example.ui.screens.vet.VetProfileScreen
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room DB & Repo Initialization
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = VetRepository(database)

        // ViewModel factory
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        val viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val activeSession by viewModel.activeSession.collectAsState()
            val currentLanguage by viewModel.currentLanguage.collectAsState()

            val layoutDirection = if (currentLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

            var authScreenState by remember { mutableStateOf("login") } // "login" or "register"

            MyApplicationTheme(
                darkTheme = themeMode == "dark",
                layoutDirection = layoutDirection
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = Triple(themeMode, currentLanguage, activeSession == null),
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "theme_and_lang_transition"
                    ) { (_, _, isAuthEmpty) ->
                        if (activeSession == null) {
                            // User is NOT logged in - Auth screen stack
                            Crossfade(targetState = authScreenState) { screen ->
                                when (screen) {
                                    "login" -> LoginScreen(
                                        viewModel = viewModel,
                                        onNavigateToRegister = { authScreenState = "register" }
                                    )
                                    "register" -> RegisterScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { authScreenState = "login" }
                                    )
                                }
                            }
                        } else {
                            // User is Logged-In
                            val role = activeSession?.userType ?: "vet"
                            if (role == "vet") {
                                VetLayoutContainer(viewModel = viewModel)
                            } else {
                                OwnerLayoutContainer(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Layout holder for Veterinarian / Student role
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetLayoutContainer(viewModel: MainViewModel) {
    var activeVetTab by remember { mutableStateOf("داشبورد") }

    Scaffold(
        topBar = {
            val session by viewModel.activeSession.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Right side: Doctor circle icon + profile info in front of it (to the left in RTL)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Circular doctor icon
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .background(Color.White, androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (session?.gender == "خانم") "👩‍⚕️" else "👨‍⚕️", fontSize = 24.sp)
                            }

                            // User Info column
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
                            ) {
                                val userTitle = session?.getFullTitle() ?: "آقای دکتر مسعود زارع"
                                val workplaceOrUni = session?.workplaceOrUni?.ifEmpty { "بیمارستان تخصصی پارس" } ?: "بیمارستان تخصصی پارس"

                                Text(
                                    text = userTitle,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = workplaceOrUni,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Left side: Theme switcher icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                                .clickable { viewModel.toggleTheme() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (themeMode == "dark") "☀️" else "🌙", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text "Vetaris" brought under application user details
                    Text(
                        text = "Vetaris",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Right // Force right alignment for Persian
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars
            ) {
                listOf(
                    NavItem("داشبورد", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "داشبورد"),
                    NavItem("دارونامه", Icons.Filled.MedicalServices, Icons.Outlined.MedicalServices, "دارونامه"),
                    NavItem("تشخیص و درمان", Icons.Filled.Healing, Icons.Outlined.Healing, "تشخیص"),
                    NavItem("ابزار محاسبه‌گر", Icons.Filled.Calculate, Icons.Outlined.Calculate, "محاسبه‌گر"),
                    NavItem("پروفایل", Icons.Filled.Person, Icons.Outlined.Person, "پروفایل")
                ).forEach { item ->
                    val isSelected = activeVetTab == item.tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeVetTab = item.tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(item.label, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = activeVetTab) { tab ->
                when (tab) {
                    "داشبورد" -> VetDashboardScreen(viewModel = viewModel)
                    "دارونامه" -> VetDrugManualScreen(viewModel = viewModel)
                    "تشخیص و درمان" -> VetDiagnosisTreatmentScreen(viewModel = viewModel)
                    "ابزار محاسبه‌گر" -> VetCalculatorScreen(viewModel = viewModel)
                    "پروفایل" -> VetProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// Layout holder for Pet Owner role
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerLayoutContainer(viewModel: MainViewModel) {
    var activeOwnerTab by remember { mutableStateOf("داشبورد") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                val session by viewModel.activeSession.collectAsState()
                val themeMode by viewModel.themeMode.collectAsState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🐕", fontSize = 18.sp)
                            }
                            Text(
                                text = "دستیار همراه صاحب پت",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                                .clickable { viewModel.toggleTheme() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (themeMode == "dark") "☀️" else "🌙", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "بخش مراقبت هوشمند و نوبت‌دهی",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "خوش آمدید، " + (session?.getFullTitle() ?: "صاحب پت محترم"),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, RoundedCornerShape(50.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (session?.gender == "خانم") "👩" else "👨", fontSize = 20.sp)
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars
                ) {
                    listOf(
                        NavItem("داشبورد", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "داشبورد"),
                        NavItem("نسخه", Icons.Filled.Assignment, Icons.Outlined.Assignment, "نسخه"),
                        NavItem("تقویم", Icons.Filled.CalendarToday, Icons.Outlined.CalendarToday, "تقویم"),
                        NavItem("نقشه", Icons.Filled.Map, Icons.Outlined.Map, "نقشه"),
                        NavItem("پروفایل", Icons.Filled.Person, Icons.Outlined.Person, "پروفایل")
                    ).forEach { item ->
                        val isSelected = activeOwnerTab == item.tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeOwnerTab = item.tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(item.label, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = activeOwnerTab) { tab ->
                    when (tab) {
                        "داشبورد" -> OwnerDashboardScreen(viewModel = viewModel)
                        "نسخه" -> OwnerPrescriptionsScreen(viewModel = viewModel)
                        "تقویم" -> OwnerCalendarScreen(viewModel = viewModel)
                        "نقشه" -> OwnerMapScreen(viewModel = viewModel)
                        "پروفایل" -> OwnerProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

data class NavItem(
    val tab: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)
