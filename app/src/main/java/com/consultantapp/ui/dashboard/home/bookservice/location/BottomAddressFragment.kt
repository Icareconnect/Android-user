package com.consultantapp.ui.dashboard.home.bookservice.location

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.SaveAddress
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.databinding.BottomSelectAddressBinding
import com.consultantapp.di.DaggerBottomSheetDialogFragment
import com.consultantapp.ui.dashboard.home.bookservice.registerservice.RegisterServiceFragment
import com.consultantapp.utils.*
import kotlinx.android.synthetic.main.item_no_data.view.*
import javax.inject.Inject


class BottomAddressFragment(private val fragment: RegisterServiceFragment) : DaggerBottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: BottomSelectAddressBinding

    private lateinit var viewModel: AddressViewModel

    private var items = ArrayList<SaveAddress>()

    private lateinit var adapter: SavedAddressAdapter


    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_select_address, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        initialise()
        listeners()
        bindObservers()
        setAdapter()
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[AddressViewModel::class.java]

        binding.clNoData.tvNoData.text = getString(R.string.no_address)
        binding.clNoData.tvNoDataDesc.text = getString(R.string.no_address_desc)

        if (isConnectedToInternet(requireContext(), true))
            viewModel.getAddress()
    }

    private fun setAdapter() {
        adapter = SavedAddressAdapter(this, items)
        binding.rvListing.adapter = adapter
    }

    private fun listeners() {
        binding.tvAdd.setOnClickListener {
            val intent = Intent(requireContext(), AddAddressActivity::class.java)
            startActivityForResult(intent, AppRequestCode.ASK_FOR_LOCATION)
        }
    }

    fun clickItem(pos: Int) {
        val item = items[pos]
        item.location = ArrayList()
        item.location?.add(items[pos].long?.toDouble() ?: 0.0)
        item.location?.add(items[pos].lat?.toDouble() ?: 0.0)

        fragment.selectedAddress(item)
        dialog?.dismiss()
    }

    private fun bindObservers() {
        viewModel.getAddress.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()

                    items.clear()
                    items.addAll(it.data?.addresses ?: emptyList())

                    adapter.notifyDataSetChanged()

                    binding.clNoData.hideShowView(items.isEmpty())

                }
                Status.ERROR -> {
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ASK_FOR_LOCATION) {
                viewModel.getAddress()
            }
        }
    }
}
