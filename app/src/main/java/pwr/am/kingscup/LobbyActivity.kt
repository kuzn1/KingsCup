package pwr.am.kingscup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import pwr.am.kingscup.databinding.ActivityLobbyBinding

class LobbyActivity : Activity() {
    private lateinit var binding: ActivityLobbyBinding
    private var owner : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        binding.idTextView.text = intent.getStringExtra("ID").toString()
        updatePlayers()
        binding.femaleCheckBox.isChecked = true
        owner = intent.getBooleanExtra("OWNER", false)

        if(owner) binding.startButton.visibility = View.VISIBLE

        setContentView(binding.root)
    }

    fun updatePlayers(){
        // todo set this function as listener on playerCount change
        // todo get player count from firebase
        val playerCount = 1
        binding.playerCountTextView.text = getString(R.string.playerCount, playerCount)
    }

    fun start(){
        Toast.makeText(applicationContext, "STARTING THE GAME", Toast.LENGTH_SHORT).show()
        // todo set this function as listener on gameStarted change
        if(intent.getStringExtra("OWNER").toBoolean()) {
            // todo change gameStarted to true in firebase
        }
        // todo start game activity
    }

    fun onClick(view: View) {
        when(view){
            binding.startButton-> start()
            binding.playersButton->
                startActivity(Intent(this, PlayerViewActivity::class.java).putExtra("OWNER", owner))
            binding.deckButton-> startActivity(Intent(this, CardViewActivity::class.java))
            binding.leaveButton-> {
                // todo send leave information to firebase
                startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            }
        }
    }

    fun onCheck(view: View) {
        when(view){
            binding.femaleCheckBox-> {
                binding.femaleCheckBox.isChecked = true
                binding.maleCheckBox.isChecked = false
                // todo send player gender to firebase
            }
            binding.maleCheckBox-> {
                binding.maleCheckBox.isChecked = true
                binding.femaleCheckBox.isChecked = false
                // todo send player gender to firebase
            }
        }
    }

}
