package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MonthPickerHeader
import com.example.ui.components.PersonCard
import com.example.ui.theme.ChurchGold
import com.example.ui.theme.ChurchNavy
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CalendarUtil
import com.example.util.PdfExportUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyAssistanceScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPersonDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    val monthAssistances by viewModel.selectedMonthAssistances.collectAsStateWithLifecycle()

    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "DELIVERED"

    val filteredList = remember(monthAssistances, statusFilter) {
        when (statusFilter) {
            "PENDING" -> monthAssistances.filter { it.assistance.status != "DELIVERED" }
            "DELIVERED" -> monthAssistances.filter { it.assistance.status == "DELIVERED" }
            else -> monthAssistances
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "المساعدات الشهرية",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ChurchNavy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("monthly_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = ChurchNavy
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val monthName = CalendarUtil.getArabicMonthName(selectedMonth)
                            val file = PdfExportUtil.generateDetailedFamilyBreakdownPdf(
                                context = context,
                                year = selectedYear,
                                month = selectedMonth,
                                monthName = monthName,
                                assistances = filteredList,
                                customTitle = "كشف تجميعة طرود ومساعدات الأسر بالمنتجات والأسعار"
                            )
                            if (file != null) {
                                PdfExportUtil.openOrSharePdf(context, file, "كشف تجميعة الأسر - شهر $monthName $selectedYear")
                            } else {
                                Toast.makeText(context, "تعذر إنشاء ملف الـ PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("btn_export_monthly_assistance_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "تصدير كشف تجميعة الأسر والمنتجات PDF",
                            tint = ChurchGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("monthly_assistance_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Month Selector Header
            MonthPickerHeader(
                year = selectedYear,
                month = selectedMonth,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onResetToCurrentMonth = {
                    viewModel.setSelectedMonthYear(viewModel.currentRealYear, viewModel.currentRealMonth)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == "ALL",
                    onClick = { statusFilter = "ALL" },
                    label = { Text("الكل (${monthAssistances.size})") }
                )
                FilterChip(
                    selected = statusFilter == "PENDING",
                    onClick = { statusFilter = "PENDING" },
                    label = { Text("لم يتم التسليم (${monthAssistances.count { it.assistance.status != "DELIVERED" }})") }
                )
                FilterChip(
                    selected = statusFilter == "DELIVERED",
                    onClick = { statusFilter = "DELIVERED" },
                    label = { Text("تم التسليم (${monthAssistances.count { it.assistance.status == "DELIVERED" }})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد سجلات مساعدات مطابقة للفلتر في هذا الشهر.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.assistance.id }) { details ->
                        details.person?.let { person ->
                            PersonCard(
                                person = person,
                                assistanceDetails = details,
                                onClick = { onNavigateToPersonDetail(person.id) }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
