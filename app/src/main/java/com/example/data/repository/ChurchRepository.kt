package com.example.data.repository

import com.example.data.dao.AssistanceDao
import com.example.data.dao.PersonDao
import com.example.data.dao.ProductDao
import com.example.data.model.*
import com.example.util.CalendarUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ChurchRepository(
    private val personDao: PersonDao,
    private val productDao: ProductDao,
    private val assistanceDao: AssistanceDao
) {
    // Person Operations
    val allPersons: Flow<List<PersonEntity>> = personDao.getAllPersons()
    val personCount: Flow<Int> = personDao.getPersonCount()

    fun searchPersons(query: String): Flow<List<PersonEntity>> = personDao.searchPersons(query)

    suspend fun getPersonById(id: Long): PersonEntity? = personDao.getPersonById(id)
    fun observePersonById(id: Long): Flow<PersonEntity?> = personDao.observePersonById(id)

    suspend fun insertOrUpdatePerson(person: PersonEntity): Long {
        return if (person.id <= 0L) {
            val entityToInsert = if (person.id != 0L) person.copy(id = 0L) else person
            personDao.insertPerson(entityToInsert)
        } else {
            personDao.updatePerson(person)
            person.id
        }
    }

    suspend fun deletePerson(person: PersonEntity) {
        personDao.deletePerson(person)
    }

    // Standard Package Operations
    fun observeStandardPackageForPerson(personId: Long): Flow<List<StandardPackageItemWithProduct>> {
        return personDao.getStandardPackageWithProducts(personId)
    }

    suspend fun setStandardPackageForPerson(personId: Long, items: List<Pair<Long, Double>>) {
        personDao.clearStandardPackageForPerson(personId)
        val packageEntities = items.filter { it.second > 0 }.map { (productId, qty) ->
            PersonStandardPackageItemEntity(
                personId = personId,
                productId = productId,
                quantity = qty
            )
        }
        if (packageEntities.isNotEmpty()) {
            personDao.insertStandardPackageItems(packageEntities)
        }
    }

    // Product Operations
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun seedDefaultProductsIfEmpty() {
        if (productDao.getProductCount() == 0) {
            val defaults = listOf(
                ProductEntity(name = "لحمة", unit = "كجم", currentPrice = 350.0, iconEmoji = "🥩", category = "لحوم ومأكولات"),
                ProductEntity(name = "أرز", unit = "كجم", currentPrice = 40.0, iconEmoji = "🍚", category = "حبوب ومواد غذائية"),
                ProductEntity(name = "مكرونة", unit = "كيس", currentPrice = 25.0, iconEmoji = "🍝", category = "نشويات"),
                ProductEntity(name = "دقيق", unit = "كجم", currentPrice = 30.0, iconEmoji = "🌾", category = "نشويات"),
                ProductEntity(name = "سكر", unit = "كجم", currentPrice = 35.0, iconEmoji = "🍬", category = "سكريات ومؤن"),
                ProductEntity(name = "زيت", unit = "لتر", currentPrice = 80.0, iconEmoji = "🫗", category = "زيوت وسمن"),
                ProductEntity(name = "فول", unit = "كجم", currentPrice = 45.0, iconEmoji = "🫘", category = "بقوليات"),
                ProductEntity(name = "ملح", unit = "عبوة", currentPrice = 10.0, iconEmoji = "🧂", category = "توابل ومؤن"),
                ProductEntity(name = "صلصة", unit = "علبة", currentPrice = 20.0, iconEmoji = "🥫", category = "معلبات"),
                ProductEntity(name = "شاي", unit = "عبوة", currentPrice = 20.0, iconEmoji = "🫖", category = "مشروبات"),
                ProductEntity(name = "لبن", unit = "لتر", currentPrice = 35.0, iconEmoji = "🥛", category = "ألبان ومجففات"),
                ProductEntity(name = "عدس", unit = "كجم", currentPrice = 50.0, iconEmoji = "🫘", category = "بقوليات"),
                ProductEntity(name = "برغل", unit = "كجم", currentPrice = 40.0, iconEmoji = "🌾", category = "حبوب ومواد غذائية")
            )
            productDao.insertProducts(defaults)
        }
    }

    suspend fun insertOrUpdateProduct(product: ProductEntity): Long {
        return if (product.id <= 0L) {
            val entityToInsert = if (product.id != 0L) product.copy(id = 0L) else product
            productDao.insertProduct(entityToInsert)
        } else {
            productDao.updateProduct(product)
            product.id
        }
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    // Monthly Assistance Operations
    fun getAssistancesForMonth(year: Int, month: Int): Flow<List<MonthlyAssistanceWithDetails>> {
        return assistanceDao.getAssistancesForMonth(year, month)
    }

    fun observeAssistanceForPersonAndMonth(personId: Long, year: Int, month: Int): Flow<MonthlyAssistanceWithDetails?> {
        return assistanceDao.observeAssistanceForPersonAndMonth(personId, year, month)
    }

    fun getAssistancesForPerson(personId: Long): Flow<List<MonthlyAssistanceWithDetails>> {
        return assistanceDao.getAssistancesForPerson(personId)
    }

    fun observeAssistanceById(assistanceId: Long): Flow<MonthlyAssistanceWithDetails?> {
        return assistanceDao.observeAssistanceById(assistanceId)
    }

    fun getAssistancesForDate(isoDate: String): Flow<List<MonthlyAssistanceWithDetails>> {
        return assistanceDao.getAssistancesForDate(isoDate)
    }

    fun getAssistancesBetweenDates(startDateIso: String, endDateIso: String): Flow<List<MonthlyAssistanceWithDetails>> {
        return assistanceDao.getAssistancesBetweenDates(startDateIso, endDateIso)
    }

    fun getAssistanceCountForMonth(year: Int, month: Int): Flow<Int> = assistanceDao.getAssistanceCountForMonth(year, month)
    fun getDeliveredCountForMonth(year: Int, month: Int): Flow<Int> = assistanceDao.getDeliveredCountForMonth(year, month)
    fun getTotalAmountForMonth(year: Int, month: Int): Flow<Double?> = assistanceDao.getTotalAmountForMonth(year, month)
    fun getAllAssistances(): Flow<List<MonthlyAssistanceWithDetails>> = assistanceDao.getAllAssistances()

    suspend fun generateMonthlyAssistanceForPerson(
        personId: Long,
        year: Int,
        month: Int
    ): Long {
        val existing = assistanceDao.getAssistanceForPersonAndMonth(personId, year, month)
        if (existing != null) return existing.assistance.id

        val person = personDao.getPersonById(personId) ?: return 0L
        val packageWithProducts = personDao.getStandardPackageListWithProducts(personId)

        val scheduledIso = CalendarUtil.calculateScheduledDate(year, month, person.scheduledDayOfMonth)

        val newAssistance = MonthlyAssistanceEntity(
            personId = personId,
            year = year,
            month = month,
            status = "PENDING",
            scheduledDate = scheduledIso,
            totalAmount = 0.0
        )
        val assistanceId = assistanceDao.insertMonthlyAssistance(newAssistance)

        var calculatedTotal = 0.0
        val itemsToInsert = mutableListOf<AssistanceItemEntity>()

        for (item in packageWithProducts) {
            val product = item.product ?: continue
            val qty = item.packageItem.quantity
            if (qty > 0) {
                val unitPrice = product.currentPrice // Snapshot unit price!
                val total = unitPrice * qty
                calculatedTotal += total
                itemsToInsert.add(
                    AssistanceItemEntity(
                        assistanceId = assistanceId,
                        productId = product.id,
                        productName = product.name,
                        productUnit = product.unit,
                        unitPriceAtTime = unitPrice,
                        quantity = qty,
                        totalPrice = total
                    )
                )
            }
        }

        if (itemsToInsert.isNotEmpty()) {
            assistanceDao.insertAssistanceItems(itemsToInsert)
        }

        assistanceDao.updateMonthlyAssistance(
            newAssistance.copy(id = assistanceId, totalAmount = calculatedTotal)
        )

        return assistanceId
    }

    suspend fun saveAssistanceCustomItems(
        assistanceId: Long,
        itemsWithQuantities: List<Triple<ProductEntity, Double, Double>> // Product, UnitPriceAtTime, Quantity
    ) {
        val currentAssistanceDetails = assistanceDao.getAssistanceById(assistanceId) ?: return

        assistanceDao.clearAssistanceItems(assistanceId)

        var total = 0.0
        val entities = mutableListOf<AssistanceItemEntity>()

        for ((product, unitPrice, qty) in itemsWithQuantities) {
            if (qty > 0) {
                val itemTotal = unitPrice * qty
                total += itemTotal
                entities.add(
                    AssistanceItemEntity(
                        assistanceId = assistanceId,
                        productId = product.id,
                        productName = product.name,
                        productUnit = product.unit,
                        unitPriceAtTime = unitPrice,
                        quantity = qty,
                        totalPrice = itemTotal
                    )
                )
            }
        }

        if (entities.isNotEmpty()) {
            assistanceDao.insertAssistanceItems(entities)
        }

        val updatedAssistance = currentAssistanceDetails.assistance.copy(
            totalAmount = total
        )
        assistanceDao.updateMonthlyAssistance(updatedAssistance)
    }

    suspend fun markAssistanceDelivered(assistanceId: Long) {
        val details = assistanceDao.getAssistanceById(assistanceId) ?: return
        val updated = details.assistance.copy(
            status = "DELIVERED",
            deliveryTimestamp = System.currentTimeMillis()
        )
        assistanceDao.updateMonthlyAssistance(updated)
    }

    suspend fun markAssistancePending(assistanceId: Long) {
        val details = assistanceDao.getAssistanceById(assistanceId) ?: return
        val updated = details.assistance.copy(
            status = "PENDING",
            deliveryTimestamp = null
        )
        assistanceDao.updateMonthlyAssistance(updated)
    }

    suspend fun ensureMonthlyAssistancesForActivePersons(year: Int, month: Int) {
        val persons = personDao.getAllPersonsList()
        for (person in persons) {
            val existing = assistanceDao.getAssistanceForPersonAndMonth(person.id, year, month)
            if (existing == null) {
                generateMonthlyAssistanceForPerson(person.id, year, month)
            }
        }
    }
}
