package fastcampus.projects.chatactivity.chatlist.chatdetail

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ChatDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatDetailBinding
    private lateinit var chatDetailAdapter: ChatDetailAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var otherUserFcmToken: String = ""
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

        chatDetailAdapter = ChatDetailAdapter()
        linearLayoutManager = LinearLayoutManager(applicationContext)

        Firebase.database.reference.child(DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.username ?: ""

                //나의 정보를 얻는것이 성공 했을때!!
                getOtherUserData()
            }


        binding.chatRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = chatDetailAdapter
        }
        chatDetailAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                linearLayoutManager.smoothScrollToPosition(
                    binding.chatRecyclerView,
                    null,
                    chatDetailAdapter.itemCount
                )
            }
        })
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

            val client = OkHttpClient()
            val root = JSONObject()

            val notification = JSONObject()

            notification.put("title", getString(R.string.app_name))
            notification.put("body", message)

            root.put("to", otherUserFcmToken)
            root.put("notification", notification)
            root.put("priority", "high")


            val requestBody =
                root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request =
                Request.Builder().post(requestBody).url("https://fcm.googleapis.com/fcm/send")
                    .header("Authorization", "key=${R.string.fcm_server_key}").build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.stackTraceToString()
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.e("ChatDetailActivity", response.message)
                }
            })
            binding.messageEditText.text.clear()
        }
    }

    private fun getOtherUserData() {
        Firebase.database.reference.child(DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)
                otherUserFcmToken = otherUserItem?.fcmToken.orEmpty()
                chatDetailAdapter.otherUserItem = otherUserItem

                //전송 버튼을 누르기 전에 이뤄진다면 오류이므로 동기적으로 변경
                binding.sendButton.isEnabled = true

                //상대방 유저 데이터를 얻는게 성공 했을때!!
                getChatData()
            }
    }

    private fun getChatData() {
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
    }
}