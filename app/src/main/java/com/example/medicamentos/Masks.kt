package com.example.medicamentos

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.math.min

// MÁSCARA PARA DATA DE NASCIMENTO (DD/MM/AAAA) - VERSÃO ROBUSTA E FINAL
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text.filter { it.isDigit() }

        val formattedText = buildString {
            for (i in digitsOnly.indices) {
                append(digitsOnly[i])
                if (i == 1 && i < digitsOnly.length - 1) append('/')
                if (i == 3 && i < digitsOnly.length - 1) append('/')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val transformed = when {
                    offset <= 1 -> offset
                    offset <= 3 -> offset + 1
                    else -> offset + 2
                }
                return transformed.coerceIn(0, formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val original = when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset - 1
                    else -> offset - 2
                }
                return original.coerceIn(0, digitsOnly.length)
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

// MÁSCARA PARA TELEFONE ((XX) XXXXX-XXXX) - VERSÃO ROBUSTA E FINAL
class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text.filter { it.isDigit() }

        val formattedText = buildString {
            if (digitsOnly.isNotEmpty()) {
                append("(")
                append(digitsOnly.substring(0, min(2, digitsOnly.length)))
            }
            if (digitsOnly.length > 2) {
                append(") ")
                append(digitsOnly.substring(2, min(7, digitsOnly.length)))
            }
            if (digitsOnly.length > 7) {
                append("-")
                append(digitsOnly.substring(7, min(11, digitsOnly.length)))
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val transformed = when {
                    offset <= 1 -> offset + 1
                    offset <= 2 -> offset + 2
                    offset <= 7 -> offset + 4
                    else -> offset + 5
                }
                return transformed.coerceIn(0, formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val original = when {
                    offset <= 2 -> offset - 1
                    offset <= 4 -> offset - 2
                    offset <= 9 -> offset - 4
                    else -> offset - 5
                }
                return original.coerceIn(0, digitsOnly.length)
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}


// FUNÇÕES DE FORMATAÇÃO PARA A TELA DE PERFIL
fun formatDob(dob: String): String {
    val digitsOnly = dob.filter { it.isDigit() }
    if (digitsOnly.length != 8) return dob // Retorna original se não tiver 8 dígitos
    return "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2, 4)}/${digitsOnly.substring(4, 8)}"
}

fun formatPhone(phone: String): String {
    val digitsOnly = phone.filter { it.isDigit() }
    if (digitsOnly.length != 11) return phone // Retorna original se não tiver 11 dígitos
    val ddd = digitsOnly.substring(0, 2)
    val firstPart = digitsOnly.substring(2, 7)
    val secondPart = digitsOnly.substring(7, 11)
    return "($ddd) $firstPart-$secondPart"
}