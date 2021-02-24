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
import com.storyous.delivery.common.api.DeliveryOrder
import kotlinx.android.synthetic.main.activity_delivery.*

class DeliveryActivity : AppCompatActivity() {

    private val viewModel by viewModels<DeliveryViewModel>()

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
        setContentView(R.layout.activity_delivery)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        viewModel.stopRinging()
        viewModel.selectedOrderLive.observe(this) { onOrderSelected(it) }

        DeliveryConfiguration.onActivityToolbarCreate(toolbar, supportFragmentManager)

        DeliveryConfiguration.deliveryRepository?.getDeliveryError()?.observe(this) {
            it?.takeIf { !it.consumed }?.run {
                consume()
                finish()
            }
        }

        viewModel.selectedOrderId = intent.getStringExtra(ARG_ORDER_ID)
        viewModel.loadOrders()

        val constraintSet = ConstraintSet().apply { clone(root) }
        fragment_delivery_settings.setOnClickListener {
            TransitionManager.beginDelayedTransition(root, AutoTransition())
            constraintSet.applyTo(root)
            settingsFragment.toggle()
        }
        fragment_delivery_settings.isVisible = DeliveryConfiguration.showSettingsBar
    }

    override fun onBackPressed() {
        if (overlappingDetailFragment?.isVisible != true) {
            super.onBackPressed()
        }

        viewModel.deselectOrder()
    }

    private fun onOrderSelected(order: DeliveryOrder?) {
        overlappingDetailFragment?.view?.isVisible = order != null
    }
}
