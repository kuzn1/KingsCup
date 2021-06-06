package pwr.am.kingscup.activity.lobby

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import pwr.am.kingscup.services.LobbyClient
import pwr.am.kingscup.R
import pwr.am.kingscup.services.LobbyServer
import pwr.am.kingscup.activity.game.GameBoardActivity
import pwr.am.kingscup.activity.menu.MainActivity
import pwr.am.kingscup.databinding.ActivityLobbyBinding


class LobbyActivity : Activity() {
    private lateinit var binding: ActivityLobbyBinding
    private var owner: Boolean = false
    private lateinit var lobbyServer: LobbyServer
    private lateinit var lobbyClient: LobbyClient
    private lateinit var config: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyBinding.inflate(layoutInflater)
        owner = intent.getBooleanExtra("OWNER", false)
        config = getSharedPreferences("KingsCupConfig", MODE_PRIVATE)

        //owner creates game = run server
        if (owner) {
            binding.startButton.visibility = View.VISIBLE
            lobbyServer = LobbyServer()
            if(intent.getBooleanExtra("recreate", false)){
                lobbyServer.recreateGame(intent.getStringExtra("gameCode").toString(), intent.getStringExtra("gameKey").toString())
            }else{
                lobbyServer.createGame()
            }

            binding.idTextView.text = lobbyServer.gameCode
            lobbyClient = LobbyClient(lobbyServer.gameKey)
        } else {
            binding.idTextView.text = intent.getStringExtra("gameCode").toString()
            lobbyClient = LobbyClient(intent.getStringExtra("gameKey").toString())
            lobbyClient.addServerTickListener(this)
        }
        lobbyClient.addServerPlayerCountListener(this)
        lobbyClient.addPlayerToServer(
            config.getString("nick", "Player"),
            config.getString("gender", "female")
        )
        lobbyClient.addListenerToPlayer(this)


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
            if (lobbyServer.getPlayerCount() > 1) {
                lobbyServer.makeGamePrivate()
                lobbyServer.removeListenerToPlayers()
            } else {
                Toast.makeText(applicationContext, getString(R.string.notEnoughPlayers), Toast.LENGTH_SHORT).show()
                return
            }
        }

        Toast.makeText(applicationContext, getString(R.string.startingGame), Toast.LENGTH_SHORT).show()
        lobbyClient.removeListeners()

        val intent = Intent(this, GameBoardActivity::class.java)
        intent.putExtra("gameKey", lobbyClient.gameKey)
        intent.putExtra("OWNER", owner)
        intent.putExtra("playerKey", lobbyClient.playerKey)
        intent.putExtra("gameCode", binding.idTextView.text)
        startActivityForResult(intent, 1)
    }

    fun onClick(view: View) {
        when (view) {
            binding.startButton -> start()
            binding.playersButton -> {
                lobbyClient.removeListeners()

                val intent = Intent(this, PlayerViewActivity::class.java)
                intent.putExtra("gameKey", lobbyClient.gameKey)
                intent.putExtra("OWNER", owner)
                intent.putExtra("playerKey", lobbyClient.playerKey)
                startActivityForResult(intent, 1)
            }
            binding.deckButton -> {
                lobbyClient.removeListeners()
                val intent = Intent(this, CardViewActivity::class.java)
                intent.putExtra("gameKey", lobbyClient.gameKey)
                intent.putExtra("OWNER", owner)
                intent.putExtra("playerKey", lobbyClient.playerKey)
                startActivityForResult(intent, 1)
            }
            binding.leaveButton -> {
                lobbyClient.removeListeners()
                lobbyClient.removePlayer()
                if (owner)
                    lobbyServer.removeGame()


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
                    if (data.getStringExtra("result") == "kick") {
                        finish()
                    }
                    if (data.getStringExtra("result") == "back") {
                        lobbyClient.addListenerToPlayer(this)
                        lobbyClient.addServerPlayerCountListener(this)
                        if (!owner)
                            lobbyClient.addServerTickListener(this)
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
                lobbyClient.updateGender("female")
            }
            binding.maleCheckBox -> {
                binding.maleCheckBox.isChecked = true
                binding.femaleCheckBox.isChecked = false
                lobbyClient.updateGender("male")
            }
        }
    }

    override fun onBackPressed() {
        Log.e("LobbyActivity", "onBackPressed")
        lobbyClient.removeListeners()
        lobbyClient.removePlayer()
        if (owner)
            lobbyServer.removeGame()


        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }
}
