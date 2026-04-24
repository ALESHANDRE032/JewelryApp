package com.example.jewelryapp.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MaterialWithUsage(
    val material: MaterialEntity,
    val usedQuantity: Double
)

// Internal Room type — only used inside SaleDao
data class SaleWithMaterialsRoom(
    @Embedded
    val sale: SaleEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SaleMaterialCrossRef::class,
            parentColumn = "saleId",
            entityColumn = "materialId"
        )
    )
    val materials: List<MaterialEntity>
)

// Public type used everywhere in UI and ViewModel
data class SaleWithMaterials(
    val sale: SaleEntity,
    val materials: List<MaterialWithUsage>
)
