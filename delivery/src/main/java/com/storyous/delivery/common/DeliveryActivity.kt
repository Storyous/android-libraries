package com.storyous.delivery.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.storyous.delivery.common.api.model.DeliveryOrder
import kotlinx.android.synthetic.main.activity_delivery.*

class DeliveryActivity : AppCompatActivity() {

    private val viewModel by viewModels<DeliveryViewModel>()

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, DeliveryActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery)
        setSupportActionBar(toolbar)

        viewModel.stopRinging()
        viewModel.getSelectedOrderLive().observe(this, Observer { order -> onOrderSelected(order) })
        onOrderSelected(null)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.search_arrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.deliveryResourceProvider.onActivityToolbarCreate(toolbar, supportFragmentManager)
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
        getOverlappingDetailFragment()?.view?.visibility =
            if (order == null) View.GONE else View.VISIBLE
    }

    private fun getOverlappingDetailFragment(): Fragment? {
        return supportFragmentManager.findFragmentByTag("overlappingDetail")
    }

    private fun isOverlappingDetailOpen(): Boolean =
        getOverlappingDetailFragment()?.view?.visibility == View.VISIBLE
}
