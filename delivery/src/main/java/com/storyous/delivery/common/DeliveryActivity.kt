package com.storyous.delivery.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.storyous.delivery.common.api.model.DeliveryOrder
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

        fun createLaunchIntent(
            context: Context,
            orderId: String? = null
        ) = Intent(context, DeliveryActivity::class.java)
            .putExtra(ARG_ORDER_ID, orderId).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        viewModel.stopRinging()
        viewModel.getSelectedOrderLive().observe(this, Observer { order -> onOrderSelected(order) })

        DeliveryConfiguration.onActivityToolbarCreate(toolbar, supportFragmentManager)

        DeliveryConfiguration.deliveryRepository?.getDeliveryError()
            ?.observe(this, Observer { exception ->
                if (exception.consume()) {
                    finish()
                }
            })

        viewModel.setSelectOrder(intent.getStringExtra(ARG_ORDER_ID))
        viewModel.loadOrders()
    }

    override fun onBackPressed() {
        if (viewModel.getSelectedOrder() == null) {
            super.onBackPressed()
        } else {
            viewModel.deselectOrder()
        }
    }

    private fun onOrderSelected(order: DeliveryOrder?) {
        getOverlappingDetailFragment()?.view?.isVisible = order != null
    }

    private fun getOverlappingDetailFragment(): Fragment? {
        return supportFragmentManager.findFragmentByTag("overlappingDetail")
    }
}
