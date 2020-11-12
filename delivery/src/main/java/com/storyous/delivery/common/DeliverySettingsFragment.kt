package com.storyous.delivery.common

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.storyous.delivery.common.views.Expandable

class DeliverySettingsFragment : Fragment(R.layout.fragment_delivery_settings), Expandable {

    override var expanded: Boolean = false
    private lateinit var collapsedSet: ConstraintSet
    private lateinit var expandedSet: ConstraintSet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collapsedSet = ConstraintSet().apply {
            clone(view as ConstraintLayout)
        }
        expandedSet = ConstraintSet().apply {
            clone(collapsedSet)
            setVisibility(R.id.header, View.GONE)
            setVisibility(R.id.form, View.VISIBLE)
        }

        collapse()
    }

    override fun expand() {
        with(view as ConstraintLayout) {
            expandedSet.applyTo(this)
        }
    }

    override fun collapse() {
        with(view as ConstraintLayout) {
            collapsedSet.applyTo(this)
        }
    }
}
