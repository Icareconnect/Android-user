package com.consultantapp.ui.drawermenu.wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Wallet
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemWalletBinding
import com.consultantapp.utils.*


class WalletAdapter(private val items: ArrayList<Wallet>) :
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
                            R.layout.rv_item_wallet, parent, false
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

    inner class ViewHolder(val binding: RvItemWalletBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(wallet: Wallet) = with(binding) {
            slideRecyclerItem(binding.root, binding.root.context)

            when (wallet.type) {
                WalletMoney.WITHDRAWAL -> {
                    tvMoneyFrom.text = binding.root.context.getString(R.string.money_sent_from,
                            getDoctorName(wallet.from))
                    tvMoneyAmount.text = "-${getCurrency(wallet.amount)}"

                }
                WalletMoney.REFUND -> {
                    tvMoneyFrom.text = binding.root.context.getString(R.string.money_refund_from,
                        getDoctorName(wallet.from))
                    tvMoneyAmount.text = "+${getCurrency(wallet.amount)}"
                }
                WalletMoney.ADD_PACKAGE -> {
                    tvMoneyFrom.text = binding.root.context.getString(R.string.added_packages)
                    tvMoneyAmount.text = "-${getCurrency(wallet.amount)}"

                }
                WalletMoney.DEPOSIT -> {
                    if (wallet.status == WalletMoney.FAILED) {
                        tvMoneyFrom.text = binding.root.context.getString(R.string.failed_transaction)
                        tvMoneyAmount.text = "${getCurrency(wallet.amount)}"
                    }else {
                        tvMoneyFrom.text = binding.root.context.getString(R.string.added_to_wallet)
                        tvMoneyAmount.text = "+${getCurrency(wallet.amount)}"
                    }
                }
            }

            tvDate.text = DateUtils.dateTimeFormatFromUTC(DateFormat.MON_DATE_YEAR, wallet.created_at)
            tvTime.text = DateUtils.dateTimeFormatFromUTC(DateFormat.TIME_FORMAT, wallet.created_at)

        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
