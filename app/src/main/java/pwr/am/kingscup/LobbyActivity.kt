package pwr.am.kingscup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import pwr.am.kingscup.databinding.ActivityLobbyBinding

class LobbyActivity : Activity() {
    private lateinit var binding: ActivityLobbyBinding
    private var owner: Boolean = false
    private lateinit var server: Server
    private lateinit var lobby: Lobby
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        owner = intent.getBooleanExtra("OWNER", false)


        //owner creates game = run server
        if (owner) {
            binding.startButton.visibility = View.VISIBLE
            server = Server()
            server.createGame()
            binding.idTextView.text = server.gameCode
            lobby = Lobby(server.gameKey)
        } else {
            binding.idTextView.text = intent.getStringExtra("gameCode").toString()
            lobby = Lobby(intent.getStringExtra("gameKey").toString())
        }
        lobby.addServerPlayerCountListener(this)
        //TODO name
        lobby.addPlayerToServer("kuba", "female")
        lobby.addListenerToPlayer(this)

        binding.femaleCheckBox.isChecked = true
        setContentView(binding.root)
    }

    fun updatePlayers(playerCount: Long) {
        binding.playerCountTextView.text = getString(R.string.playerCount, playerCount)
    }

    fun start() {
        Toast.makeText(applicationContext, "STARTING THE GAME", Toast.LENGTH_SHORT).show()
        // todo set this function as listener on gameStarted change
        if (intent.getStringExtra("OWNER").toBoolean()) {
            // todo change gameStarted to true in firebase
        }
        // todo start game activity
    }

    fun onClick(view: View) {
        when (view) {
            binding.startButton -> start()
            binding.playersButton -> {
                val intent = Intent(this, PlayerViewActivity::class.java)
                intent.putExtra("gameKey", lobby.gameKey)
                intent.putExtra("OWNER", owner)
                startActivity(intent)
            }
            binding.deckButton -> startActivity(Intent(this, CardViewActivity::class.java))
            binding.leaveButton -> {
                // todo send leave information to firebase
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
        }
    }

    fun onCheck(view: View) {
        when (view) {
            binding.femaleCheckBox -> {
                binding.femaleCheckBox.isChecked = true
                binding.maleCheckBox.isChecked = false
                // todo send player gender to firebase
            }
            binding.maleCheckBox -> {
                binding.maleCheckBox.isChecked = true
                binding.femaleCheckBox.isChecked = false
                // todo send player gender to firebase
            }
        }
    }
    //TODO on leave

}
