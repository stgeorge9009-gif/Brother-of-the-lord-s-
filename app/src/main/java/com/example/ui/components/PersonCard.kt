package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MonthlyAssistanceWithDetails
import com.example.data.model.PersonEntity
import com.example.ui.theme.ChurchGreen
import com.example.ui.theme.ChurchGreenContainer
import com.example.ui.theme.ChurchNavy
import com.example.ui.theme.ChurchRed
import com.example.ui.theme.ChurchRedContainer

@Composable
fun PersonCard(
    person: PersonEntity,
    assistanceDetails: MonthlyAssistanceWithDetails?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("person_card_${person.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ChurchNavy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.take(1),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ChurchNavy
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F2240),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "عدد أفراد الأسرة: ${person.familyMembers}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF334155)
                    )
                }

                if (person.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = person.phone,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            // Status & Value Column
            Column(horizontalAlignment = Alignment.End) {
                val isDelivered = assistanceDetails?.assistance?.status == "DELIVERED"
                val amount = assistanceDetails?.assistance?.totalAmount ?: 0.0

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDelivered) ChurchGreenContainer else ChurchRedContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDelivered) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isDelivered) ChurchGreen else ChurchRed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isDelivered) "تم التسليم" else "لم يتم التسليم",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDelivered) ChurchGreen else ChurchRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${amount.toInt()} جنيه",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ChurchNavy
                )
            }
        }
    }
}
