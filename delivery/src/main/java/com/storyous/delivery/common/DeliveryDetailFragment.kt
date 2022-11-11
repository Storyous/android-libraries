package com.storyous.delivery.common

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.storyous.commonutils.Toaster
import com.storyous.commonutils.castOrNull
import com.storyous.commonutils.extensions.positiveButton
import com.storyous.commonutils.viewBinding
import com.storyous.delivery.common.api.Customer
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.api.Desk
import com.storyous.delivery.common.databinding.FragmentDeliveryDetailBinding
import timber.log.Timber
import java.math.BigDecimal

@Suppress("TooManyFunctions")
class DeliveryDetailFragment : Fragment(R.layout.fragment_delivery_detail) {

    private val binding by viewBinding<FragmentDeliveryDetailBinding>()
    private val itemsAdapter by lazy { DeliveryDetailItemsAdapter() }
    private val timesAdapter by lazy { DeliveryTimesAdapter() }
    private val viewModel by viewModels<DeliveryViewModel>({ requireActivity() })
    private var autodeclineTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectedOrderLive.observe(this, this::onOrderSelected)
        viewModel.loadingOrderAccepting.observe(this) {
            binding.buttons.buttonAccept.showOverlay(it)
        }
        viewModel.loadingOrderCancelling.observe(this) {
            binding.buttons.buttonCancel.showOverlay(it)
        }
        viewModel.loadingOrderDispatching.observe(this) {
            binding.buttons.buttonDispatch.showOverlay(it)
        }
        viewModel.printOrderBillState.observe(this) {
            binding.buttons.buttonPrintBill.showOverlay(it?.isLoading() == true)
            if (it?.isError() == true) {
                Toaster.showShort(requireContext(), R.string.print_delivery_copy_failed)
            }
        }
        viewModel.messagesToShow.observe(this) { onNewMessages(it) }
        viewModel.acceptFunction.observe(this) {
            binding.buttons.buttonAccept.isVisible = it?.first == true
            binding.buttons.buttonAccept.isEnabled = it?.second == true
        }
        viewModel.cancelFunction.observe(this) {
            binding.buttons.buttonCancel.isVisible = it.first
            binding.buttons.buttonCancel.isEnabled = it.second
        }
        viewModel.dispatchFunction.observe(this) {
            val globallyOff = DeliveryConfiguration.globalDispatchDisabled.first
            binding.buttons.buttonDispatch.isVisible = it?.first == true
            binding.buttons.buttonDispatch.isEnabled = it?.second == true && !globallyOff
            binding.warning.text = DeliveryConfiguration.globalDispatchDisabled.second
            binding.warning.isVisible = globallyOff && it?.second == true && binding.warning.text.isNotEmpty()
        }
        viewModel.printBillFunction.observe(this) {
            binding.buttons.buttonPrintBill.isVisible = it?.first == true
            binding.buttons.buttonPrintBill.isEnabled = it?.second == true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.orderItems.adapter = itemsAdapter
        binding.orderItems.addItemDecoration(
            DividerItemDecoration(
                view.context,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.noDetail.visibility = View.VISIBLE
        binding.detail.visibility = View.GONE

        binding.buttons.buttonAccept.setOnClickListener {
            Timber.i("Confirm order")
            viewModel.selectedOrder?.also { viewModel.acceptOrder(it) }
                ?: Toaster.showShort(requireContext(), R.string.delivery_confirm_error_other)
        }
        binding.buttons.buttonCancel.setOnClickListener {
            Timber.i("Cancel order")
            onOrderCancelClicked()
        }
        binding.buttons.buttonDispatch.setOnClickListener {
            Timber.i("Dispatch order")
            viewModel.selectedOrder?.also { viewModel.dispatchOrder(it) }
                ?: Toaster.showShort(requireContext(), R.string.delivery_dispatch_error_other)
        }
        binding.buttons.buttonPrintBill.setOnClickListener {
            Timber.i("Print order bill")
            viewModel.selectedOrder?.also { viewModel.printBill(it) }
        }
        binding.meta.deliveryDates.adapter = timesAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autodeclineTimer?.cancel()
    }

    private fun onNewMessages(messages: List<Int>?) {
        messages?.iterator()?.forEach { messageId ->
            val message = when (messageId) {
                DeliveryViewModel.MESSAGE_ERROR_STATE_CONFLICT ->
                    getString(R.string.delivery_confirm_error_conflict)
                DeliveryViewModel.MESSAGE_ERROR_OTHER ->
                    getString(R.string.delivery_confirm_error_other)
                else -> null
            }
            if (context != null && message != null) {
                Toaster.showLong(requireContext(), message)
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
                    viewModel.selectedOrder?.let { viewModel.cancelOrder(it, reason) }
                    Timber.i("Order cancelled with reason: $reason")
                    dialog.dismiss()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setSingleChoiceItems(adapter, -1) { dialog, _ ->
                dialog.castOrNull<AlertDialog>()?.positiveButton?.isEnabled = true
            }
            .show()
            .apply { positiveButton?.isEnabled = false }
    }

    private fun onOrderSelected(order: DeliveryOrder?) {
        order?.apply {
            binding.noDetail.visibility = View.GONE
            binding.detail.visibility = View.VISIBLE

            itemsAdapter.items = items
            updateCustomerInfo(customer, desk)
            updatePaymentType(alreadyPaid)
            updateDiscount(discountWithVat?.takeIf { it > BigDecimal.ZERO })
            updateTips(tipWithVat?.takeIf { it > BigDecimal.ZERO })
            updateOrderNote(note)
            updateOrderNumber(provider, orderId)
            updateDates(this)
            binding.info.isVisible = state == DeliveryOrder.STATE_SCHEDULING_DELIVERY
            autodeclineTimer?.cancel()
            autodeclineTimer = AutodeclineCountdown.newInstance(
                this,
                binding.autodeclineCountdown,
                R.string.autodecline_info
            ) {
                viewModel.refreshFunctions()
            }
        } ?: repaintNoOrderSelected()
    }

    private fun updateTips(tipWithVat: BigDecimal?) {
        binding.meta.deliveryTipsGroup.isVisible = tipWithVat != null
        tipWithVat?.let {
            binding.meta.deliveryTips.text = DeliveryConfiguration.formatter.formatPrice(it)
        }
    }

    private fun updateDiscount(discountWithVat: BigDecimal?) {
        binding.meta.deliveryDiscountGroup.isVisible = discountWithVat != null
        discountWithVat?.let {
            binding.meta.deliveryDiscount.text = DeliveryConfiguration.formatter.formatPrice(it)
        }
    }

    private fun updateDates(order: DeliveryOrder) {
        val times = mutableListOf<Pair<String, String>>()
        val provider = ContextStringResProvider(requireContext().applicationContext)
        if (order.timing != null) {
            times.addAll(order.getTimingTranslations(provider))
        } else {
            times.add(
                order.getLegacyDeliveryTypeTranslation(provider) to order.getLegacyDeliveryTime(
                    provider
                )
            )
        }
        timesAdapter.items = times
    }

    @Suppress("unused")
    private fun updateOrderNumber(provider: String, orderId: String) {
        binding.meta.orderNumber.text = DeliveryModel.getOrderInfo(provider) { resId, param ->
            getString(resId, "$param")
        }
        binding.meta.orderNumber.isVisible = binding.meta.orderNumber.text.isNotEmpty()
        /* we will use only provider name until we have correct order number. then we will add:
         + "\n$orderId"
         */
    }

    private fun updatePaymentType(alreadyPaid: Boolean) {
        binding.meta.deliveryPaymentTypeHeader.text = getString(R.string.delivery_payment_type_header)
        binding.meta.deliveryPaymentType.text = getString(
            if (alreadyPaid) {
                R.string.delivery_already_paid
            } else {
                R.string.delivery_pay_on_delivery
            }
        )
    }

    private fun updateOrderNote(note: String?) {
        binding.orderNoteGroup.isVisible = note?.isNotBlank() == true
        binding.meta.orderNote.text = note
    }

    private fun repaintNoOrderSelected() {
        binding.noDetail.visibility = View.VISIBLE
        binding.detail.visibility = View.GONE
        binding.orderNoteGroup.isVisible = false
    }

    private fun updateCustomerInfo(customer: Customer, desk: Desk?) {
        binding.meta.customerDetailName.text = customer.name
        binding.meta.customerDetailPhone.text = customer.phoneNumber
        binding.meta.customerDetailAddress.text = desk?.name?.let { getString(R.string.table, it) }
            ?: customer.deliveryAddress
    }
}
