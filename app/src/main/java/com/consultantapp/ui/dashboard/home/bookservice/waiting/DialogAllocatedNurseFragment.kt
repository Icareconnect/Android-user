package com.consultantapp.ui.dashboard.home.bookservice.waiting

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.databinding.ItemAllocatedNurseBinding
import com.consultantapp.utils.PrefsManager
import com.consultantapp.utils.gone
import com.consultantapp.utils.loadImage
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject


class DialogAllocatedNurseFragment(
        private val fragment: WaitingAllocationFragment,
        private var doctorData: UserData?
) : DaggerDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: ItemAllocatedNurseBinding


    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.item_allocated_nurse, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        initialise()
        listeners()
    }

    private fun initialise() {
        if (doctorData == null) {
            binding.ivImage.gone()
            binding.tvName.text = getString(R.string.no_nurse_found)
        } else {
            loadImage(binding.ivImage, doctorData?.profile_image,
                    R.drawable.ic_profile_placeholder)
            binding.tvName.text = doctorData?.name
        }
    }

    private fun listeners() {

        binding.tvOk.setOnClickListener {
            if (doctorData == null) {
                dialog?.dismiss()
                requireActivity().supportFragmentManager.popBackStack()
            } else
                requireActivity().finish()
        }
    }


}
