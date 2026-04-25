package com.example.jewelryapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE materials ADD COLUMN quantity REAL NOT NULL DEFAULT 1.0")
        database.execSQL("ALTER TABLE materials ADD COLUMN unit TEXT NOT NULL DEFAULT 'шт'")
        database.execSQL("ALTER TABLE materials ADD COLUMN unitCost REAL NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE sale_material_cross_ref ADD COLUMN usedQuantity REAL NOT NULL DEFAULT 1.0")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE sales ADD COLUMN comment TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE sales ADD COLUMN saleDate INTEGER NOT NULL DEFAULT 0")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE sales ADD COLUMN productId INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE sales ADD COLUMN extraExpensesCost INTEGER NOT NULL DEFAULT 0")

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                expectedSalePrice INTEGER NOT NULL,
                expectedCost INTEGER NOT NULL,
                quantity INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS product_material_cross_ref (
                productId INTEGER NOT NULL,
                materialId INTEGER NOT NULL,
                usedQuantity REAL NOT NULL,
                PRIMARY KEY (productId, materialId),
                FOREIGN KEY (productId) REFERENCES products(id) ON DELETE CASCADE,
                FOREIGN KEY (materialId) REFERENCES materials(id) ON DELETE CASCADE
            )
        """.trimIndent())

        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_product_material_cross_ref_productId " +
            "ON product_material_cross_ref(productId)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_product_material_cross_ref_materialId " +
            "ON product_material_cross_ref(materialId)"
        )

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS sale_expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                saleId INTEGER NOT NULL,
                expenseType TEXT NOT NULL,
                amount INTEGER NOT NULL,
                FOREIGN KEY (saleId) REFERENCES sales(id) ON DELETE CASCADE
            )
        """.trimIndent())

        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_sale_expenses_saleId ON sale_expenses(saleId)"
        )
    }
}

@Database(
    entities = [
        MaterialEntity::class,
        SaleEntity::class,
        SaleMaterialCrossRef::class,
        ProductEntity::class,
        ProductMaterialCrossRef::class,
        SaleExpenseEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao
    abstract fun saleDao(): SaleDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jewelry_app_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
