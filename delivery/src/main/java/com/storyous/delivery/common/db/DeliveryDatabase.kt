package com.storyous.delivery.common.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.storyous.delivery.common.BuildConfig

@Database(
    entities = [
        DeliveryOrder::class, DeliveryItem::class, DeliveryAddition::class, Customer::class
    ],
    version = 3
)
@TypeConverters(Converters::class)
abstract class DeliveryDatabase : RoomDatabase() {

    abstract fun deliveryDao(): DeliveryDao

    companion object {
        @Volatile
        private var instance: DeliveryDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            DeliveryDatabase::class.java,
            BuildConfig.DATABASE
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
