package com.consultantapp.ui.dashboard.doctor.detail

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.Service
import com.consultantapp.databinding.BottomFilterRequestBinding
import com.consultantapp.di.DaggerBottomSheetDialogFragment
import com.consultantapp.utils.PrefsManager
import javax.inject.Inject


class BottomRequestFragment(private val activity: DoctorDetailActivity, private val service: Service)
    : DaggerBottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: BottomFilterRequestBinding


    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_filter_request, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        initialise()
        listeners()
    }

    private fun initialise() {


    }

    private fun listeners() {

        binding.tvNow.setOnClickListener {
            activity.hiApiDoctorRequest(false, service)
            dialog?.dismiss()
        }

        binding.tvSchedule.setOnClickListener {
            activity.hiApiDoctorRequest(true, service)
            dialog?.dismiss()
        }
    }
}
