package pwr.am.kingscup.services

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.R
import pwr.am.kingscup.activity.lobby.LobbyActivity

class LobbyClient(val gameKey : String) {

    private val database = Firebase.database
    private var referenceGames = database.getReference("games")
    var playerKey = ""
    private lateinit var listenerToPlayer : ChildEventListener
    private lateinit var listenerToServerPlayerCount : ValueEventListener
    private lateinit var ListenerToServerTick : ValueEventListener


    //listener for server_tick, returns to LobbyActivity
    fun addServerTickListener(activity: Activity){
        ListenerToServerTick = referenceGames.child(gameKey).child("gamedata").child("server_tick").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if((snapshot.value as Long).toString() == "1"){
                    removeServerPlayerCountListener()
                    removeListenerToPlayer()
                    removeServerTickListener()
                    val intent = Intent()
                    intent.putExtra("result", "start")
                    activity.setResult(Activity.RESULT_OK, intent)
                    activity.finish()
                }
            }
        })
    }
    //listener for server_tick, calls start() from LobbyActivity
    fun addServerTickListener(activity: LobbyActivity){
        ListenerToServerTick = referenceGames.child(gameKey).child("gamedata").child("server_tick").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if((snapshot.value as Long).toString() == "1"){
                   activity.start()
                }
            }
        })

    }

    //removes ListenerToServerTick
    fun removeServerTickListener(){
        if(this::ListenerToServerTick.isInitialized)
            referenceGames.child(gameKey).child("gamedata").child("server_tick").removeEventListener(ListenerToServerTick)
    }

    //listener for being kicked
    fun addListenerToPlayer(activity: Activity) {
        listenerToPlayer = referenceGames.child(gameKey).child("players").child(playerKey).addChildEventListener(object :
            ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                Toast.makeText(activity, activity.getString(R.string.kickOutMsg), Toast.LENGTH_LONG)
                    .show()
                removeServerPlayerCountListener()
                removeListenerToPlayer()
                removeServerTickListener()
                val intent = Intent()
                intent.putExtra("result", "kick")
                activity.setResult(Activity.RESULT_OK, intent)
                activity.finish()
            }
        })
        referenceGames.child(gameKey).child("players").child(playerKey).onDisconnect().removeValue()
    }

    //removes listenerToPlayer
    fun removeListenerToPlayer(){
        if(this::listenerToPlayer.isInitialized)
            referenceGames.child(gameKey).child("players").child(playerKey).removeEventListener(listenerToPlayer)
    }

    //listener for "player_count". Calls lobbyActivity.updatePlayers()
    fun addServerPlayerCountListener(lobbyActivity: LobbyActivity) {
        listenerToServerPlayerCount = referenceGames.child(gameKey).child("gamedata").child("player_count").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                lobbyActivity.updatePlayers(snapshot.value as Long)
            }
        })
    }

    //removes listenerToServerPlayerCount
    fun removeServerPlayerCountListener(){
        if(this::listenerToServerPlayerCount.isInitialized)
             referenceGames.child(gameKey).child("gamedata").child("player_count").removeEventListener(listenerToServerPlayerCount)
    }

    //generates playerKey and add player to server
    fun addPlayerToServer(name: String?, gender: String?){
        playerKey = database.getReference("temp").push().key.toString()
        database.getReference("temp").child(playerKey).removeValue()
        referenceGames.child(gameKey).child("players").child(playerKey).setValue(Player(name, gender))
    }

    //removes player from game
    fun removePlayer(){
        referenceGames.child(gameKey).child("players").child(playerKey).removeValue()
    }

    //updates information about gender
    fun updateGender(gender: String?){
        referenceGames.child(gameKey).child("players").child(playerKey).child("gender").setValue(gender)
    }

    //removes all listeners
    fun removeListeners(){
        referenceGames.child(gameKey).child("players").child(playerKey).onDisconnect().cancel()
        removeListenerToPlayer()
        removeServerTickListener()
        removeServerPlayerCountListener()

    }


    data class Player(
        val name: String? = null,
        val gender : String? = null
    )
}