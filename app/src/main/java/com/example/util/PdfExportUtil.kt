package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.MonthlyAssistanceWithDetails
import com.example.data.model.ProductEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExportUtil {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN_HORIZONTAL = 36f
    private const val MARGIN_TOP = 40f
    private const val MARGIN_BOTTOM = 40f

    // Color definitions
    private const val COLOR_NAVY = 0xFF0F2240.toInt()
    private const val COLOR_GOLD = 0xFFB45309.toInt()
    private const val COLOR_LIGHT_GOLD = 0xFFFEF3C7.toInt()
    private const val COLOR_DARK_GRAY = 0xFF1E293B.toInt()
    private const val COLOR_MUTED_GRAY = 0xFF64748B.toInt()
    private const val COLOR_ROW_ALT = 0xFFF8FAFC.toInt()
    private const val COLOR_BORDER = 0xFFCBD5E1.toInt()
    private const val COLOR_GREEN = 0xFF15803D.toInt()
    private const val COLOR_RED = 0xFFB91C1C.toInt()

    fun generateWarehouseProductsPdf(
        context: Context,
        items: List<ProductEntity>,
        grandTotal: Double,
        reportTitle: String = "تقرير بضاعة المخزن والمنتجات"
    ): File? {
        return try {
            val pdfDoc = PdfDocument()
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar"))
            val nowFormatted = dateFormat.format(Date())

            val itemsPerPage = 16
            val totalPages = if (items.isEmpty()) 1 else ((items.size - 1) / itemsPerPage) + 1

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas = page.canvas

                // Background
                canvas.drawColor(Color.WHITE)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val isFirstPage = pageIndex == 0
                val isLastPage = pageIndex == totalPages - 1

                var currentY = MARGIN_TOP

                // Header on every page (expanded on first page)
                currentY = drawReportHeader(
                    canvas = canvas,
                    paint = paint,
                    startY = currentY,
                    reportTitle = reportTitle,
                    dateTime = nowFormatted,
                    isFirstPage = isFirstPage,
                    grandTotal = grandTotal,
                    itemsCount = items.size
                )

                // Table column definitions (RTL Order: From Right to Left)
                // Left margin is MARGIN_HORIZONTAL, Right margin is PAGE_WIDTH - MARGIN_HORIZONTAL
                val tableLeft = MARGIN_HORIZONTAL
                val tableRight = PAGE_WIDTH - MARGIN_HORIZONTAL
                val tableWidth = tableRight - tableLeft

                // Columns: [Total: 80pt] [Quantity: 65pt] [Price: 65pt] [Category: 85pt] [Name: rest] [No: 30pt]
                val colNoWidth = 30f
                val colTotalWidth = 85f
                val colQtyWidth = 70f
                val colPriceWidth = 70f
                val colCatWidth = 85f
                val colNameWidth = tableWidth - (colNoWidth + colTotalWidth + colQtyWidth + colPriceWidth + colCatWidth)

                // Table Header
                val headerHeight = 26f
                paint.style = Paint.Style.FILL
                paint.color = COLOR_NAVY
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + headerHeight, paint)

                paint.color = Color.WHITE
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 9.5f
                paint.textAlign = Paint.Align.CENTER

                val headerTextY = currentY + 17f
                var xCursor = tableRight

                // Col 1 (Rightmost): م
                canvas.drawText("م", xCursor - colNoWidth / 2, headerTextY, paint)
                xCursor -= colNoWidth

                // Col 2: اسم المنتج والوحدة
                canvas.drawText("اسم الصنف والوحدة", xCursor - colNameWidth / 2, headerTextY, paint)
                xCursor -= colNameWidth

                // Col 3: الفئة
                canvas.drawText("الفئة", xCursor - colCatWidth / 2, headerTextY, paint)
                xCursor -= colCatWidth

                // Col 4: سعر الوحدة
                canvas.drawText("سعر الوحدة", xCursor - colPriceWidth / 2, headerTextY, paint)
                xCursor -= colPriceWidth

                // Col 5: الكمية
                canvas.drawText("الكمية", xCursor - colQtyWidth / 2, headerTextY, paint)
                xCursor -= colQtyWidth

                // Col 6 (Leftmost): الإجمالي
                canvas.drawText("الإجمالي", xCursor - colTotalWidth / 2, headerTextY, paint)

                currentY += headerHeight

                // Draw Table Rows for this page
                val startIndex = pageIndex * itemsPerPage
                val endIndex = minOf(startIndex + itemsPerPage, items.size)
                val rowHeight = 24f

                for (i in startIndex until endIndex) {
                    val item = items[i]
                    val isAlt = (i % 2 == 1)

                    // Row background
                    paint.style = Paint.Style.FILL
                    paint.color = if (isAlt) COLOR_ROW_ALT else Color.WHITE
                    canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, paint)

                    // Row bottom border
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 0.5f
                    paint.color = COLOR_BORDER
                    canvas.drawLine(tableLeft, currentY + rowHeight, tableRight, currentY + rowHeight, paint)

                    paint.style = Paint.Style.FILL
                    paint.typeface = Typeface.DEFAULT
                    paint.textSize = 9f
                    paint.color = COLOR_DARK_GRAY

                    val rowTextY = currentY + 16f
                    var rowX = tableRight

                    // No.
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("${i + 1}", rowX - colNoWidth / 2, rowTextY, paint)
                    rowX -= colNoWidth

                    // Name
                    paint.textAlign = Paint.Align.RIGHT
                    val displayName = "${item.name} (${item.unit})"
                    canvas.drawText(displayName, rowX - 6f, rowTextY, paint)
                    rowX -= colNameWidth

                    // Category
                    paint.textAlign = Paint.Align.CENTER
                    paint.color = COLOR_MUTED_GRAY
                    canvas.drawText(item.category, rowX - colCatWidth / 2, rowTextY, paint)
                    rowX -= colCatWidth

                    // Price
                    paint.color = COLOR_DARK_GRAY
                    val priceStr = formatAmount(item.currentPrice) + " ج.م"
                    canvas.drawText(priceStr, rowX - colPriceWidth / 2, rowTextY, paint)
                    rowX -= colPriceWidth

                    // Quantity
                    paint.color = COLOR_NAVY
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val qtyStr = formatQuantity(item.quantity)
                    canvas.drawText(qtyStr, rowX - colQtyWidth / 2, rowTextY, paint)
                    rowX -= colQtyWidth

                    // Total
                    paint.color = COLOR_GOLD
                    val totalStr = formatAmount(item.totalProductPrice) + " ج.م"
                    canvas.drawText(totalStr, rowX - colTotalWidth / 2, rowTextY, paint)

                    currentY += rowHeight
                }

                // If last page, draw Grand Total Summary Box
                if (isLastPage) {
                    currentY += 14f

                    // Summary Card
                    paint.style = Paint.Style.FILL
                    paint.color = COLOR_LIGHT_GOLD
                    canvas.drawRoundRect(tableLeft, currentY, tableRight, currentY + 38f, 8f, 8f, paint)

                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1f
                    paint.color = COLOR_GOLD
                    canvas.drawRoundRect(tableLeft, currentY, tableRight, currentY + 38f, 8f, 8f, paint)

                    paint.style = Paint.Style.FILL
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 11f
                    paint.color = COLOR_NAVY
                    paint.textAlign = Paint.Align.RIGHT
                    canvas.drawText("الإجمالي النهائي لجميع المنتجات:", tableRight - 16f, currentY + 24f, paint)

                    paint.textSize = 13f
                    paint.color = COLOR_GOLD
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText("${formatAmount(grandTotal)} جنيه مصري", tableLeft + 16f, currentY + 24f, paint)
                }

                // Page Footer on every page
                drawReportFooter(canvas, paint, pageIndex + 1, totalPages)

                pdfDoc.finishPage(page)
            }

            // Save PDF to cache or files directory
            val reportsDir = File(context.cacheDir, "pdf_reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }
            val fileName = "تقرير_المخزن_${System.currentTimeMillis()}.pdf"
            val file = File(reportsDir, fileName)
            val fos = FileOutputStream(file)
            pdfDoc.writeTo(fos)
            fos.flush()
            fos.close()
            pdfDoc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateFamilyAssistanceReportPdf(
        context: Context,
        year: Int,
        month: Int,
        monthName: String,
        assistances: List<MonthlyAssistanceWithDetails>
    ): File? {
        return try {
            val pdfDoc = PdfDocument()
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar"))
            val nowFormatted = dateFormat.format(Date())

            val itemsPerPage = 12
            val totalPages = if (assistances.isEmpty()) 1 else ((assistances.size - 1) / itemsPerPage) + 1
            val grandTotal = assistances.sumOf { it.assistance.totalAmount }

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas = page.canvas

                canvas.drawColor(Color.WHITE)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val isFirstPage = pageIndex == 0
                val isLastPage = pageIndex == totalPages - 1

                var currentY = MARGIN_TOP

                // Header
                currentY = drawReportHeader(
                    canvas = canvas,
                    paint = paint,
                    startY = currentY,
                    reportTitle = "كشف مساعدات الأسر - شهر $monthName $year",
                    dateTime = nowFormatted,
                    isFirstPage = isFirstPage,
                    grandTotal = grandTotal,
                    itemsCount = assistances.size,
                    countLabel = "عدد الأسر"
                )

                val tableLeft = MARGIN_HORIZONTAL
                val tableRight = PAGE_WIDTH - MARGIN_HORIZONTAL
                val tableWidth = tableRight - tableLeft

                // Columns: [Status: 55pt] [Total: 65pt] [Package Items: 175pt] [Phone/Address: 90pt] [Name: rest] [No: 25pt]
                val colNoWidth = 25f
                val colStatusWidth = 55f
                val colTotalWidth = 65f
                val colPackageWidth = 175f
                val colPhoneWidth = 90f
                val colNameWidth = tableWidth - (colNoWidth + colStatusWidth + colTotalWidth + colPackageWidth + colPhoneWidth)

                // Table Header
                val headerHeight = 26f
                paint.style = Paint.Style.FILL
                paint.color = COLOR_NAVY
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + headerHeight, paint)

                paint.color = Color.WHITE
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 9f
                paint.textAlign = Paint.Align.CENTER

                val headerTextY = currentY + 17f
                var xCursor = tableRight

                canvas.drawText("م", xCursor - colNoWidth / 2, headerTextY, paint)
                xCursor -= colNoWidth

                canvas.drawText("اسم الأسرة / المستفيد", xCursor - colNameWidth / 2, headerTextY, paint)
                xCursor -= colNameWidth

                canvas.drawText("العنوان والهاتف", xCursor - colPhoneWidth / 2, headerTextY, paint)
                xCursor -= colPhoneWidth

                canvas.drawText("الأصناف المقررة بالكمية", xCursor - colPackageWidth / 2, headerTextY, paint)
                xCursor -= colPackageWidth

                canvas.drawText("الإجمالي", xCursor - colTotalWidth / 2, headerTextY, paint)
                xCursor -= colTotalWidth

                canvas.drawText("الحالة", xCursor - colStatusWidth / 2, headerTextY, paint)

                currentY += headerHeight

                // Draw rows
                val startIndex = pageIndex * itemsPerPage
                val endIndex = minOf(startIndex + itemsPerPage, assistances.size)
                val rowHeight = 32f

                for (i in startIndex until endIndex) {
                    val item = assistances[i]
                    val isAlt = (i % 2 == 1)

                    paint.style = Paint.Style.FILL
                    paint.color = if (isAlt) COLOR_ROW_ALT else Color.WHITE
                    canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, paint)

                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 0.5f
                    paint.color = COLOR_BORDER
                    canvas.drawLine(tableLeft, currentY + rowHeight, tableRight, currentY + rowHeight, paint)

                    paint.style = Paint.Style.FILL
                    paint.typeface = Typeface.DEFAULT
                    paint.textSize = 8.5f
                    paint.color = COLOR_DARK_GRAY

                    val rowTextY = currentY + 20f
                    var rowX = tableRight

                    // No
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("${i + 1}", rowX - colNoWidth / 2, rowTextY, paint)
                    rowX -= colNoWidth

                    // Beneficiary Name
                    paint.textAlign = Paint.Align.RIGHT
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val personName = item.person?.name ?: "مستفيد غير محدد"
                    canvas.drawText(personName, rowX - 4f, rowTextY, paint)
                    rowX -= colNameWidth

                    // Phone & Address
                    paint.typeface = Typeface.DEFAULT
                    paint.textSize = 7.5f
                    paint.color = COLOR_MUTED_GRAY
                    val phoneAddr = buildString {
                        if (!item.person?.phone.isNullOrBlank()) append(item.person?.phone)
                        if (!item.person?.address.isNullOrBlank()) {
                            if (isNotEmpty()) append(" - ")
                            append(item.person?.address)
                        }
                    }.ifBlank { "-" }
                    canvas.drawText(phoneAddr, rowX - 4f, rowTextY, paint)
                    rowX -= colPhoneWidth

                    // Package Items with detailed prices and quantities
                    paint.textSize = 7.5f
                    paint.color = COLOR_DARK_GRAY
                    val itemsSummary = if (item.items.isEmpty()) {
                        "لا توجد أصناف مضافة"
                    } else {
                        item.items.joinToString(" ، ") {
                            "${it.productName} (${formatQuantity(it.quantity)} ${it.productUnit} × ${formatAmount(it.unitPriceAtTime)} = ${formatAmount(it.totalPrice)} ج.م)"
                        }
                    }
                    canvas.drawText(itemsSummary, rowX - 4f, rowTextY, paint)
                    rowX -= colPackageWidth

                    // Total Price
                    paint.textAlign = Paint.Align.CENTER
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = COLOR_GOLD
                    canvas.drawText("${formatAmount(item.assistance.totalAmount)} ج.م", rowX - colTotalWidth / 2, rowTextY, paint)
                    rowX -= colTotalWidth

                    // Status
                    val isDelivered = item.assistance.status == "DELIVERED"
                    paint.color = if (isDelivered) COLOR_GREEN else COLOR_RED
                    canvas.drawText(if (isDelivered) "تم التسليم" else "متبقي", rowX - colStatusWidth / 2, rowTextY, paint)

                    currentY += rowHeight
                }

                if (isLastPage) {
                    currentY += 14f
                    paint.style = Paint.Style.FILL
                    paint.color = COLOR_LIGHT_GOLD
                    canvas.drawRoundRect(tableLeft, currentY, tableRight, currentY + 38f, 8f, 8f, paint)

                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1f
                    paint.color = COLOR_GOLD
                    canvas.drawRoundRect(tableLeft, currentY, tableRight, currentY + 38f, 8f, 8f, paint)

                    paint.style = Paint.Style.FILL
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 11f
                    paint.color = COLOR_NAVY
                    paint.textAlign = Paint.Align.RIGHT
                    canvas.drawText("إجمالي قيمة مساعدات الشهر:", tableRight - 16f, currentY + 24f, paint)

                    paint.textSize = 13f
                    paint.color = COLOR_GOLD
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText("${formatAmount(grandTotal)} جنيه مصري", tableLeft + 16f, currentY + 24f, paint)
                }

                drawReportFooter(canvas, paint, pageIndex + 1, totalPages)
                pdfDoc.finishPage(page)
            }

            val reportsDir = File(context.cacheDir, "pdf_reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()
            val fileName = "كشف_مساعدات_${year}_${month}_${System.currentTimeMillis()}.pdf"
            val file = File(reportsDir, fileName)
            val fos = FileOutputStream(file)
            pdfDoc.writeTo(fos)
            fos.flush()
            fos.close()
            pdfDoc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateDetailedFamilyBreakdownPdf(
        context: Context,
        year: Int,
        month: Int,
        monthName: String,
        assistances: List<MonthlyAssistanceWithDetails>,
        customTitle: String = "كشف تجميعة مساعدات الأسر بالمنتجات والأسعار التفصيلية"
    ): File? {
        return try {
            val pdfDoc = PdfDocument()
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar"))
            val nowFormatted = dateFormat.format(Date())

            val grandTotal = assistances.sumOf { it.assistance.totalAmount }
            val tableLeft = MARGIN_HORIZONTAL
            val tableRight = PAGE_WIDTH - MARGIN_HORIZONTAL
            val tableWidth = tableRight - tableLeft

            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            var page = pdfDoc.startPage(pageInfo)
            var canvas = page.canvas
            canvas.drawColor(Color.WHITE)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            var currentY = MARGIN_TOP

            currentY = drawReportHeader(
                canvas = canvas,
                paint = paint,
                startY = currentY,
                reportTitle = "$customTitle - شهر $monthName $year",
                dateTime = nowFormatted,
                isFirstPage = true,
                grandTotal = grandTotal,
                itemsCount = assistances.size,
                countLabel = "عدد الأسر"
            )

            val maxYBeforeFooter = PAGE_HEIGHT - MARGIN_BOTTOM - 20f

            for (index in assistances.indices) {
                val family = assistances[index]
                val items = family.items
                val familyBlockHeight = 26f + (if (items.isNotEmpty()) (20f + items.size * 18f) else 24f) + 24f + 8f

                if (currentY + familyBlockHeight > maxYBeforeFooter && currentY > MARGIN_TOP + 100f) {
                    drawReportFooter(canvas, paint, currentPageNumber, currentPageNumber)
                    pdfDoc.finishPage(page)

                    currentPageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas
                    canvas.drawColor(Color.WHITE)
                    currentY = MARGIN_TOP

                    currentY = drawReportHeader(
                        canvas = canvas,
                        paint = paint,
                        startY = currentY,
                        reportTitle = "$customTitle - شهر $monthName $year (تابع)",
                        dateTime = nowFormatted,
                        isFirstPage = false,
                        grandTotal = grandTotal,
                        itemsCount = assistances.size,
                        countLabel = "عدد الأسر"
                    )
                }

                paint.style = Paint.Style.FILL
                paint.color = COLOR_NAVY
                canvas.drawRoundRect(tableLeft, currentY, tableRight, currentY + 24f, 6f, 6f, paint)

                paint.color = Color.WHITE
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 10f
                paint.textAlign = Paint.Align.RIGHT
                val familyName = family.person?.name ?: "مستفيد غير محدد"
                val membersCount = family.person?.familyMembers ?: 1
                val familyInfo = "$membersCount أفراد"
                canvas.drawText("${index + 1}. $familyName  •  [$familyInfo]", tableRight - 10f, currentY + 16f, paint)

                paint.typeface = Typeface.DEFAULT
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.LEFT
                val contactInfo = buildString {
                    if (!family.person?.phone.isNullOrBlank()) append("الهاتف: ${family.person?.phone}   ")
                    if (!family.person?.address.isNullOrBlank()) append("العنوان: ${family.person?.address}")
                }
                canvas.drawText(contactInfo, tableLeft + 10f, currentY + 16f, paint)

                currentY += 26f

                if (items.isNotEmpty()) {
                    paint.style = Paint.Style.FILL
                    paint.color = COLOR_LIGHT_GOLD
                    canvas.drawRect(tableLeft, currentY, tableRight, currentY + 18f, paint)

                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 0.5f
                    paint.color = COLOR_BORDER
                    canvas.drawLine(tableLeft, currentY + 18f, tableRight, currentY + 18f, paint)

                    paint.style = Paint.Style.FILL
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 8f
                    paint.color = COLOR_DARK_GRAY

                    val subTotalW = 75f
                    val subPriceW = 75f
                    val subQtyW = 85f
                    val subProdW = tableWidth - (subTotalW + subPriceW + subQtyW)

                    val subHeaderY = currentY + 13f
                    var sx = tableRight

                    paint.textAlign = Paint.Align.RIGHT
                    canvas.drawText("الصنف المقترح للأسرة", sx - 10f, subHeaderY, paint)
                    sx -= subProdW

                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("الكمية المقررة", sx - subQtyW / 2, subHeaderY, paint)
                    sx -= subQtyW

                    canvas.drawText("سعر الوحدة", sx - subPriceW / 2, subHeaderY, paint)
                    sx -= subPriceW

                    canvas.drawText("إجمالي الصنف", sx - subTotalW / 2, subHeaderY, paint)

                    currentY += 18f

                    for (item in items) {
                        paint.style = Paint.Style.FILL
                        paint.color = Color.WHITE
                        canvas.drawRect(tableLeft, currentY, tableRight, currentY + 18f, paint)

                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 0.5f
                        paint.color = COLOR_BORDER
                        canvas.drawLine(tableLeft, currentY + 18f, tableRight, currentY + 18f, paint)

                        paint.style = Paint.Style.FILL
                        paint.typeface = Typeface.DEFAULT
                        paint.textSize = 8f
                        paint.color = COLOR_DARK_GRAY

                        val rowY = currentY + 13f
                        var rx = tableRight

                        paint.textAlign = Paint.Align.RIGHT
                        canvas.drawText("• ${item.productName}", rx - 10f, rowY, paint)
                        rx -= subProdW

                        paint.textAlign = Paint.Align.CENTER
                        paint.color = COLOR_NAVY
                        canvas.drawText("${formatQuantity(item.quantity)} ${item.productUnit}", rx - subQtyW / 2, rowY, paint)
                        rx -= subQtyW

                        paint.color = COLOR_DARK_GRAY
                        canvas.drawText("${formatAmount(item.unitPriceAtTime)} ج.م", rx - subPriceW / 2, rowY, paint)
                        rx -= subPriceW

                        paint.color = COLOR_GOLD
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText("${formatAmount(item.totalPrice)} ج.م", rx - subTotalW / 2, rowY, paint)

                        currentY += 18f
                    }
                } else {
                    paint.style = Paint.Style.FILL
                    paint.color = COLOR_ROW_ALT
                    canvas.drawRect(tableLeft, currentY, tableRight, currentY + 20f, paint)

                    paint.color = COLOR_MUTED_GRAY
                    paint.typeface = Typeface.DEFAULT
                    paint.textSize = 8f
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("لا توجد أصناف مضافة لطرد هذه الأسرة", tableLeft + tableWidth / 2, currentY + 14f, paint)
                    currentY += 20f
                }

                paint.style = Paint.Style.FILL
                paint.color = COLOR_ROW_ALT
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + 22f, paint)

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.8f
                paint.color = COLOR_BORDER
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + 22f, paint)

                paint.style = Paint.Style.FILL
                val summaryY = currentY + 15f

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 9f
                paint.color = COLOR_NAVY
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("إجمالي طرد الأسرة: ${formatAmount(family.assistance.totalAmount)} جنيه", tableRight - 10f, summaryY, paint)

                val isDelivered = family.assistance.status == "DELIVERED"
                paint.color = if (isDelivered) COLOR_GREEN else COLOR_RED
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(if (isDelivered) "الحالة: تم الاستلام" else "الحالة: متبقي للتسليم", tableLeft + tableWidth * 0.45f, summaryY, paint)

                paint.color = COLOR_MUTED_GRAY
                paint.typeface = Typeface.DEFAULT
                paint.textSize = 8f
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("توقيع المستلم: __________________", tableLeft + 10f, summaryY, paint)

                currentY += 28f
            }

            if (currentY + 45f > maxYBeforeFooter) {
                drawReportFooter(canvas, paint, currentPageNumber, currentPageNumber)
                pdfDoc.finishPage(page)

                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                canvas.drawColor(Color.WHITE)
                currentY = MARGIN_TOP + 20f
            }

            currentY += 10f
            paint.style = Paint.Style.FILL
            paint.color = COLOR_LIGHT_GOLD
            canvas.drawRoundRect(tableLeft, currentY, tableRight, currentY + 40f, 8f, 8f, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f
            paint.color = COLOR_GOLD
            canvas.drawRoundRect(tableLeft, currentY, tableRight, currentY + 40f, 8f, 8f, paint)

            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 12f
            paint.color = COLOR_NAVY
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("الإجمالي الكلي لمساعدات جميع الأسر المذكورة:", tableRight - 16f, currentY + 25f, paint)

            paint.textSize = 14f
            paint.color = COLOR_GOLD
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("${formatAmount(grandTotal)} جنيه مصري", tableLeft + 16f, currentY + 25f, paint)

            drawReportFooter(canvas, paint, currentPageNumber, currentPageNumber)
            pdfDoc.finishPage(page)

            val reportsDir = File(context.cacheDir, "pdf_reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()
            val fileName = "تجميعة_الأسر_والمنتجات_${year}_${month}_${System.currentTimeMillis()}.pdf"
            val file = File(reportsDir, fileName)
            val fos = FileOutputStream(file)
            pdfDoc.writeTo(fos)
            fos.flush()
            fos.close()
            pdfDoc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawReportHeader(
        canvas: Canvas,
        paint: Paint,
        startY: Float,
        reportTitle: String,
        dateTime: String,
        isFirstPage: Boolean,
        grandTotal: Double,
        itemsCount: Int,
        countLabel: String = "عدد الأصناف"
    ): Float {
        var y = startY
        val left = MARGIN_HORIZONTAL
        val right = PAGE_WIDTH - MARGIN_HORIZONTAL

        // Church / Organization Header Bar
        paint.color = COLOR_NAVY
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("خدمة إخوة الرب - رعاية الأسر المحتاجة", right, y + 14f, paint)

        paint.color = COLOR_MUTED_GRAY
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("تاريخ التقرير: $dateTime", left, y + 14f, paint)

        y += 24f

        // Decorative separating line
        paint.strokeWidth = 2f
        paint.color = COLOR_GOLD
        paint.style = Paint.Style.STROKE
        canvas.drawLine(left, y, right, y, paint)

        y += 18f

        // Title
        paint.style = Paint.Style.FILL
        paint.color = COLOR_NAVY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(reportTitle, right, y, paint)

        if (isFirstPage) {
            y += 18f
            // Subtitle badges
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 9.5f
            paint.color = COLOR_DARK_GRAY
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$countLabel: $itemsCount صنف/مستفيد   |   إجمالي القيمة: ${formatAmount(grandTotal)} ج.م", right, y, paint)
        }

        y += 16f
        return y
    }

    private fun drawReportFooter(canvas: Canvas, paint: Paint, currentPage: Int, totalPages: Int) {
        val y = PAGE_HEIGHT - MARGIN_BOTTOM + 18f
        val left = MARGIN_HORIZONTAL
        val right = PAGE_WIDTH - MARGIN_HORIZONTAL

        paint.strokeWidth = 0.5f
        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        canvas.drawLine(left, y - 10f, right, y - 10f, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8f
        paint.color = COLOR_MUTED_GRAY
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("تم استخراج التقرير بواسطة تطبيق إخوة الرب • Created by Mekhaeel Yasser", right, y, paint)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("صفحة $currentPage من $totalPages", left, y, paint)
    }

    fun openOrSharePdf(context: Context, file: File, title: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر فتح ملف الـ PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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
}
