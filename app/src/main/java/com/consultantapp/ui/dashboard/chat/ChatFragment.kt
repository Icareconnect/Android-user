package com.consultantapp.ui.dashboard.chat

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.chat.ChatList
import com.consultantapp.data.models.responses.chat.ChatMessage
import com.consultantapp.data.network.ApiKeys
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.PER_PAGE_LOAD
import com.consultantapp.data.network.PushType
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.databinding.FragmentChatBinding
import com.consultantapp.ui.dashboard.chat.chatdetail.ChatDetailActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.AppRequestCode.REQ_CHAT
import com.consultantapp.utils.AppSocket.Events.DELIVERED_MESSAGE
import com.google.gson.Gson
import dagger.android.support.DaggerFragment
import io.socket.client.Ack
import kotlinx.android.synthetic.main.item_no_data.view.*
import org.json.JSONObject
import javax.inject.Inject

class ChatFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var appSocket: AppSocket

    private lateinit var binding: FragmentChatBinding

    private var rootView: View? = null

    private lateinit var adapter: ChatAdapter

    private lateinit var llm: LinearLayoutManager

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private lateinit var viewModel: ChatViewModel

    private var items = ArrayList<ChatList>()

    private var isReceiverRegistered = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
            rootView = binding.root

            viewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]
            bindObservers()
            listeners()
            setAdapter()
            //getListing(true)

            try {
                appSocket.addOnMessageReceiver(messageReceiver)
            } catch (e: Exception) {
                appSocket.init()
                appSocket.addOnMessageReceiver(messageReceiver)
            }

            LocalBroadcastManager.getInstance(activity as Context)
                .registerReceiver(broadcastChat, IntentFilter(UPDATE_CHAT))

            requireActivity().registerReceiver(
                broadcastReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )


            binding.clNoData.ivNoData.setImageResource(R.drawable.ic_chat_empty)
            binding.clNoData.tvNoData.text = getString(R.string.no_chat)
            binding.clNoData.tvNoDataDesc.text = getString(R.string.no_chat_desc)
        }
        return rootView
    }


    private val messageReceiver = AppSocket.OnMessageReceiver { message ->
        refreshChatLogs(message, message.senderId, 1)
        sendMessageDelivered(message)
    }

    private fun sendMessageDelivered(message: ChatMessage?) {
        val obj = JSONObject()
        obj.put("messageId", message?.messageId)
        obj.put("receiverId", message?.senderId)
        obj.put("senderId", message?.receiverId)
        appSocket.emit(DELIVERED_MESSAGE, obj, Ack {

        })
    }

    private fun refreshChatLogs(message: ChatMessage, userId: String?, unDeliveredCount: Int = 0) {
        requireActivity().runOnUiThread {
            val index = items.indexOf(items.find {
                it.to_user?.id == userId
            })
            if (index == -1) {
                getListing(true)
            } else {
                if (message.request_id != null)
                    items[index].id = message.request_id

                val chatMessage = ChatMessage()
                chatMessage.message = message.message
                chatMessage.sentAt = message.sentAt

                items[index].last_message = chatMessage
                items[index].image = message.imageUrl
                items[index].messageType = message.messageType

                if (message.senderId != ChatDetailActivity.otherUserID)
                    items[index].unReadCount = items[index].unReadCount.plus(unDeliveredCount)

                items.sortByDescending {
                    it.last_message?.sentAt
                }
                adapter.notifyDataSetChanged()

            }
        }
    }


    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                getListing(true)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        try {
            appSocket.removeOnMessageReceiver(messageReceiver)
            LocalBroadcastManager.getInstance(activity as Context).unregisterReceiver(broadcastChat)
            requireActivity().unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
        }
    }


    fun hitApiRefresh() {
        getListing(true)
    }

    private fun getListing(firstHit: Boolean) {

        if (firstHit) {
            isFirstPage = true
            isLastPage = false
        }

        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            if (!isFirstPage && items.isNotEmpty())
                hashMap[ApiKeys.AFTER] = items[items.size - 1].id ?: ""

            hashMap[ApiKeys.PER_PAGE] = PER_PAGE_LOAD.toString()

            viewModel.getChatListing(hashMap)
        }
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            if (isConnectedToInternet(requireContext(), true)) {
                getListing(true)
            } else
                binding.swipeRefresh.isRefreshing = false
        }

        binding.rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvChat.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMoreItems && !isLastPage && lastVisibleItemPosition >= totalItemCount) {
                    isLoadingMoreItems = true
                    getListing(false)
                }
            }
        })

    }

    private fun bindObservers() {
        viewModel.chatListing.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    isLoadingMoreItems = false

                    val tempList = it.data?.lists ?: emptyList()
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

                    /*Set Message delivered*/
                    items.forEach {
                        if (it.last_message?.status != AppSocket.MessageStatus.SEEN &&
                            it.last_message?.status != AppSocket.MessageStatus.DELIVERED
                        ) {
                            /*If message is not read*/
                            sendMessageDelivered(it.last_message)
                        }
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
                    if (!binding.swipeRefresh.isRefreshing && !isLoadingMoreItems)
                        binding.clLoader.visible()
                }
            }
        })
    }

    private fun setAdapter() {
        llm = LinearLayoutManager(activity)
        binding.rvChat.layoutManager = llm

        adapter = ChatAdapter(this, items)
        binding.rvChat.adapter = adapter
        binding.rvChat.itemAnimator = null
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQ_CHAT) {
                if (data != null) {
                    val lastMsgData =
                        Gson().fromJson(data.getStringExtra(LAST_MESSAGE), ChatMessage::class.java)
                    val userId = data.getStringExtra(OTHER_USER_ID)
                    refreshChatLogs(lastMsgData, userId)
                }
            }
        }
    }

    fun startActivity(item: ChatList) {
        startActivityForResult(
            Intent(context, ChatDetailActivity::class.java)
                .putExtra(USER_ID, item.to_user?.id)
                .putExtra(USER_NAME, item.to_user?.name)
                .putExtra(EXTRA_REQUEST_ID, item.id)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), REQ_CHAT
        )
    }

    private val broadcastChat = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            getListing(true)
        }
    }


    override fun onResume() {
        super.onResume()
        registerReceiver()
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(PushType.REQUEST_COMPLETED)
            intentFilter.addAction(PushType.CHAT_STARTED)
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(refreshRequests, intentFilter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshRequests)
            isReceiverRegistered = false
        }
    }

    private val refreshRequests = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                PushType.REQUEST_COMPLETED, PushType.COMPLETED, PushType.CHAT_STARTED -> {
                    getListing(true)
                }
            }
        }
    }
}