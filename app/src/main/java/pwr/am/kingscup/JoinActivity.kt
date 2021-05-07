package pwr.am.kingscup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import pwr.am.kingscup.databinding.ActivityJoinBinding

class JoinActivity : Activity() {
    private lateinit var binding: ActivityJoinBinding

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onClick(view: View) {
        when(view){
            binding.joinButton-> {
                val id = binding.textInput.editableText.toString()
                if(id == "DEVDEV") { //todo search in firebase if game exists
                    //todo register player in game in firebase
                    val intent = Intent(this, LobbyActivity::class.java)
                    intent.putExtra("ID", id)
                    intent.putExtra("OWNER", false)
                    startActivity(intent)
                } else Toast.makeText(applicationContext, "Wrong ID",Toast.LENGTH_SHORT).show()
            }
            binding.backButton -> finish()
        }
    }
}
