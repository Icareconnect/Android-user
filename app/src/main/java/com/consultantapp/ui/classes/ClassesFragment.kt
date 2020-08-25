package com.consultantapp.ui.classes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Categories
import com.consultantapp.data.models.responses.ClassData
import com.consultantapp.data.models.responses.JitsiClass
import com.consultantapp.data.network.ApiKeys.AFTER
import com.consultantapp.data.network.ApiKeys.PER_PAGE
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.PER_PAGE_LOAD
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityListingToolbarBinding
import com.consultantapp.ui.classes.ClassesDetailFragment.Companion.CLASS_DATA
import com.consultantapp.ui.dashboard.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
import com.consultantapp.ui.jitsimeet.JitsiActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.item_no_data.view.*
import javax.inject.Inject

class ClassesFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityListingToolbarBinding

    private var rootView: View? = null

    private lateinit var viewModel: ClassesViewModel

    private lateinit var progressDialog: ProgressDialog

    private var items = ArrayList<ClassData>()

    private lateinit var adapter: ClassesAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private var classSelectedData: ClassData? = null

    private var categoryData: Categories? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.activity_listing_toolbar, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        categoryData = arguments?.getSerializable(CATEGORY_PARENT_ID) as Categories
        binding.tvTitle.text = categoryData?.name

        binding.clNoData.ivNoData.setImageResource(R.drawable.ic_requests_empty_state)
        binding.clNoData.tvNoData.text = getString(R.string.no_classes)
        binding.clNoData.tvNoDataDesc.text = getString(R.string.no_classes_desc)
    }

    private fun setAdapter() {
        adapter = ClassesAdapter(this, items)
        binding.rvListing.adapter = adapter
        binding.rvListing.itemAnimator = null
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            hitApi(true)
        }


        binding.rvListing.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvListing.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMoreItems && !isLastPage && lastVisibleItemPosition >= totalItemCount) {
                    isLoadingMoreItems = true
                    hitApi(false)
                }
            }
        })
    }

    private fun hitApi(firstHit: Boolean) {
        if (firstHit) {
            isFirstPage = true
            isLastPage = false
        }

        val hashMap = HashMap<String, String>()
        if (isConnectedToInternet(requireContext(), true)) {
            if (!isFirstPage && items.isNotEmpty())
                hashMap[AFTER] = items[items.size - 1].id ?: ""

            hashMap[PER_PAGE] = PER_PAGE_LOAD.toString()
            hashMap["type"] = "USER_SIDE"
            hashMap["CategoryId"] = categoryData?.id ?: ""

            viewModel.classesList(hashMap)
        }
    }

    override fun onResume() {
        super.onResume()
        hitApi(true)
    }

    private fun bindObservers() {
        viewModel.classes.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    isLoadingMoreItems = false

                    val tempList = it.data?.classes ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                        items.addAll(tempList)

                        adapter.notifyDataSetChanged()
                    } else {
                        val oldSize = items.size
                        items.addAll(tempList)

                        adapter.notifyItemRangeInserted(oldSize, items.size)
                    }

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.clNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)

                    binding.swipeRefresh.isRefreshing = false
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!isLoadingMoreItems && !binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                }
            }
        })

        viewModel.enrollUser.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    hitApi(true)
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
                    jitsiClass.id = classSelectedData?.id
                    jitsiClass.name = classSelectedData?.name
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

    fun clickItem(item: ClassData?) {
        val fragment = ClassesDetailFragment()
        val bundle = Bundle()
        bundle.putSerializable(CLASS_DATA, item)
        fragment.arguments = bundle
        replaceFragment(requireActivity().supportFragmentManager,
                fragment, R.id.container)
    }

    fun startCall(item: ClassData?) {
        classSelectedData = item

        if (classSelectedData?.isOccupied == false) {
            AlertDialogUtil.instance.createOkCancelDialog(requireActivity(), R.string.occupy_class,
                    R.string.occupy_class_message, R.string.occupy_class, R.string.cancel, false,
                    object : AlertDialogUtil.OnOkCancelDialogListener {
                        override fun onOkButtonClicked() {
                            if (isConnectedToInternet(requireContext(), true)) {
                                val hashMap = HashMap<String, String>()
                                hashMap["class_id"] = classSelectedData?.id ?: ""
                                viewModel.enrollUser(hashMap)
                            }
                        }

                        override fun onCancelButtonClicked() {
                        }
                    }).show()
        } else if (classSelectedData?.isOccupied == true) {
            if (isConnectedToInternet(requireContext(), true)) {
                val hashMap = HashMap<String, String>()
                hashMap["class_id"] = classSelectedData?.id ?: ""
                viewModel.joinClass(hashMap)
            }
        } else if (classSelectedData?.status == ClassType.ADDED) {
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
}
