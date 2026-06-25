package com.example.ui.screens.vet.calculators

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.example.data.database.Pet

@Composable
fun BloodTransfusionCalculator(
    activePet: Pet? = null,
    initWeight: String = "",
    selectedSpecies: String? = null,
    currentLang: String = "en"
) {
    var weightStr by remember(initWeight) { mutableStateOf(initWeight.ifEmpty { "1" }) }
    var currentPcv by remember { mutableStateOf("15") } // Patient's current Hematocrit %
    var targetPcv by remember { mutableStateOf("25") } // Target Hematocrit %
    var donorPcv by remember { mutableStateOf("40") } // Donor's standard Hematocrit %

    val isDog = if (activePet != null) {
        activePet.species.lowercase() != "cat"
    } else {
        selectedSpecies?.lowercase() != "cat"
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val themeCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.White
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface

    val pinkBgColor = if (isDark) Color(0xFF4C0519) else Color(0xFFFCE7F3)
    val pinkTextColor = if (isDark) Color(0xFFFDA4AF) else Color(0xFF9D174D)
    val pinkBorderColor = if (isDark) Color(0xFFE11D48) else Color(0xFFF472B6)

    val bulletColor = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF818CF8)

    val weight = weightStr.toDoubleOrNull() ?: 10.0
    val recipientPcv = currentPcv.toDoubleOrNull() ?: 15.0
    val target = targetPcv.toDoubleOrNull() ?: 25.0
    val donor = donorPcv.toDoubleOrNull() ?: 40.0

    // Dose = ((Target - Recipient) / Donor) * Weight * N
    val n = if (isDog) 90.0 else 60.0
    val bloodVolume = if (donor > 0) {
        val result = ((target - recipientPcv) / donor) * weight * n
        if (result < 0) 0.0 else result
    } else {
        0.0
    }
    val resultFormatted = String.format("%.1f", bloodVolume)

    // Accordions visibility
    var isAboutExpanded by remember { mutableStateOf(false) }
    var isProcExpanded by remember { mutableStateOf(false) }
    var isCrossExpanded by remember { mutableStateOf(false) }

    val direction = if (currentLang == "fa") {
        androidx.compose.ui.unit.LayoutDirection.Rtl
    } else {
        androidx.compose.ui.unit.LayoutDirection.Ltr
    }

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            if (activePet == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2),
                        contentColor = if (isDark) Color(0xFFFECACA) else Color(0xFF991B1B)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFFFCA5A5)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (currentLang == "fa") "⚠️ ابزار تخصصی انتقال خون" else "⚠️ Specialized Transfusion Tool",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLang == "fa") {
                                "این ابزار به طور اختصاصی برای سگ‌ها و گربه‌ها طراحی شده است. کاربر گرامی، بیمار فعالی انتخاب نشده است. محاسبات بر اساس وزن پیش‌فرض ۱ کیلوگرم و گونه پیش‌فرض سگ انجام می‌شوند. لطفاً ابتدا پرونده بیمار را در پذیرش فعال کنید تا محاسبات دقیق‌تری داشته باشید."
                            } else {
                                "This tool is exclusively for dogs and cats. Dear user, no active patient has been selected. Calculations are performed based on a default weight of 1 kg and a default animal species of dog. Please first activate a patient's record in admission for more accurate calculations."
                            },
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Calculation Fields List Card
            Card(
                colors = CardDefaults.cardColors(containerColor = themeCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    
                    // Row 1: Recipient's weight
                    val currentWeightKg = weightStr.toDoubleOrNull() ?: 0.0
                    val showWeightWarning = currentWeightKg > 0.0 && ((!isDog && currentWeightKg > 12.0) || (isDog && currentWeightKg > 80.0))
                    val weightBorderColors = if (showWeightWarning) {
                        OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFFF59E0B).copy(alpha = 0.6f),
                            focusedLabelColor = Color(0xFFD97706),
                            unfocusedLabelColor = Color(0xFFD97706).copy(alpha = 0.8f)
                        )
                    } else {
                        OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚖️", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLang == "fa") "وزن گیرنده" else "Recipient Weight",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textPrimary
                                    )
                                }
                                Text(
                                    text = if (currentLang == "fa") "وزن به کیلوگرم" else "Weight in Kg",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }
                            
                            OutlinedTextField(
                                value = weightStr,
                                onValueChange = { weightStr = it },
                                placeholder = {
                                    Text(
                                        text = if (currentLang == "fa") "وزن به کیلوگرم" else "Weight in kg",
                                        fontSize = 12.sp
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = weightBorderColors,
                                singleLine = true,
                                modifier = Modifier.width(150.dp)
                            )
                        }
                        if (showWeightWarning) {
                            Text(
                                text = "⚠️ آیا از وزن وارد شده اطمینان دارید؟",
                                color = Color(0xFFD97706),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    // Row 2: Desired PCV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💉", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLang == "fa") "PCV مورد نظر" else "Desired PCV",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textPrimary
                                )
                            }
                            Text(
                                text = if (currentLang == "fa") "پس از انتقال خون" else "After transfusion",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                        
                        OutlinedTextField(
                            value = targetPcv,
                            onValueChange = { targetPcv = it },
                            placeholder = {
                                Text(
                                    text = if (currentLang == "fa") "درصد PCV هدف" else "Desired PCV %",
                                    fontSize = 12.sp
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true,
                            modifier = Modifier.width(150.dp)
                        )
                    }

                    // Row 3: Current PCV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💉", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLang == "fa") "PCV فعلی" else "Current PCV",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textPrimary
                                )
                            }
                            Text(
                                text = if (currentLang == "fa") "پیش از انتقال خون" else "Before transfusion",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                        
                        OutlinedTextField(
                            value = currentPcv,
                            onValueChange = { currentPcv = it },
                            placeholder = {
                                Text(
                                    text = if (currentLang == "fa") "درصد PCV فعلی" else "Current PCV %",
                                    fontSize = 12.sp
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true,
                            modifier = Modifier.width(150.dp)
                        )
                    }

                    // Row 4: Donor PCV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💉", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLang == "fa") "PCV اهداکننده" else "Donor PCV",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textPrimary
                                )
                            }
                            Text(
                                text = if (currentLang == "fa") "PCV خون اهداکننده" else "PCV of Donor Blood",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                        
                        OutlinedTextField(
                            value = donorPcv,
                            onValueChange = { donorPcv = it },
                            placeholder = {
                                Text(
                                    text = if (currentLang == "fa") "درصد PCV اهداکننده" else "Donor PCV %",
                                    fontSize = 12.sp
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true,
                            modifier = Modifier.width(150.dp)
                        )
                    }

                    // Row 4: Blood Volume Output
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🩸", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLang == "fa") "حجم خون" else "Blood Volume",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                            }
                            Text(
                                text = if (currentLang == "fa") "محاسبه شده به میلی‌لیتر" else "Calculated in mls",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isDog) "🐕" else "🐈",
                                fontSize = 24.sp
                            )
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(pinkBgColor)
                                    .border(1.dp, pinkBorderColor, RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                val dispResult = if (currentLang == "fa") "$resultFormatted میلی‌لیتر" else "$resultFormatted ml"
                                Text(
                                    text = dispResult,
                                    color = pinkTextColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info blocks (Transfusion Rate, Monitoring, Reactions)
            Text(
                text = if (currentLang == "fa") "نرخ انتقال خون:" else "Transfusion Rate:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BulletItem(
                    text = if (currentLang == "fa") "شروع تمامی انتقال‌های خون با نرخ ۱ تا ۲ میلی‌لیتر در دقیقه" else "Start all transfusions at 1 to 2 ml/minute",
                    bulletColor = bulletColor
                )
                BulletItem(
                    text = if (currentLang == "fa") "سگ‌های بالغ: حداکثر نرخ ۳ تا ۶ میلی‌لیتر در دقیقه" else "Adult dogs: maximum rate of 3 to 6 ml/minute",
                    bulletColor = bulletColor
                )
                BulletItem(
                    text = if (currentLang == "fa") "گربه‌ها، بچه‌گربه‌ها و توله‌سگ‌ها: حداکثر نرخ ۱ تا ۲ میلی‌لیتر در دقیقه" else "Cats, kittens, puppies: maximum rate of 1 to 2 ml/minute",
                    bulletColor = bulletColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (currentLang == "fa") "پایش (مانیتورینگ):" else "Monitoring:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BulletItem(
                    text = if (currentLang == "fa") "ثبت مقادیر پایه شامل PCV/TP، وزن، دما، نبض، نرخ تنفس، زمان پر شدن مویرگی (CRT) و رنگ مخاط پیش از شروع انتقال" else "Document baseline PCV/TP, weight, temperature, pulse, respiratory rate, CRT and MM color prior to transfusion",
                    bulletColor = bulletColor
                )
                BulletItem(
                    text = if (currentLang == "fa") "طی ۶۰ دقیقه اول: کنترل علائم حیاتی (TPR)، CRT و رنگ مخاط هر ۱۵ دقیقه" else "During first 60 minutes: TPR, CRT and MM color Q15 minutes",
                    bulletColor = bulletColor
                )
                BulletItem(
                    text = if (currentLang == "fa") "سپس هر ۳۰ دقیقه تا انتهای زمان انتقال خون" else "Then Q30 minutes for the duration of the transfusion",
                    bulletColor = bulletColor
                )
                BulletItem(
                    text = if (currentLang == "fa") "در صورت مشاهده هرگونه نشانه واکنش به انتقال، بلافاصله انتقال را متوقف کرده و درمان را آغاز کنید." else "If signs of transfusion reaction observed, Stop transfusion and initiate treatment.",
                    bulletColor = bulletColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (currentLang == "fa") "واکنش‌های انتقال خون:" else "Transfusion Reactions:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletItem(
                    title = if (currentLang == "fa") "واکنش‌های همولیتیک با واسطه ایمنی" else "Immune-Mediated Hemolytic Reactions",
                    text = if (currentLang == "fa") {
                        "واکنش‌های حاد. ناشی از وجود آنتی‌بادی‌های قبلی یا حساسیت ایجاد شده در انتقال قبلی. نادر اما جدی‌ترین واکنش. اولین نشانه بالینی هیپوترمی (کاهش دمای بدن) است. سایر نشانه‌ها شامل استفراغ، تاکی‌کاردی (ضربان قلب سریع)، تاکی‌پنه (تنفس سریع)، ضعف، لرزش، تورم صورت، افت فشار خون، هموگلوبینمی و هموگلوبینوری است."
                    } else {
                        "Acute reactions. Result of preexisting antibodies or sensitization from a previous transfusion. Rare but the most serious reaction. Earliest clinical sign is hypothermia. Other signs include vomiting, tachycardia, tachypnea, weakness, tremors, facial swelling, hypotension, hemoglobinemia, hemoglobinuria."
                    },
                    bulletColor = bulletColor
                )
                BulletItem(
                    title = if (currentLang == "fa") "واکنش‌های غیرهمولیتیک با واسطه ایمنی" else "Immune-Mediated Non-Hemolytic Reactions",
                    text = if (currentLang == "fa") {
                        "ناشی از آنتی‌بادی علیه گلبول‌های قرمز، لکوسیت‌ها، پلاکت‌ها یا پروتئین‌های پلاسما. غالباً گذرا هستند. نشانه‌های بالینی: آنافیلاکسی، کهیر، خارش، تب، تاکی‌پنه، تنگی نفس، استفراغ و علائم عصبی."
                    } else {
                        "Result from antibodies to RBCs, leukocytes, platelets, or plasma proteins. Most often transient. Clinical signs: anaphylaxis, urticaria, pruritis, hyperthermia, tachypena, dyspnea, vomiting, neurologic signs"
                    },
                    bulletColor = bulletColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (currentLang == "fa") "اطلاعات انتقال خون" else "Transfusion Info",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Accordion 1: About Transfusions
            AccordionItem(
                title = if (currentLang == "fa") "درباره انتقال خون" else "About Transfusions",
                isExpanded = isAboutExpanded,
                onHeaderClick = { isAboutExpanded = !isAboutExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (currentLang == "fa") "فرمول انتقال خون" else "Transfusion Formula",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = textPrimary
                    )
                    Text(
                        text = if (currentLang == "fa") {
                            "حجم خون مورد نیاز (میلی‌لیتر) = ((PCV هدف - PCV گیرنده) ÷ PCV اهداکننده) X وزن (کیلوگرم) X N\nN = ۹۰ برای سگ‌ها، N = ۶۰ برای گربه‌ها"
                        } else {
                            "Blood Dose (ml) = ((Target PCV - Recipient PCV) ÷ Donor PCV) X KG X N\nN = 90 for Dogs, N = 60 for Cats"
                        },
                        fontSize = 11.sp,
                        color = textSecondary,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Products List as a high-fidelity cards grid
                    val prodList = if (currentLang == "fa") {
                        listOf(
                            TransfusionProduct("خون کامل تازه", "۱۲ تا ۲۰ میلی‌لیتر/کیلوگرم", "هر ۲۴ ساعت", "کم‌خونی، جایگزینی پلاکت و فاکتورهای انعقادی"),
                            TransfusionProduct("گلبول‌های قرمز متراکم (Packed RBC)", "۶ تا ۱۰ میلی‌لیتر/کیلوگرم", "هر ۱۲ تا ۲۴ ساعت", "کم‌خونی"),
                            TransfusionProduct("پلاسمای غنی از پلاکت", "۶ تا ۱۰ میلی‌لیتر/کیلوگرم", "هر ۸ تا ۱۲ ساعت", "اختلال عملکرد پلاکت، ترومبوسیتوپنی"),
                            TransfusionProduct("پلاسمای تازه و تازه منجمد شده (FFP)", "۶ تا ۱۲ میلی‌لیتر/کیلوگرم", "هر ۸ تا ۱۲ ساعت", "نقص فاکتورهای انعقادی، بیماری فون ویلبراند (vWD)، انعقاد درون‌رگی منتشر (DIC)، هیپوپروتئینمی"),
                            TransfusionProduct("پلاسمای منجمد شده", "۶ تا ۱۲ میلی‌لیتر/کیلوگرم", "هر ۸ تا ۱۲ ساعت", "هیپوپروتئینمی"),
                            TransfusionProduct("رسوب سرمی پلاسما (Cryoprecipitate)", "۱ واحد به ازای هر ۱۰ کیلوگرم", "هر ۴ تا ۱۲ ساعت (بر حسب نیاز)", "هموفیلی A (نقص فاکتور VIII)، نقص فیبرینوژن، بیماری فون ویلبراند"),
                            TransfusionProduct("مایع رویی کرایو (Cryosupernatant)", "۶ تا ۱۲ میلی‌لیتر/کیلوگرم", "هر ۸ تا ۱۲ ساعت", "هموفیلی B (نقص فاکتور IX)، نقص فاکتورهای VII، X یا XI، کمبود ویتامین K، هیپوپروتئینمی")
                        )
                    } else {
                        listOf(
                            TransfusionProduct("Fresh whole blood", "12 to 20 ml/kg", "q. 24 h", "anemia, platelet & factor replacement"),
                            TransfusionProduct("Packed red cells", "6 to 10 ml/kg", "q. 12 to 24 h", "anemia"),
                            TransfusionProduct("Platelet rich plasma", "6 to 10 ml/kg", "q. 8 to 12 h", "platelet dysfunction, thrombocytopenia"),
                            TransfusionProduct("Fresh and fresh frozen plasma", "6 to 12 ml/kg", "q. 8 to 12 h", "coagulation factor deficiencies, vWD, DIC, hypoproteinemia"),
                            TransfusionProduct("Frozen plasma", "6 to 12 ml/kg", "q. 8 to 12 h", "hypoproteinemia"),
                            TransfusionProduct("Plasma cryoprecipitate", "1 unit/10 kg", "q. 4 to 12 h (as needed)", "hemophilia A (factor VIII deficiency), fibrinogen deficiency, von Willebrand disease"),
                            TransfusionProduct("Cryosupernatant", "6 to 12 ml/kg", "q. 8 to 12 h", "hemophilia B (factor IX deficiency), factor VII, X, or XI deficiency, vitamin K deficiency, hypoproteinemia")
                        )
                    }

                    val volLabel = if (currentLang == "fa") "حجم: " else "Vol: "
                    val freqLabel = if (currentLang == "fa") "دفعات: " else "Freq: "
                    val indLabel = if (currentLang == "fa") "موارد مصرف: " else "Indications: "

                    prodList.forEach { prod ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("$volLabel${prod.vol}", fontSize = 11.sp, color = textSecondary, modifier = Modifier.weight(1f))
                                    Text("$freqLabel${prod.freq}", fontSize = 11.sp, color = textSecondary, modifier = Modifier.weight(1f))
                                }
                                Text("$indLabel${prod.indications}", fontSize = 10.sp, color = textSecondary)
                            }
                        }
                    }

                    Text(
                        text = if (currentLang == "fa") "* ۱ واحد = رسوب تهیه شده از ۲۰۰ میلی‌لیتر پلاسمای تازه منجمد شده" else "* 1 unit = cryoprecipitate produced from 200 ml of fresh frozen plasma",
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                }
            }

            // Accordion 2: Processing and Storage
            AccordionItem(
                title = if (currentLang == "fa") "پردازش و نگهداری" else "Processing and Storage",
                isExpanded = isProcExpanded,
                onHeaderClick = { isProcExpanded = !isProcExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val procBullets = if (currentLang == "fa") {
                        listOf(
                            "خون کامل تازه" to "تزریق ظرف ۴ تا ۶ ساعت پس از خون‌گیری انجام شود.",
                            "پلاسمای تازه" to "سانتریفیوژ جهت جداسازی پلاسما از خون کامل؛ تزریق ظرف ۴ تا ۶ ساعت پس از جمع‌آوری انجام شود.",
                            "پلاسمای تازه منجمد شده" to "جمع‌آوری در ضد انعقاد سیترات، جداسازی پلاسما از خون کامل ظرف ۴ تا ۶ ساعت پس از جمع‌آوری؛ امکان نگهداری به صورت منجمد تا ۱ سال.",
                            "پلاسمای غنی از پلاکت" to "جمع‌آوری در ضد انعقاد سیترات، جداسازی پلاسمای غنی از پلاکت از خون کامل ظرف ۴ تا ۶ ساعت پس از جمع‌آوری؛ پردازش و نگهداری در دمای اتاق، تزریق ظرف ۴۸ ساعت پس از جمع‌آوری.",
                            "گلبول‌های قرمز متراکم (Packed RBC)" to "جمع‌آوری در ضد انعقاد سیترات، جداسازی از خون کامل ظرف ۴ تا ۶ ساعت؛ ترکیب گلبول‌های متراکم با افزودنی‌ها جهت پایداری زیست‌پذیری سلولی؛ نگهداری در یخچال (۴ تا ۸ درجه سانتی‌گراد) تا ۴ هفته.",
                            "رسوب سرمی پلاسما (Cryoprecipitate)" to "تهیه شده از پلاسمای تازه منجمد شده؛ نگهداری به صورت منجمد تا ۱ سال؛ اندازه واحدها متغیر است، لطفاً جهت دوز دقیق با تأمین‌کننده چک کنید.",
                            "مایع رویی کرایو (Cryosupernatant)" to "تهیه شده از پلاسمای تازه منجمد شده؛ نگهداری به صورت منجمد تا ۱ سال."
                        )
                    } else {
                        listOf(
                            "Fresh whole blood" to "Transfuse within 4 to 6 hour of collection.",
                            "Fresh plasma" to "Centrifuged to separate plasma from whole blood, transfuse within 4 to 6 hr. of collection.",
                            "Fresh frozen plasma" to "Collect in citrate anticoagulant, separate plasma from whole blood within 4 to 6 hr. of collection, store frozen for up to 1 year.",
                            "Platelet rich plasma" to "Collect in citrate anticoagulant, separate platelet rich plasma from whole blood within 4 to 6 hr. of collection, process and store at room temperature, transfuse within 48 hr. of collection.",
                            "Packed Red Cells" to "Collect in citrate anticoagulant, separate from whole blood within 4 to 6 hr. of collection, combine packed cells with additives for sustained red cell viability, store under refrigeration (4 to 8 C) for up to 4 weeks.",
                            "Plasma Cryoprecipitate" to "Prepared from fresh frozen plasma, store frozen for up to 1 year, unit size varies, check with each supplier for dosage.",
                            "Cryosupernatant" to "Prepared from fresh frozen plasma, store frozen for up to 1 year."
                        )
                    }

                    procBullets.forEach { (bulletTitle, bulletText) ->
                        BulletItem(title = bulletTitle, text = bulletText, bulletColor = bulletColor)
                    }
                }
            }

            // Accordion 3: Cross-matching
            AccordionItem(
                title = if (currentLang == "fa") "سازگاری متقاطع (کراس‌مچ)" else "Cross-matching",
                isExpanded = isCrossExpanded,
                onHeaderClick = { isCrossExpanded = !isCrossExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(
                            text = if (currentLang == "fa") "سگ‌سانان" else "Canine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val dogBullets = if (currentLang == "fa") {
                                listOf(
                                    "۱۲ نوع گروه خونی در سگ‌ها" to false,
                                    "نام‌گذاری شده با عنوان DEA و یک عدد (مانند DEA 1, DEA 2, DEA 3 و غیره)" to false,
                                    "مهم‌ترین آن‌ها گروه DEA 1 شامل 1.1 و 1.2 است" to false,
                                    "گروه DEA 1.1 مثبت = گیرنده عمومی" to false,
                                    "گروه DEA 1.1 و DEA 1.2 منفی = دهنده عمومی" to false,
                                    "نیازی به انجام تست کراس‌مچ برای اولین بار انتقال خون نیست" to false,
                                    "حساس‌سازی حدود ۳ روز زمان می‌برد؛ انجام تست کراس‌مچ ۷۲ ساعت پس از دریافت اولین خون در سگ الزامی است" to false
                                )
                            } else {
                                listOf(
                                    "12 canine blood types" to false,
                                    "Designated DEA and a number (DEA 1, DEA 2, DEA 3, etc.)" to false,
                                    "Most important are DEA 1: 1.1 and 1.2" to false,
                                    "DEA 1.1 Positive = universal recipient" to false,
                                    "DEA 1.1, DEA 1.2 Negative = universal donor" to false,
                                    "Cross-match does not need to be performed on a first time transfusion" to false,
                                    "Sensitization takes ~ 3 days\nCross-match needed 72 hours after dog receives transfusion" to false
                                )
                            }
                            dogBullets.forEach { (text, isBold) ->
                                BulletItem(text = text, bulletColor = bulletColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    Column {
                        Text(
                            text = if (currentLang == "fa") "گربه‌سانان" else "Feline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val catBullets = if (currentLang == "fa") {
                                listOf(
                                    "سیستم گروه خونی AB" to false,
                                    "گروه‌های خونی: A, B, AB (نادر)" to false,
                                    "دارای آلوآنتی‌بادی‌های طبیعی علیه سایر گروه‌های خونی = بدون دهنده عمومی" to false,
                                    "گروه A: آلوآنتی‌بادی‌های ضعیف ضد B، واکنش خفیف در صورت دریافت خون نوع B" to false,
                                    "گروه B: آلوآنتی‌بادی‌های قوی ضد A، واکنش بسیار شدید در صورت دریافت خون نوع A" to false,
                                    "تمامی گربه‌ها باید کراس‌مچ شوند!" to true
                                )
                            } else {
                                listOf(
                                    "AB blood type system" to false,
                                    "Types: A, B, AB(rare)" to false,
                                    "Have natural occurring alloantibodies to other blood groups = No universal donor" to false,
                                    "Type A: Weak anti-B alloantibodies, mild reaction if transfused with B blood" to false,
                                    "Type B: High anti-A alloantibodies, severe reaction if transfused with A blood" to false,
                                    "ALL CATS SHOULD BE CROSS-MATCHED" to true
                                )
                            }
                            catBullets.forEach { (text, isBold) ->
                                BulletItem(text = text, bulletColor = bulletColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple Helper Composables for our high-fidelity layout
@Composable
fun BulletItem(
    text: String,
    bulletColor: Color,
    title: String = "",
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(bulletColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            if (title.isNotEmpty()) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp, fontWeight = fontWeight)
        }
    }
}

@Composable
fun AccordionItem(
    title: String,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (MaterialTheme.colorScheme.background.red < 0.3f) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else Color(0xFFF9FAFB)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
                ) {
                    content()
                }
            }
        }
    }
}

data class TransfusionProduct(
    val name: String,
    val vol: String,
    val freq: String,
    val indications: String
)
