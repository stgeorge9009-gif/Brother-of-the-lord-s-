package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(ChurchGoldContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✝️",
                    fontSize = 54.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "إخوة الرب",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                color = Color(0xFF0F2240)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "تطبيق إدارة المساعدات والخدمة الكنسية",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF78350F)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = ChurchGoldContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text(
                    text = "«كُلَّ مَا فَعَلْتُمُوهُ بِأَحَدِ إِخْوَتِي هؤُلاَءِ الصِّغَارِ، فَبِي فَعَلْتُمْ»\n(متى 25: 40)",
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F2240),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onNavigateNext,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp)
                    .testTag("start_service_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ChurchGoldContainer),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(
                    text = "الدخول للخدمة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F2240)
                )
            }
        }
    }
}
