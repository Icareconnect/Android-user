package com.consultantapp.ui.classes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.ClassData
import com.consultantapp.data.models.responses.JitsiClass
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentClassDetailBinding
import com.consultantapp.ui.jitsimeet.JitsiActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ClassesDetailFragment() : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentClassDetailBinding

    private var rootView: View? = null

    private lateinit var viewModel: ClassesViewModel

    private lateinit var progressDialog: ProgressDialog

    private var classData: ClassData? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_class_detail, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        binding.layoutClass.cvClass.elevation = 0f
        progressDialog = ProgressDialog(requireActivity())
        binding.clLoader.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))

        if (requireActivity().intent.hasExtra(CLASS_ID)) {
            if (isConnectedToInternet(requireContext(), true)) {
                val hashMap = HashMap<String, String>()
                hashMap["class_id"] = requireActivity().intent.getStringExtra(CLASS_ID) ?:""
                viewModel.classDetail(hashMap)
            }
        } else if (arguments?.containsKey(CLASS_DATA) == true) {
            classData = arguments?.getSerializable(CLASS_DATA) as ClassData
            setData()
        } else {
            binding.toolbar.performClick()
        }
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.layoutClass.tvStartClass.setOnClickListener {
            startCall()
        }
    }

    private fun setData() {
        binding.tvTitle.text = classData?.name
        binding.layoutClass.tvName.text = getDoctorName(classData?.created_by)
        binding.layoutClass.tvDesc.text = classData?.created_by?.categoryData?.name
                ?: getString(R.string.na)

        loadImage(
                binding.layoutClass.ivPic, classData?.created_by?.profile_image,
                R.drawable.ic_profile_placeholder
        )

        binding.layoutClass.tvClassName.text = classData?.name

        val classTime = DateUtils.dateTimeFormatFromUTC(DateFormat.DATE_TIME_FORMAT, classData?.bookingDateUTC)
        binding.layoutClass.tvClassTime.text = classTime
        binding.layoutClass.tvClassPrice.text = getCurrency(classData?.price)

        if (classData?.status == ClassType.COMPLETED) {
            binding.layoutClass.tvStartClass.gone()
        } else {
            if (classData?.isOccupied == true) {
                binding.layoutClass.tvStartClass.text = getString(R.string.join_class)
            } else {
                binding.layoutClass.tvStartClass.text = getString(R.string.occupy_class)
            }
        }
    }

    private fun bindObservers() {
        viewModel.classDetail.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.clLoader.setBackgroundColor(0)

                    classData = it.data
                    setData()

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

        viewModel.enrollUser.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    val hashMap = HashMap<String, String>()
                    hashMap["class_id"] = classData?.id ?: ""
                    viewModel.classDetail(hashMap)
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

        viewModel.joinClass.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    /*Data for jitsi class*/
                    val jitsiClass = JitsiClass()
                    jitsiClass.id = classData?.id
                    jitsiClass.name = classData?.name
                    jitsiClass.isClass = true

                        startActivity(Intent(requireActivity(), JitsiActivity::class.java)
                                .putExtra(EXTRA_CALL_NAME, jitsiClass))
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })
    }

    private fun startCall() {
        if (classData?.isOccupied == false) {
            AlertDialogUtil.instance.createOkCancelDialog(requireActivity(), R.string.occupy_class,
                    R.string.occupy_class_message, R.string.occupy_class, R.string.cancel, false,
                    object : AlertDialogUtil.OnOkCancelDialogListener {
                        override fun onOkButtonClicked() {
                            if (isConnectedToInternet(requireContext(), true)) {
                                val hashMap = HashMap<String, String>()
                                hashMap["class_id"] = classData?.id ?: ""
                                viewModel.enrollUser(hashMap)
                            }
                        }

                        override fun onCancelButtonClicked() {
                        }
                    }).show()
        } else if (classData?.isOccupied == true) {
            if (isConnectedToInternet(requireContext(), true)) {
                val hashMap = HashMap<String, String>()
                hashMap["class_id"] = classData?.id ?: ""
                viewModel.joinClass(hashMap)
            }
        } else if (classData?.status == ClassType.ADDED) {
            AlertDialogUtil.instance.createOkCancelDialog(requireActivity(), R.string.join_class,
                    R.string.join_class_message, R.string.ok, R.string.cancel, false,
                    object : AlertDialogUtil.OnOkCancelDialogListener {
                        override fun onOkButtonClicked() {
                        }

                        override fun onCancelButtonClicked() {
                        }
                    }).show()
        }
    }

    companion object {
        const val CLASS_DATA = "CLASS_DATA"
        const val CLASS_ID = "CLASS_ID"
    }
}
