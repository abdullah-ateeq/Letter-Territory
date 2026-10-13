package com.example.letterterritory.domain

/**
 * Fast prefix tree for dictionary lookup and prefix pruning during board searches.
 * Optimized for uppercase English letters 'A'..'Z'.
 */
class Trie {
    private class Node {
        val children = arrayOfNulls<Node>(26)
        var isWord = false
    }

    private val root = Node()
    var size = 0
        private set

    fun insert(word: String) {
        if (word.isEmpty()) return
        var current = root
        for (i in word.indices) {
            val char = word[i].uppercaseChar()
            if (char !in 'A'..'Z') return
            val index = char - 'A'
            var child = current.children[index]
            if (child == null) {
                child = Node()
                current.children[index] = child
            }
            current = child
        }
        if (!current.isWord) {
            current.isWord = true
            size++
        }
    }

    fun contains(word: String): Boolean {
        if (word.isEmpty()) return false
        var current = root
        for (i in word.indices) {
            val char = word[i].uppercaseChar()
            if (char !in 'A'..'Z') return false
            val index = char - 'A'
            current = current.children[index] ?: return false
        }
        return current.isWord
    }

    fun isPrefix(prefix: String): Boolean {
        if (prefix.isEmpty()) return true
        var current = root
        for (i in prefix.indices) {
            val char = prefix[i].uppercaseChar()
            if (char !in 'A'..'Z') return false
            val index = char - 'A'
            current = current.children[index] ?: return false
        }
        return true
    }
}
