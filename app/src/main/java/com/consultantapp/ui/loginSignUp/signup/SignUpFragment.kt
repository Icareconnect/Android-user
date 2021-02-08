package com.consultantapp.ui.loginSignUp.signup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.consultantapp.R
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentSignupBinding
import com.consultantapp.ui.LoginViewModel
import com.consultantapp.ui.dashboard.chat.UploadFileViewModel
import com.consultantapp.ui.loginSignUp.login.LoginFragment
import com.consultantapp.ui.loginSignUp.masterprefrence.MasterPrefrenceFragment
import com.consultantapp.ui.loginSignUp.masterprefrence.MasterPrefrenceFragment.Companion.MASTER_PREFRENCE_TYPE
import com.consultantapp.ui.loginSignUp.verifyotp.VerifyOTPFragment
import com.consultantapp.utils.*
import com.consultantapp.utils.PermissionUtils
import com.consultantapp.utils.dialogs.ProgressDialog
import com.consultantapp.utils.dialogs.ProgressDialogImage
import dagger.android.support.DaggerFragment
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import okhttp3.MediaType
import okhttp3.RequestBody
import permissions.dispatcher.*
import java.io.File
import javax.inject.Inject

@RuntimePermissions
class SignUpFragment : DaggerFragment(), OnDateSelected {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var appSocket: AppSocket

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentSignupBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var progressDialogImage: ProgressDialogImage

    private lateinit var viewModel: LoginViewModel

    private lateinit var viewModelUpload: UploadFileViewModel

    private var isUpdate = false

    private var fileToUpload: File? = null

    private var hashMap = HashMap<String, Any>()


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)
            rootView = binding.root

            initialise()
            listeners()
            setEditInformation()
            bindObservers()
        }
        return rootView
    }


    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        viewModelUpload = ViewModelProvider(this, viewModelFactory)[UploadFileViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
        progressDialogImage = ProgressDialogImage(requireActivity())

        binding.cbTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.cbTerms.setText(setAcceptTerms(requireActivity()), TextView.BufferType.SPANNABLE)
    }

    private fun setEditInformation() {
        editTextScroll(binding.etBio)
        if (arguments?.containsKey(UPDATE_PROFILE) == true) {
            binding.tvTitle.text = getString(R.string.update)

            val userData = userRepository.getUser()

            binding.etName.setText(userData?.name ?: "")
            binding.etBio.setText(userData?.profile?.bio ?: "")
            binding.etEmail.setText(userData?.email ?: "")

            if (!userData?.profile?.dob.isNullOrEmpty())
                binding.etDob.setText(DateUtils.dateFormatChange(
                        DateFormat.DATE_FORMAT,
                        DateFormat.DATE_FORMAT_SLASH, userData?.profile?.dob ?: ""))

            loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)

            binding.ilPassword.gone()
            binding.ilConfirmPassword.gone()
            binding.tvAlreadyRegister.gone()
            binding.tvLogin.gone()
            binding.cbTerms.gone()
            binding.ccpCountryCode.gone()
            binding.ivLine.gone()
            binding.etMobileNumber.gone()
            binding.ivLine1.gone()

            isUpdate = true
        } else if (arguments?.containsKey(UPDATE_NUMBER) == true) {
            binding.ilPassword.gone()
            binding.ilConfirmPassword.gone()
            binding.tvAlreadyRegister.gone()
            binding.tvLogin.gone()
            binding.cbTerms.gone()

            binding.ccpCountryCode.gone()
            binding.ivLine.gone()
            binding.etMobileNumber.gone()
            binding.ivLine1.gone()

            isUpdate = true
        }
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (arguments?.containsKey(UPDATE_PROFILE) == true)
                requireActivity().finish()
            else if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.etDob.setOnClickListener {
            DateUtils.openDatePicker(requireActivity(), this, true, false)
        }

        binding.tvLogin.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            replaceFragment(
                    requireActivity().supportFragmentManager,
                    LoginFragment(), R.id.container
            )

        }

        binding.ivNext.setOnClickListener {
            checkValidation()
        }

        binding.ivPic.setOnClickListener {
            getStorageWithPermissionCheck()
        }
    }

    private fun checkValidation() {
        when {
            binding.etName.text.toString().trim().isEmpty() -> {
                binding.etName.showSnackBar(getString(R.string.enter_name))
            }
            /*binding.etDob.text.toString().isEmpty() -> {
                binding.etDob.showSnackBar(getString(R.string.select_dob))
            }*/
            (binding.etMobileNumber.visibility == View.VISIBLE &&
                    (binding.etMobileNumber.text.toString().isEmpty() || binding.etMobileNumber.text.toString().length < 6)) -> {
                binding.etEmail.showSnackBar(getString(R.string.enter_phone_number))
            }
            (!isUpdate && binding.etEmail.text.toString().trim().isEmpty()) -> {
                binding.etEmail.showSnackBar(getString(R.string.enter_email))
            }
            (binding.etEmail.text.toString().trim().isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(
                    binding.etEmail.text.toString().trim()).matches()) -> {
                binding.etEmail.showSnackBar(getString(R.string.enter_correct_email))
            }
            (!isUpdate && binding.etPassword.text.toString().trim().length < 8) -> {
                binding.etPassword.showSnackBar(getString(R.string.enter_password))
            }
            binding.cbTerms.visibility == View.VISIBLE && !binding.cbTerms.isChecked -> {
                binding.cbTerms.showSnackBar(getString(R.string.check_all_terms))
            }
            isConnectedToInternet(requireContext(), true) -> {
                if (fileToUpload != null && fileToUpload?.exists() == true) {
                    uploadFileOnServer(fileToUpload)
                } else {
                    hitApi(null)
                }
            }
        }
    }

    private fun uploadFileOnServer(fileToUpload: File?) {
        val hashMap = HashMap<String, RequestBody>()

        hashMap["type"] = getRequestBody(DocType.IMAGE)

        val body: RequestBody =
                RequestBody.create(MediaType.parse("text/plain"), fileToUpload)
        hashMap["image\"; fileName=\"" + fileToUpload?.name] = body

        viewModelUpload.uploadFile(hashMap)

    }

    private fun hitApi(image: String?) {
        hashMap = HashMap()
        hashMap["name"] = binding.etName.text.toString().trim()
        if (binding.etMobileNumber.text.toString().trim().isNotEmpty()) {
            hashMap["country_code"] = binding.ccpCountryCode.selectedCountryCodeWithPlus
            hashMap["phone"] = binding.etMobileNumber.text.toString()
        }

        if (image != null)
            hashMap["profile_image"] = image

        if (binding.etEmail.text.toString().trim().isNotEmpty())
            hashMap["email"] = binding.etEmail.text.toString().trim()

        /*Update profile or register*/
        when {
            arguments?.containsKey(UPDATE_NUMBER) == true -> {
                viewModel.updateProfile(hashMap)
            }
            arguments?.containsKey(UPDATE_PROFILE) == true -> {
                viewModel.updateProfile(hashMap)
            }
            else -> {
                val hashMapOtp = HashMap<String, Any>()
                hashMapOtp["email"] = binding.etEmail.text.toString().trim()
                viewModel.sendEmailOtp(hashMapOtp)
            }
        }
    }


    private fun bindObservers() {
        viewModel.sendEmailOtp.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    hashMap["password"] = binding.etPassword.text.toString().trim()
                    hashMap["user_type"] = APP_TYPE

                    val fragment = VerifyOTPFragment()
                    val bundle = Bundle()
                    bundle.putString(VerifyOTPFragment.EXTRA_EMAIL, binding.etEmail.text.toString().trim())
                    bundle.putSerializable(VerifyOTPFragment.EXTRA_EMAIL_DATA, hashMap)
                    fragment.arguments = bundle

                    replaceFragment(requireActivity().supportFragmentManager,
                            fragment, R.id.container)

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

        viewModelUpload.uploadFile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialogImage.setLoading(false)

                    hitApi(it.data?.image_name ?: "")
                }
                Status.ERROR -> {
                    progressDialogImage.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialogImage.setLoading(true)

                }
            }
        })

        viewModel.updateProfile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

