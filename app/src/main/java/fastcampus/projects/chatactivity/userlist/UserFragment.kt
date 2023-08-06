package fastcampus.projects.chatactivity.userlist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fastcampus.projects.chatactivity.Key.Companion.DB_CHATROOMS
import fastcampus.projects.chatactivity.Key.Companion.DB_USERS
import fastcampus.projects.chatactivity.R
import fastcampus.projects.chatactivity.chatlist.ChatRoomItem
import fastcampus.projects.chatactivity.chatlist.chatdetail.ChatDetailActivity
import fastcampus.projects.chatactivity.databinding.FragmentUserlistBinding
import java.util.UUID

class UserFragment : Fragment(R.layout.fragment_userlist) {
    private lateinit var binding: FragmentUserlistBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding = FragmentUserlistBinding.bind(view)
        //유저 클릭시 채팅방으로 이동하게 정보 받아오기 및 설정
        val userListAdapter = UserAdapter { otherUser ->
            //기본값 가져오기
            val myUserId = Firebase.auth.currentUser?.uid ?: ""
            val chatRoomsDB =
                Firebase.database.reference.child(DB_CHATROOMS).child(myUserId)
                    .child(otherUser.userId ?: "")

            chatRoomsDB.get().addOnSuccessListener {
                //챗룸 있는지 없는지 파악 후, 챗룸 생성 및 챗룸 아이디와 상대방 아이디 intent 설정
                var chatRoomId = ""
                if (it.value != null) {
                    //데이터가 존재 (메시지)
                    val chatRoom = it.getValue(ChatRoomItem::class.java)
                    chatRoomId = chatRoom?.chatRoomId ?: ""
                } else {
                    //데이터 없으므로 chatRoom생성하여 id값도 생성하여 넣음
                    chatRoomId = UUID.randomUUID().toString()
                    val newChatRoom = ChatRoomItem(
                        chatRoomId = chatRoomId,
                        otherUserName = otherUser.username,
                        otherUserId = otherUser.userId
                    )
                    chatRoomsDB.setValue(newChatRoom)
                }
                val intent = Intent(context, ChatDetailActivity::class.java)

                //상수로 정해두는것이 좋음
                intent.putExtra("otherUserId", otherUser.userId)
                intent.putExtra("chatRoomId", chatRoomId)

                startActivity(intent)
            }
            //"ChatRooms/myUserId/otherUserId"
        }
        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userListAdapter
        }

        val currentId = Firebase.auth.currentUser?.uid
        
        //리스트를 한번만 불러오면 되기 때문에 singleValueEvent 사용
        Firebase.database.reference.child(DB_USERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val userItemList = mutableListOf<UserItem>()

                    snapshot.children.forEach {
                        val user = it.getValue(UserItem::class.java)

                        //자신을 제외한 유저들만 나오게 구현
                        if (user?.userId != currentId) {
                            userItemList.add(user!!)
                        }
                    }
                    userListAdapter.submitList(userItemList)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
}