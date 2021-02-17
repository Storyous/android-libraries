package com.storyous.delivery.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.storyous.delivery.common.api.DeliveryOrder
import kotlinx.android.synthetic.main.fragment_delivery_list.*

/**
 * A placeholder fragment containing a simple view.
 */
class DeliveryListFragment : Fragment(R.layout.fragment_delivery_list) {

    private val viewModel by viewModels<DeliveryViewModel>({ requireActivity() })
    private val deliveryItemsAdapter by lazy {
        DeliveryItemsAdapter { viewModel.selectedOrderId = it.orderId }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getDeliveryOrdersLive().observe(this, Observer { orders -> showOrders(orders) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deliveryItemsAdapter.setStringValues(
            getString(R.string.delivery_orders_new),
            getString(R.string.delivery_orders_waiting),
            getString(R.string.delivery_orders_processed),
            getString(R.string.delivery_orders_declined)
        )

        list_deliveries.adapter = deliveryItemsAdapter

        val layoutManager = LinearLayoutManager(view.context)
        list_deliveries.layoutManager = layoutManager

        val dividerDecoration = DividerItemDecoration(view.context, layoutManager.orientation)
        list_deliveries.addItemDecoration(dividerDecoration)
    }

    private fun showOrders(orders: List<DeliveryOrder>?) {
        orders?.let { deliveryItemsAdapter.setOrders(it) }
    }
}
