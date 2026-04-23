package com.example.jewelryapp.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class SaleWithMaterials(
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