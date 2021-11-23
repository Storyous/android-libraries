package com.storyous.delivery.common

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.storyous.commonutils.viewBinding
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.databinding.FragmentDeliveryListBinding

/**
 * A placeholder fragment containing a simple view.
 */
class DeliveryListFragment : Fragment(R.layout.fragment_delivery_list) {

    private val binding by viewBinding<FragmentDeliveryListBinding>()
    private val viewModel by viewModels<DeliveryViewModel>({ requireActivity() })
    private val commonSelectedState = SelectedState()
    private val newDeliveryItemsAdapter by lazy {
        DeliveryItemsAdapter(commonSelectedState) { viewModel.selectedOrderId = it.orderId }
    }
    private val deliveryItemsAdapter by lazy {
        DeliveryItemsAdapter(commonSelectedState) { viewModel.selectedOrderId = it.orderId }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.filteredStatesLive.observe(this) {
            binding.ordersFilter.text = getString(
                R.string.show_with, when {
                    it.contains(DeliveryOrder.STATE_CONFIRMED) -> getString(R.string.delivery_orders_waiting)
                    it.contains(DeliveryOrder.STATE_DISPATCHED) -> getString(R.string.delivery_orders_processed)
                    it.contains(DeliveryOrder.STATE_DECLINED) -> getString(R.string.delivery_orders_declined)
                    else -> getString(R.string.delivery_orders_waiting)
                }
            )
        }
        viewModel.newOrdersLive.observe(this) {
            newDeliveryItemsAdapter.setOrders(it) {
                toAdapterItemsByState(
                    getString(R.string.delivery_orders_new),
                    getString(R.string.delivery_orders_waiting),
                    getString(R.string.delivery_orders_processed),
                    getString(R.string.delivery_orders_declined)
                )
            }
        }
        viewModel.filteredOrdersLive.observe(this) {
            deliveryItemsAdapter.setOrders(it) {
                toAdapterItemsByDate(requireContext())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.listDeliveriesNew) {
            adapter = newDeliveryItemsAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }

        with(binding.ordersFilter) {
            setOnClickListener {
                showStateFilterMenu(it)
            }
        }

        with(binding.listDeliveries) {
            adapter = deliveryItemsAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
    }

    private fun showStateFilterMenu(anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.order_state_filter, menu)
            setOnMenuItemClickListener {
                viewModel.filteredStates = when (it.itemId) {
                    R.id.confirmed_orders -> listOf(DeliveryOrder.STATE_CONFIRMED)
                    R.id.processed_orders -> listOf(DeliveryOrder.STATE_DISPATCHED)
                    R.id.declined_orders -> listOf(DeliveryOrder.STATE_DECLINED)
                    else -> listOf(DeliveryOrder.STATE_CONFIRMED)
                }
                true
            }
        }.show()
    }
}
