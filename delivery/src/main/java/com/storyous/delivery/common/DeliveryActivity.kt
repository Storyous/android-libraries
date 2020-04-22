package com.storyous.delivery.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
            .putExtra(ARG_ORDER_ID, orderId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery)
        setSupportActionBar(toolbar)

        viewModel.stopRinging()
        viewModel.getSelectedOrderLive().observe(this, Observer { order -> onOrderSelected(order) })
        onOrderSelected(null)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        DeliveryConfiguration.onActivityToolbarCreate(toolbar, supportFragmentManager)

        DeliveryConfiguration.deliveryRepository?.getDeliveryError()
            ?.observe(this, Observer { exception ->
                if (exception.consume()) {
                    finish()
                }
            })

        intent.getStringExtra(ARG_ORDER_ID)?.also {
            viewModel.setSelectOrder(it)
        }
        viewModel.loadOrders()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when {
            isOverlappingDetailOpen() -> {
                viewModel.getSelectedOrderLive().value = null
                true
            }
            item.itemId == android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (isOverlappingDetailOpen()) {
            viewModel.deselectOrder()
            return
        }
        super.onBackPressed()
    }

    private fun onOrderSelected(order: DeliveryOrder?) {
        getOverlappingDetailFragment()?.view?.isVisible = order != null
    }

    private fun getOverlappingDetailFragment(): Fragment? {
        return supportFragmentManager.findFragmentByTag("overlappingDetail")
    }

    private fun isOverlappingDetailOpen(): Boolean =
        getOverlappingDetailFragment()?.view?.isVisible == true
}
