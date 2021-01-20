package com.consultantapp.ui.drawermenu.addmoney

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantapp.R
import com.consultantapp.appClientDetails
import com.consultantapp.data.models.responses.Wallet
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.PushType
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentAddMoenyBinding
import com.consultantapp.ui.dashboard.DoctorViewModel
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.ui.drawermenu.addmoney.AddCardFragment.Companion.CARD_DETAILS
import com.consultantapp.ui.drawermenu.wallet.WalletViewModel
import com.consultantapp.ui.webview.WebViewActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import dagger.android.support.DaggerAppCompatActivity
import org.json.JSONObject
import javax.inject.Inject

class AddMoneyActivity : DaggerAppCompatActivity(), PaymentResultListener {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentAddMoenyBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: WalletViewModel

    private lateinit var viewModelDoctor: DoctorViewModel

    private lateinit var adapter: CardsAdapter

    private var items = ArrayList<Wallet>()

    var selectedCardId = ""

    private var paymentFrom = PaymentFrom.STRIPE

    private var isReceiverRegistered = false

    private var transactionId = ""

    private val mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_add_moeny)
        initialise()
        listeners()
        bindObservers()
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[WalletViewModel::class.java]
        viewModelDoctor = ViewModelProvider(this, viewModelFactory)[DoctorViewModel::class.java]
        progressDialog = ProgressDialog(this)
        binding.tvSymbol.text = getCurrencySymbol()

        if (intent.hasExtra(EXTRA_PRICE)) {
            binding.tvTitle.visible()
            binding.tvSymbol.visible()
            binding.etAmount.visible()
            binding.etAmountBg.visible()
            binding.tvPay.visible()
            binding.etAmount.setText(intent.getStringExtra(EXTRA_PRICE))
            binding.etAmount.isFocusable = false
            binding.etAmount.isClickable = false
        }

        when (paymentFrom) {
            PaymentFrom.STRIPE -> {
                binding.tvAddCard.visible()
                binding.tvSelectCard.visible()
                binding.rvListing.visible()
                setAdapter()
                viewModel.cardListing(HashMap())
            }
            PaymentFrom.RAZOR_PAY -> {
                binding.tvAddCard.gone()
                loadRazorPay()
            }
            PaymentFrom.CCA_VENUE -> {
                binding.tvAddCard.gone()
            }
        }
    }

    private fun setAdapter() {
        adapter = CardsAdapter(this, items)
        binding.rvListing.adapter = adapter
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.tvAddCard.setOnClickListener {
            disableButton(binding.tvAddCard)

            startActivityForResult(Intent(this, DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.ADD_CARD), AppRequestCode.ADD_CARD)

        }

        binding.tvPay.setOnClickListener {
            disableButton(binding.tvPay)
            when {
                binding.etAmount.text.toString().isEmpty() -> {
                    binding.etAmount.showSnackBar(getString(R.string.enter_amount))
                }
                paymentFrom == PaymentFrom.STRIPE && selectedCardId.isEmpty() -> {
                    binding.etAmount.showSnackBar(getString(R.string.select_card))
                }
                else -> {
                    if (isConnectedToInternet(this, true)) {
                        when (paymentFrom) {
                            PaymentFrom.STRIPE -> {
                                if (intent.hasExtra(EXTRA_REQUEST_ID)) {
                                    val hashMap = intent.getSerializableExtra(EXTRA_REQUEST_ID) as HashMap<String, Any>
                                    hashMap["request_step"] = "create"
                                    hashMap["card_id"] = selectedCardId
                                    viewModelDoctor.createRequest(hashMap)
                                } else {
                                    val hashMap = HashMap<String, Any>()
                                    hashMap["balance"] = binding.etAmount.text.toString()
                                    hashMap["card_id"] = selectedCardId
                                    viewModel.addMoney(hashMap)
                                }
                            }
                            PaymentFrom.RAZOR_PAY -> {
                                val hashMap = HashMap<String, String>()

                                val amount = (binding.etAmount.text.toString().toInt()) * 100
                                hashMap["balance"] = amount.toString()
                                viewModel.razorPayCreateOrder(hashMap)
                            }

                            PaymentFrom.CCA_VENUE -> {
                                //startActivity(Intent(this, InitialScreenActivity::class.java))
                            }
                        }
                    }
                }
            }
        }
    }


    private fun bindObservers() {
        viewModel.addMoney.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    /*If need authontication*/
                    if (it.data?.requires_source_action == true) {
                        startActivityForResult(Intent(this, WebViewActivity::class.java)
                                .putExtra(WebViewActivity.LINK_TITLE, getString(R.string.payment))
                                .putExtra(WebViewActivity.PAYMENT_URL, it.data.url)
                                .putExtra(EXTRA_REQUEST_ID, it.data.transaction_id), AppRequestCode.ADD_MONEY)
                    } else {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }

                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

        viewModel.cardListing.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()

                    items.clear()
                    items.addAll(it.data?.cards ?: emptyList())
                    if (items.isNotEmpty()) {
                        items[0].isSelected = true
                        selectedCardId = items[0].id ?: ""
                    }

                    adapter.notifyDataSetChanged()


                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    adapter.setAllItemsLoaded(true)

                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                }
            }
        })

        viewModel.orderCreate.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    if (it.data?.order_id?.isNotEmpty() == true)
                        startRazorPayPayment(binding.etAmount.text.toString(), it.data.order_id
                                ?: "")
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

        viewModel.deleteCard.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    viewModel.cardListing(HashMap())
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

        viewModelDoctor.createRequest.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    transactionId = it.data?.transaction_id ?: ""
                    binding.tvStatus.visible()

                    keepCheckingRequestStatus()
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

        viewModelDoctor.requestCheck.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    if (it.data?.isRequestCreated == true) {
                        mHandler.removeCallbacksAndMessages(null)
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else
                        keepCheckingRequestStatus()
                }
                Status.ERROR -> {
                    binding.tvStatus.gone()
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                }
            }
        })
    }

    private fun keepCheckingRequestStatus() {
        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed({
            if (isConnectedToInternet(this, true)) {
                val hashMap = HashMap<String, String>()
                hashMap["transaction_id"] = transactionId
                viewModelDoctor.requestCheck(hashMap)
            }
        }, 5000)
    }

    fun editCard(item: Wallet) {
        startActivityForResult(Intent(this, DrawerActivity::class.java)
                .putExtra(PAGE_TO_OPEN, DrawerActivity.ADD_CARD)
                .putExtra(CARD_DETAILS, item), AppRequestCode.ADD_MONEY)
    }

    fun deleteCard(item: Wallet) {
        AlertDialogUtil.instance.createOkCancelDialog(
                this, R.string.delete,
                R.string.delete_message, R.string.delete, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        if (isConnectedToInternet(this@AddMoneyActivity, true)) {
                            val hashMap = HashMap<String, Any>()

                            hashMap["card_id"] = item.id ?: ""
                            viewModel.deleteCard(hashMap)
                        }
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ADD_CARD) {
                viewModel.cardListing(HashMap())
            } else if (requestCode == AppRequestCode.ADD_MONEY) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }


    val TAG: String = AddMoneyActivity::class.toString()

    private fun loadRazorPay() {
        /*
        * To ensure faster loading of the Checkout form,
        * call this method as early as possible in your checkout flow
        * */
        Checkout.preload(this)
    }

    private fun startRazorPayPayment(amount: String, orderId: String) {
        /*
        *  You need to pass current activity in order to let Razorpay create CheckoutActivity
        * */
        val activity: Activity = this
        val co = Checkout()
        co.setKeyID(appClientDetails.razorKey ?: "rzp_test_NIJ8Fwm7fvVNDU")

        try {
            val userData = userRepository.getUser()

            val options = JSONObject()
            options.put("name", "Razorpay Corp")
            options.put("description", "Demoing Charges")
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("currency", "INR")
            options.put("order_id", orderId)
            options.put("amount", (amount.toInt() * 100))

            options.put("user_id", userData?.id)
            val prefill = JSONObject()
            prefill.put("email", userData?.email)
            prefill.put("contact", userData?.phone)

            options.put("prefill", prefill)
            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        try {
            Toast.makeText(this,
                    "Payment failed $errorCode \n $response", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        try {
            Toast.makeText(this, "Payment Successful $razorpayPaymentId",
                    Toast.LENGTH_LONG).show()

            setResult(Activity.RESULT_OK)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(PushType.BOOKING_RESERVED)
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    callCancelledReceiver, intentFilter
            )
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(callCancelledReceiver)
            isReceiverRegistered = false
        }
    }

    private val callCancelledReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra(EXTRA_TRANSACTION_ID) == transactionId) {
                when (intent.action) {
                    PushType.BOOKING_RESERVED -> {
                        mHandler.removeCallbacksAndMessages(null)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }
}