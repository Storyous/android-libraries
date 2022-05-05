package com.storyous.bills.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.storyous.bills.BuildConfig

@Database(
    entities = [
        CachedBill::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class BillsDatabase : RoomDatabase() {

    abstract fun billsDao(): BillsDao

    companion object {
        @Volatile
        private var instance: BillsDatabase? = null
        private val LOCK = Any()

        operator fun invoke(
            context: Context,
            fieldConversionErrorHandler: (Throwable, String?) -> Unit
        ) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context, fieldConversionErrorHandler).also { instance = it }
        }

        private fun buildDatabase(
            context: Context,
            fieldConversionErrorHandler: (Throwable, String?) -> Unit
        ) = Room.databaseBuilder(
            context,
            BillsDatabase::class.java,
            BuildConfig.DATABASE
        )
            .addTypeConverter(Converters(fieldConversionErrorHandler))
            .fallbackToDestructiveMigration()
            .build()
    }
}
