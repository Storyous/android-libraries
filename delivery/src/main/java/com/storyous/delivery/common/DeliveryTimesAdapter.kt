package com.storyous.delivery.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.storyous.commonutils.recyclerView.ItemsAdapter
import com.storyous.delivery.common.databinding.DeliveryDetailDateBinding

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
            DeliveryDetailDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = items.count()

    override fun onBindViewHolder(viewHolder: ItemDetailViewHolder, position: Int) {
        with(items[position]) { viewHolder.bind(first, second) }
    }

    class ItemDetailViewHolder(
        binding: DeliveryDetailDateBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val labelView = binding.estimatedPickupLabel
        private val valueView = binding.estimatedPickupValue

        fun bind(label: String, value: String) {
            labelView.text = label
            valueView.text = value
        }
    }
}
