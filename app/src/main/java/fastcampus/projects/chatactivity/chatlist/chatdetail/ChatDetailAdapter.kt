package fastcampus.projects.chatactivity.chatlist.chatdetail

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fastcampus.projects.chatactivity.databinding.ItemChatBinding
import fastcampus.projects.chatactivity.databinding.ItemChatroomBinding
import fastcampus.projects.chatactivity.userlist.UserItem

class ChatDetailAdapter : ListAdapter<ChatDetailItem, ChatDetailAdapter.ViewHolder>(diffUtil) {

    var otherUserItem: UserItem? = null


    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatDetailItem) {
            if (item.userId == otherUserItem?.userId) {
                binding.usernameTextView.isVisible = true
                binding.usernameTextView.text = otherUserItem?.username
                binding.messageTextView.text = item.message
                binding.messageTextView.gravity = Gravity.START
            } else {
                binding.usernameTextView.isVisible = false
                binding.messageTextView.text = item.message
                binding.messageTextView.gravity = Gravity.END
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ChatDetailItem>() {
            override fun areItemsTheSame(
                oldItem: ChatDetailItem,
                newItem: ChatDetailItem
            ): Boolean {
                return oldItem.chatId == newItem.chatId
            }

            override fun areContentsTheSame(
                oldItem: ChatDetailItem,
                newItem: ChatDetailItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}