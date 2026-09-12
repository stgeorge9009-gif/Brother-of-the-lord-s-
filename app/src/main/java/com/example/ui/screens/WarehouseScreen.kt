package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ProductEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WarehouseSortOption
import com.example.util.PdfExportUtil
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Long) -> Unit
) {
    val context = LocalContext.current

    val products by viewModel.warehouseProducts.collectAsState()
    val allProducts by viewModel.products.collectAsState()
    val searchQuery by viewModel.warehouseSearchQuery.collectAsState()
    val currentSort by viewModel.warehouseSortOption.collectAsState()
    val selectedIds by viewModel.selectedWarehouseProductIds.collectAsState()

    val totalValue by viewModel.totalWarehouseValue.collectAsState()
    val totalQuantity by viewModel.totalWarehouseQuantity.collectAsState()
    val totalCount by viewModel.totalWarehouseItemCount.collectAsState()

    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var showAggregationSheet by remember { mutableStateOf(false) }

    // Aggregation Dialog / Bottom Sheet
    if (showAggregationSheet) {
        val aggregatedItems = if (selectedIds.isNotEmpty()) {
            allProducts.filter { selectedIds.contains(it.id) }
        } else {
            allProducts
        }
        val aggregatedGrandTotal = aggregatedItems.sumOf { it.totalProductPrice }

        AggregationDialog(
            items = aggregatedItems,
            grandTotal = aggregatedGrandTotal,
            isFilteredSelection = selectedIds.isNotEmpty(),
            onDismiss = { showAggregationSheet = false },
            onExportPdf = {
                val pdfFile = PdfExportUtil.generateWarehouseProductsPdf(
                    context = context,
                    items = aggregatedItems,
                    grandTotal = aggregatedGrandTotal,
                    reportTitle = if (selectedIds.isNotEmpty()) "تقرير تجميع الأصناف المختارة بالمخزن" else "تقرير بضاعة المخزن والمنتجات الشامل"
                )
                if (pdfFile != null) {
                    PdfExportUtil.openOrSharePdf(context, pdfFile, "تقرير تجميع منتجات المخزن")
                } else {
                    Toast.makeText(context, "حدث خطأ أثناء تصدير ملف الـ PDF", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val product = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "تأكيد حذف المنتج",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف صنف \"${product.name}\" نهائيًا من المخزن؟ لن يمكنك استرجاعه بعد الحذف.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                        Toast.makeText(context, "تم حذف الصنف بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_product_button")
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { productToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_product_button")
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "المخزن وإدارة الأصناف",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ChurchNavy
                        )
                        Text(
                            text = "جرد البضائع، حساب الإجماليات، وتصدير التقارير",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("warehouse_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = ChurchNavy
                        )
                    }
                },
                actions = {
                    // PDF quick export
                    IconButton(
                        onClick = {
                            val file = PdfExportUtil.generateWarehouseProductsPdf(
                                context = context,
                                items = allProducts,
                                grandTotal = totalValue,
                                reportTitle = "تقرير كشف المخزن والمنتجات الشامل"
                            )
                            if (file != null) {
                                PdfExportUtil.openOrSharePdf(context, file, "كشف المخزن الشامل")
                            } else {
                                Toast.makeText(context, "تعذر إنشاء ملف الـ PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("warehouse_quick_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "تصدير PDF",
                            tint = ChurchGold
                        )
                    }

                    // Group / Aggregate button in top bar
                    IconButton(
                        onClick = { showAggregationSheet = true },
                        modifier = Modifier.testTag("warehouse_top_aggregate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Summarize,
                            contentDescription = "تجميع المنتجات",
                            tint = ChurchNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddProduct,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إضافة صنف جديد", fontWeight = FontWeight.Bold) },
                containerColor = ChurchNavy,
                contentColor = Color.White,
                modifier = Modifier.testTag("warehouse_fab_add_product")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("warehouse_screen_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary KPI Cards (إجمالي الأصناف - إجمالي الكميات - إجمالي قيمة المخزن)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WarehouseKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "إجمالي الأصناف",
                        value = "$totalCount صنف",
                        icon = Icons.Default.Inventory2,
                        containerColor = ChurchNavy.copy(alpha = 0.08f),
                        contentColor = ChurchNavy
                    )

                    WarehouseKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "إجمالي الكمية",
                        value = formatQuantity(totalQuantity),
                        icon = Icons.Default.Numbers,
                        containerColor = ChurchGoldContainer,
                        contentColor = ChurchNavy
                    )

                    WarehouseKpiCard(
                        modifier = Modifier.weight(1.3f),
                        title = "قيمة المخزن",
                        value = "${formatAmount(totalValue)} ج.م",
                        icon = Icons.Default.AccountBalanceWallet,
                        containerColor = ChurchGreen.copy(alpha = 0.1f),
                        contentColor = ChurchGreen
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setWarehouseSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("warehouse_search_input"),
                    placeholder = { Text("ابحث عن صنف بالاسم أو الفئة...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ChurchNavy)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setWarehouseSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChurchNavy,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // Sorting Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ترتيب حسب:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    WarehouseSortOption.values().forEach { option ->
                        FilterChip(
                            selected = currentSort == option,
                            onClick = { viewModel.setWarehouseSortOption(option) },
                            label = { Text(option.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ChurchNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Selection & Aggregation Action Banner
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedIds.size == allProducts.size && allProducts.isNotEmpty(),
                                onCheckedChange = { checked ->
                                    if (checked) viewModel.selectAllWarehouseProducts()
                                    else viewModel.clearWarehouseProductSelection()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = ChurchNavy)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedIds.isNotEmpty()) "محدد (${selectedIds.size})" else "تحديد الكل للتجميع",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Clear selection button if any
                            if (selectedIds.isNotEmpty()) {
                                TextButton(onClick = { viewModel.clearWarehouseProductSelection() }) {
                                    Text("إلغاء التحديد", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            // Group Products Button
                            Button(
                                onClick = { showAggregationSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ChurchGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_aggregate_products")
                            ) {
                                Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedIds.isNotEmpty()) "تجميع المحدد (${selectedIds.size})" else "تجميع المنتجات",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Product List or Empty State
            if (products.isEmpty()) {
                item {
                    EmptyWarehouseState(
                        hasQuery = searchQuery.isNotEmpty(),
                        onAddProduct = onNavigateToAddProduct,
                        onClearSearch = { viewModel.setWarehouseSearchQuery("") }
                    )
                }
            } else {
                items(products, key = { it.id }) { item ->
                    WarehouseProductCard(
                        product = item,
                        isSelected = selectedIds.contains(item.id),
                        onToggleSelect = { viewModel.toggleWarehouseProductSelection(item.id) },
                        onEdit = { onNavigateToEditProduct(item.id) },
                        onDelete = { productToDelete = item },
                        onQuickQuantityChange = { delta ->
                            val newQty = (item.quantity + delta).coerceAtLeast(0.0)
                            viewModel.updateProductStock(item.id, newQty)
                        }
                    )
                }
            }

            // Extra padding at bottom for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun WarehouseKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WarehouseProductCard(
    product: ProductEntity,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickQuantityChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("warehouse_product_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ChurchNavy.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, ChurchNavy)
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Checkbox, Emoji/Photo, Name, Category, Edit & Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = ChurchNavy)
                )

                // Product visual (Photo or Emoji)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChurchGoldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!product.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = File(product.imageUri),
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = product.iconEmoji.ifBlank { "📦" },
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Name & Category
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ChurchNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!product.isActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "معطل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${product.category} • وحدة القياس: ${product.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = ChurchNavy,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Bottom Row: Price, Quantity (+ / -), Total calculation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Unit price
                Column {
                    Text(
                        text = "سعر الوحدة",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatAmount(product.currentPrice)} ج.م",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChurchNavy
                    )
                }

                // Quick Quantity Controls (+ / -)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الكمية بالمخزن",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onQuickQuantityChange(-1.0) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            enabled = product.quantity > 0.0
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تقليل الكمية", modifier = Modifier.size(16.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ChurchNavy.copy(alpha = 0.08f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "${formatQuantity(product.quantity)} ${product.unit}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ChurchNavy,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = { onQuickQuantityChange(1.0) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة الكمية", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Product Total Value (سعر الوحدة × الكمية)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ChurchGoldContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChurchGold.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "الإجمالي",
                            style = MaterialTheme.typography.labelSmall,
                            color = ChurchNavy
                        )
                        Text(
                            text = "${formatAmount(product.totalProductPrice)} ج.م",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ChurchNavy
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AggregationDialog(
    items: List<ProductEntity>,
    grandTotal: Double,
    isFilteredSelection: Boolean,
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .testTag("aggregation_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Summarize,
                        contentDescription = null,
                        tint = ChurchGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تجميع المنتجات والمخزن",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ChurchNavy
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isFilteredSelection) "بيان بالأصناف المحددة وحساب إجمالياتها:" else "بيان بجميع أصناف المخزن وحساب إجمالياتها:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Table Header
                Surface(
                    color = ChurchNavy,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المنتج",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.weight(1.8f)
                        )
                        Text(
                            text = "السعر",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "الكمية",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "الإجمالي",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }

                // Table Rows
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items) { item ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = ChurchNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1.8f)
                                )
                                Text(
                                    text = formatAmount(item.currentPrice),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${formatQuantity(item.quantity)} ${item.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${formatAmount(item.totalProductPrice)} ج.م",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = ChurchNavy,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1.3f)
                                )
                            }
                        }
                    }
                }

                // Grand Total Banner
                Surface(
                    color = ChurchGoldContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, ChurchGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "الإجمالي النهائي:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ChurchNavy
                            )
                            Text(
                                text = "مجموع ${items.size} صنف بالمخزن",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${formatAmount(grandTotal)} ج.م",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = ChurchNavy
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onExportPdf()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChurchNavy),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_export_pdf_dialog")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تصدير PDF", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun EmptyWarehouseState(
    hasQuery: Boolean,
    onAddProduct: () -> Unit,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(ChurchGoldContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasQuery) Icons.Default.SearchOff else Icons.Default.Inventory2,
                contentDescription = null,
                tint = ChurchNavy,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = if (hasQuery) "لا توجد نتائج تطابق بحثك" else "المخزن فارغ حالياً",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = ChurchNavy
        )

        Text(
            text = if (hasQuery) "جرب البحث بكلمات أخرى أو امسح شريط البحث." else "ابدأ بإضافة الأصناف والمنتجات والكميات المتاحة في المخزن.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (hasQuery) {
            TextButton(onClick = onClearSearch) {
                Text("مسح البحث")
            }
        } else {
            Button(
                onClick = onAddProduct,
                colors = ButtonDefaults.buttonColors(containerColor = ChurchNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة أول منتج")
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        String.format(Locale.US, "%,d", amount.toLong())
    } else {
        String.format(Locale.US, "%,.2f", amount)
    }
}

private fun formatQuantity(quantity: Double): String {
    return if (quantity % 1.0 == 0.0) {
        quantity.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", quantity)
    }
}
