package com.storyous.delivery.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.storyous.commonutils.recyclerView.ItemsAdapter
import kotlinx.android.synthetic.main.delivery_detail_date.view.*

class DeliveryTimesAdapter :
    RecyclerView.Adapter<DeliveryTimesAdapter.ItemDetailViewHolder>(),
    ItemsAdapter<Pair<String, String>> {

    override var items = listOf<Pair<String, String>>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, itemType: Int): ItemDetailViewHolder {
        return ItemDetailViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.delivery_detail_date, parent, false)
        )
    }

    override fun getItemCount() = items.count()

    override fun onBindViewHolder(viewHolder: ItemDetailViewHolder, position: Int) {
        with(items[position]) { viewHolder.bind(first, second) }
    }

    class ItemDetailViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val labelView = itemView.estimated_pickup_label
        private val valueView = itemView.estimated_pickup_value

        fun bind(label: String, value: String) {
            labelView.text = label
            valueView.text = value
        }
    }
}
