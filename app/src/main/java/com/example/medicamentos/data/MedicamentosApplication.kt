package com.example.medicamentos.data

import android.app.Application

/**
 * Classe Application customizada para inicializar o banco de dados.
 */
class MedicamentosApplication : Application() {
    // Usando 'lazy' para que o banco de dados só seja criado quando for acessado pela primeira vez.
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}