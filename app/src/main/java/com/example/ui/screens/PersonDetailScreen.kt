package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MonthPickerHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CalendarUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    viewModel: MainViewModel,
    personId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditPerson: (Long) -> Unit,
    onNavigateToEditPackage: (Long) -> Unit,
    onNavigateToEditAssistance: (Long) -> Unit
) {
    val context = LocalContext.current

    val person by viewModel.repository.observePersonById(personId)
        .collectAsStateWithLifecycle(initialValue = null)

    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    val currentMonthAssistanceFlow = remember(personId, selectedYear, selectedMonth) {
        viewModel.repository.observeAssistanceForPersonAndMonth(personId, selectedYear, selectedMonth)
    }
    val currentAssistanceDetails by currentMonthAssistanceFlow.collectAsStateWithLifecycle(initialValue = null)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog && person != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("حذف البيانات") },
            text = { Text("هل أنت تأكد من حذف بيانات ${person?.name} وسجلات المساعدات الخاصة به؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        person?.let { viewModel.deletePerson(it) }
                        showDeleteConfirmDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = person?.name ?: "تفاصيل الشخص",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ChurchNavy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("person_detail_back_button")
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
                        onClick = { onNavigateToEditPerson(personId) },
                        modifier = Modifier.testTag("btn_edit_person_info")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل الشخص",
                            tint = ChurchNavy
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("btn_delete_person")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الشخص",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        if (person == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val currentPerson = person!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .testTag("person_detail_screen"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Profile Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(ChurchNavy.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentPerson.name.take(1),
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ChurchNavy
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentPerson.name,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "عدد أفراد الأسرة: ${currentPerson.familyMembers} أفراد",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            if (currentPerson.phone.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = ChurchNavy,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = currentPerson.phone,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            if (currentPerson.address.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = null,
                                        tint = ChurchNavy,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = currentPerson.address,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = ChurchGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "موعد المساعدة: يوم ${currentPerson.scheduledDayOfMonth} من كل شهر",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = ChurchGold
                                )
                            }

                            if (currentPerson.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "ملاحظات: ${currentPerson.notes}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Month Switcher Header
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

                // Monthly Assistance Breakdown Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المساعدة الشهرية",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TextButton(onClick = { onNavigateToEditPackage(personId) }) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تعديل السلة الثابتة")
                        }
                    }
                }

                val assistance = currentAssistanceDetails?.assistance
                val assistanceItems = currentAssistanceDetails?.items ?: emptyList()
                val isDelivered = assistance?.status == "DELIVERED"
                val totalAmount = assistance?.totalAmount ?: 0.0

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Status Banner
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDelivered) ChurchGreenContainer else ChurchRedContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isDelivered) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                                            contentDescription = null,
                                            tint = if (isDelivered) ChurchGreen else ChurchRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isDelivered) "✅ تم التسليم" else "⏳ لم يتم التسليم بعد",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isDelivered) ChurchGreen else ChurchRed
                                        )
                                    }
                                }

                                val deliveryTs = assistance?.deliveryTimestamp
                                if (deliveryTs != null) {
                                    Text(
                                        text = CalendarUtil.formatDateTimeArabic(deliveryTs),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (assistanceItems.isEmpty()) {
                                Text(
                                    text = "لم يتم تحديد منتجات لهذه المساعدة بعد.\nانقر على \"تعديل المساعدة\" لاختيار المنتجات والكميات.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    assistanceItems.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
                                            Text(
                                                text = "${item.productName} — $qtyStr ${item.productUnit}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                                            )
                                            Text(
                                                text = "${item.totalPrice.toInt()} جنيه",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = ChurchNavy
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                            // Total Price
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "إجمالي المساعدة:",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${totalAmount.toInt()} جنيه",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ChurchNavy
                                )
                            }
                        }
                    }
                }

                // Action Buttons Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Register Delivery Toggle Button
                        if (assistance != null) {
                            Button(
                                onClick = {
                                    viewModel.toggleAssistanceDelivered(assistance.id, isDelivered)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_register_delivery"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDelivered) ChurchRed else ChurchGreen
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDelivered) Icons.Default.Cancel else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF0F2240)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isDelivered) "تراجع عن التسليم" else "تسجيل التسليم الآن",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0F2240)
                                )
                            }
                        }

                        // Customize Specific Assistance Button
                        if (assistance != null) {
                            OutlinedButton(
                                onClick = { onNavigateToEditAssistance(assistance.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_edit_assistance_items"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تعديل المساعدة لهذا الشهر",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Sync to Native Phone Calendar Button
                        if (currentAssistanceDetails != null) {
                            OutlinedButton(
                                onClick = {
                                    val success = viewModel.addAssistanceToCalendar(context, currentAssistanceDetails!!)
                                    if (success) {
                                        Toast.makeText(context, "تم فتح التقويم لإضافة الموعد", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_add_to_calendar"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = ChurchGold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "إضافة الموعد إلى تقويم الهاتف",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ChurchGold
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
}
