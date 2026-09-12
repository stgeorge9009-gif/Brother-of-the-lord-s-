package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ui.theme.ChurchGoldContainer
import com.example.ui.theme.ChurchNavy
import com.example.ui.viewmodel.MainViewModel
import com.example.util.FileUtil
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    viewModel: MainViewModel,
    productId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("كجم") }
    var priceText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("0") }
    var iconEmoji by remember { mutableStateOf("🍚") }
    var category by remember { mutableStateOf("حبوب ومواد غذائية") }
    var isActive by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = FileUtil.saveUriToInternalStorage(context, it)
            if (savedPath != null) {
                imagePath = savedPath
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            val savedPath = FileUtil.saveUriToInternalStorage(context, tempCameraUri!!)
            if (savedPath != null) {
                imagePath = savedPath
            }
        }
    }

    val popularEmojis = listOf("🥩", "🍚", "🍝", "🌾", "🍬", "🫗", "🫘", "🧂", "🥫", "🫖", "🥛", "📦")
    val defaultUnits = listOf("كجم", "جرام", "لتر", "مل", "قطعة", "عبوة", "كيس", "علبة")
    val categories = listOf(
        "حبوب ومواد غذائية",
        "نشويات",
        "سكريات ومؤن",
        "زيوت وسمن",
        "بقوليات",
        "توابل ومؤن",
        "معلبات",
        "مشروبات",
        "ألبان ومجففات",
        "لحوم ومأكولات",
        "أخرى"
    )

    fun performSave() {
        val price = priceText.toDoubleOrNull()
        val qty = quantityText.toDoubleOrNull()
        if (name.isBlank()) {
            nameError = true
            Toast.makeText(context, "يرجى كتابة اسم المنتج أولاً", Toast.LENGTH_SHORT).show()
        } else if (price == null || price < 0.0) {
            priceError = true
            Toast.makeText(context, "يرجى إدخال سعر صحيح", Toast.LENGTH_SHORT).show()
        } else if (qty == null || qty < 0.0) {
            quantityError = true
            Toast.makeText(context, "يرجى إدخال كمية صحيحة", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.saveProduct(
                id = productId,
                name = name,
                unit = unit,
                currentPrice = price,
                quantity = qty,
                iconEmoji = iconEmoji,
                imageUri = imagePath,
                category = category,
                isActive = isActive,
                notes = notes,
                onComplete = {
                    Toast.makeText(context, "✅ تم حفظ التعديلات والمنتج في قاعدة البيانات بنجاح", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            )
        }
    }

    LaunchedEffect(productId) {
        if (productId > 0L) {
            val found = viewModel.products.value.find { it.id == productId }
            if (found != null) {
                name = found.name
                unit = found.unit
                priceText = if (found.currentPrice % 1.0 == 0.0) found.currentPrice.toInt().toString() else found.currentPrice.toString()
                quantityText = if (found.quantity % 1.0 == 0.0) found.quantity.toInt().toString() else found.quantity.toString()
                iconEmoji = found.iconEmoji
                category = found.category
                isActive = found.isActive
                notes = found.notes
                imagePath = found.imageUri
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (productId > 0L) "تعديل المنتج والأسعار" else "إضافة منتج جديد",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ChurchNavy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_product_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = ChurchNavy
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { performSave() },
                        colors = ButtonDefaults.buttonColors(containerColor = ChurchNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("top_bar_save_product_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "حفظ (Save)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("add_edit_product_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product Photo Header & Picker
            Text(
                text = "صورة المنتج:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ChurchGoldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!imagePath.isNullOrBlank()) {
                        AsyncImage(
                            model = File(imagePath!!),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = iconEmoji, fontSize = 36.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اختر من معرض الصور")
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val file = File(context.cacheDir, "camera_product_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("التقاط صورة بالكامل")
                    }

                    if (!imagePath.isNullOrBlank()) {
                        TextButton(
                            onClick = { imagePath = null },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("إزالة الصورة وتأكيد الأيقونة")
                        }
                    }
                }
            }

            // Emoji selector fallback
            Text(
                text = "أو اختر أيقونة تعبيرية كبديل:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                popularEmojis.take(6).forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (iconEmoji == emoji && imagePath == null) ChurchNavy else ChurchGoldContainer)
                            .clickable {
                                iconEmoji = emoji
                                imagePath = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
            }

            // Product Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_name_input"),
                label = { Text("اسم المنتج *") },
                placeholder = { Text("مثال: أرز ممتاز، لحمة بقر، زيت عباد") },
                leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                isError = nameError,
                supportingText = {
                    if (nameError) Text("يرجى كتابة اسم المنتج", color = MaterialTheme.colorScheme.error)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Category Selection
            Text(
                text = "فئة المنتج:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("الفئة") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Price Field
            OutlinedTextField(
                value = priceText,
                onValueChange = {
                    priceText = it
                    if (it.isNotBlank()) priceError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_price_input"),
                label = { Text("السعر الحالي (بالجنيه) *") },
                placeholder = { Text("40") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = priceError,
                supportingText = {
                    if (priceError) Text("يرجى كتابة سعر صحيح", color = MaterialTheme.colorScheme.error)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Unit Field & Shortcuts
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_unit_input"),
                label = { Text("وحدة القياس *") },
                placeholder = { Text("كجم، لتر، علبة...") },
                leadingIcon = { Icon(Icons.Default.SquareFoot, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Text(
                text = "اختصارات سريعة للوحدات المتاحة:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                defaultUnits.take(4).forEach { u ->
                    FilterChip(
                        selected = unit == u,
                        onClick = { unit = u },
                        label = { Text(u) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                defaultUnits.drop(4).forEach { u ->
                    FilterChip(
                        selected = unit == u,
                        onClick = { unit = u },
                        label = { Text(u) }
                    )
                }
            }

            // Quantity Field
            OutlinedTextField(
                value = quantityText,
                onValueChange = {
                    quantityText = it
                    if (it.isNotBlank()) quantityError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_quantity_input"),
                label = { Text("الكمية المتوفرة بالمخزن *") },
                placeholder = { Text("0") },
                leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = quantityError,
                supportingText = {
                    if (quantityError) Text("يرجى كتابة كمية صحيحة (0 أو أكثر)", color = MaterialTheme.colorScheme.error)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Quantity Shortcuts (+1, +5, +10, -1, -5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تعديل سريع:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(-5.0, -1.0, 1.0, 5.0, 10.0).forEach { delta ->
                    AssistChip(
                        onClick = {
                            val current = quantityText.toDoubleOrNull() ?: 0.0
                            val updated = (current + delta).coerceAtLeast(0.0)
                            quantityText = if (updated % 1.0 == 0.0) updated.toInt().toString() else updated.toString()
                            quantityError = false
                        },
                        label = {
                            Text(if (delta > 0) "+${delta.toInt()}" else "${delta.toInt()}")
                        }
                    )
                }
            }

            // Live Automatic Price Calculation Card (سعر الوحدة × الكمية = إجمالي المنتج)
            val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
            val parsedQty = quantityText.toDoubleOrNull() ?: 0.0
            val calculatedTotal = parsedPrice * parsedQty

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ChurchGoldContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = ChurchNavy,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حساب قيمة المنتج تلقائياً",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ChurchNavy
                            )
                        }
                        Text(
                            text = "سعر الوحدة × الكمية",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${if (parsedPrice % 1.0 == 0.0) parsedPrice.toInt().toString() else parsedPrice.toString()} ج.م × ${if (parsedQty % 1.0 == 0.0) parsedQty.toInt().toString() else parsedQty.toString()} $unit =",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${if (calculatedTotal % 1.0 == 0.0) calculatedTotal.toLong().toString() else String.format(java.util.Locale.US, "%.2f", calculatedTotal)} جنيه",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ChurchNavy
                        )
                    }
                }
            }

            // Active / Inactive Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "حالة المنتج (مفعل للخدمة)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "إذا تم تعطيله، لن يظهر المنتج كخيار لإضافة مساعدات جديدة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

            // Optional Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("ملاحظات إضافية (اختياري)") },
                placeholder = { Text("مثال: يفضل جودة ممتاز أو رقم الصنف") },
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = { performSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_product_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ChurchNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (productId > 0L) "حفظ التعديلات (Save)" else "حفظ المنتج (Save)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
