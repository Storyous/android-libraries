package com.storyous.delivery.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.storyous.commonutils.DateUtils
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.commonutils.adapters.Header
import com.storyous.commonutils.adapters.Item
import com.storyous.commonutils.adapters.ItemType
import com.storyous.commonutils.adapters.ItemType.HEADER_DISABLED
import com.storyous.commonutils.adapters.ItemType.HEADER_ENABLED
import com.storyous.commonutils.adapters.ListItem
import java.util.ArrayList

class DeliveryItemsAdapter(
    private val onClickListener: (DeliveryOrder) -> Unit
) : RecyclerView.Adapter<DeliveryItemsAdapter.DeliveryViewHolder>() {

    private val ordersNew = ArrayList<DeliveryOrder>()
    private val ordersWaiting = ArrayList<DeliveryOrder>()
    private val ordersProcessed = ArrayList<DeliveryOrder>()
    private val ordersDeclined = ArrayList<DeliveryOrder>()
    private val data = ArrayList<ListItem<DeliveryOrder>>()

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

    fun setStringValues(headerNew: String, headerWaiting: String, headerProcessed: String, headerDeclined: String) {
        textHeaderNew = headerNew
        textHeaderWaiting = headerWaiting
        textHeaderProcessed = headerProcessed
        textHeaderDeclined = headerDeclined
    }

    fun setOrders(items: List<DeliveryOrder>) {
        ordersNew.clear()
        ordersWaiting.clear()
        ordersProcessed.clear()
        ordersDeclined.clear()

        ordersNew.addAll(items.filter { it.state == DeliveryOrder.STATE_NEW })
        ordersWaiting.addAll(items.filter { it.state == DeliveryOrder.STATE_WAITING })
        ordersProcessed.addAll(items.filter { it.state == DeliveryOrder.STATE_DISPATCHED })
        ordersDeclined.addAll(items.filter { it.state == DeliveryOrder.STATE_DECLINED })

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
                DeliveryHeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_delivery_header, parent, false))
            }
            ItemType.ITEM -> {
                DeliveryItemViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_delivery, parent, false),
                    clickListener)
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

    abstract class DeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(listItem: ListItem<DeliveryOrder>)
    }

    class DeliveryItemViewHolder(
        itemView: View,
        private val onClickListener: (Int, DeliveryOrder) -> Unit
    ) : DeliveryViewHolder(itemView) {

        val time: TextView = itemView.findViewById(R.id.text_item_delivery_time)
        val name: TextView = itemView.findViewById(R.id.text_item_delivery_customer)
        val address: TextView = itemView.findViewById(R.id.text_item_delivery_address)
        val price: TextView = itemView.findViewById(R.id.text_item_delivery_price)
        val deliveryType: TextView = itemView.findViewById(R.id.text_item_delivery_type)

        override fun bind(listItem: ListItem<DeliveryOrder>) {
            val deliveryOrder = (listItem as Item).itemValue
            itemView.setOnClickListener {
                onClickListener(adapterPosition, deliveryOrder)
            }

            time.text = getDeliveryTime(time.context, deliveryOrder)
            name.text = deliveryOrder.customer.name
            address.text = deliveryOrder.customer.deliveryAddress
            price.text = calcTotalPriceFormatted(deliveryOrder)
            deliveryType.text = getDeliveryType(deliveryType.context, deliveryOrder)
        }

        private fun getDeliveryTime(context: Context, order: DeliveryOrder): String {

            val prefix = if (order.deliveryOnTime) R.string.time_at else R.string.time_till
            val date = DateUtils.HM.format(order.deliveryTime)

            return context.getString(prefix, date)
        }

        private fun getDeliveryType(context: Context, order: DeliveryOrder): String {
            return when (order.deliveryType) {
                DeliveryOrder.TYPE_DELIVERY -> context.getString(R.string.delivery_type_delivery)
                DeliveryOrder.TYPE_TAKEAWAY -> context.getString(R.string.delivery_type_takeaway)
                DeliveryOrder.TYPE_DISPATCH -> context.getString(R.string.delivery_type_dispatch)
                else -> ""
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
            return DeliveryConfiguration.formatter.formatPrice(total)
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
