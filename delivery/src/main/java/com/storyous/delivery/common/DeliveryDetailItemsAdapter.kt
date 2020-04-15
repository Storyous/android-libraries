package com.storyous.delivery.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.storyous.commonutils.recyclerView.ItemsAdapter
import com.storyous.commonutils.recyclerView.getString
import com.storyous.commonutils.show
import com.storyous.delivery.common.api.model.DeliveryAddition
import com.storyous.delivery.common.api.model.DeliveryItem
import kotlinx.android.synthetic.main.payment_item_additions_subitem.view.*

class DeliveryDetailItemsAdapter(
    private var deliveryResourceProvider: IDeliveryResourceProvider
) : RecyclerView.Adapter<DeliveryDetailItemsAdapter.ItemDetailViewHolder>(),
    ItemsAdapter<DeliveryItem> {

    override var items = listOf<DeliveryItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, itemType: Int): ItemDetailViewHolder {
        return ItemDetailViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_delivery_detail, parent, false),
            deliveryResourceProvider
        )
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(viewHolder: ItemDetailViewHolder, position: Int) {
        viewHolder.bind(items[position])
    }

    class ItemDetailViewHolder(
        itemView: View,
        private val deliveryResourceProvider: IDeliveryResourceProvider
    ) : RecyclerView.ViewHolder(itemView) {
        private val count = itemView.findViewById<TextView>(R.id.count_value)
        private val measure = itemView.findViewById<TextView>(R.id.count_measure)
        private val title = itemView.findViewById<TextView>(R.id.item_title)
        private val price = itemView.findViewById<TextView>(R.id.price_value)
        private val currency = itemView.findViewById<TextView>(R.id.price_currency)
        private val subitems = itemView.findViewById<LinearLayout>(R.id.subitems)

        fun bind(item: DeliveryItem) {
            count.text = deliveryResourceProvider.formatCount(item.count)
            measure.text = item.measure ?: getString(R.string.piece_measure)
            title.text = item.title

            price.text = deliveryResourceProvider.formatPriceWithoutCurrency(item.unitPriceWithVat, item.count)
            currency.text = deliveryResourceProvider.defaultCurrency()

            subitems.show(!item.additions.isNullOrEmpty())

            val inflater = LayoutInflater.from(itemView.context)
            subitems.removeAllViews()
            item.additions?.forEach {
                val view = inflater.inflate(R.layout.payment_item_additions_subitem, subitems, false)
                bindSubItem(view, item, it)
                subitems.addView(view)
            }
        }

        private fun bindSubItem(
            subItemView: View,
            mainItem: DeliveryItem,
            item: DeliveryAddition
        ) {
            subItemView.sign.text = if (item.countPerMainItem >= 0) "\uff0b " else "\u2014 "
            subItemView.title.text = item.title.let {
                if (item.countPerMainItem == 1) it else "${item.countPerMainItem} $it"
            }

            val formattedPriceWithoutCurrency = deliveryResourceProvider.formatPriceWithoutCurrency(
                item.unitPriceWithVat, item.countPerMainItem * mainItem.count
            )
            subItemView.price_value.text = formattedPriceWithoutCurrency.let {
                if (item.countPerMainItem < 0) "-$it" else it
            }
            subItemView.price_currency.text = deliveryResourceProvider.defaultCurrency()
        }
    }
}
