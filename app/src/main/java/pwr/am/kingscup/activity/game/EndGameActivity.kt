package pwr.am.kingscup.activity.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.R
import pwr.am.kingscup.activity.lobby.LobbyActivity
import pwr.am.kingscup.activity.menu.MainActivity
import pwr.am.kingscup.databinding.ActivityEndGameBinding

class EndGameActivity : Activity() {

    private lateinit var binding : ActivityEndGameBinding
    private var gameKey = ""
    private var owner = false
    private var playerKey = ""
    private var gameCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEndGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gameKey = intent.getStringExtra("gameKey").toString()
        owner = intent.getBooleanExtra("OWNER", false)
        playerKey = intent.getStringExtra("playerKey").toString()
        gameCode = intent.getStringExtra("gameCode").toString()
    }

    fun lobby(view: View) {
        if (owner){
            val intent = Intent(this, LobbyActivity::class.java)
            intent.putExtra("gameCode",gameCode)
            intent.putExtra("gameKey", gameKey)
            intent.putExtra("OWNER", owner)
            intent.putExtra("playerKey", playerKey)
            intent.putExtra("recreate", true)
            startActivity(intent)
        }else{
            Firebase.database.getReference("openGames").child(gameCode).get().addOnSuccessListener {
                if (it.child("gameCode").value == null) {
                    Toast.makeText(applicationContext, getString(R.string.returnWaitForCreator), Toast.LENGTH_LONG).show()
                } else {
                    val intent = Intent(this, LobbyActivity::class.java)
                    intent.putExtra("gameCode",gameCode)
                    intent.putExtra("gameKey", gameKey)
                    intent.putExtra("OWNER", owner)
                    intent.putExtra("playerKey", playerKey)
                    intent.putExtra("recreate", true)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
        }
    }

    fun leave(view: View) {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }
}