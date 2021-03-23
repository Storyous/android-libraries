package com.storyous.delivery.common

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.storyous.delivery.common.api.DeliveryOrder
import kotlinx.android.synthetic.main.fragment_delivery.*

class DeliveryFragment : Fragment(R.layout.fragment_delivery) {

    companion object {
        private const val ARG_ORDER_ID = "orderId"

        fun newInstance(orderId: String?): DeliveryFragment = DeliveryFragment().apply {
            arguments = Bundle().apply { putString(ARG_ORDER_ID, orderId) }
        }
    }

    private val viewModel by viewModels<DeliveryViewModel>({ requireActivity() })

    private val overlappingDetailFragment
        get() = childFragmentManager.findFragmentByTag("overlappingDetail")

    private val settingsFragment
        get() = childFragmentManager.findFragmentById(R.id.fragment_delivery_settings) as DeliverySettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.stopRinging()
        viewModel.selectedOrderLive.observe(this, this::onOrderSelected)

        DeliveryConfiguration.deliveryRepository?.getDeliveryError()?.observe(this) {
            it?.takeIf { !it.consumed }?.run {
                consume()
                DeliveryConfiguration.onUnauthorizedError()
            }
        }

        viewModel.selectedOrderId = arguments?.getString(ARG_ORDER_ID)
        viewModel.loadOrders()

        requireActivity().onBackPressedDispatcher.addCallback { onBackPressed() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val constraintSet = ConstraintSet().apply { clone(root) }
        fragment_delivery_settings.setOnClickListener {
            TransitionManager.beginDelayedTransition(root, AutoTransition())
            constraintSet.applyTo(root)
            settingsFragment.toggle()
        }
        fragment_delivery_settings.isVisible = DeliveryConfiguration.showSettingsBar
        DeliveryConfiguration.onCreateActionButton(fragment_delivery_button, childFragmentManager)
    }

    private fun onBackPressed() {
        if (overlappingDetailFragment?.isVisible == true) {
            viewModel.deselectOrder()
        }
    }

    private fun onOrderSelected(order: DeliveryOrder?) {
        overlappingDetailFragment?.view?.isVisible = order != null
    }
}
