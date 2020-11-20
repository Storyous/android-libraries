package com.storyous.delivery.common.views

interface Expandable {
    var expanded: Boolean
    fun expand()
    fun collapse()
    fun toggle(): Boolean {
        expanded = !expanded
        if (expanded) {
            expand()
        } else {
            collapse()
        }
        return expanded
    }
}
