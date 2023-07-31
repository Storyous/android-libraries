package com.storyous.delivery.common

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.DateUtils
import com.storyous.commonutils.TimestampUtil
import com.storyous.commonutils.adapters.Header
import com.storyous.commonutils.adapters.Item
import com.storyous.commonutils.adapters.ItemType
import com.storyous.commonutils.adapters.ItemType.HEADER_DISABLED
import com.storyous.commonutils.adapters.ItemType.HEADER_ENABLED
import com.storyous.commonutils.adapters.ListItem
import com.storyous.commonutils.provider
import com.storyous.commonutils.recyclerView.ItemsAdapter
import com.storyous.commonutils.recyclerView.getString
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.api.DeliveryTiming
import com.storyous.delivery.common.databinding.ListItemDeliveryBinding
import java.math.BigDecimal
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeliveryItemsAdapter(
    private val selectedState: SelectedState = SelectedState(),
    private val onClickListener: (DeliveryOrder) -> Unit
) : RecyclerView.Adapter<DeliveryItemsAdapter.DeliveryViewHolder>(),
    ItemsAdapter<ListItem<DeliveryOrder>>,
    CoroutineScope by CoroutineProviderScope() {

    private var mapItemsJob: Job? = null
    override var items: List<ListItem<DeliveryOrder>> = listOf()

    private val clickListener: (Int, DeliveryOrder) -> Unit = { position, item ->

        with(selectedState) {
            orderId = item.orderId
            notifyItemChanged(position)
            onStateChanged()
            lastPosition = position
            onStateChanged = { notifyItemChanged(lastPosition) }
        }

        onClickListener(item)
    }

    fun setOrders(
        items: List<DeliveryOrder>,
        mapper: List<DeliveryOrder>.() -> List<ListItem<DeliveryOrder>>
    ) {
        mapItemsJob?.cancel()
        mapItemsJob = launch {
            val newItems = withContext(provider.Default) { mapper(items) }

            synchronized(this@DeliveryItemsAdapter.items) {
                val diffResult = DiffUtil.calculateDiff(
                    DeliveryOrderDiffCallback(this@DeliveryItemsAdapter.items, newItems)
                )
                this@DeliveryItemsAdapter.items = newItems
                diffResult.dispatchUpdatesTo(this@DeliveryItemsAdapter)
            }
        }
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
                    ListItemDeliveryBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    clickListener
                )
            }
            else -> throw IllegalArgumentException("Unknown item type $itemType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].getType()
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(viewHolder: DeliveryViewHolder, position: Int) {
        items[position].let {
            viewHolder.bind(it)
            if ((it is Item) && it.itemValue.orderId == selectedState.orderId) {
                viewHolder.itemView.isSelected = true
                selectedState.lastPosition = viewHolder.bindingAdapterPosition
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
}

class DeliveryOrderDiffCallback(
    val oldItems: List<ListItem<DeliveryOrder>>,
    val newItems: List<ListItem<DeliveryOrder>>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldItems.size

    override fun getNewListSize(): Int = newItems.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        return oldItem is Item
                && newItem is Item
                && oldItem.itemValue.orderId == newItem.itemValue.orderId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }
}

class DeliveryItemViewHolder(
    private val binding: ListItemDeliveryBinding,
    private val onClickListener: (Int, DeliveryOrder) -> Unit
) : DeliveryItemsAdapter.DeliveryViewHolder(binding.root) {

    private val provider = ContextStringResProvider(binding.root.context)
    private val timeFrom: TextView = binding.textItemDeliveryTimeFrom
    private val timeTo: TextView = binding.textItemDeliveryTimeTo
    val name: TextView = binding.textItemDeliveryCustomer
    val address: TextView = binding.textItemDeliveryAddress
    val price: TextView = binding.textItemDeliveryPrice
    val deliveryType: TextView = binding.textItemDeliveryType
    val autodeclineCountdown: TextView = binding.autodeclineCountdown
    var autodeclineTimer: CountDownTimer? = null

    override fun bind(listItem: ListItem<DeliveryOrder>) {
        val deliveryOrder = (listItem as Item).itemValue
        itemView.setOnClickListener {
            onClickListener(bindingAdapterPosition, deliveryOrder)
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
        binding.info.isVisible = deliveryOrder.state == DeliveryOrder.STATE_SCHEDULING_DELIVERY

        autodeclineTimer?.cancel()
        autodeclineTimer = AutodeclineCountdown.newInstance(deliveryOrder, autodeclineCountdown)
    }

    override fun recycle() {
        autodeclineTimer?.cancel()
    }

    private fun getDeliveryTime(order: DeliveryOrder): Pair<String, String> = when {
        order.timing == null -> {
            order.getLegacyDeliveryTime(provider) to ""
        }
        order.timing.showTime() == DeliveryTiming.SHOW_ASAP -> {
            provider.getString(R.string.delivery_asap) to ""
        }
        else -> {
            order.getImportantTimingTranslation(provider)?.let { timing ->
                timing.second?.getTranslation(provider) ?: (timing.first to "")
            } ?: "" to ""
        }
    }

    private fun calcTotalPriceFormatted(order: DeliveryOrder): String {
        var total = 0.0
        for (item in order.items) {
            total += item.unitPriceWithVat.toDouble() * item.count
            item.additions?.iterator()?.forEach { addition ->
                total += addition.unitPriceWithVat.toDouble() * addition.countPerMainItem * item.count
            }
        }
        return DeliveryConfiguration.formatter.formatPrice(BigDecimal.valueOf(total))
    }
}

private class DeliveryHeaderViewHolder(itemView: View) :
    DeliveryItemsAdapter.DeliveryViewHolder(itemView) {
    val text = itemView as TextView

    override fun bind(listItem: ListItem<DeliveryOrder>) {
        val header = listItem as Header
        text.text = header.title
        text.isEnabled = header.headerType == HEADER_ENABLED
    }
}

fun List<DeliveryOrder>.toAdapterItemsByState(
    textHeaderNew: String,
    textHeaderWaiting: String,
    textHeaderProcessed: String,
    textHeaderDeclined: String
): List<ListItem<DeliveryOrder>> {

    val ordersNew = filter {
        it.state == DeliveryOrder.STATE_NEW || it.state == DeliveryOrder.STATE_SCHEDULING_DELIVERY
    }
    val ordersWaiting = filter { it.state == DeliveryOrder.STATE_CONFIRMED }
    val ordersProcessed = filter { it.state == DeliveryOrder.STATE_DISPATCHED }
    val ordersDeclined = filter { it.state == DeliveryOrder.STATE_DECLINED }

    val data = mutableListOf<ListItem<DeliveryOrder>>()
    if (ordersNew.isNotEmpty()) {
        data.add(Header(textHeaderNew, HEADER_DISABLED))
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
    return data
}

fun List<DeliveryOrder>.toAdapterItemsByDate(context: Context): List<ListItem<DeliveryOrder>> {
    val toDayDate: Date?.() -> Date? = {
        this?.let {
            Calendar.getInstance().apply {
                time = it
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        }
    }
    val today = TimestampUtil.getDate().toDayDate()
    val tomorrow = TimestampUtil.getCalendar().apply {
        add(Calendar.DAY_OF_YEAR, 1)
    }.time.toDayDate()

    return map { Item(it) }
        .groupBy {
            it.itemValue.timing?.mostImportantTime.toDayDate() ?: today
        }
        .toSortedMap(compareBy { it })
        .flatMap { (date, orders) ->
            val title = when (date) {
                today -> context.getString(R.string.today)
                tomorrow -> context.getString(R.string.tomorrow)
                else -> DateUtils.DMY.format(date!!)
            }
            listOf(Header<DeliveryOrder>(title, HEADER_DISABLED)) +
                    orders.sortedBy { it.itemValue.timing?.mostImportantTime }
        }
}

data class SelectedState(
    var orderId: String? = null,
    var lastPosition: Int = -1,
    var onStateChanged: () -> Unit = {}
)
