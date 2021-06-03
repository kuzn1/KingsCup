package pwr.am.kingscup.services


import android.util.Log

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class   LobbyServer()  {

    private val database = Firebase.database
    private var referenceGames = database.getReference("games")
    private var openGames = database.getReference("openGames")
    private var gameTick = 0
    private var playerCount = 0
    private lateinit var listenerToPlayers : ChildEventListener
    var gameCode = ""
    var gameKey : String = ""

    fun createGame(){
        generateRoomCode()

        val gameData = Gamedata(0,"waiting",0,0)
        val game = Game(gameCode , gameData)
        gameKey = referenceGames.push().key.toString()
        referenceGames.child(gameKey).setValue(game)
        addCardsToGame()

        addListenerToPlayers()
        makeGamePublic()
        Log.wtf("Server", "Game created")
    }
    fun recreateGame(code : String, key : String ){
        gameCode = code
        gameKey = key

        val gameData = Gamedata(0,"waiting",0,0)
        val game = Game(gameCode , gameData)
        referenceGames.child(gameKey).setValue(game)
        addCardsToGame()

        addListenerToPlayers()
        makeGamePublic()
        Log.wtf("Server", "Game recreate")
    }





    private fun generateRoomCode(){
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZ"
        gameCode =  (1..6)
            .map { charset.random() }
            .joinToString("")
    }

    private fun addCardsToGame() {
        val cards = HashMap<String, Long>()
        for(i in 0..51)
            if(i<13 && i!=8 && i!=9 && i!=10) cards.put(i.toString(), 1)
            else cards.put(i.toString(), 0)
        referenceGames.child(gameKey).child("card_set").setValue(cards)
    }

    //updates player_count
    //TODO remove event listener when not needed
    fun addListenerToPlayers(){
        listenerToPlayers = referenceGames.child(gameKey).child("players").addChildEventListener(object :
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

    //adds gameKey and gameCode to openGames so players can find game
    fun makeGamePublic(){
    openGames.child(gameCode).setValue(OpenGame(gameCode, gameKey))
    }
    //removes gameKey and gameCode from openGames
    fun makeGamePrivate(){
        openGames.child(gameCode).removeValue()
    }
    fun removeListenerToPlayers(){
        referenceGames.child(gameKey).child("players").removeEventListener(listenerToPlayers)
    }

    fun removeGame(){
        referenceGames.child(gameKey).child("players").removeEventListener(listenerToPlayers)
        referenceGames.child(gameKey).child("players").removeValue()
        openGames.child(gameCode).removeValue()
        referenceGames.child(gameKey).removeValue()
    }

    fun getPlayerCount(): Int = playerCount

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
    )

    data class Game(
        val gameCode: String? = null,
        val gamedata: Gamedata? = null,
    )

    data class Card (
        val id : Int,
        val count : Int
    )
}