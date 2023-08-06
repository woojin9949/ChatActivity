package fastcampus.projects.chatactivity.chatlist

import android.content.Intent
import android.os.Bundle
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
import fastcampus.projects.chatactivity.R
import fastcampus.projects.chatactivity.chatlist.chatdetail.ChatDetailActivity
import fastcampus.projects.chatactivity.databinding.FragmentChatlistBinding
import fastcampus.projects.chatactivity.databinding.FragmentUserlistBinding

class ChatListFragment : Fragment(R.layout.fragment_chatlist) {
    private lateinit var binding: FragmentChatlistBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentChatlistBinding.bind(view)
        val chatListAdapter = ChatListAdapter {
            //클릭한 유저의 아이디와 챗룸 아이디를 넣음
            val intent = Intent(context, ChatDetailActivity::class.java)
            intent.putExtra("otherUserId", it.otherUserId)
            intent.putExtra("chatRoomId", it.chatRoomId)
            startActivity(intent)
        }


        binding.chatListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }
        //파이어베이스의 uid를 가져와서 챗룸에 대한 정보를 받아 올 수 있게 설정
        val myUserId = Firebase.auth.currentUser?.uid ?: return
        val chatRoomsDB = Firebase.database.reference.child(DB_CHATROOMS).child(myUserId)
        
        //챗룸에서 받아온 정보를 ChatRoomItem 형식으로 변환
        chatRoomsDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomList = snapshot.children.map {
                    it.getValue(ChatRoomItem::class.java)
                }
                chatListAdapter.submitList(chatRoomList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}