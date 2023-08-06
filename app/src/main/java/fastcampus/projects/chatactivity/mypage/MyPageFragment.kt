package fastcampus.projects.chatactivity.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fastcampus.projects.chatactivity.Key.Companion.DB_USERS
import fastcampus.projects.chatactivity.LoginActivity
import fastcampus.projects.chatactivity.R
import fastcampus.projects.chatactivity.databinding.FragmentMypageBinding
import fastcampus.projects.chatactivity.userlist.UserItem

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var binding: FragmentMypageBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMypageBinding.bind(view)

        val myUserId = Firebase.auth.currentUser?.uid ?: ""
        val myUserDB = Firebase.database.reference.child(DB_USERS).child(myUserId)

        myUserDB.get().addOnSuccessListener {
            val currentUserItem = it.getValue(UserItem::class.java) ?: return@addOnSuccessListener
            binding.usernameEditText.setText(currentUserItem.username)
            binding.statusEditText.setText(currentUserItem.status)
        }
        binding.applyButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val status = binding.statusEditText.text.toString()
            if (username.isEmpty()) {
                Toast.makeText(context, "유저이름은 빈 값으로 둘 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = mutableMapOf<String, Any>()
            user["username"] = username
            user["status"] = status
            myUserDB.updateChildren(user)
        }
        binding.logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish()
        }
    }
}