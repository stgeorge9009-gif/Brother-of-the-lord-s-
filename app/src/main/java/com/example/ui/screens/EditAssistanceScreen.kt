package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProductEntity
import com.example.ui.theme.ChurchGoldContainer
import com.example.ui.theme.ChurchNavy
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAssistanceScreen(
    viewModel: MainViewModel,
    assistanceId: Long,
    onNavigateBack: () -> Unit
) {
    val details by viewModel.repository.observeAssistanceById(assistanceId)
        .collectAsStateWithLifecycle(initialValue = null)

    val products by viewModel.products.collectAsStateWithLifecycle()

    // Map productId -> Pair(unitPriceAtTime, quantity)
    val itemsState = remember { mutableStateMapOf<Long, Pair<Double, Double>>() }

    LaunchedEffect(details, products) {
        if (details != null) {
            itemsState.clear()
            // First load existing items from assistance
            details!!.items.forEach { item ->
                if (item.productId != null) {
                    itemsState[item.productId] = Pair(item.unitPriceAtTime, item.quantity)
                }
            }
        }
    }

    // Live calculated total
    val liveTotal = remember(itemsState.toMap()) {
        itemsState.values.sumOf { (price, qty) -> price * qty }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تعديل المساعدة لـ ${details?.person?.name ?: ""}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ChurchNavy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("assistance_edit_back_button")
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
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الإجمالي المحسوب:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${liveTotal.toInt()} جنيه",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = ChurchNavy
                        )
                    }

                    Button(
                        onClick = {
                            val listToSave = mutableListOf<Triple<ProductEntity, Double, Double>>()
                            products.forEach { prod ->
                                val pair = itemsState[prod.id]
                                if (pair != null && pair.second > 0.0) {
                                    listToSave.add(Triple(prod, pair.first, pair.second))
                                }
                            }
                            viewModel.saveCustomAssistanceItems(assistanceId, listToSave) {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("btn_save_assistance_items"),
                        colors = ButtonDefaults.buttonColors(containerColor = ChurchNavy),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ المساعدة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("edit_assistance_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "اختر كميات المنتجات لهذه المساعدة تحديداً. يظهر الإجمالي تلقائياً أسفل الشاشة.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(products, key = { it.id }) { product ->
                val pair = itemsState[product.id]
                val currentPrice = pair?.first ?: product.currentPrice
                val currentQty = pair?.second ?: 0.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ChurchGoldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(product.iconEmoji, style = MaterialTheme.typography.titleLarge)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${currentPrice.toInt()} ج / ${product.unit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Quantity Counter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledIconButton(
                                onClick = {
                                    if (currentQty > 0) {
                                        val newQty = (currentQty - 1.0).coerceAtLeast(0.0)
                                        itemsState[product.id] = Pair(currentPrice, newQty)
                                    }
                                },
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "إنقاص")
                            }

                            Text(
                                text = currentQty.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.widthIn(min = 28.dp),
                                color = ChurchNavy
                            )

                            FilledIconButton(
                                onClick = {
                                    val newQty = currentQty + 1.0
                                    itemsState[product.id] = Pair(currentPrice, newQty)
                                },
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = ChurchNavy
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "زيادة", tint = MaterialTheme.colorScheme.onPrimary)
                            }
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
