package com.storyous.commonutils.recyclerView

interface SelectableItemsAdapter {

    var selectedItems: Map<String, Double>

    var onSelectedItemsChangedListener: ((Map<String, Double>) -> Unit)

    var isEnabled: Boolean
}
