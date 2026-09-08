package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun productDao(): ProductDao
    abstract fun assistanceDao(): AssistanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ekhwat_al_rab_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
