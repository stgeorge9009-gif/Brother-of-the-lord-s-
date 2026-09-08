package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MetricCard
import com.example.ui.components.MonthPickerHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CalendarUtil

data class ProductReportItem(
    val productName: String,
    val unit: String,
    val totalQuantity: Double,
    val totalCost: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    val totalPersons by viewModel.totalPersonCount.collectAsStateWithLifecycle()
    val monthAssistances by viewModel.selectedMonthAssistances.collectAsStateWithLifecycle()
    val allAssistances by viewModel.allAssistances.collectAsStateWithLifecycle()

    val monthDeliveredCount = monthAssistances.count { it.assistance.status == "DELIVERED" }
    val monthPendingCount = monthAssistances.size - monthDeliveredCount
    val monthTotalCost = monthAssistances.sumOf { it.assistance.totalAmount }

    // Product breakdown for selected month
    val productReports = remember(monthAssistances) {
        val map = mutableMapOf<String, ProductReportItem>()
        for (assistance in monthAssistances) {
            for (item in assistance.items) {
                val existing = map[item.productName]
                val qty = (existing?.totalQuantity ?: 0.0) + item.quantity
                val cost = (existing?.totalCost ?: 0.0) + item.totalPrice
                map[item.productName] = ProductReportItem(
                    productName = item.productName,
                    unit = item.productUnit,
                    totalQuantity = qty,
                    totalCost = cost
                )
            }
        }
        map.values.sortedByDescending { it.totalCost }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "التقارير والإحصائيات",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ChurchNavy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("reports_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
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
                .testTag("reports_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
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

            // High Level Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ChurchGoldContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "تقرير ${CalendarUtil.getArabicMonthName(selectedMonth)} $selectedYear",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF78350F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "إجمالي تكلفة المساعدات = ${monthTotalCost.toInt()} جنيه",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F2240)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "عدد المساعدات المستحقة: ${monthAssistances.size} مساعدة لـ $totalPersons شخص/أسرة",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }

            // Stats Metrics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "المساعدات المسلمة",
                            value = "$monthDeliveredCount",
                            icon = Icons.Default.CheckCircle,
                            containerColor = ChurchGreenContainer,
                            iconColor = ChurchGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "المساعدات المتبقية",
                            value = "$monthPendingCount",
                            icon = Icons.Default.HourglassEmpty,
                            containerColor = ChurchRedContainer,
                            iconColor = ChurchRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Product Cost & Quantity Breakdown
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "توزيع التكلفة والكميات حسب المنتج",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (productReports.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "لا توجد مساعدات أو منتجات مسجلة لهذا الشهر.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(productReports, key = { it.productName }) { report ->
                    val percentage = if (monthTotalCost > 0) (report.totalCost / monthTotalCost).toFloat() else 0f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = report.productName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${report.totalCost.toInt()} جنيه",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ChurchNavy
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "الكمية الموزعة: ${report.totalQuantity.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }} ${report.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(percentage * 100).toInt()}% من الإجمالي",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { percentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ChurchNavy,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
