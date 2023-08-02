package com.storyous.delivery.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.storyous.commonutils.viewBinding
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.databinding.ActivityDeliveryBinding

class DeliveryActivity : AppCompatActivity(R.layout.activity_delivery) {

    private val viewModel by viewModels<DeliveryViewModel>()
    private val binding by viewBinding<ActivityDeliveryBinding>()

    companion object {
        private const val ARG_ORDER_ID = "orderId"

        @Throws(ConfigurationInvalidException::class)
        fun launch(context: Context) {
            DeliveryConfiguration.checkValid()
            context.startActivity(createLaunchIntent(context))
        }

        @Suppress("MemberVisibilityCanBePrivate")
        fun createLaunchIntent(
            context: Context,
            orderId: String? = null
        ) = Intent(context, DeliveryActivity::class.java).apply {
            putExtra(ARG_ORDER_ID, orderId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }

    private val overlappingDetailFragment
        get() = supportFragmentManager.findFragmentByTag("overlappingDetail")

    private val settingsFragment
        get() = supportFragmentManager.findFragmentById(R.id.fragment_delivery_settings) as DeliverySettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            viewModel.stopRinging()
            viewModel.selectedOrderLive.observe(this@DeliveryActivity) { onOrderSelected(it) }

            DeliveryConfiguration.onActivityToolbarCreate(toolbar, supportFragmentManager)

            DeliveryConfiguration.deliveryRepository?.getDeliveryError()
                ?.observe(this@DeliveryActivity) {
                    it?.takeIf { !it.consumed }?.run {
                        consume()
                        finish()
                    }
                }

            viewModel.selectedOrderId = intent.getStringExtra(ARG_ORDER_ID)
            viewModel.loadOrders()

            val constraintSet = ConstraintSet().apply { clone(root) }
            fragmentDeliverySettings.setOnClickListener {
                TransitionManager.beginDelayedTransition(root, AutoTransition())
                constraintSet.applyTo(root)
                settingsFragment.toggle()
            }
            fragmentDeliverySettings.isVisible = DeliveryConfiguration.showSettingsBar
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (overlappingDetailFragment?.isVisible != true) {
            onBackPressedDispatcher.onBackPressed()
        }

        viewModel.deselectOrder()
    }

    private fun onOrderSelected(order: DeliveryOrder?) {
        overlappingDetailFragment?.view?.isVisible = order != null
    }
}
