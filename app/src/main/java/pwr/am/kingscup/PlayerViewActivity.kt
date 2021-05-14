package pwr.am.kingscup

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import pwr.am.kingscup.databinding.ActivityPlayerViewBinding
import android.view.LayoutInflater
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.databinding.PlayerViewRowBinding
import java.util.*
import kotlin.collections.ArrayList


class PlayerViewActivity : Activity() {
    private lateinit var binding: ActivityPlayerViewBinding
    private lateinit var players: ArrayList<Pair<String, String>>
    private val database = Firebase.database
    private var referenceGames = database.getReference("games")
    private lateinit var listener: ChildEventListener
    private lateinit var gameKey: String


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityPlayerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameKey = intent.getStringExtra("gameKey").toString()
        players = ArrayList()
        listener = referenceGames.child(gameKey)
            .child("players").addChildEventListener(object : ChildEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val a = Pair(snapshot.child("name").value.toString(), snapshot.key.toString())
                    players.add(a)
                    loadPlayers(intent.getBooleanExtra("OWNER", false))
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    for (player in players){
                        if(player.second == snapshot.key.toString())
                            players.remove(player)
                    }
                    loadPlayers(intent.getBooleanExtra("OWNER", false))
                }

            })
    }

    fun loadPlayers(owner: Boolean) {
        binding.playerList.removeAllViews()
        for (player: Pair<String, String> in players) {
            val row = PlayerViewRowBinding.inflate(LayoutInflater.from(this))
            row.nickName.text = player.first
            if (owner) {
                row.kickButton.visibility = View.VISIBLE
                row.kickButton.setOnClickListener { kickPlayer(player.second) }
            }
            binding.playerList.addView(row.root)
        }

    }

    //TODO YOU CAN KICK YOURSELF!!!
    //TODO BAN
    fun kickPlayer(playerID: String) {
        referenceGames.child(gameKey).child("players").child(playerID).removeValue()
        Toast.makeText(this, "Kick player:" + playerID, Toast.LENGTH_SHORT).show()
    }

    fun back(view: View) {
        referenceGames.child(gameKey).child("players").removeEventListener(listener)
        finish()
    }

    override fun onBackPressed() {
        referenceGames.child(gameKey).child("players").removeEventListener(listener)
        finish()
        super.onBackPressed()
    }


}