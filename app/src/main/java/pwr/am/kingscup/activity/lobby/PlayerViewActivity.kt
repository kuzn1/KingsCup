package pwr.am.kingscup.activity.lobby

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.services.LobbyClient
import pwr.am.kingscup.databinding.ActivityPlayerViewBinding
import pwr.am.kingscup.databinding.PlayerViewRowBinding


class PlayerViewActivity : Activity() {
    private lateinit var binding: ActivityPlayerViewBinding
    private lateinit var players: ArrayList<Pair<String, String>>
    private val database = Firebase.database
    private var referenceGames = database.getReference("games")
    private lateinit var listener: ChildEventListener
    private lateinit var gameKey: String
    private lateinit var lobbyClient: LobbyClient
    private lateinit var config : SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        config = getSharedPreferences("KingsCupConfig", MODE_PRIVATE)

        binding = ActivityPlayerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameKey = intent.getStringExtra("gameKey").toString()
        lobbyClient = LobbyClient(gameKey)
        lobbyClient.playerKey = intent.getStringExtra("playerKey").toString()
        lobbyClient.addServerTickListener(this)

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
                    if(snapshot.key.toString() == intent.getStringExtra("playerKey").toString()){
                        referenceGames.child(gameKey).child("players").removeEventListener(listener)
                        val intent = Intent()
                        intent.putExtra("result", "kick")
                        lobbyClient.removeServerTickListener()
                        setResult(RESULT_OK, intent)
                        finish()
                    }

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
            if (owner && player.first != config.getString("nick", "Player")) {
                row.kickButton.visibility = View.VISIBLE
                row.kickButton.setOnClickListener { kickPlayer(player.second) }
            }
            binding.playerList.addView(row.root)
        }
    }
    private fun kickPlayer(playerID: String) {
        referenceGames.child(gameKey).child("players").child(playerID).removeValue()
        Toast.makeText(this, "Kick player:" + playerID, Toast.LENGTH_SHORT).show()
    }

    fun back(view: View) {
        referenceGames.child(gameKey).child("players").removeEventListener(listener)
        lobbyClient.removeServerTickListener()
        val intent = Intent()
        intent.putExtra("result", "back");
        this.setResult(RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        back(View(this))
    }


}