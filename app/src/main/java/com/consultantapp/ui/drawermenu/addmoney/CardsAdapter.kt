package com.consultantapp.ui.drawermenu.addmoney

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Wallet
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemCardsBinding


class CardsAdapter(private val activity: AddMoneyActivity, private val items: ArrayList<Wallet>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != LOADING)
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.rv_item_cards, parent, false
                    )
            )
        } else {
            ViewHolderLoader(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_paging_loader, parent, false
                    )
            )
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) items.size else items.size + 1

    override fun getItemViewType(position: Int) = if (position >= items.size) LOADING else ITEM

    inner class ViewHolder(val binding: RvItemCardsBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.clCard.setOnClickListener {
                val clickedPosition = adapterPosition
                if (!items[clickedPosition].isSelected) {
                    activity.selectedCardId = items[clickedPosition].id ?: ""
                    items.forEachIndexed { index, wallet ->
                        items[index].isSelected = index == clickedPosition
                        notifyDataSetChanged()
                    }
                }
            }

            binding.tvEdit.setOnClickListener {
                activity.editCard(items[adapterPosition])
            }

            binding.tvDelete.setOnClickListener {
                activity.deleteCard(items[adapterPosition])
            }
        }

        fun bind(wallet: Wallet) = with(binding) {

            binding.rbSelect.isChecked = wallet.isSelected
            binding.tvCardName.text = binding.root.context.getString(R.string.card_ending_with,
                    wallet.card_brand, wallet.last_four_digit)

        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
