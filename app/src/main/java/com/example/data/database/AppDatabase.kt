package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AssistanceDao
import com.example.data.dao.PersonDao
import com.example.data.dao.ProductDao
import com.example.data.model.*

@Database(
    entities = [
        PersonEntity::class,
        ProductEntity::class,
        PersonStandardPackageItemEntity::class,
        MonthlyAssistanceEntity::class,
        AssistanceItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun productDao(): ProductDao
    abstract fun assistanceDao(): AssistanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN quantity REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ekhwat_al_rab_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
