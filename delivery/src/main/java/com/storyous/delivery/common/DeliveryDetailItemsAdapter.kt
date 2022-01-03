package com.storyous.delivery.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.storyous.commonutils.recyclerView.ItemsAdapter
import com.storyous.commonutils.recyclerView.getString
import com.storyous.commonutils.show
import com.storyous.delivery.common.api.DeliveryAddition
import com.storyous.delivery.common.api.DeliveryItem
import com.storyous.delivery.common.databinding.ListItemDeliveryAdditionBinding
import com.storyous.delivery.common.databinding.ListItemDeliveryDetailBinding
import java.math.BigDecimal

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
            ListItemDeliveryDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(viewHolder: ItemDetailViewHolder, position: Int) {
        viewHolder.bind(items[position])
    }

    class ItemDetailViewHolder(
        binding: ListItemDeliveryDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val count = binding.countSquare.value
        private val measure = binding.countSquare.type
        private val title = binding.itemTitle
        private val price = binding.priceSquare.value
        private val currency = binding.priceSquare.type
        private val subitems = binding.subitems

        fun bind(item: DeliveryItem) {
            count.text = DeliveryConfiguration.formatter.formatCount(item.count)
            measure.text = item.measure ?: getString(R.string.piece_measure)
            title.text = item.title

            price.text = DeliveryConfiguration.formatter.formatPriceWithoutCurrency(
                item.unitPriceWithVat.multiply(BigDecimal.valueOf(item.count))
            )
            currency.text = DeliveryConfiguration.formatter.defaultCurrency(itemView.context)

            subitems.show(!item.additions.isNullOrEmpty())

            val inflater = LayoutInflater.from(itemView.context)
            subitems.removeAllViews()
            item.additions?.iterator()?.forEach {
                val binding = ListItemDeliveryAdditionBinding.inflate(inflater, subitems, false)
                bindSubItem(binding, item, it)
                subitems.addView(binding.root)
            }
        }

        private fun bindSubItem(
            additionBinding: ListItemDeliveryAdditionBinding,
            mainItem: DeliveryItem,
            item: DeliveryAddition
        ) {
            additionBinding.sign.text = if (item.countPerMainItem >= 0) "\uff0b " else "\u2014 "
            additionBinding.title.text = item.title.let {
                if (item.countPerMainItem == 1) it else "${item.countPerMainItem} $it"
            }

            val formattedPriceWithoutCurrency = DeliveryConfiguration.formatter.formatPrice(
                item.unitPriceWithVat.multiply(BigDecimal.valueOf(item.countPerMainItem * mainItem.count))
            )
            additionBinding.price.text = formattedPriceWithoutCurrency.let {
                if (item.countPerMainItem < 0) "-$it" else it
            }
        }
    }
}
