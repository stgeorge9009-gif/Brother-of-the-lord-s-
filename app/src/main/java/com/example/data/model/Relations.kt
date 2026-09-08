package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class PersonWithPackage(
    @Embedded val person: PersonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "personId"
    )
    val packageItems: List<PersonStandardPackageItemEntity>
)

data class MonthlyAssistanceWithDetails(
    @Embedded val assistance: MonthlyAssistanceEntity,
    @Relation(
        parentColumn = "personId",
        entityColumn = "id"
    )
    val person: PersonEntity? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "assistanceId"
    )
    val items: List<AssistanceItemEntity>
)

data class StandardPackageItemWithProduct(
    @Embedded val packageItem: PersonStandardPackageItemEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity? = null
)
