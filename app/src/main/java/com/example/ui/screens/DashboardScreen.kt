package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.MetricCard
import com.example.ui.components.MonthPickerHeader
import com.example.ui.components.PersonCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CalendarUtil

data class QuickNavAction(
    val title: String,
    val icon: ImageVector,
    val containerColor: Color,
    val iconColor: Color,
    val tag: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToPeople: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToAssistance: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPersonDetail: (Long) -> Unit
) {
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    val totalPersons by viewModel.totalPersonCount.collectAsStateWithLifecycle()
    val totalAssistancesCount by viewModel.monthlyTotalCount.collectAsStateWithLifecycle()
    val deliveredCount by viewModel.monthlyDeliveredCount.collectAsStateWithLifecycle()
    val totalAmount by viewModel.monthlyTotalAmount.collectAsStateWithLifecycle()

    val monthAssistances by viewModel.selectedMonthAssistances.collectAsStateWithLifecycle()
    val remainingCount = (totalAssistancesCount - deliveredCount).coerceAtLeast(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✝️ ", fontSize = 20.sp)
                        Text(
                            text = "إخوة الرب - لوحة التحكم",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ChurchNavy
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = ChurchNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("dashboard_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Image Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ChurchGoldContainer)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.church_banner_1786468365513),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.25f
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = ChurchNavy.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "اليوم: ${CalendarUtil.formatArabicDisplayDate(CalendarUtil.getTodayIsoString())}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF0F2240),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text = "خدمة المساعدات الاجتماعية والأسر المحتاجة",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0F2240)
                            )
                            Text(
                                text = "Created by Mekhaeel Yasser",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF0F2240).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Month Picker Header
            item {
                MonthPickerHeader(
                    year = selectedYear,
                    month = selectedMonth,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onResetToCurrentMonth = {
                        viewModel.setSelectedMonthYear(viewModel.currentRealYear, viewModel.currentRealMonth)
                    }
                )
            }

            // Overview Stats Grid
            item {
                Text(
                    text = "ملخص الشهر",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "الأسر المحتاجة",
                            value = "$totalPersons شخص",
                            icon = Icons.Default.People,
                            containerColor = ChurchNavy.copy(alpha = 0.08f),
                            iconColor = ChurchNavy,
                            modifier = Modifier.weight(1f),
                            tag = "stat_card_persons"
                        )
                        MetricCard(
                            title = "إجمالي القيمة",
                            value = "${totalAmount.toInt()} جنيه",
                            icon = Icons.Default.AttachMoney,
                            containerColor = ChurchGoldContainer,
                            iconColor = ChurchGold,
                            modifier = Modifier.weight(1f),
                            tag = "stat_card_total_amount"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "مساعدات الشهر",
                            value = "$totalAssistancesCount",
                            icon = Icons.Default.Assignment,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "تم تسليمها",
                            value = "$deliveredCount",
                            icon = Icons.Default.CheckCircle,
                            containerColor = ChurchGreenContainer,
                            iconColor = ChurchGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "المتبقية",
                            value = "$remainingCount",
                            icon = Icons.Default.HourglassEmpty,
                            containerColor = ChurchRedContainer,
                            iconColor = ChurchRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Actions Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "الوصول السريع",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                val quickActions = listOf(
                    QuickNavAction("الأشخاص والأسر", Icons.Default.People, ChurchNavy.copy(alpha = 0.1f), ChurchNavy, "btn_nav_people", onNavigateToPeople),
                    QuickNavAction("المنتجات والأسعار", Icons.Default.ShoppingBag, ChurchGoldContainer, ChurchGold, "btn_nav_products", onNavigateToProducts),
                    QuickNavAction("سجل المساعدات", Icons.Default.ReceiptLong, ChurchGreenContainer, ChurchGreen, "btn_nav_assistance", onNavigateToAssistance),
                    QuickNavAction("التقويم والمواعيد", Icons.Default.CalendarMonth, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primary, "btn_nav_calendar", onNavigateToCalendar),
                    QuickNavAction("التقارير والإحصائيات", Icons.Default.BarChart, ChurchRedContainer, ChurchRed, "btn_nav_reports", onNavigateToReports),
                    QuickNavAction("الإعدادات", Icons.Default.Settings, MaterialTheme.colorScheme.surfaceVariant, ChurchNavy, "btn_nav_settings", onNavigateToSettings)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    quickActions.chunked(2).forEach { rowActions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowActions.forEach { action ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .testTag(action.tag)
                                        .clickable { action.onClick() },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = action.containerColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(action.iconColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = action.icon,
                                                contentDescription = null,
                                                tint = action.iconColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = action.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF0F2240)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Beneficiaries Preview
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مساعدات هذا الشهر (${monthAssistances.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onNavigateToAssistance) {
                        Text("عرض الكل", color = ChurchNavy)
                    }
                }
            }

            if (monthAssistances.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "لا توجد أسر محتاجة مسجلة لشهور هذا العام حتى الآن.\nقم بإضافة شخص من شاشة الأشخاص لبدء الخدمة.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                items(monthAssistances.take(5), key = { it.assistance.id }) { details ->
                    details.person?.let { person ->
                        PersonCard(
                            person = person,
                            assistanceDetails = details,
                            onClick = { onNavigateToPersonDetail(person.id) }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ChurchNavy.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Created by Mekhaeel Yasser",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ChurchNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "تم الصنع بواسطة ميخائيل ياسر",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
