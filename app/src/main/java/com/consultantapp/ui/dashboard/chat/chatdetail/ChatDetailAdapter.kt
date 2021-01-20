package com.consultantapp.ui.dashboard.chat.chatdetail


import android.app.Activity
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.chat.ChatMessage
import com.consultantapp.data.network.Config
import com.consultantapp.databinding.*
import com.consultantapp.utils.*
import com.consultantapp.utils.DateUtils.dateFormatFromMillis
import java.util.*

class ChatDetailAdapter(private var context: ChatDetailActivity,
                        private var data: ArrayList<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TEXT_LEFT = 0
        private const val TEXT_RIGHT = 1
        private const val IMAGE_LEFT = 2
        private const val IMAGE_RIGHT = 3
        private const val TYPING = 4
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].messageType == DocType.MESSAGE_TYPING) {
            TYPING
        } else if (data[position].receiverId == context.userRepository.getUser()?.id) {
            if (data[position].messageType == DocType.TEXT) {
                TEXT_LEFT
            } else {
                IMAGE_LEFT
            }
        } else {
            if (data[position].messageType == DocType.TEXT) {
                TEXT_RIGHT
            } else {
                IMAGE_RIGHT
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): RecyclerView.ViewHolder {
        return when (position) {

            TEXT_RIGHT -> {
                ViewHolderTextRight(
                    DataBindingUtil.inflate(LayoutInflater
                        .from(context), R.layout.item_chat_text_right, viewGroup, false))

            }

            TEXT_LEFT -> {
                ViewHolderTextLeft(
                    DataBindingUtil.inflate(LayoutInflater
                        .from(context),R.layout.item_chat_text_left, viewGroup, false))
            }

            IMAGE_RIGHT -> {
                ViewHolderImageRight(
                    DataBindingUtil.inflate(LayoutInflater
                        .from(context),R.layout.item_chat_image_right, viewGroup, false))
            }

            IMAGE_LEFT -> {
                ViewHolderImageLeft(
                    DataBindingUtil.inflate(LayoutInflater
                        .from(context),R.layout.item_chat_image_left, viewGroup, false))
            }

            TYPING -> {
                ViewHolderType(
                    DataBindingUtil.inflate(LayoutInflater
                        .from(context), R.layout.item_chat_typing, viewGroup, false))
            }


            else ->  ViewHolderTextRight(
                DataBindingUtil.inflate(LayoutInflater
                    .from(context), R.layout.item_chat_text_right, viewGroup, false))
        }
    }

    inner class ViewHolderType(val binding: ItemChatTypingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val showDateHeader: Boolean
        if (position == data.size - 1) {
            showDateHeader = true
        } else {
            val cal1 = Calendar.getInstance()
            cal1.timeInMillis = data[position + 1].sentAt ?: 0
            val cal2 = Calendar.getInstance()
            cal2.timeInMillis = data[position].sentAt ?: 0
            showDateHeader = !(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(
                Calendar.DAY_OF_YEAR))
        }
        when (holder) {
            is ViewHolderTextRight -> holder.bind(data[position], showDateHeader)
            is ViewHolderTextLeft -> holder.bind(data[position], showDateHeader)
            is ViewHolderImageRight -> holder.bind(data[position], showDateHeader)
            is ViewHolderImageLeft -> holder.bind(data[position], showDateHeader)
            is ViewHolderType -> holder.bind()
        }
    }


    inner class ViewHolderTextRight(val binding: ItemChatTextRightBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessage, showDateHeader: Boolean) = with(binding) {
            tvChatTextRight.text = chat.message
            timeRight.text = chat.sentAt?.let { DateUtils.formatDateTime(context, it, DateUtils.FORMAT_SHOW_TIME) }
            if (showDateHeader) {
                tvTimeRight.text = chat.sentAt?.let { getDateHeader(it) }
                tvTimeRight.visible()
            } else {
                tvTimeRight.gone()
            }

            ivTick.setImageResource(getTickValue(chat.status))
        }


    }

    inner class ViewHolderTextLeft(val binding: ItemChatTextLeftBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessage, showDateHeader: Boolean) = with(binding) {
            tvChatTextLeft.text = chat.message
            timeLeft.text = chat.sentAt?.let { DateUtils.formatDateTime(context, it, DateUtils.FORMAT_SHOW_TIME) }
            if (showDateHeader) {
                tvTimeLeft.text = chat.sentAt?.let { getDateHeader(it) }
                tvTimeLeft.visible()
            } else {
                tvTimeLeft.gone()
            }
        }
    }

    inner class ViewHolderImageRight(val binding: ItemChatImageRightBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessage, showDateHeader: Boolean) = with(binding) {
            mediaTimeRight.text = chat.sentAt?.let { DateUtils.formatDateTime(context, it, DateUtils.FORMAT_SHOW_TIME) }
            if (showDateHeader) {
                tvHeaderMediaRight.text = chat.sentAt?.let { getDateHeader(it) }
                tvHeaderMediaRight.visible()
            } else {
                tvHeaderMediaRight.gone()
            }
            loadImage(ivImageRight, chat.imageUrl, R.drawable.image_placeholder)

            /*Click*/
            ivImageRight.setOnClickListener {
                val itemImages = ArrayList<String>()
                itemImages.add("${Config.imageURL}uploads/${chat.imageUrl}")
                viewImageFull(context as Activity, itemImages, 0)
            }

            ivTick.setImageResource(getTickValue(chat.status))
        }
    }

    inner class ViewHolderImageLeft(val binding: ItemChatImageLeftBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessage, showDateHeader: Boolean) = with(binding) {
            mediaTimeLeft.text = chat.sentAt?.let { DateUtils.formatDateTime(context, it, DateUtils.FORMAT_SHOW_TIME) }
            if (showDateHeader) {
                tvHeaderMediaLeft.text = chat.sentAt?.let { getDateHeader(it) }
                tvHeaderMediaLeft.visible()
            } else {
                tvHeaderMediaLeft.gone()
            }
            loadImage(
                ivImageLeft, chat.imageUrl
                , R.drawable.image_placeholder
            )

            /*Click*/
            ivImageLeft.setOnClickListener {
                val itemImages = ArrayList<String>()
                itemImages.add("${Config.imageURL}${ImageFolder.UPLOADS}${chat.imageUrl}")
                viewImageFull(context as Activity, itemImages, 0)
            }
        }
    }

    private fun getDateHeader(millis: Long): String? {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        val dateString: String?
        dateString = when {
            DateUtils.isToday(calendar.timeInMillis) -> context.getString(R.string.today)
            isYesterday(calendar) -> String.format("%s", context.getString(R.string.yesterday))
            else -> dateFormatFromMillis(DateFormat.DATE_FORMAT, calendar.timeInMillis)
        }
        return dateString
    }

    private fun getTickValue(status: String?): Int {
        return when (status) {
            AppSocket.MessageStatus.NOT_SENT -> R.drawable.ic_wait
            AppSocket.MessageStatus.SENT -> R.drawable.ic_sent
            AppSocket.MessageStatus.DELIVERED -> R.drawable.ic_delivered
            AppSocket.MessageStatus.SEEN -> R.drawable.ic_seen
            else -> R.drawable.ic_wait
        }
    }
}



