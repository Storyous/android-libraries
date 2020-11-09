package com.storyous.delivery.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.storyous.delivery.common.api.Customer
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.api.Desk
import kotlinx.android.synthetic.main.delivery_detail_buttons.*
import kotlinx.android.synthetic.main.fragment_delivery_detail.*

@Suppress("TooManyFunctions")
class DeliveryDetailFragment : Fragment() {

    private val itemsAdapter by lazy { DeliveryDetailItemsAdapter() }
    private val timesAdapter by lazy { DeliveryTimesAdapter() }
    private val viewModel by viewModels<DeliveryViewModel>({ requireActivity() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getSelectedOrderLive().observe(this) { onOrderSelected(it) }
        viewModel.loadingOrderAccepting.observe(this) {
            button_accept.showOverlay(it == true)
        }
        viewModel.loadingOrderCancelling.observe(this) {
            button_cancel.showOverlay(it == true)
        }
        viewModel.loadingOrderDispatching.observe(this) {
            button_dispatch.showOverlay(it == true)
        }
        viewModel.messagesToShow.observe(this) { onNewMessages(it) }
        viewModel.acceptFunction.observe(this) {
            button_accept.isVisible = it.first
            button_accept.isEnabled = it.second
        }
        viewModel.cancelFunction.observe(this) {
            button_cancel.isVisible = it.first
            button_cancel.isEnabled = it.second
        }
        viewModel.dispatchFunction.observe(this) {
            val globallyOff = DeliveryConfiguration.globalDispatchDisabled.first
            button_dispatch.isVisible = it.first
            button_dispatch.isEnabled = it.second && !globallyOff
            warning.text = DeliveryConfiguration.globalDispatchDisabled.second
            warning.isVisible = globallyOff && it.second && warning.text.isNotEmpty()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_delivery_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        order_items.adapter = itemsAdapter
        val linearLayoutManager = LinearLayoutManager(view.context)
        order_items.layoutManager = linearLayoutManager
        order_items.addItemDecoration(
            DividerItemDecoration(
                view.context,
                linearLayoutManager.orientation
            )
        )
        noDetail.visibility = View.VISIBLE
        detail.visibility = View.GONE

        button_accept.setOnClickListener {
            viewModel.getSelectedOrderLive().value?.also { viewModel.acceptOrder(it) }
                ?: Toast.makeText(
                    context,
                    R.string.delivery_confirm_error_other,
                    Toast.LENGTH_SHORT
                ).show()
        }
        button_cancel.setOnClickListener { onOrderCancelClicked() }
        button_dispatch.setOnClickListener {
            viewModel.getSelectedOrderLive().value?.also { viewModel.dispatchOrder(it) }
                ?: Toast.makeText(
                    context,
                    R.string.delivery_dispatch_error_other,
                    Toast.LENGTH_SHORT
                ).show()
        }
        delivery_dates.adapter = timesAdapter
    }

    private fun onNewMessages(messages: List<Int>?) {
        messages?.forEach { messageId ->
            val message = when (messageId) {
                DeliveryViewModel.MESSAGE_ERROR_STATE_CONFLICT ->
                    getString(R.string.delivery_confirm_error_conflict)
                DeliveryViewModel.MESSAGE_ERROR_OTHER ->
                    getString(R.string.delivery_confirm_error_other)
                else -> null
            }
            if (context != null && message != null) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
        if (messages?.isNotEmpty() == true) {
            viewModel.notifyMessagesShown()
        }
    }

    private fun onOrderCancelClicked() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.cancel_reason_singlechoice,
            resources.getStringArray(R.array.delivery_cancel_reasons)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delivery_cancel_reason_title)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                if (selectedPosition >= 0) {
                    val reason =
                        resources.getStringArray(R.array.delivery_cancel_reasons)[selectedPosition]
                    viewModel.getSelectedOrder()?.let { viewModel.cancelOrder(it, reason) }
                    dialog.dismiss()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setSingleChoiceItems(adapter, -1, null)
            .show()
    }

    private fun onOrderSelected(order: DeliveryOrder?) {
        order?.let {
            noDetail.visibility = View.GONE
            detail.visibility = View.VISIBLE

            itemsAdapter.items = order.items
            updateCustomerInfo(it.customer, it.desk)
            updatePaymentType(it.alreadyPaid)
            updateOrderNote(it.note)
            updateOrderNumber(it.provider, it.orderId)
            updateDates(it)
            info.isVisible = order.state == DeliveryOrder.STATE_SCHEDULING_DELIVERY
        } ?: repaintNoOrderSelected()
    }

    private fun updateDates(order: DeliveryOrder) {
        val times = mutableListOf<Pair<String, String>>()
        val provider = ContextStringResProvider(requireContext().applicationContext)
        if (DeliveryConfiguration.useOrderTimingField && order.timing != null) {
            times.addAll(order.getTimingTranslations(provider))
        } else {
            times.add(order.getLegacyDeliveryTypeTranslation(provider) to order.getLegacyDeliveryTime(provider))
        }
        timesAdapter.items = times
    }

    private fun updateOrderNumber(provider: String, orderId: String) {
        order_number.text = DeliveryModel.getOrderInfo(provider) { resId, param ->
            getString(resId, "$param")
        }
        order_number.isVisible = order_number.text.isNotEmpty()
        /* we will use only provider name until we have correct order number. then we will add:
         + "\n$orderId"
         */
    }

    private fun updatePaymentType(alreadyPaid: Boolean) {
        delivery_payment_type_header.text = getString(R.string.delivery_payment_type_header)
        delivery_payment_type.text = getString(
            if (alreadyPaid) {
                R.string.delivery_already_paid
            } else {
                R.string.delivery_pay_on_delivery
            }
        )
    }

    private fun updateOrderNote(note: String?) {
        order_note_group.isVisible = note?.isNotBlank() == true
        order_note.text = note
    }

    private fun repaintNoOrderSelected() {
        noDetail.visibility = View.VISIBLE
        detail.visibility = View.GONE
        order_note_header.isVisible = false
        order_note_group.isVisible = false
    }

    private fun updateCustomerInfo(customer: Customer, desk: Desk?) {
        customer_detail_name.text = customer.name
        customer_detail_phone.text = customer.phoneNumber
        customer_detail_address.text = desk?.name?.let { getString(R.string.table, it) }
            ?: customer.deliveryAddress
    }
}
