package pwr.am.kingscup.activity.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import pwr.am.kingscup.services.GameServer
import pwr.am.kingscup.services.GameClient
import pwr.am.kingscup.databinding.ActivityGameBoardBinding
import pwr.am.kingscup.render.*

class GameBoardActivity : Activity() {
    private lateinit var binding: ActivityGameBoardBinding
    private var owner: Boolean = false
    private lateinit var gameKey: String
    private lateinit var playerKey: String
    private lateinit var gameClient: GameClient
    private lateinit var gameCode: String

    lateinit var glView: OpenGLView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBoardBinding.inflate(layoutInflater)
        owner = intent.getBooleanExtra("OWNER", false)
        playerKey = intent.getStringExtra("playerKey").toString()
        gameKey = intent.getStringExtra("gameKey").toString()
        gameCode = intent.getStringExtra("gameCode").toString()

        if (owner) {
            intent = Intent(this, GameServer::class.java)
            intent.putExtra("gameKey", gameKey)
            startService(intent)
        }

        glView = OpenGLView(this)
        setContentView(glView)
        glView.drawables.add(Background())

        gameClient = GameClient(gameKey, playerKey, this, glView.drawables)
        gameClient.addListenerToPlayers()
        gameClient.getPlayerGender()
        gameClient.addListenerToGameData()
        gameClient.setupDeck()

    }

    fun startEndGameActivity() {
        val intent = Intent(this, EndGameActivity::class.java)
        intent.putExtra("gameKey", gameKey)
        intent.putExtra("OWNER", owner)
        intent.putExtra("playerKey", playerKey)
        intent.putExtra("gameCode", gameCode)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    override fun onDestroy() {
        stopService(Intent(this, GameServer::class.java))
        super.onDestroy()
    }
}