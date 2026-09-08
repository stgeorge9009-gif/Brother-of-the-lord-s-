package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MonthPickerHeader
import com.example.ui.components.PersonCard
import com.example.ui.theme.ChurchGold
import com.example.ui.theme.ChurchNavy
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CalendarUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPersonDetail: (Long) -> Unit
) {
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedIsoDate by viewModel.selectedCalendarDateIso.collectAsStateWithLifecycle()

    val monthAssistances by viewModel.selectedMonthAssistances.collectAsStateWithLifecycle()
    val selectedDateAssistances by viewModel.selectedCalendarDateAssistances.collectAsStateWithLifecycle()

    // Map scheduled ISO date -> Count of assistances
    val dateCounts = remember(monthAssistances) {
        monthAssistances.groupBy { it.assistance.scheduledDate }
    }

    val gridDays = remember(selectedYear, selectedMonth) {
        CalendarUtil.getMonthCalendarGrid(selectedYear, selectedMonth)
    }

    var quickFilterTab by remember { mutableIntStateOf(0) } // 0: Selected Day, 1: Today, 2: This Month, 3: Overdue

    val todayIso = CalendarUtil.getTodayIsoString()

    val displayList = remember(monthAssistances, selectedDateAssistances, quickFilterTab, todayIso) {
        when (quickFilterTab) {
            1 -> monthAssistances.filter { it.assistance.scheduledDate == todayIso }
            2 -> monthAssistances
            3 -> monthAssistances.filter { it.assistance.scheduledDate < todayIso && it.assistance.status != "DELIVERED" }
            else -> selectedDateAssistances
        }
    }

    val dayNames = listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "جدول وتقويم المساعدات",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ChurchNavy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("calendar_back_button")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("calendar_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Month Picker Header
            MonthPickerHeader(
                year = selectedYear,
                month = selectedMonth,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onResetToCurrentMonth = {
                    viewModel.setSelectedMonthYear(viewModel.currentRealYear, viewModel.currentRealMonth)
                    viewModel.selectCalendarDate(todayIso)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = quickFilterTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = quickFilterTab == 0,
                    onClick = { quickFilterTab = 0 },
                    text = { Text("اليوم المحدد", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = quickFilterTab == 1,
                    onClick = { quickFilterTab = 1 },
                    text = { Text("مساعدات اليوم", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = quickFilterTab == 2,
                    onClick = { quickFilterTab = 2 },
                    text = { Text("كل الشهر", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = quickFilterTab == 3,
                    onClick = { quickFilterTab = 3 },
                    text = { Text("المتأخرة", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (quickFilterTab == 0) {
                // Calendar Grid View
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Day name headers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            dayNames.forEach { dayName ->
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid Days
                        val rows = gridDays.chunked(7)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            rows.forEach { rowDays ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    rowDays.forEach { day ->
                                        val isSelected = day.isoDate == selectedIsoDate
                                        val count = dateCounts[day.isoDate]?.size ?: 0

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    when {
                                                        isSelected -> ChurchNavy
                                                        day.isToday -> ChurchGold.copy(alpha = 0.2f)
                                                        !day.isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                    }
                                                )
                                                .border(
                                                    width = if (day.isToday) 1.5.dp else 0.dp,
                                                    color = if (day.isToday) ChurchGold else Color.Transparent,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    viewModel.selectCalendarDate(day.isoDate)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = day.dayNumber.toString(),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = when {
                                                        isSelected -> Color(0xFF0F2240)
                                                        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )

                                                if (count > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) ChurchGold else ChurchNavy)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "مساعدات يوم ${CalendarUtil.formatArabicDisplayDate(selectedIsoDate)} (${selectedDateAssistances.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // List of assistances for selected view
            if (displayList.isEmpty()) {
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
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد مساعدات مجدولة لهذه الفترة.",
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
                    items(displayList, key = { it.assistance.id }) { details ->
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