//                    startActivity(Intent(requireContext(), MainActivity::class.java))

                    requireActivity().setResult(Activity.RESULT_OK)
                    if (requireActivity().intent.hasExtra(UPDATE_PROFILE))
                        requireActivity().finish()
                    else if (arguments?.containsKey(UPDATE_NUMBER) == true) {
                        val fragment = MasterPrefrenceFragment()
                        val bundle = Bundle()
                        bundle.putString(MASTER_PREFRENCE_TYPE, PreferencesType.WORK_ENVIRONMENT)
                        fragment.arguments = bundle

                        replaceFragment(requireActivity().supportFragmentManager,
                                fragment, R.id.container)
                    }
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

    override fun onDateSelected(date: String) {
        binding.etDob.setText(date)
    }

    private fun selectImages() {
        FilePickerBuilder.instance
                .setMaxCount(1)
                .setActivityTheme(R.style.AppTheme)
                .setActivityTitle(getString(R.string.select_image))
                .enableVideoPicker(false)
                .enableCameraSupport(true)
                .showGifs(false)
                .showFolderView(true)
                .enableSelectAll(false)
                .enableImagePicker(true)
                .setCameraPlaceholder(R.drawable.ic_camera)
                .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .pickPhoto(this, AppRequestCode.IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == AppRequestCode.IMAGE_PICKER) {
                val docPaths = ArrayList<Uri>()
                docPaths.addAll(data?.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                        ?: emptyList())

                fileToUpload = File(getPathUri(requireContext(), docPaths[0]))
                Glide.with(requireContext()).load(fileToUpload).into(binding.ivPic)

            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getStorage() {
        selectImages()
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireContext(), R.string.media_permission, request)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(
                requireContext(), R.string.media_permission
        )
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(
                requireContext(), R.string.media_permission
        )
    }
}
