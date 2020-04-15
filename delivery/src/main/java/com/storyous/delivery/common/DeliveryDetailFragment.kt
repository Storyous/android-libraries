package com.storyous.delivery.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.storyous.delivery.common.api.model.Customer
import com.storyous.delivery.common.api.model.DeliveryOrder
import kotlinx.android.synthetic.main.fragment_delivery_detail.*

@Suppress("TooManyFunctions")
class DeliveryDetailFragment : Fragment() {

    companion object {
        fun newInstance() = DeliveryDetailFragment()
    }

    private val itemsAdapter by lazy {
        DeliveryDetailItemsAdapter(
            (requireContext().applicationContext as IDeliveryApplication).deliveryResourceProvider
        )
    }
    private lateinit var viewModel: DeliveryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProviders.of(this).get(DeliveryViewModel::class.java)
        } ?: throw IllegalStateException("Fragment is not within an activity")

        viewModel.getSelectedOrderLive().observe(this, Observer { order -> onOrderSelected(order) })
        viewModel.getDeliveryOrdersLive().observe(this, Observer { orders ->
            onOrderSetChanged(orders)
        })
        viewModel.loadingOrderAccepting.observe(this, Observer { loading ->
            button_accept.showOverlay(loading ?: false)
        })
        viewModel.loadingOrderCancelling.observe(this, Observer { loading ->
            button_cancel.showOverlay(loading ?: false)
        })
        viewModel.messagesToShow.observe(this, Observer { messages -> onNewMessages(messages) })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_delivery_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        order_items.adapter = itemsAdapter
        val linearLayoutManager = LinearLayoutManager(view.context)
        order_items.layoutManager = linearLayoutManager
        order_items.addItemDecoration(DividerItemDecoration(view.context, linearLayoutManager.orientation))
        noDetail.visibility = View.VISIBLE
        detail.visibility = View.GONE

        button_accept.setOnClickListener {
            viewModel.getSelectedOrderLive().value?.also { viewModel.acceptOrder(it) }
                ?: Toast.makeText(context, R.string.delivery_confirm_error_other, Toast.LENGTH_SHORT).show()
        }
        button_cancel.setOnClickListener { onOrderCancelClicked() }
    }

    private fun onNewMessages(messages: java.util.ArrayList<Int>?) {
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
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.delivery_cancel_reason_title)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                if (selectedPosition >= 0) {
                    val reason = resources.getStringArray(R.array.delivery_cancel_reasons)[selectedPosition]
                    viewModel.onCancelOrderClicked(reason)
                    dialog.dismiss()
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->

            }
            .setSingleChoiceItems(adapter, -1) { _, _ -> }
            .show()
    }

    private fun onOrderSetChanged(list: List<DeliveryOrder>?) {
        val selected = list?.find { it.orderId == viewModel.getSelectedOrderLive().value?.orderId }
        if (selected == null) {
            repaintNoOrderSelected()
        } else {
            onOrderSelected(selected)
        }
    }

    private fun onOrderSelected(order: DeliveryOrder?) {
        order?.let {
            noDetail.visibility = View.GONE
            detail.visibility = View.VISIBLE
            button_accept.isEnabled = it.state == DeliveryOrder.STATE_NEW
            button_cancel.isEnabled = it.state == DeliveryOrder.STATE_NEW

            itemsAdapter.items = order.items
            updateCustomerInfo(it.customer)
            updatePaymentType(it.alreadyPaid)
            updateOrderNote(it.note)
            updateOrderNumber(it.provider, it.orderId)
        }
    }

    private fun updateOrderNumber(provider: String, orderId: String) {
        order_number.text = DeliveryModel.getOrderInfo(
            provider, { resId, param -> getString(resId, "$param") }
        )
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
        order_note_group.visibility = if (note?.isBlank() ?: true) View.GONE else View.VISIBLE
        order_note.text = note
    }

    private fun repaintNoOrderSelected() {
        noDetail.visibility = View.VISIBLE
        detail.visibility = View.GONE
        order_note_group.visibility = View.GONE
    }

    private fun updateCustomerInfo(customer: Customer) {
        customer_detail_name.text = customer.name
        customer_detail_phone.text = customer.phoneNumber
        customer_detail_address.text = customer.deliveryAddress
    }
}
