package com.example.medicamentos.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// CORREÇÃO 1: Versão do banco atualizada (ex: para 4)
// CORREÇÃO 2: Adicionada a anotação @TypeConverters
@Database(entities = [Treatment::class, MedicationDose::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun treatmentDao(): TreatmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicamentos_database"
                )
                    // CORREÇÃO 3: Adicionada a migração destrutiva
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}