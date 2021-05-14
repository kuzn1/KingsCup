package pwr.am.kingscup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.databinding.ActivityJoinBinding
import kotlin.math.log

class JoinActivity : Activity() {
    private lateinit var binding: ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onClick(view: View) {
        when (view) {
            binding.joinButton -> {
                val id = binding.textInput.editableText.toString()
                Firebase.database.getReference("openGames").child(id).get().addOnSuccessListener {
                    if (it.child("gameCode").value == null || id.length < 6) {
                        Toast.makeText(applicationContext, "Wrong ID", Toast.LENGTH_SHORT).show()
                    } else {
                        //Log.e("mytesting", it.child("gameCode").value.toString())
                        //Log.e("mytesting", it.child("gameKey").value.toString())
                        val intent = Intent(this, LobbyActivity::class.java)
                        intent.putExtra("gameCode", it.child("gameCode").value.toString())
                        intent.putExtra("gameKey", it.child("gameKey").value.toString())
                        intent.putExtra("OWNER", false)
                        startActivity(intent)
                    }
                }
            }
            binding.backButton -> finish()
        }
    }
}
