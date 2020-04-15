package com.storyous.delivery.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.storyous.delivery.common.api.model.DeliveryOrder
import kotlinx.android.synthetic.main.fragment_delivery_list.*

/**
 * A placeholder fragment containing a simple view.
 */
class DeliveryListFragment : Fragment() {

    private lateinit var viewModel: DeliveryViewModel
    private val deliveryItemsAdapter by lazy {
        DeliveryItemsAdapter(
            (context?.applicationContext as IDeliveryApplication).deliveryResourceProvider
        ) { viewModel.setSelectOrder(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this).get(DeliveryViewModel::class.java)
        } ?: throw IllegalStateException("Fragment is not within an activity")
        viewModel.getDeliveryOrdersLive().observe(this, Observer { orders -> showOrders(orders) })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_delivery_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deliveryItemsAdapter.setStringValues(
            context?.getString(R.string.delivery_orders_new) ?: "",
            context?.getString(R.string.delivery_orders_waiting) ?: "",
            context?.getString(R.string.delivery_orders_processed) ?: "",
            context?.getString(R.string.delivery_orders_declined) ?: ""
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
