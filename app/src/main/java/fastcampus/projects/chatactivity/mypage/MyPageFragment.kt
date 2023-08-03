package fastcampus.projects.chatactivity.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fastcampus.projects.chatactivity.LoginActivity
import fastcampus.projects.chatactivity.R
import fastcampus.projects.chatactivity.databinding.FragmentMypageBinding

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var binding: FragmentMypageBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMypageBinding.bind(view)

        binding.applyButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val status = binding.statusEditText.text.toString()
            if (username.isEmpty()) {
                Toast.makeText(context, "유저이름은 빈 값으로 둘 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //todo FireBase realtime database update
        }
        binding.logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finish()
        }
    }
}