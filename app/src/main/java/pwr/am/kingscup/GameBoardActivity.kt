package pwr.am.kingscup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Display
import android.view.KeyEvent
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.databinding.ActivityGameBoardBinding
import pwr.am.kingscup.render.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class GameBoardActivity : Activity() {
    private lateinit var binding: ActivityGameBoardBinding
    private var owner: Boolean = false
    private lateinit var gameKey: String
    private lateinit var playerKey: String
    private lateinit var playerLogic : PlayerLogic
    private lateinit var game : Game

    lateinit var glView: OpenGLView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBoardBinding.inflate(layoutInflater)
        owner = intent.getBooleanExtra("OWNER", false)
        playerKey = intent.getStringExtra("playerKey").toString()
        gameKey = intent.getStringExtra("gameKey").toString()

        if(owner){
            intent = Intent(this, GameLogic::class.java)
            intent.putExtra("gameKey", gameKey)
            startService(intent)
        }

        //todo
        //playerLogic.getPlayerGender()
        //playerLogic = PlayerLogic(gameKey , playerKey)
        //playerLogic.addListenerToGameData()


        glView = OpenGLView(this)
        setContentView(glView)
        glView.drawables.add(Background())
        game = Game(this, glView.drawables)
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(6<keyCode && keyCode<17)
            game.start(keyCode-7)
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        stopService(Intent(this, GameLogic::class.java))
        super.onDestroy()
    }
}