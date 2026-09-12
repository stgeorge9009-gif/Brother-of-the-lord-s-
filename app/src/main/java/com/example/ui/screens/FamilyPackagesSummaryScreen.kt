package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MonthlyAssistanceWithDetails
import com.example.ui.components.MonthPickerHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.CalendarUtil
import com.example.util.PdfExportUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyPackagesSummaryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPersonDetail: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val assistances by viewModel.selectedMonthAssistances.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "DELIVERED", "PENDING"
    var selectedAssistanceIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // Update selected IDs when assistances change (default all selected)
    LaunchedEffect(assistances) {
        selectedAssistanceIds = assistances.map { it.assistance.id }.toSet()
    }

    val filteredList = remember(assistances, searchQuery, statusFilter) {
        assistances.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    (item.person?.name?.contains(searchQuery, ignoreCase = true) == true) ||
                    (item.person?.phone?.contains(searchQuery) == true) ||
                    (item.person?.address?.contains(searchQuery, ignoreCase = true) == true) ||
                    (item.person?.notes?.contains(searchQuery, ignoreCase = true) == true)

            val matchesStatus = when (statusFilter) {
                "DELIVERED" -> item.assistance.status == "DELIVERED"
                "PENDING" -> item.assistance.status == "PENDING"
                else -> true
            }
            matchesQuery && matchesStatus
        }
    }

    val selectedAssistances = remember(assistances, selectedAssistanceIds) {
        assistances.filter { selectedAssistanceIds.contains(it.assistance.id) }
    }

    val totalSelectedAmount = remember(selectedAssistances) {
        selectedAssistances.sumOf { it.assistance.totalAmount }
    }

    val totalSelectedPackagesCount = remember(selectedAssistances) {
        selectedAssistances.sumOf { it.items.size }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "تجميعة الأسر والمساعدات",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ChurchNavy
                        )
                        Text(
                            text = "تفاصيل منتجات وأسعار طرود الأسر وتصدير PDF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("family_packages_back_btn")
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
                            val itemsToExport = if (selectedAssistances.isNotEmpty()) selectedAssistances else assistances
                            if (itemsToExport.isEmpty()) {
                                Toast.makeText(context, "لا توجد أسر محددة للتصدير", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            val monthName = CalendarUtil.getArabicMonthName(selectedMonth)
                            val file = PdfExportUtil.generateDetailedFamilyBreakdownPdf(
                                context = context,
                                year = selectedYear,
                                month = selectedMonth,
                                monthName = monthName,
                                assistances = itemsToExport,
                                customTitle = "كشف تجميعة طرود ومساعدات الأسر بالمنتجات والأسعار"
                            )
                            if (file != null) {
                                PdfExportUtil.openOrSharePdf(context, file, "كشف تجميعة الأسر - شهر $monthName $selectedYear")
                            } else {
                                Toast.makeText(context, "تعذر إنشاء ملف الـ PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("family_packages_top_pdf_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "تصدير كشف الأسر PDF",
                            tint = ChurchGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Month Selector
            item {
                Spacer(modifier = Modifier.height(4.dp))
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

            // Big Action Card: Total Summary & PDF Export
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("family_packages_summary_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ChurchGoldContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "إجمالي التجميعة المحددة",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ChurchNavy
                                )
                                Text(
                                    text = "${selectedAssistances.size} أسرة محددة من إجمالي ${assistances.size} أسرة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ChurchNavy.copy(alpha = 0.8f)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ChurchGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${totalSelectedAmount.toInt()} جنيه",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = ChurchGold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Divider(color = ChurchGold.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إجمالي أصناف المنتجات: $totalSelectedPackagesCount منتج مقرر",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = ChurchNavy
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    selectedAssistanceIds = if (selectedAssistanceIds.size == assistances.size) {
                                        emptySet()
                                    } else {
                                        assistances.map { it.assistance.id }.toSet()
                                    }
                                }
                            ) {
                                Checkbox(
                                    checked = selectedAssistanceIds.size == assistances.size && assistances.isNotEmpty(),
                                    onCheckedChange = { checked ->
                                        selectedAssistanceIds = if (checked) {
                                            assistances.map { it.assistance.id }.toSet()
                                        } else {
                                            emptySet()
                                        }
                                    }
                                )
                                Text(
                                    text = if (selectedAssistanceIds.size == assistances.size) "إلغاء التحديد" else "تحديد الكل",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ChurchNavy
                                )
                            }
                        }

                        // Big Button to Export PDF
                        Button(
                            onClick = {
                                val itemsToExport = if (selectedAssistances.isNotEmpty()) selectedAssistances else assistances
                                if (itemsToExport.isEmpty()) {
                                    Toast.makeText(context, "يرجى تحديد أسر للتصدير أولاً", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val monthName = CalendarUtil.getArabicMonthName(selectedMonth)
                                val file = PdfExportUtil.generateDetailedFamilyBreakdownPdf(
                                    context = context,
                                    year = selectedYear,
                                    month = selectedMonth,
                                    monthName = monthName,
                                    assistances = itemsToExport,
                                    customTitle = "كشف تجميعة طرود ومساعدات الأسر بالمنتجات والأسعار"
                                )
                                if (file != null) {
                                    PdfExportUtil.openOrSharePdf(context, file, "كشف تجميعة الأسر - شهر $monthName $selectedYear")
                                } else {
                                    Toast.makeText(context, "تعذر إنشاء ملف الـ PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("family_packages_export_pdf_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChurchNavy)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = ChurchGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تصدير كشف تجميعة الأسر بالمنتجات والأسعار PDF",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("family_packages_search_input"),
                    placeholder = { Text("بحث باسم الأسرة، الفئة، أو الهاتف...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ChurchNavy) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChurchNavy,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )
            }

            // Filter Chips (All, Delivered, Pending)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = statusFilter == "ALL",
                        onClick = { statusFilter = "ALL" },
                        label = { Text("جميع الأسر (${assistances.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChurchNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = statusFilter == "DELIVERED",
                        onClick = { statusFilter = "DELIVERED" },
                        label = { Text("تم الاستلام") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChurchGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = statusFilter == "PENDING",
                        onClick = { statusFilter = "PENDING" },
                        label = { Text("متبقي للتسليم") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChurchRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Empty State
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FamilyRestroom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "لا توجد نتائج مطابقة لبحثك" else "لا توجد مساعدات مسجلة لهذا الشهر",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "يمكنك توليد مساعدات الشهر من شاشة سجل المساعدات",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Family Cards
                items(filteredList, key = { it.assistance.id }) { item ->
                    val isSelected = selectedAssistanceIds.contains(item.assistance.id)
                    FamilyPackageDetailCard(
                        item = item,
                        isSelected = isSelected,
                        onToggleSelection = {
                            selectedAssistanceIds = if (isSelected) {
                                selectedAssistanceIds - item.assistance.id
                            } else {
                                selectedAssistanceIds + item.assistance.id
                            }
                        },
                        onPersonClick = {
                            item.person?.let { p -> onNavigateToPersonDetail(p.id) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyPackageDetailCard(
    item: MonthlyAssistanceWithDetails,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onPersonClick: () -> Unit
) {
    val isDelivered = item.assistance.status == "DELIVERED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("family_card_${item.assistance.id}")
            .clickable { onToggleSelection() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, ChurchGold) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Selection Checkbox, Name, Category & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPersonClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.person?.name ?: "مستفيد غير محدد",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ChurchNavy
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ChurchNavy.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = "${item.person?.familyMembers ?: 1} أفراد",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = ChurchNavy,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val contactText = buildString {
                        if (!item.person?.phone.isNullOrBlank()) append(item.person?.phone)
                        if (!item.person?.address.isNullOrBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append(item.person?.address)
                        }
                    }
                    if (contactText.isNotBlank()) {
                        Text(
                            text = contactText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDelivered) ChurchGreenContainer else ChurchRedContainer
                ) {
                    Text(
                        text = if (isDelivered) "تم الاستلام" else "متبقي",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDelivered) ChurchGreen else ChurchRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Products list breakdown
            if (item.items.isNotEmpty()) {
                Text(
                    text = "الأصناف المقررة بالأسعار والكميات:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Header row for products table
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "الصنف",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.4f)
                        )
                        Text(
                            text = "الكمية",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "سعر الوحدة",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "الإجمالي",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    item.items.forEach { productItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = productItem.productName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = ChurchNavy,
                                modifier = Modifier.weight(1.4f)
                            )
                            Text(
                                text = "${formatQuantity(productItem.quantity)} ${productItem.productUnit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${productItem.unitPriceAtTime.toInt()} ج.م",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${productItem.totalPrice.toInt()} ج.م",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = ChurchGold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "لا توجد أصناف مضافة لطرد هذه الأسرة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Total Card Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إجمالي طرد الأسرة:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ChurchNavy
                )
                Text(
                    text = "${item.assistance.totalAmount.toInt()} جنيه مصري",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = ChurchGold
                )
            }
        }
    }
}

private fun formatQuantity(qty: Double): String {
    return if (qty % 1.0 == 0.0) qty.toInt().toString() else String.format("%.2f", qty)
}
