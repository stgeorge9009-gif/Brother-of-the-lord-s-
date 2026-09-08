package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val familyMembers: Int = 1,
    val notes: String = "",
    val scheduledDayOfMonth: Int = 10, // Default scheduled assistance day (e.g., 10th of each month)
    val imageUri: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String, // e.g. "كجم", "جرام", "لتر", "مل", "قطعة", "عبوة", "كيس", "علبة"
    val currentPrice: Double,
    val iconEmoji: String = "📦",
    val imageUri: String? = null,
    val category: String = "مواد غذائية",
    val isActive: Boolean = true,
    val notes: String = ""
)

// Standard recurring monthly package item template for a person
@Entity(
    tableName = "person_standard_packages",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId"), Index("productId")]
)
data class PersonStandardPackageItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val productId: Long,
    val quantity: Double
)

@Entity(
    tableName = "monthly_assistances",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId"), Index(value = ["personId", "year", "month"], unique = true)]
)
data class MonthlyAssistanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val year: Int,
    val month: Int, // 1..12
    val status: String = "PENDING", // "PENDING", "DELIVERED"
    val scheduledDate: String, // "YYYY-MM-DD"
    val deliveryTimestamp: Long? = null,
    val totalAmount: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "assistance_items",
    foreignKeys = [
        ForeignKey(
            entity = MonthlyAssistanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["assistanceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("assistanceId")]
)
data class AssistanceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assistanceId: Long,
    val productId: Long? = null,
    val productName: String,
    val productUnit: String,
    val unitPriceAtTime: Double, // Snapshot unit price at the time assistance was generated/registered
    val quantity: Double,
    val totalPrice: Double = unitPriceAtTime * quantity
)
