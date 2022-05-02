package com.storyous.bills.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.google.gson.GsonBuilder
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

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            BillsDatabase::class.java,
            BuildConfig.DATABASE
        )
            .fallbackToDestructiveMigration()
            .build()

        var gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
    }
}
