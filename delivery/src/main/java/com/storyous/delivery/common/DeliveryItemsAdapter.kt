package com.storyous.delivery.common

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.storyous.commonutils.adapters.Header
import com.storyous.commonutils.adapters.Item
import com.storyous.commonutils.adapters.ItemType
import com.storyous.commonutils.adapters.ItemType.HEADER_DISABLED
import com.storyous.commonutils.adapters.ItemType.HEADER_ENABLED
import com.storyous.commonutils.adapters.ListItem
import com.storyous.commonutils.recyclerView.getString
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.api.DeliveryTiming
import kotlinx.android.synthetic.main.list_item_delivery.view.*
import java.math.BigDecimal

class DeliveryItemsAdapter(
    private val onClickListener: (DeliveryOrder) -> Unit
) : RecyclerView.Adapter<DeliveryItemsAdapter.DeliveryViewHolder>() {

    private var ordersNew = listOf<DeliveryOrder>()
    private var ordersWaiting = listOf<DeliveryOrder>()
    private var ordersProcessed = listOf<DeliveryOrder>()
    private var ordersDeclined = listOf<DeliveryOrder>()
    private val data = mutableListOf<ListItem<DeliveryOrder>>()

    private var textHeaderNew = ""
    private var textHeaderWaiting = ""
    private var textHeaderProcessed = ""
    private var textHeaderDeclined = ""

    private var selectedOrderId = ""
    private var lastSelectedViewPosition: Int = -1
    private val clickListener: (Int, DeliveryOrder) -> Unit = { position, it ->

        selectedOrderId = it.orderId
        notifyItemChanged(lastSelectedViewPosition)
        notifyItemChanged(position)
        lastSelectedViewPosition = position

        onClickListener(it)
    }

    fun setStringValues(
        headerNew: String,
        headerWaiting: String,
        headerProcessed: String,
        headerDeclined: String
    ) {
        textHeaderNew = headerNew
        textHeaderWaiting = headerWaiting
        textHeaderProcessed = headerProcessed
        textHeaderDeclined = headerDeclined
    }

    fun setOrders(items: List<DeliveryOrder>) {
        ordersNew = items.filter {
            it.state == DeliveryOrder.STATE_NEW || it.state == DeliveryOrder.STATE_SCHEDULING_DELIVERY
        }
        ordersWaiting = items.filter { it.state == DeliveryOrder.STATE_CONFIRMED }
        ordersProcessed = items.filter { it.state == DeliveryOrder.STATE_DISPATCHED }
        ordersDeclined = items.filter { it.state == DeliveryOrder.STATE_DECLINED }

        resetItems()
    }

    private fun resetItems() {
        data.clear()
        if (ordersNew.isNotEmpty()) {
            data.add(Header(textHeaderNew, HEADER_ENABLED))
            data.addAll(ordersNew.map { Item(it) })
        }
        if (ordersWaiting.isNotEmpty()) {
            data.add(Header(textHeaderWaiting, HEADER_DISABLED))
            data.addAll(ordersWaiting.map { Item(it) })
        }
        if (ordersProcessed.isNotEmpty()) {
            data.add(Header(textHeaderProcessed, HEADER_DISABLED))
            data.addAll(ordersProcessed.map { Item(it) })
        }
        if (ordersDeclined.isNotEmpty()) {
            data.add(Header(textHeaderDeclined, HEADER_DISABLED))
            data.addAll(ordersDeclined.map { Item(it) })
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, itemType: Int): DeliveryViewHolder {
        return when (itemType) {
            ItemType.HEADER -> {
                DeliveryHeaderViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_delivery_header, parent, false)
                )
            }
            ItemType.ITEM -> {
                DeliveryItemViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_delivery, parent, false),
                    clickListener
                )
            }
            else -> throw IllegalArgumentException("Unknown item type $itemType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].getType()
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(viewHolder: DeliveryViewHolder, position: Int) {
        data[position].let {
            viewHolder.bind(it)
            if ((it is Item) && it.itemValue.orderId == selectedOrderId) {
                viewHolder.itemView.isSelected = true
                lastSelectedViewPosition = viewHolder.adapterPosition
            } else {
                viewHolder.itemView.isSelected = false
            }
        }
    }

    override fun onViewRecycled(holder: DeliveryViewHolder) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    abstract class DeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(listItem: ListItem<DeliveryOrder>)
        open fun recycle() = Unit
    }

    class DeliveryItemViewHolder(
        itemView: View,
        private val onClickListener: (Int, DeliveryOrder) -> Unit
    ) : DeliveryViewHolder(itemView) {

        private val provider = ContextStringResProvider(itemView.context)
        val timeFrom: TextView = itemView.text_item_delivery_time_from
        val timeTo: TextView = itemView.text_item_delivery_time_to
        val name: TextView = itemView.text_item_delivery_customer
        val address: TextView = itemView.text_item_delivery_address
        val price: TextView = itemView.text_item_delivery_price
        val deliveryType: TextView = itemView.text_item_delivery_type
        val autodeclineCountdown: TextView = itemView.autodecline_countdown
        var autodeclineTimer: CountDownTimer? = null

        override fun bind(listItem: ListItem<DeliveryOrder>) {
            val deliveryOrder = (listItem as Item).itemValue
            itemView.setOnClickListener {
                onClickListener(adapterPosition, deliveryOrder)
            }

            name.text = deliveryOrder.customer.name
            address.text = deliveryOrder.desk?.name?.let { getString(R.string.table, it) }
                ?: deliveryOrder.customer.deliveryAddress
            price.text = calcTotalPriceFormatted(deliveryOrder)
            deliveryType.text = deliveryOrder.getDeliveryTypeTranslation(provider)
            getDeliveryTime(deliveryOrder).let {
                timeFrom.text = it.first
                timeFrom.isVisible = it.first.isNotEmpty()
                timeTo.text = it.second
                timeTo.isVisible = it.second.isNotEmpty()
            }
            itemView.info.isVisible = deliveryOrder.state == DeliveryOrder.STATE_SCHEDULING_DELIVERY

            autodeclineTimer?.cancel()
            autodeclineTimer = AutodeclineCountdown.newInstance(deliveryOrder, autodeclineCountdown)
        }

        override fun recycle() {
            autodeclineTimer?.cancel()
        }

        private fun getDeliveryTime(order: DeliveryOrder): Pair<String, String> {
            return if (!DeliveryConfiguration.useOrderTimingField || order.timing == null) {
                order.getLegacyDeliveryTime(provider) to ""
            } else if (order.timing.showTime() == DeliveryTiming.SHOW_ASAP) {
                provider.getString(R.string.delivery_asap) to ""
            } else {
                order.getImportantTimingTranslation(provider)?.let { timing ->
                    timing.second?.getTranslation(provider) ?: (timing.first to "")
                } ?: "" to ""
            }
        }

        private fun calcTotalPriceFormatted(order: DeliveryOrder): String {
            var total = 0.0
            for (item in order.items) {
                total += item.unitPriceWithVat.toDouble() * item.count
                item.additions?.forEach { addition ->
                    total += addition.unitPriceWithVat.toDouble() * addition.countPerMainItem * item.count
                }
            }
            return DeliveryConfiguration.formatter.formatPrice(BigDecimal.valueOf(total))
        }
    }

    private class DeliveryHeaderViewHolder(itemView: View) : DeliveryViewHolder(itemView) {
        val text = itemView as TextView

        override fun bind(listItem: ListItem<DeliveryOrder>) {
            val header = listItem as Header
            text.text = header.title
            text.isEnabled = header.headerType == HEADER_ENABLED
        }
    }
}
