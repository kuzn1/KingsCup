package pwr.am.kingscup

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Display
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.databinding.ActivityGameBoardBinding
import pwr.am.kingscup.render.Background
import pwr.am.kingscup.render.Card
import pwr.am.kingscup.render.Drawable
import pwr.am.kingscup.render.OpenGLView
import java.util.*
import kotlin.concurrent.schedule

class GameBoardActivity : Activity() {
    private lateinit var binding: ActivityGameBoardBinding
    private var owner: Boolean = false
    private lateinit var gameKey: String
    private lateinit var playerKey: String

    private lateinit var glView: OpenGLView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBoardBinding.inflate(layoutInflater)
        owner = intent.getBooleanExtra("OWNER", false)
        playerKey = intent.getStringExtra("playerKey").toString()
        gameKey = intent.getStringExtra("gameKey").toString()

        if(owner){
            //TODO Start GameLogicClass
            //temp
            Firebase.database.getReference("games").child(gameKey).child("gamedata").child("server_tick").setValue(1)
        }
        //TODO Start BoardClass

        glView = OpenGLView(this)
        setContentView(glView)
        glView.drawables.add(Background())
        glView.drawables.add(Card(1))
        glView.drawables.add(Card(12))
        glView.drawables[1].move(1.0f, 0.0f, -3.0f)
        glView.drawables[2].move(-1.0f, 0.0f, -3.0f)
        spinXAnimation(glView.drawables[1])
        spinYAnimation(glView.drawables[2])
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    fun spinXAnimation(drawable : Drawable){
        Timer("spin", false).schedule(20) {
            drawable.rotateX(1.0f)
            spinXAnimation(drawable)
        }
    }

    fun spinYAnimation(drawable : Drawable){
        Timer("spin", false).schedule(20) {
            drawable.rotateY(1.0f)
            spinYAnimation(drawable)
        }
    }
}