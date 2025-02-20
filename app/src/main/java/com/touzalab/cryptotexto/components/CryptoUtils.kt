package com.touzalab.cryptotexto.components

import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.experimental.xor
import kotlin.math.pow
import java.math.BigInteger
import kotlin.random.Random

private val frenchCharMap = mapOf(
    'é' to 'e', 'è' to 'e', 'ê' to 'e', 'ë' to 'e',
    'à' to 'a', 'â' to 'a', 'ä' to 'a',
    'î' to 'i', 'ï' to 'i',
    'ô' to 'o', 'ö' to 'o',
    'ù' to 'u', 'û' to 'u', 'ü' to 'u',
    'ÿ' to 'y',
    'ç' to 'c',
    'É' to 'E', 'È' to 'E', 'Ê' to 'E', 'Ë' to 'E',
    'À' to 'A', 'Â' to 'A', 'Ä' to 'A',
    'Î' to 'I', 'Ï' to 'I',
    'Ô' to 'O', 'Ö' to 'O',
    'Ù' to 'U', 'Û' to 'U', 'Ü' to 'U',
    'Ÿ' to 'Y',
    'Ç' to 'C'
)

private fun normalizeChar(char: Char): Char {
    return frenchCharMap[char] ?: char
}

fun cesarEncrypt(message: String, key: Int): String {
    val normalizedKey = ((key % 26) + 26) % 26

    return message.map { char ->
        val normalizedChar = normalizeChar(char)
        when {
            normalizedChar.isLetter() -> {
                val base = if (normalizedChar.isUpperCase()) 'A' else 'a'
                val shifted = (normalizedChar.code - base.code + normalizedKey) % 26
                (base.code + shifted).toChar()
            }
            else -> char
        }
    }.joinToString("")
}

fun cesarDecrypt(message: String, key: Int): String {
    val normalizedKey = ((key % 26) + 26) % 26

    return message.map { char ->
        val normalizedChar = normalizeChar(char)
        when {
            normalizedChar.isLetter() -> {
                val base = if (normalizedChar.isUpperCase()) 'A' else 'a'
                val shifted = (normalizedChar.code - base.code - normalizedKey + 26) % 26
                (base.code + shifted).toChar()
            }
            else -> char
        }
    }.joinToString("")
}

fun vigenereEncrypt(message: String, key: String): String {
    if (key.isEmpty()) return message

    val normalizedKey = key.map { normalizeChar(it) }
    var keyIndex = 0

    return message.map { char ->
        val normalizedChar = normalizeChar(char)
        if (normalizedChar.isLetter()) {
            val base = if (normalizedChar.isUpperCase()) 'A' else 'a'
            val keyChar = normalizedKey[keyIndex % normalizedKey.size]
            val shift = normalizeChar(keyChar).lowercaseChar() - 'a'
            keyIndex++
            val shifted = (normalizedChar.code - base.code + shift) % 26
            (base.code + shifted).toChar()
        } else {
            char
        }
    }.joinToString("")
}

fun vigenereDecrypt(message: String, key: String): String {
    if (key.isEmpty()) return message

    val normalizedKey = key.map { normalizeChar(it) }
    var keyIndex = 0

    return message.map { char ->
        val normalizedChar = normalizeChar(char)
        if (normalizedChar.isLetter()) {
            val base = if (normalizedChar.isUpperCase()) 'A' else 'a'
            val keyChar = normalizedKey[keyIndex % normalizedKey.size]
            val shift = normalizeChar(keyChar).lowercaseChar() - 'a'
            keyIndex++
            val shifted = (normalizedChar.code - base.code - shift + 26) % 26
            (base.code + shifted).toChar()
        } else {
            char
        }
    }.joinToString("")
}

fun xorEncrypt(message: String, key: String): String {
    if (key.isEmpty()) return message

    // Convertir le message et la clé en séquences de bytes normalisées
    val messageBytes = message.map { normalizeChar(it).code.toByte() }
    val keyBytes = key.map { normalizeChar(it).code.toByte() }

    return messageBytes.mapIndexed { index, messageByte ->
        val keyByte = keyBytes[index % keyBytes.size]
        (messageByte xor keyByte).toInt().toChar()
    }.joinToString("")
}

