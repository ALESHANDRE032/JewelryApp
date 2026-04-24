package com.example.jewelryapp.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

// Internal Room type — only used inside ProductDao
data class ProductWithMaterialsRoom(
    @Embedded
    val product: ProductEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProductMaterialCrossRef::class,
            parentColumn = "productId",
            entityColumn = "materialId"
        )
    )
    val materials: List<MaterialEntity>
)

// Public type used in UI and ViewModel
data class ProductWithMaterials(
    val product: ProductEntity,
    val materials: List<MaterialWithUsage>
)
