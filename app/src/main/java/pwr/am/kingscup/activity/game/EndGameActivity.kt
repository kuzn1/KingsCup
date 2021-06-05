package pwr.am.kingscup.activity.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.R
import pwr.am.kingscup.activity.lobby.LobbyActivity
import pwr.am.kingscup.activity.menu.MainActivity
import pwr.am.kingscup.databinding.ActivityEndGameBinding

class EndGameActivity : Activity() {

    private lateinit var binding: ActivityEndGameBinding
    private var gameKey = ""
    private var owner = false
    private var playerKey = ""
    private var gameCode = ""
    private lateinit var listenerToGames: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEndGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gameKey = intent.getStringExtra("gameKey").toString()
        owner = intent.getBooleanExtra("OWNER", false)
        playerKey = intent.getStringExtra("playerKey").toString()
        gameCode = intent.getStringExtra("gameCode").toString()

        if (!owner) {
            listenerToGames = Firebase.database.getReference("end_games").child(gameKey)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == "EndGameActivity")
                            return
                        if (snapshot.value == null) {
                            Firebase.database.getReference("end_games").child(gameKey)
                                .removeEventListener(listenerToGames)
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.HostLeft),
                                Toast.LENGTH_LONG
                            ).show()
                            binding.lobbyButton.visibility = View.INVISIBLE
                        } else {
                            Firebase.database.getReference("end_games").child(gameKey)
                                .removeEventListener(listenerToGames)
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.HostIsBack),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            Firebase.database.getReference("end_games").child(gameKey).setValue("EndGameActivity")
        }
    }


    fun lobby(view: View) {
        if (owner) {
            Firebase.database.getReference("end_games").child(gameKey).setValue("Lobby")
            val intent = Intent(this, LobbyActivity::class.java)
            intent.putExtra("gameCode", gameCode)
            intent.putExtra("gameKey", gameKey)
            intent.putExtra("OWNER", owner)
            intent.putExtra("playerKey", playerKey)
            intent.putExtra("recreate", true)
            startActivity(intent)
            finish()
        } else {
            Firebase.database.getReference("openGames").child(gameCode).get().addOnSuccessListener {
                if (it.child("gameCode").value == null) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.returnWaitForCreator),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Firebase.database.getReference("end_games").child(gameKey)
                        .removeEventListener(listenerToGames)
                    val intent = Intent(this, LobbyActivity::class.java)
                    intent.putExtra("gameCode", gameCode)
                    intent.putExtra("gameKey", gameKey)
                    intent.putExtra("OWNER", owner)
                    intent.putExtra("playerKey", playerKey)
                    intent.putExtra("recreate", true)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    fun leave(view: View) {
        if (owner) {
            Firebase.database.getReference("end_games").child(gameKey).removeValue()
        } else {
            Firebase.database.getReference("end_games").child(gameKey)
                .removeEventListener(listenerToGames)
        }
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }

    override fun onBackPressed() {
        leave(View(this))
    }
}