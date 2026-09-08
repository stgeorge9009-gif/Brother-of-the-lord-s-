package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ChurchRepository
import com.example.util.CalendarUtil
import com.example.util.NotificationUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = ChurchRepository(
        personDao = db.personDao(),
        productDao = db.productDao(),
        assistanceDao = db.assistanceDao()
    )

    // Current device real year & month
    val currentRealYear = CalendarUtil.getCurrentYear()
    val currentRealMonth = CalendarUtil.getCurrentMonth()

    // Selected Year & Month State for browsing
    private val _selectedYear = MutableStateFlow(currentRealYear)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(currentRealMonth)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Search query for persons
    private val _personSearchQuery = MutableStateFlow("")
    val personSearchQuery: StateFlow<String> = _personSearchQuery.asStateFlow()

    // Selected Date ISO for Calendar view (defaults to today ISO)
    private val _selectedCalendarDateIso = MutableStateFlow(CalendarUtil.getTodayIsoString())
    val selectedCalendarDateIso: StateFlow<String> = _selectedCalendarDateIso.asStateFlow()

    // Search/Filtered Persons
    @OptIn(ExperimentalCoroutinesApi::class)
    val persons: StateFlow<List<PersonEntity>> = _personSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allPersons
            else repository.searchPersons(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Products
    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Assistances for Selected Month
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMonthAssistances: StateFlow<List<MonthlyAssistanceWithDetails>> =
        combine(_selectedYear, _selectedMonth) { year, month ->
            year to month
        }.flatMapLatest { (year, month) ->
            repository.getAssistancesForMonth(year, month)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Assistances for Selected Calendar Date
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCalendarDateAssistances: StateFlow<List<MonthlyAssistanceWithDetails>> =
        _selectedCalendarDateIso.flatMapLatest { isoDate ->
            repository.getAssistancesForDate(isoDate)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard & Reports Statistics
    val totalPersonCount: StateFlow<Int> = repository.personCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyTotalCount: StateFlow<Int> = combine(_selectedYear, _selectedMonth) { y, m -> y to m }
        .flatMapLatest { (y, m) -> repository.getAssistanceCountForMonth(y, m) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyDeliveredCount: StateFlow<Int> = combine(_selectedYear, _selectedMonth) { y, m -> y to m }
        .flatMapLatest { (y, m) -> repository.getDeliveredCountForMonth(y, m) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyTotalAmount: StateFlow<Double> = combine(_selectedYear, _selectedMonth) { y, m -> y to m }
        .flatMapLatest { (y, m) -> repository.getTotalAmountForMonth(y, m) }
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // All Assistances for aggregate reports
    val allAssistances: StateFlow<List<MonthlyAssistanceWithDetails>> = repository.getAllAssistances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedDefaultProductsIfEmpty()
            repository.ensureMonthlyAssistancesForActivePersons(currentRealYear, currentRealMonth)
            NotificationUtil.createNotificationChannel(getApplication())
        }
    }

    fun setPersonSearchQuery(query: String) {
        _personSearchQuery.value = query
    }

    fun setSelectedMonthYear(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        viewModelScope.launch {
            repository.ensureMonthlyAssistancesForActivePersons(year, month)
        }
    }

    fun previousMonth() {
        var y = _selectedYear.value
        var m = _selectedMonth.value - 1
        if (m < 1) {
            m = 12
            y--
        }
        setSelectedMonthYear(y, m)
    }

    fun nextMonth() {
        var y = _selectedYear.value
        var m = _selectedMonth.value + 1
        if (m > 12) {
            m = 1
            y++
        }
        setSelectedMonthYear(y, m)
    }

    fun selectCalendarDate(isoDate: String) {
        _selectedCalendarDateIso.value = isoDate
    }

    // Beneficiary Actions
    fun savePerson(
        id: Long,
        name: String,
        phone: String,
        address: String,
        familyMembers: Int,
        notes: String,
        scheduledDayOfMonth: Int,
        imageUri: String?,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val person = PersonEntity(
                id = if (id <= 0L) 0L else id,
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                familyMembers = familyMembers.coerceAtLeast(1),
                notes = notes.trim(),
                scheduledDayOfMonth = scheduledDayOfMonth.coerceIn(1, 31),
                imageUri = imageUri
            )
            val personId = repository.insertOrUpdatePerson(person)
            // Ensure assistance record for current selected month
            repository.generateMonthlyAssistanceForPerson(personId, _selectedYear.value, _selectedMonth.value)
            onComplete(personId)
        }
    }

    fun deletePerson(person: PersonEntity) {
        viewModelScope.launch {
            repository.deletePerson(person)
        }
    }

    fun savePersonStandardPackage(personId: Long, items: List<Pair<Long, Double>>) {
        viewModelScope.launch {
            repository.setStandardPackageForPerson(personId, items)
            // Update current month's assistance if pending
            repository.generateMonthlyAssistanceForPerson(personId, _selectedYear.value, _selectedMonth.value)
        }
    }

    // Product Actions
    fun saveProduct(
        id: Long,
        name: String,
        unit: String,
        currentPrice: Double,
        iconEmoji: String,
        imageUri: String?,
        category: String = "مواد غذائية",
        isActive: Boolean = true,
        notes: String = "",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = if (id <= 0L) 0L else id,
                name = name.trim(),
                unit = unit.trim(),
                currentPrice = currentPrice.coerceAtLeast(0.0),
                iconEmoji = if (iconEmoji.isBlank()) "📦" else iconEmoji,
                imageUri = imageUri,
                category = category.ifBlank { "مواد غذائية" },
                isActive = isActive,
                notes = notes.trim()
            )
            repository.insertOrUpdateProduct(product)
            onComplete()
        }
    }

    fun toggleProductActive(product: ProductEntity) {
        viewModelScope.launch {
            val updated = product.copy(isActive = !product.isActive)
            repository.insertOrUpdateProduct(updated)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // Assistance Actions
    fun saveCustomAssistanceItems(
        assistanceId: Long,
        itemsWithQuantities: List<Triple<ProductEntity, Double, Double>>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveAssistanceCustomItems(assistanceId, itemsWithQuantities)
            onComplete()
        }
    }

    fun toggleAssistanceDelivered(assistanceId: Long, currentlyDelivered: Boolean) {
        viewModelScope.launch {
            if (currentlyDelivered) {
                repository.markAssistancePending(assistanceId)
            } else {
                repository.markAssistanceDelivered(assistanceId)
            }
        }
    }

    fun addAssistanceToCalendar(context: Context, details: MonthlyAssistanceWithDetails): Boolean {
        return CalendarUtil.addAssistanceToDeviceCalendar(
            context = context,
            personName = details.person?.name ?: "مستحق المساعدة",
            scheduledIsoDate = details.assistance.scheduledDate,
            totalAmount = details.assistance.totalAmount
        )
    }

    fun sendMonthlyReminderNotification(context: Context) {
        val pendingCount = selectedMonthAssistances.value.count { it.assistance.status != "DELIVERED" }
        val monthName = CalendarUtil.getArabicMonthName(_selectedMonth.value)
        val title = "🔔 تذكير بمساعدات $monthName ${_selectedYear.value}"
        val message = if (pendingCount > 0) {
            "يوجد $pendingCount مساعدة بانتظار التسليم لهذا الشهر. انقر للمتابعة والتجهيز."
        } else {
            "جميع مساعدات شهر $monthName تم تسليمها بنجاح. شكراً لخدمتكم!"
        }
        NotificationUtil.showAssistanceReminderNotification(context, 1001, title, message)
    }

    // Backup JSON Data
    suspend fun exportDataAsJson(): String {
        val root = JSONObject()

        val personsList = repository.allPersons.first()
        val personsArray = JSONArray()
        for (p in personsList) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("phone", p.phone)
            obj.put("address", p.address)
            obj.put("familyMembers", p.familyMembers)
            obj.put("notes", p.notes)
            obj.put("scheduledDayOfMonth", p.scheduledDayOfMonth)
            personsArray.put(obj)
        }
        root.put("persons", personsArray)

        val productsList = repository.allProducts.first()
        val productsArray = JSONArray()
        for (pr in productsList) {
            val obj = JSONObject()
            obj.put("id", pr.id)
            obj.put("name", pr.name)
            obj.put("unit", pr.unit)
            obj.put("currentPrice", pr.currentPrice)
            obj.put("iconEmoji", pr.iconEmoji)
            productsArray.put(obj)
        }
        root.put("products", productsArray)

        return root.toString(2)
    }
}
