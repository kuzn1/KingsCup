package pwr.am.kingscup.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.schedule
import kotlin.random.Random.Default.nextInt


class GameServer() : Service() {
    private val database = Firebase.database

    private var referenceGames = database.getReference("games")
    private lateinit var referenceActivity: DatabaseReference
    private lateinit var referencePlayers: DatabaseReference
    private var gameTick = 0
    private var playerCount = 0
    private lateinit var listenerToPlayers: ChildEventListener
    private lateinit var listenerToResponses: ChildEventListener
    private var gameKey = ""
    private var responseArray = ArrayList<Response>()
    private var playerArray = ArrayList<Player>()
    private var cardArray = ArrayList<Card>()
    private var ActivityCheck = true


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        gameKey = intent?.getStringExtra("gameKey").toString()
        Log.e("Server", gameKey)
        start()
        updateGameTick()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun stopService(name: Intent?): Boolean {
        Log.e("Server", "stopService")
        return super.stopService(name)
    }

    override fun onDestroy() {
        Log.e("Server", "onDestroy")
        ActivityCheck = false
        referenceGames.child(gameKey).child("players").removeEventListener(listenerToPlayers)
        referenceGames.child(gameKey).child("responses").removeEventListener(listenerToResponses)
        referenceGames.child(gameKey).removeValue().addOnSuccessListener {
            super.onDestroy()
        }.addOnFailureListener {
            super.onDestroy()
        }
    }

    fun setNewGameStatus(status: String) {
        referenceGames.child(gameKey).child("gamedata").child("game_status").setValue(status)
    }

    fun updateGameTick() {
        gameTick++
        referenceGames.child(gameKey).child("gamedata").child("server_tick").setValue(gameTick)
    }

    private fun setCurrentCardId(id: Int) {
        referenceGames.child(gameKey).child("gamedata").child("current_card_id").setValue(id)
    }

    private fun setCurrentPlayerKey(key: String) {
        referenceGames.child(gameKey).child("gamedata").child("current_player_id").setValue(key)
    }

