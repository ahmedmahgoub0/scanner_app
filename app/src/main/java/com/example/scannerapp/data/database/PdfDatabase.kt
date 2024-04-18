package com.example.scannerapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.scannerapp.data.database.converters.DateTypeConverter
import com.example.scannerapp.data.database.dao.PdfDao
import com.example.scannerapp.data.models.PdfEntity

@Database(
    entities = [PdfEntity::class], version = 1,
    exportSchema = false
)
@TypeConverters(DateTypeConverter::class)
abstract class PdfDatabase : RoomDatabase() {
    abstract val pdfDao: PdfDao

    companion object {
        @Volatile
        private var INSTANCE: PdfDatabase? = null
        fun getInstance(context: Context): PdfDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PdfDatabase::class.java,
                    "pdf_db"
                ).build().also {
                    INSTANCE = it
                }
            }

        }
    }

}