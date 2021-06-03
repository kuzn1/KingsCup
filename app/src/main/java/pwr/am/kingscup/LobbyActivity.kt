package pwr.am.kingscup

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import pwr.am.kingscup.databinding.ActivityLobbyBinding


class LobbyActivity : Activity() {
    private lateinit var binding: ActivityLobbyBinding
    private var owner: Boolean = false
    private lateinit var server: Server
    private lateinit var lobby: Lobby
    private lateinit var config: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        owner = intent.getBooleanExtra("OWNER", false)
        config = getSharedPreferences("KingsCupConfig", MODE_PRIVATE)

        //owner creates game = run server
        if (owner) {
            binding.startButton.visibility = View.VISIBLE
            server = Server()
            if(intent.getBooleanExtra("recreate", false)){
                server.recreateGame(intent.getStringExtra("gameCode").toString(), intent.getStringExtra("gameKey").toString())
            }else{
                server.createGame()
            }

            binding.idTextView.text = server.gameCode
            lobby = Lobby(server.gameKey)
        } else {
            binding.idTextView.text = intent.getStringExtra("gameCode").toString()
            lobby = Lobby(intent.getStringExtra("gameKey").toString())
            lobby.addServerTickListener(this)
        }
        lobby.addServerPlayerCountListener(this)
        lobby.addPlayerToServer(
            //TODO defValue for nick should be random, or required on First login
            config.getString("nick", "Player"),
            config.getString("gender", "female")
        )
        lobby.addListenerToPlayer(this)


        if (config.getString("gender", "female") == "female")
            binding.femaleCheckBox.isChecked = true
        else
            binding.maleCheckBox.isChecked = true

        setContentView(binding.root)
    }

    fun updatePlayers(playerCount: Long) {
        binding.playerCountTextView.text = getString(R.string.playerCount, playerCount)
    }

    fun start() {
        if (owner) {
            if (server.getPlayerCount() > 1) {
                server.makeGamePrivate()
                server.removeListenerToPlayers()
            } else {
                Toast.makeText(applicationContext, "NOT ENOUGH PLAYERS", Toast.LENGTH_SHORT).show()
                return
            }
        }

        Toast.makeText(applicationContext, "STARTING THE GAME", Toast.LENGTH_SHORT).show()
        lobby.removeListeners()

        val intent = Intent(this, GameBoardActivity::class.java)
        intent.putExtra("gameKey", lobby.gameKey)
        intent.putExtra("OWNER", owner)
        intent.putExtra("playerKey", lobby.playerKey)
        intent.putExtra("gameCode", binding.idTextView.text)
        startActivityForResult(intent, 1)
    }

    fun onClick(view: View) {
        when (view) {
            binding.startButton -> start()
            binding.playersButton -> {
                lobby.removeListeners()

                val intent = Intent(this, PlayerViewActivity::class.java)
                intent.putExtra("gameKey", lobby.gameKey)
                intent.putExtra("OWNER", owner)
                intent.putExtra("playerKey", lobby.playerKey)
                startActivityForResult(intent, 1)
            }
            binding.deckButton -> {
                lobby.removeListeners()
                val intent = Intent(this, CardViewActivity::class.java)
                intent.putExtra("gameKey", lobby.gameKey)
                intent.putExtra("OWNER", owner)
                intent.putExtra("playerKey", lobby.playerKey)
                startActivityForResult(intent, 1)
            }
            binding.leaveButton -> {
                lobby.removeListeners()
                lobby.removePlayer()
                if (owner)
                    server.removeGame()


                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
            binding.idTextView -> {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, binding.idTextView.text.toString())
                intent.type = "text/plain"

                startActivity(Intent.createChooser(intent, null))
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.getStringExtra("result") == "kick")
                        finish()
                    if (data.getStringExtra("result") == "back") {
                        lobby.addListenerToPlayer(this)
                        lobby.addServerPlayerCountListener(this)
                        if (!owner)
                            lobby.addServerTickListener(this)
                    }
                    if (data.getStringExtra("result") == "start")
                        start()
                }
            }
        }
    }

    fun onCheck(view: View) {
        when (view) {
            binding.femaleCheckBox -> {
                binding.femaleCheckBox.isChecked = true
                binding.maleCheckBox.isChecked = false
                lobby.updateGender("female")
            }
            binding.maleCheckBox -> {
                binding.maleCheckBox.isChecked = true
                binding.femaleCheckBox.isChecked = false
                lobby.updateGender("male")
            }
        }
    }
    //TODO on leave

}