    private fun addListenerToPlayers() {
        listenerToPlayers =
            referenceGames.child(gameKey).child("players").addChildEventListener(object :
                ChildEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    playerCount++
                    referenceGames.child(gameKey).child("gamedata").child("player_count")
                        .setValue(playerCount)
                    playerArray.add(
                        Player(
                            snapshot.key.toString(),
                            snapshot.child("gender").value.toString(),
                            true
                        )
                    )
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    playerCount--

                    playerArray.find { it.playerKey == snapshot.key }?.isOnline = false

                    //if less then 2 players left end service/game
                    if (playerCount == 0 || playerCount == 1) {
                        Log.e("Server", "stop service")
                        stopSelf()
                        return
                    }

                    referenceGames.child(gameKey).child("gamedata").child("player_count")
                        .setValue(playerCount)

                    //if currentState = WAITING_FOR_PLAYERS_TO_CONNECT
                    if (currentState == State.WAITING_FOR_PLAYERS_TO_CONNECT) {
                        Log.e("Server", "disconnect on WAITING_FOR_PLAYERS_TO_CONNECT")
                        if (responseArray.find { it.playerKey == snapshot.key } == null) {
                            Log.e("Server", "Simulating disconnect")
                            responseToAction(Response(snapshot.key, gameTick, "Join", ""))
                        }
                        return
                    }
                    //current player

                    //if (playerArray[currentPlayer].playerKey == snapshot.key) {
                    cardState = 0
                    responseArray.clear()

                    pickNextPlayerAndSendNewCard()
                    /*
                } else {
                    //card handling
                    when (currentCard) {
                        0, 13, 26, 39,
                        2, 15, 28, 41,
                        4, 17, 30, 43,
                        5, 18, 31, 44,
                        7, 20, 33, 46,
                        12, 25, 38, 51 -> {
                            if (responseArray.find { it.playerKey == snapshot.key } == null) {
                                responseToAction(
                                    Response(
                                        snapshot.key,
                                        gameTick,
                                        "CardActionDone",
                                        ""
                                    )
                                )
                            }
                        }
                        1, 14, 27, 40 -> {
                            if (cardState == 1 && selectedPlayer == snapshot.key) {
                                selectedPlayer = ""
                                cardState = 0
                                pickNextPlayerAndSendNewCard()
                            }
                        }
                        3, 16, 29, 42,
                        6, 19, 32, 45 -> {
                            if (cardState == 0) {
                                if (responseArray.find { it.playerKey == snapshot.key } == null) {
                                    responseToAction(
                                        Response(
                                            snapshot.key, gameTick, "Time", "-1"
                                        )
                                    )
                                }
                            } else {
                                if (snapshot.key == playerWithMaxTime) {
                                    playerWithMaxTime = ""
                                    cardState = 0
                                    pickNextPlayerAndSendNewCard()
                                }
                            }
                        }
                        11, 24, 37, 50 -> {
                            if (cardState == 1 && selectedPlayer == snapshot.key) {
                                selectedPlayer = ""
                                cardState = 0
                                pickNextPlayerAndSendNewCard()
                            } else if(cardState == 2){
                                if (responseArray.find { it.playerKey == snapshot.key } == null) {
                                    responseToAction(
                                        Response(
                                            snapshot.key, gameTick, "CardActionDone", ""
                                        )
                                    )
                                }
                            }
                        }
                    }
                }*/
                }
            })
    }

    private fun addListenerToResponses() {
        listenerToResponses =
            referenceGames.child(gameKey).child("responses").addChildEventListener(object :
                ChildEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                    Log.e("Server", snapshot.toString())
                    val response = Response(
                        snapshot.child("playerKey").value as String?,
                        (snapshot.child("server_tick").value as Long).toInt(),
                        snapshot.child("data").value as String?,
                        snapshot.child("additionalData").value as String?,
                        snapshot.child("additionalData2").value as String?
                    )
                    responseToAction(response)
                }
            })
    }

    private var currentState = State.WAITING_FOR_PLAYERS_TO_CONNECT
    private var currentPlayer = 0
    private var currentCard = 0

    //based on currentState handles responses
    fun responseToAction(response: Response) {
        Log.e("Server", "responseToAction")
        //pick random player
        if (currentPlayer == -1) {
            Log.e("Server", "PickingRandomPlayer")
            currentPlayer = nextInt(0, playerCount)
        }

        //wait for all players to load
        if (currentState == State.WAITING_FOR_PLAYERS_TO_CONNECT) {
            Log.e("Server", "WAITING_FOR_PLAYERS_TO_CONNECT")
            if (response.data == "Join") {
                playerArray.find { response.playerKey == it.playerKey }?.responded = true
                var temp = true
                for (player in playerArray) {
                    if (player.responded == false && player.isOnline == true) {
                        temp = false
                    }
                }
                if (temp) {
                    playerArray.forEach { it.responded = false }
                    pickNextPlayerAndSendNewCard()
                }
            }
            return
        }

        //wait until player picks card
        if (currentState == State.WAIT_FOR_PLAYER_TO_DRAW_CARD) {
            Log.e("Server", "WAIT_FOR_PLAYER_TO_DRAW_CARD")
            if (response.playerKey == playerArray[currentPlayer].playerKey && response.server_tick == gameTick && response.data == "Drawn") {
                setNewGameStatus("CardAction")
                currentState = State.CARD_ACTION
                updateGameTick()
            }
            return
        }
        //card action
        if (currentState == State.CARD_ACTION) {
            Log.e("Server", "CARD_ACTION")
            if (response.server_tick == gameTick) {
                cardAction(response)
            }
            return
        }
        if (currentState == State.WAIT_FOR_ALL_PLAYERS_TO_ACCEPT) {
            Log.e("Server", "WAIT_FOR_ALL_PLAYERS_TO_ACCEPT")
            if (response.server_tick == gameTick && response.data == "Accepted") {
                playerArray.find { response.playerKey == it.playerKey }?.responded = true
                var temp = true
                for (player in playerArray) {
                    if (player.responded == false && player.isOnline == true) {
                        temp = false
                    }
                }
                if (temp) {
                    playerArray.forEach { it.responded = false }
                    responseArray.clear()
                    pickNextPlayerAndSendNewCard()
                }
            }
        }
    }


    fun pickNextPlayerAndSendNewCard() {

        //pick player
        currentPlayer = (currentPlayer + 1) % playerArray.size
        while (!playerArray[currentPlayer].isOnline!!)
            currentPlayer = (currentPlayer + 1) % playerArray.size
        setCurrentPlayerKey(playerArray[currentPlayer].playerKey)

        //0 cards FinishGame
        if (cardArray.size == 0) {
            Log.e("Server", " 0 cards left")
            setNewGameStatus("FinishGame")
            updateGameTick()
            stopSelf()
            return
        }

        //pick card
        val temp = nextInt(0, cardArray.size)

        currentCard = cardArray[temp].id
        setCurrentCardId(currentCard)

        cardArray.removeAt(temp)

        //set new game status
        setNewGameStatus("DrawCard")
        currentState = State.WAIT_FOR_PLAYER_TO_DRAW_CARD
        responseArray.clear()
        updateGameTick()
        Log.e(
            "Server",
            "picked new player $currentPlayer  ${playerArray[currentPlayer]} and card $currentCard"
        )
    }

    var cardState = 0
    private var selectedPlayer = ""
    private var playersToDrink = ArrayList<String>()

    //based on card handles players responses
    private fun cardAction(response: Response) {

        when (currentCard) {

            //All this cards require players to post response CardActionDone
            0, 13, 26, 39,  //Ace - everyone without current player drinks
            2, 15, 28, 41,  //Three - only current player drinks
            4, 17, 30, 43,  //Five - all males drinks
            5, 18, 31, 44,  //Six - all females drinks
            7, 20, 33, 46,  //Eight - random players drinks
            12, 25, 38, 51  //King - all players finish drinks
            -> {
                Log.e("Server", "Ace/Three/Five/Six/Eight/King")
                if (response.data == "CardActionDone") {
                    responseArray.add(response)
                    playerArray.find { response.playerKey == it.playerKey }?.responded = true
                    Log.e("Server", playerArray.toString())
                    var temp = true
                    for (player in playerArray) {
                        if (player.responded == false && player.isOnline == true) {
                            temp = false
                        }
                    }
                    if (temp) {
                        Log.e("Server", "card action done")
                        playerArray.forEach { it.responded = false }
                        responseArray.clear()
                        currentState = State.WAIT_FOR_ALL_PLAYERS_TO_ACCEPT
                        setNewGameStatus("AcceptThisRound")
                        updateGameTick()
                    }
                }
            }

            1, 14, 27, 40  //Two - player chooses other player to take a drink
            -> {
                Log.e("Server", "Two")
                if (cardState == 0) {
                    if (response.data == "PickedPlayer" && response.playerKey == playerArray[currentPlayer].playerKey) {
                        cardState = 1
                        selectedPlayer = response.additionalData.toString()
                        referenceGames.child(gameKey).child("gamedata")
                            .child("players_to_drink")
                            .setValue(selectedPlayer)
                        setNewGameStatus("Drinks")
                        updateGameTick()
                        return
                    }
                }
                if (cardState == 1) {
                    if (response.data == "CardActionDone" && response.playerKey == selectedPlayer) {
                        cardState = 0
                        currentState = State.WAIT_FOR_ALL_PLAYERS_TO_ACCEPT
                        setNewGameStatus("AcceptThisRound")
                        updateGameTick()
                        selectedPlayer = ""
                        return
                    }
                }
            }

            3, 16, 29, 42, //Four - last player to touch the floor has to drink
            6, 19, 32, 45  //Seven - last person to raise their hand has to drink
            -> {
                Log.e("Server", "Four/Seven")
                //wait for times
                if (cardState == 0) {
                    if (response.data == "Time") {
                        responseArray.add(response)
                        playerArray.find { response.playerKey == it.playerKey }?.responded =
                            true
                        var temp = true
                        for (player in playerArray) {
                            if (player.responded == false && player.isOnline == true) {
                                temp = false
                            }
                        }
                        if (temp) {
                            Log.e("Server", "All times collected")
                            playersToDrink.clear()

                            responseArray.sortBy { it.additionalData?.toLong() }

                            val maxTime =
                                responseArray.maxByOrNull { it.additionalData?.toLong()!! }?.additionalData?.toLong()!!

                            for (r in responseArray)
                                if (r.additionalData!!.toLong() == maxTime)
                                    playersToDrink.add(r.playerKey.toString())



                            cardState = 1
                            var string = "";
                            for (player in responseArray) {
                                string = string.plus(player.playerKey)
                                string = string.plus("|")
                                string = string.plus(player.additionalData)
                                string = string.plus("|")

                            }

                            var string2 = ""
                            for (player in playersToDrink) {
                                string2 = string2.plus(player)
                                string2 = string2.plus("|")
                            }
                            string2 = string2.dropLast(1)




                            responseArray.clear()
                            string = string.dropLast(1)
                            playerArray.forEach { it.responded = false }

                            referenceGames.child(gameKey).child("gamedata")
                                .child("players_to_drink_with_time").setValue(string)

                            referenceGames.child(gameKey).child("gamedata")
                                .child("players_to_drink").setValue(string2)
                            setNewGameStatus("MultipleDrinks")
                            updateGameTick()
                            return
                        }
                    }
                }
                //wait for player to drink
                if (cardState == 1) {
                    if (playerArray.find { it.playerKey == response.playerKey }?.responded == false) {
                        playerArray.find { it.playerKey == response.playerKey }?.responded =
                            true
                        playersToDrink.remove(response.playerKey)

                        if (playersToDrink.isEmpty()) {
                            Log.e("Server", "card action done")
                            playerArray.forEach { it.responded = false }
                            cardState = 0
                            currentState = State.WAIT_FOR_ALL_PLAYERS_TO_ACCEPT
                            setNewGameStatus("AcceptThisRound")
                            updateGameTick()
                            return
                        }

                    }
                }
            }

            11, 24, 37, 50 //Queen - pick player and ask him question
            -> {
                Log.e("Server", "Queen")
                if (cardState == 0) {
                    responseArray.clear()
                    if (response.data == "PickedPlayer" && response.playerKey == playerArray[currentPlayer].playerKey) {
                        cardState = 1
                        selectedPlayer = response.additionalData.toString()
                        referenceGames.child(gameKey).child("gamedata")
                            .child("selected_player_for_question").setValue(selectedPlayer)
                        referenceGames.child(gameKey).child("gamedata")
                            .child("question").setValue(response.additionalData2.toString())
                        setNewGameStatus("Question")
                        updateGameTick()
                        return
                    }
                }
                if (cardState == 1) {
                    if (response.data == "Answer" && response.playerKey == selectedPlayer) {
                        cardState = 2
                        referenceGames.child(gameKey).child("gamedata")
                            .child("answer").setValue(response.additionalData.toString())
                        setNewGameStatus("Answer")
                        updateGameTick()
                        return
                    }
                }
                if (cardState == 2 && response.data == "CardActionDone") {
                    responseArray.add(response)
                    playerArray.find { response.playerKey == it.playerKey }?.responded = true
                    var temp = true
                    for (player in playerArray) {
                        if (player.responded == false && player.isOnline == true) {
                            temp = false
                        }
                    }
                    if (temp) {
                        cardState = 0
                        Log.e("Server", "card action done")
                        playerArray.forEach { it.responded = false }
                        responseArray.clear()
                        currentState = State.WAIT_FOR_ALL_PLAYERS_TO_ACCEPT
                        setNewGameStatus("AcceptThisRound")
                        updateGameTick()
                    }
                }
            }
        }
    }

    private fun start() {
        referencePlayers = database.getReference("games/$gameKey/players")

        currentPlayer = -1
        addListenerToPlayers()

        val referenceCards = referenceGames.child(gameKey).child("card_set")

        referenceCards.get().addOnSuccessListener {
            for (i in 0..51) {
                if ((it.child(i.toString()).value as Long).toInt() > 0)
                    cardArray.add(Card(i, (it.child(i.toString()).value as Long).toInt()))
            }
            addListenerToResponses()
        }

        startActivityCheck()
    }

    private fun startActivityCheck() {
        referenceActivity = referenceGames.child("$gameKey/activity")

        val playerMap = HashMap<String, Long>()
        referencePlayers.get().addOnSuccessListener {
            for (player in it.children)
                playerMap.put(player.key as String, 0L)

            referenceActivity.setValue(playerMap)
            referenceActivity.child("tick").setValue(0L)

            Timer("activity_check", true).schedule(5000) {
                activityCheck(0L)
            }
        }
    }

    private fun activityCheck(i: Long) {
        if (ActivityCheck) {
            referenceActivity.get().addOnSuccessListener {
                for (player in it.children) {
                    if ((player.value as Long) < i) {
                        referencePlayers.child(player.key!!).removeValue()
                    }
                }
                referenceActivity.child("tick").setValue(i + 1)
                Timer("activity_check", true).schedule(10000) {
                    activityCheck(i + 1)
                }
            }
        }
    }

    class Response(
        val playerKey: String? = null,
        val server_tick: Int = -1,
        val data: String? = null,
        val additionalData: String? = null,
        val additionalData2: String? = null
    )

    data class Player(
        val playerKey: String,
        val gender: String? = null,
        var isOnline: Boolean? = true,
        var responded: Boolean? = false
    )

    data class Card(
        val id: Int,
        val count: Int
    )

    enum class State {
        WAITING_FOR_PLAYERS_TO_CONNECT,
        WAIT_FOR_PLAYER_TO_DRAW_CARD,
        CARD_ACTION,
        WAIT_FOR_ALL_PLAYERS_TO_ACCEPT
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}