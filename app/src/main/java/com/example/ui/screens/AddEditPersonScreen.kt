package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.ChurchGold
import com.example.ui.theme.ChurchNavy
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPersonScreen(
    viewModel: MainViewModel,
    personId: Long,
    onNavigateBack: () -> Unit,
    onPersonSaved: (Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var familyMembersText by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var scheduledDayText by remember { mutableStateOf("10") }

    var nameError by remember { mutableStateOf(false) }

    LaunchedEffect(personId) {
        if (personId > 0L) {
            val person = viewModel.repository.getPersonById(personId)
            if (person != null) {
                name = person.name
                phone = person.phone
                address = person.address
                familyMembersText = person.familyMembers.toString()
                notes = person.notes
                scheduledDayText = person.scheduledDayOfMonth.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (personId > 0L) "تعديل بيانات الشخص" else "إضافة شخص جديد",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ChurchNavy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_person_back_button")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("add_edit_person_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("person_name_input"),
                label = { Text("الاسم بالكامل *") },
                placeholder = { Text("مثال: مينا إبراهيم") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = nameError,
                supportingText = {
                    if (nameError) Text("يرجى كتابة الاسم", color = MaterialTheme.colorScheme.error)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Family Members Count Field
            OutlinedTextField(
                value = familyMembersText,
                onValueChange = { familyMembersText = it.filter { c -> c.isDigit() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("person_family_members_input"),
                label = { Text("عدد أفراد الأسرة *") },
                placeholder = { Text("4") },
                leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Scheduled Assistance Day of Month Field
            OutlinedTextField(
                value = scheduledDayText,
                onValueChange = { scheduledDayText = it.filter { c -> c.isDigit() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("person_scheduled_day_input"),
                label = { Text("موعد المساعدة الشهري (يوم من 1 إلى 31) *") },
                placeholder = { Text("10 (يعني يوم 10 من كل شهر)") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                supportingText = { Text("سيقوم التطبيق بإرسال تذكير في هذا اليوم من كل شهر") }
            )

            // Phone Field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("person_phone_input"),
                label = { Text("رقم الهاتف (اختياري)") },
                placeholder = { Text("01234567890") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Address Field
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("person_address_input"),
                label = { Text("العنوان (اختياري)") },
                placeholder = { Text("الشارع، المنطقة...") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Notes Field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("person_notes_input"),
                label = { Text("ملاحظات (اختياري)") },
                placeholder = { Text("أي تفاصيل إضافية...") },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        val familyMembers = familyMembersText.toIntOrNull() ?: 1
                        val scheduledDay = scheduledDayText.toIntOrNull() ?: 10
                        viewModel.savePerson(
                            id = personId,
                            name = name,
                            phone = phone,
                            address = address,
                            familyMembers = familyMembers,
                            notes = notes,
                            scheduledDayOfMonth = scheduledDay,
                            imageUri = null,
                            onComplete = { newId ->
                                onPersonSaved(newId)
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_person_button"),
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
                    text = if (personId > 0L) "حفظ التعديلات" else "حفظ البيانات",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
