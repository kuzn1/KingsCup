package pwr.am.kingscup


import android.util.Log

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class Server() {

    private val database = Firebase.database
    private var referenceGames = database.getReference("games")
    private var openGames = database.getReference("openGames")
    private var gameTick = 0
    private var playerCount = 0
    var gameCode = ""
    var gameKey : String = ""

    fun createGame(){
        generateRoomCode()

        val gameData = Gamedata(0,"waiting",0,0)
        val game = Game(gameCode , gameData)
        gameKey = referenceGames.push().key.toString()
        referenceGames.child(gameKey).setValue(game)

        addListenerToPlayers()
        makeGamePublic()
    }
    private fun generateRoomCode(){
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZ"
        gameCode =  (1..6)
            .map { charset.random() }
            .joinToString("")
    }
    fun addCardsToGame(cards : ArrayList<Card>) {
        referenceGames.child(gameKey).child("card_set").setValue(cards)
    }
    fun setNewGameStatus(status : String){
        referenceGames.child(gameKey).child("gamedata").child("game_status").setValue(status)
    }
    fun updateGameTick(){
        gameTick++
        referenceGames.child(gameKey).child("gamedata").child("server_tick").setValue(gameTick)
    }

    //updates player_count
    //TODO remove event listener when not needed
    fun addListenerToPlayers(){
        referenceGames.child(gameKey).child("players").addChildEventListener(object :
            ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                playerCount++
                referenceGames.child(gameKey).child("gamedata").child("player_count").setValue(playerCount)
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                playerCount--
                referenceGames.child(gameKey).child("gamedata").child("player_count").setValue(playerCount)
            }
        })
    }
/*
    fun searchForPlayers() {
        referencePlayers.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.w(myTag, "Failed to read value.", error.toException())
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child("roomCode").value == gameCode) {
                    //gives player gameKey
                    referencePlayers.child(snapshot.key.toString())
                        .child("gameid")
                        .setValue(mykey)
                    //adds player to game
                    referenceGames.child(mykey)
                        .child("players")
                        .child(snapshot.key.toString())
                        .child("name")
                        .setValue(snapshot.child("name").value)

                    playerList.add(snapshot.child("name").value as String)
                    Log.e(myTag, playerList.toString())

                    //val toast = Toast.makeText(
                    //    applicationContext,
                    //    "PlayerJoin " + snapshot.child("name").value,
                    //    Toast.LENGTH_SHORT
                    //)
                    //toast.show()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

 */

    //adds gameKey and gameCode to openGames so players can find game
    //TODO remove when game starts
    fun makeGamePublic(){
    openGames.child(gameCode).setValue(OpenGame(gameCode, gameKey))

}
    data class OpenGame(
        val gameCode : String? = null,
        val gameKey : String? = null
    )

    data class Gamedata(
        val server_tick: Int? = null,
        val game_status: String? = null,
        val current_card_id: Int? = null,
        val current_player_id: Int? = null,
        val player_count : Int? = 0
        //val addidtional_data: String? = null
        //TODO
    )

    data class Game(
        val gameCode: String? = null,
        val gamedata: Gamedata? = null,
        //TODO
    )

    data class Card (
        val id : Int,
        val count : Int
        //TODO maybe add text
    )
}