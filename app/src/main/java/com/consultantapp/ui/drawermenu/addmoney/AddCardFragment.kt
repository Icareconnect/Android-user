package com.consultantapp.ui.drawermenu.addmoney

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.Wallet
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.databinding.FragmentAddCardBinding
import com.consultantapp.ui.drawermenu.wallet.WalletViewModel
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class AddCardFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentAddCardBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: WalletViewModel

    private var cardDetails: Wallet? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding =
                    DataBindingUtil.inflate(inflater, R.layout.fragment_add_card, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[WalletViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        val card = binding.cardForm

        if (requireActivity().intent?.hasExtra(CARD_DETAILS) == true) {
            binding.tvHeader.text = getString(R.string.edit_card)
            cardDetails = requireActivity().intent?.getSerializableExtra(CARD_DETAILS) as Wallet
            card.cardRequired(false)
                    .cvvRequired(false)

            binding.tvCardDetails.visible()
            binding.tvCardDetails.text = getString(R.string.card_ending_with,
                    cardDetails?.card_brand, cardDetails?.last_four_digit)
            binding.btnAddCard.text = getString(R.string.update)
        } else {
            card.cardRequired(true)
                    .cvvRequired(true)
        }

        card.expirationRequired(true)
                .setup(requireActivity() as AppCompatActivity)
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.btnAddCard.setOnClickListener {
            if (binding.cardForm.isValid) {
                disableButton(binding.btnAddCard)
                if (isConnectedToInternet(requireContext(), true)) {
                    val hashMap = HashMap<String, Any>()

                    hashMap["exp_month"] = binding.cardForm.expirationMonth
                    hashMap["exp_year"] = binding.cardForm.expirationYear

                    if (arguments?.containsKey(CARD_DETAILS) == true) {
                        hashMap["card_id"] = cardDetails?.id ?: ""
                        viewModel.updateCard(hashMap)
                    } else {
                        hashMap["card_number"] = binding.cardForm.cardNumber
                        hashMap["cvc"] = binding.cardForm.cvv
                        viewModel.addCard(hashMap)
                    }


                }
            }
        }
    }


    private fun bindObservers() {
        viewModel.addCard.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
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

        viewModel.updateCard.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    resultFragmentIntent(this, targetFragment ?: this,
                            AppRequestCode.ADD_MONEY, null)
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

    companion object {
        const val CARD_DETAILS = "CARD_DETAILS"
    }
}