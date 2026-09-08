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
import com.example.ui.theme.ChurchGoldContainer
import com.example.ui.theme.ChurchNavy
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonPackageScreen(
    viewModel: MainViewModel,
    personId: Long,
    onNavigateBack: () -> Unit
) {
    val person by viewModel.repository.observePersonById(personId)
        .collectAsStateWithLifecycle(initialValue = null)

    val products by viewModel.products.collectAsStateWithLifecycle()
    val existingPackage by viewModel.repository.observeStandardPackageForPerson(personId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Map productId -> quantity
    val quantitiesMap = remember { mutableStateMapOf<Long, Double>() }

    LaunchedEffect(existingPackage) {
        if (existingPackage.isNotEmpty()) {
            quantitiesMap.clear()
            existingPackage.forEach { item ->
                quantitiesMap[item.packageItem.productId] = item.packageItem.quantity
            }
        }
    }

    // Live calculated total
    val liveTotal = remember(quantitiesMap.toMap(), products) {
        products.sumOf { p -> (quantitiesMap[p.id] ?: 0.0) * p.currentPrice }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "السلة الثابتة لـ ${person?.name ?: ""}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ChurchNavy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("package_back_button")
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
                            text = "الإجمالي المتوقع:",
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
                            val items = quantitiesMap.map { (prodId, qty) -> prodId to qty }
                            viewModel.savePersonStandardPackage(personId, items)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("btn_save_package"),
                        colors = ButtonDefaults.buttonColors(containerColor = ChurchNavy),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ السلة", fontWeight = FontWeight.Bold)
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
                .testTag("edit_person_package_screen"),
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
                        text = "حدد كميات المنتجات التي يستلمها الشخص شهرياً. سيتم حساب المساعدة الشهرية تلقائياً بناءً على هذه السلة.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(products, key = { it.id }) { product ->
                val currentQty = quantitiesMap[product.id] ?: 0.0

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
                                    text = "${product.currentPrice.toInt()} ج / ${product.unit}",
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
                                        quantitiesMap[product.id] = (currentQty - 1.0).coerceAtLeast(0.0)
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
                                    quantitiesMap[product.id] = currentQty + 1.0
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
