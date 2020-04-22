package com.storyous.delivery.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.storyous.commonutils.recyclerView.ItemsAdapter
import com.storyous.commonutils.recyclerView.getString
import com.storyous.commonutils.show
import com.storyous.delivery.common.api.model.DeliveryAddition
import com.storyous.delivery.common.api.model.DeliveryItem
import kotlinx.android.synthetic.main.list_item_delivery_detail.view.*
import kotlinx.android.synthetic.main.payment_item_additions_subitem.view.*
import kotlinx.android.synthetic.main.typed_value_layout.view.*

class DeliveryDetailItemsAdapter :
    RecyclerView.Adapter<DeliveryDetailItemsAdapter.ItemDetailViewHolder>(),
    ItemsAdapter<DeliveryItem> {

    override var items = listOf<DeliveryItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, itemType: Int): ItemDetailViewHolder {
        return ItemDetailViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_delivery_detail, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(viewHolder: ItemDetailViewHolder, position: Int) {
        viewHolder.bind(items[position])
    }

    class ItemDetailViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val count = itemView.count_square.value
        private val measure = itemView.count_square.type
        private val title = itemView.item_title
        private val price = itemView.price_square.value
        private val currency = itemView.price_square.type
        private val subitems = itemView.subitems

        fun bind(item: DeliveryItem) {
            count.text = DeliveryConfiguration.formatter.formatCount(item.count)
            measure.text = item.measure ?: getString(R.string.piece_measure)
            title.text = item.title

            price.text = DeliveryConfiguration.formatter.formatPriceWithoutCurrency(
                item.unitPriceWithVat,
                item.count
            )
            currency.text = DeliveryConfiguration.formatter.defaultCurrency(itemView.context)

            subitems.show(!item.additions.isNullOrEmpty())

            val inflater = LayoutInflater.from(itemView.context)
            subitems.removeAllViews()
            item.additions?.forEach {
                val view =
                    inflater.inflate(R.layout.payment_item_additions_subitem, subitems, false)
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

            val formattedPriceWithoutCurrency = DeliveryConfiguration.formatter.formatPrice(
                item.unitPriceWithVat.toDouble() * item.countPerMainItem * mainItem.count
            )
            subItemView.price.text = formattedPriceWithoutCurrency.let {
                if (item.countPerMainItem < 0) "-$it" else it
            }
        }
    }
}
