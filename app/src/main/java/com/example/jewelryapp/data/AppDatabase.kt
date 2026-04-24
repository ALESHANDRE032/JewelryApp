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

@Database(
    entities = [
        MaterialEntity::class,
        SaleEntity::class,
        SaleMaterialCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao
    abstract fun saleDao(): SaleDao

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
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
