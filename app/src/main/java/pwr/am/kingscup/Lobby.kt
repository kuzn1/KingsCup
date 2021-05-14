package pwr.am.kingscup

import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Lobby(val gameKey : String) {

    private val myTag = "mytesting"
    private val database = Firebase.database
    private var referenceGames = database.getReference("games")
    private var referencePlayers = database.getReference("players")
    private var playerKey = ""

    //TODO Create new activity and clean event listeners
    fun addServerTickListener(){
        referenceGames.child(gameKey).child("gamedata").child("server_tick").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.e(myTag, (snapshot.value as Long).toString())
                //TODO server update do action
            }
        })
    }
    //TODO remove event listener when not needed
    fun addListenerToPlayer(lobbyActivity: LobbyActivity) {
        referenceGames.child(gameKey).child("players").child(playerKey).addChildEventListener(object :
            ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                Toast.makeText(lobbyActivity, "You got kick out!", Toast.LENGTH_LONG).show()
                //TODO remove event listeners
                //TODO player in another activity crash
                lobbyActivity.finish()
            }
        })
    }

    //listener for "player_count". Calls lobbyActivity.updatePlayers()
    //TODO remove event listener when not needed
    fun addServerPlayerCountListener(lobbyActivity: LobbyActivity) {
        referenceGames.child(gameKey).child("gamedata").child("player_count").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                lobbyActivity.updatePlayers(snapshot.value as Long)
            }
        })
    }

    //generates playerKey and add player to server
    //TODO find better way to generate unique playerKey
    fun addPlayerToServer(name: String?, gender: String?){
        playerKey = database.getReference("temp").push().key.toString()
        database.getReference("temp").child(playerKey).removeValue()
        referenceGames.child(gameKey).child("players").child(playerKey).setValue(Player(name, gender))
    }

    data class Player(
        val name: String? = null,
        val gender : String? = null
    )
}