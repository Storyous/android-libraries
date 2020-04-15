package com.storyous.commonutils.adapters


object ItemType {
    const val HEADER = 1
    const val ITEM = 2

    const val HEADER_DISABLED = 0
    const val HEADER_ENABLED = 1
}

interface ListItem<T> {
    fun getType(): Int
}

open class Header<T>(val title: String, val headerType: Int) : ListItem<T> {
    override fun getType(): Int {
        return ItemType.HEADER
    }
}

class Item<T>(val itemValue: T) : ListItem<T> {
    override fun getType(): Int {
        return ItemType.ITEM
    }
}
