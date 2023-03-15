package com.example.tickerboard

object AlphabetMapper {
    private val alphabet = " ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789•".toList()
    val size: Int = alphabet.size

    fun getLetterAt(index: Int): Char = alphabet[index % size]

    fun getIndexOf(letter: Char): Int {
        val index = alphabet.indexOf(letter.uppercaseChar())
        return if(index < 0) alphabet.lastIndex else index
    }
}