fun xorDecrypt(message: String, key: String): String {
    return xorEncrypt(message, key)  // XOR est son propre inverse
}

// Algorithme Affine (ax + b) mod m
fun affineEncrypt(message: String, a: Int, b: Int): String {
    // Vérifie si 'a' est premier avec 26
    if (!areCoprimes(a, 26)) return message

    return message.map { char ->
        val normalizedChar = normalizeChar(char)
        when {
            normalizedChar.isLetter() -> {
                val base = if (normalizedChar.isUpperCase()) 'A' else 'a'
                val x = normalizedChar.code - base.code
                val encryptedValue = ((a * x + b) % 26 + 26) % 26
                (base.code + encryptedValue).toChar()
            }
            else -> char
        }
    }.joinToString("")
}

fun affineDecrypt(message: String, a: Int, b: Int): String {
    // Trouver l'inverse multiplicatif de 'a' modulo 26
    val aInverse = multiplicativeInverse(a, 26) ?: return message

    return message.map { char ->
        val normalizedChar = normalizeChar(char)
        when {
            normalizedChar.isLetter() -> {
                val base = if (normalizedChar.isUpperCase()) 'A' else 'a'
                val y = normalizedChar.code - base.code
                val decryptedValue = ((aInverse * (y - b)) % 26 + 26) % 26
                (base.code + decryptedValue).toChar()
            }
            else -> char
        }
    }.joinToString("")
}

// Chiffrement par transposition (permutation)
fun transpositionEncrypt(message: String, key: String): String {
    if (message.isEmpty() || key.isEmpty()) return message

    // Créer une table de permutation basée sur la clé
    val keyIndices = key.mapIndexed { index, c -> c to index }
        .sortedBy { it.first }
        .mapIndexed { index, pair -> pair.second to index }
        .toMap()

    val columnCount = key.length
    val rowCount = (message.length + columnCount - 1) / columnCount
    val matrix = Array(rowCount) { CharArray(columnCount) { ' ' } }

    // Remplir la matrice
    message.forEachIndexed { index, char ->
        val row = index / columnCount
        val col = index % columnCount
        matrix[row][col] = char
    }

    // Lire la matrice selon la permutation
    return buildString {
        for (col in keyIndices.keys.sorted()) {
            val permutedCol = keyIndices[col] ?: continue
            for (row in matrix.indices) {
                if (row * columnCount + permutedCol < message.length) {
                    append(matrix[row][permutedCol])
                }
            }
        }
    }
}

fun transpositionDecrypt(encrypted: String, key: String): String {
    if (encrypted.isEmpty() || key.isEmpty()) return encrypted

    // Créer la table de permutation inverse
    val keyIndices = key.mapIndexed { index, c -> c to index }
        .sortedBy { it.first }
        .mapIndexed { index, pair -> index to pair.second }
        .toMap()

    val columnCount = key.length
    val rowCount = (encrypted.length + columnCount - 1) / columnCount
    val matrix = Array(rowCount) { CharArray(columnCount) { ' ' } }

    // Remplir la matrice selon la permutation
    var charIndex = 0
    for (col in keyIndices.keys.sorted()) {
        val originalCol = keyIndices[col] ?: continue
        for (row in matrix.indices) {
            if (charIndex < encrypted.length) {
                matrix[row][originalCol] = encrypted[charIndex++]
            }
        }
    }

    // Lire la matrice normalement
    return buildString {
        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                if (row * columnCount + col < encrypted.length) {
                    append(matrix[row][col])
                }
            }
        }
    }.trim()
}

// Fonctions utilitaires
fun areCoprimes(a: Int, b: Int): Boolean {
    return findGCD(a, b) == 1
}

private fun findGCD(a: Int, b: Int): Int {
    if (b == 0) return a
    return findGCD(b, a % b)
}

private fun multiplicativeInverse(a: Int, m: Int): Int? {
    var t = 0
    var newT = 1
    var r = m
    var newR = a

    while (newR != 0) {
        val quotient = r / newR
        val tempT = newT
        newT = t - quotient * newT
        t = tempT
        val tempR = newR
        newR = r - quotient * newR
        r = tempR
    }

    if (r > 1) return null
    if (t < 0) t += m
    return t
}
