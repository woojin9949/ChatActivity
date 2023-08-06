package fastcampus.projects.chatactivity.chatlist.chatdetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fastcampus.projects.chatactivity.Key.Companion.DB_CHATROOMS
import fastcampus.projects.chatactivity.Key.Companion.DB_CHATS
import fastcampus.projects.chatactivity.Key.Companion.DB_USERS
import fastcampus.projects.chatactivity.R
import fastcampus.projects.chatactivity.databinding.ActivityChatDetailBinding
import fastcampus.projects.chatactivity.userlist.UserItem

class ChatDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatDetailBinding
    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""

    private val chatItemList = mutableListOf<ChatDetailItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatRoomId = intent.getStringExtra("chatRoomId") ?: return
        otherUserId = intent.getStringExtra("otherUserId") ?: return

        myUserId = Firebase.auth.currentUser?.uid ?: ""

        val chatDetailAdapter = ChatDetailAdapter()

        Firebase.database.reference.child(DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.username ?: ""
            }

        Firebase.database.reference.child(DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)
                chatDetailAdapter.otherUserItem = otherUserItem
            }


        Firebase.database.reference.child(DB_CHATS).child(chatRoomId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatDetailItem = snapshot.getValue(ChatDetailItem::class.java)
                    chatDetailItem ?: return

                    chatItemList.add(chatDetailItem)
                    
                    //리스트를 업데이트를 해야하기 때문에 사용
                    chatDetailAdapter.submitList(chatItemList.toMutableList())
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatDetailAdapter
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "빈 메시지 전송 불가", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newChatItem = ChatDetailItem(
                message = message,
                userId = myUserId,
            )
            Firebase.database.reference.child(DB_CHATS).child(chatRoomId).push().apply {
                newChatItem.chatId = key //push단에서는 key를 통해 생성해준다(getKey())
                setValue(newChatItem)
            }

            val updates: MutableMap<String, Any> = hashMapOf(
                "$DB_CHATROOMS/$myUserId/$otherUserId/lastMessage" to message,
                "$DB_CHATROOMS/$otherUserId/$myUserId/lastMessage" to message,
                "$DB_CHATROOMS/$otherUserId/$myUserId/chatRoomId" to chatRoomId,
                "$DB_CHATROOMS/$otherUserId/$myUserId/otherUserId" to myUserId,
                "$DB_CHATROOMS/$otherUserId/$myUserId/otherUserName" to myUserName,
            )
            Firebase.database.reference.updateChildren(updates)

            binding.messageEditText.text.clear()
        }
    }
}