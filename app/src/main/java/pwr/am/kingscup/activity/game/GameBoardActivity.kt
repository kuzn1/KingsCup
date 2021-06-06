package pwr.am.kingscup.activity.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import pwr.am.kingscup.R
import pwr.am.kingscup.activity.menu.MainActivity
import pwr.am.kingscup.services.GameServer
import pwr.am.kingscup.services.GameClient
import pwr.am.kingscup.render.*

class GameBoardActivity : Activity() {
    private var owner: Boolean = false
    private lateinit var gameKey: String
    private lateinit var playerKey: String
    private lateinit var gameClient: GameClient
    private lateinit var gameCode: String

    private lateinit var menuButton : Button
    lateinit var glView: OpenGLView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        gameClient.addListenerToActivity()

        val config = getSharedPreferences("KingsCupConfig", MODE_PRIVATE)
        gameClient.enableCardSound = config.getBoolean("cardSound", true)
        gameClient.enableSfxSound = config.getBoolean("sfxSound", true)
        Log.e("GameClient", "SFX:${gameClient.enableSfxSound} Card:${gameClient.enableCardSound}")
        gameClient.initTTS()

        if(owner) {
            menuButton = Button(ContextThemeWrapper(this, R.style.menuButton), null, R.style.menuButton)
            addContentView(
                menuButton,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            )
            menuButton.setOnClickListener {
                PlayerKickOverlay(this, gameKey).show()
                true
            }
        }
    }

    fun startEndGameActivity() {
        val intent = Intent(this, EndGameActivity::class.java)
        intent.putExtra("gameKey", gameKey)
        intent.putExtra("OWNER", owner)
        intent.putExtra("playerKey", playerKey)
        intent.putExtra("gameCode", gameCode)
        startActivity(intent)
        finish()
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
        if(owner)
            stopService(Intent(this, GameServer::class.java))
        super.onDestroy()
    }

    override fun onBackPressed() {
        gameClient.removeAllListeners()
        if(owner)
            stopService(Intent(this, GameServer::class.java))
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }
}