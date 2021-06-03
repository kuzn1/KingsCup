package pwr.am.kingscup.services

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.R
import pwr.am.kingscup.activity.game.GameBoardActivity
import pwr.am.kingscup.activity.menu.MainActivity
import pwr.am.kingscup.event.*
import pwr.am.kingscup.render.Drawable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.random.Random

class GameClient(
    var gameKey: String,
    val playerKey: String,
    val context: GameBoardActivity,
    val drawables: ArrayList<Drawable>
) {

    private val database = Firebase.database
    private var referenceGames = database.getReference("games")
    private var gameTick = 1
    private var gender = ""
    private var current_card_id = 0
    private lateinit var listenerToGameData: ValueEventListener
    private lateinit var listenerToPlayers: ChildEventListener
    private lateinit var listenerToActivityTick: ValueEventListener
    private var currentEvent: Event = DeckSetupEvent(this)
    private var cardEvent: Event = DeckSetupEvent(this)
    private var playerArray = ArrayList<Player>()
    private var pickedPlayer = ""

    //todo player Listener
    //todo disconnect

    private fun sendResponse(
        data: String,
        additionalData: String,
        additionalData2: String? = null
    ) {
        referenceGames.child(gameKey).child("responses").push()
            .setValue(
                GameServer.Response(
                    playerKey,
                    gameTick,
                    data,
                    additionalData,
                    additionalData2
                )
            )
    }

    fun addListenerToPlayers() {
        listenerToPlayers = referenceGames.child(gameKey).child("players")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    playerArray.add(
                        Player(snapshot.key.toString(),true, snapshot.child("name").value.toString())
                    )
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    if (snapshot.key.toString() == playerKey) {
                        Toast.makeText(context, context.getString(R.string.server_down), Toast.LENGTH_LONG).show()
                        context.runOnUiThread{
                            val intent = Intent(
                                context,
                                MainActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            context.startActivity(intent)
                        }
                    }
                    else {
                        playerArray.find { it.playerKey == snapshot.key.toString() }?.isOnline = false
                        Toast.makeText(context, "player ${snapshot.child("name").value.toString()} disconnected", Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    fun addListenerToGameData() {
        listenerToGameData = referenceGames.child(gameKey).child("gamedata")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.e("GameClient","snapshot received: $snapshot")
                    if(snapshot.child("server_tick").value != null) {
                        if (gameTick < (snapshot.child("server_tick").value as Long).toInt()) {
                            Log.e("GameClient", snapshot.toString())
                            gameTick = (snapshot.child("server_tick").value as Long).toInt()
                            handleServerUpdate(snapshot)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun addListenerToActivity() {
        listenerToActivityTick = referenceGames.child("$gameKey/activity/tick")
            .addValueEventListener(object : ValueEventListener {
                val playerReference = referenceGames.child("$gameKey/activity/$playerKey")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value != null)
                        playerReference.setValue(snapshot.value as Long)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getPlayerGender() {
        referenceGames.child(gameKey).child("players").child(playerKey).child("gender").get()
            .addOnSuccessListener {
                gender = it.value.toString()
            }
    }

    fun setupDeck() {
        currentEvent = DeckSetupEvent(this)

        val referenceCards = referenceGames.child(gameKey).child("card_set")

        referenceCards.get().addOnSuccessListener{
            val cardIdList : ArrayList<Int> = ArrayList()
            for (i in 0..51){
                if((it.child(i.toString()).value as Long).toInt()>0)
                    cardIdList.add(i)
            }
            (currentEvent as DeckSetupEvent).deckInit(cardIdList)
            currentEvent.start()
        }
    }

    fun handleServerUpdate(snapshot: DataSnapshot) {
        val current_player_id = snapshot.child("current_player_id").value.toString()
        current_card_id = (snapshot.child("current_card_id").value as Long).toInt()
        val game_status = snapshot.child("game_status").value.toString()
        currentEvent.end()
        when (game_status) {
            "DrawCard" -> {
                Log.e("Client ", "DrawCard")
                currentEvent = ShuffleEvent(this)
                if (current_player_id == playerKey)
                        (currentEvent as ShuffleEvent).enableDraw()
                currentEvent.start()
            }
            "CardAction" -> {
                Log.e("Client ", "CardAction")
                currentEvent = DrawEvent(this)
                (currentEvent as DrawEvent).setCard(current_card_id)
                when (current_card_id) {
                    //Ace - everyone without current player drinks
                    0, 13, 26, 39 -> {
                        if (current_player_id == playerKey) {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Other players drink")
                            (cardEvent as InfoEvent).sendCardActionDone()
                        } else {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText("You have to take a drink")
                            (cardEvent as InfoAcceptEvent).setButtonText("I drank")
                            (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                        }
                    }
                    //Two - player chooses other player to take a drink
                    1, 14, 27, 40 -> {
                        if (current_player_id == playerKey) {
                            cardEvent = PlayerChooseEvent(this)
                            (cardEvent as PlayerChooseEvent).setPlayers(playerArray)
                            (cardEvent as PlayerChooseEvent).setKey("player_choose_event_drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Player ${playerArray.find { it.playerKey == current_player_id }?.name} chooses another player")
                        }
                    }
                    //Three - only current player drinks
                    2, 15, 28, 41 -> {
                        if (current_player_id == playerKey) {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText("You have to take a drink")
                            (cardEvent as InfoAcceptEvent).setButtonText("I drank")
                            (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Player ${playerArray.find { it.playerKey == current_player_id }?.name} have to take a drink")
                            (cardEvent as InfoEvent).sendCardActionDone()
                        }
                    }
                    //Four - last player to touch the floor has to drink
                    3, 16, 29, 42 -> {
                        cardEvent = AccelerationEvent(this)
                        (cardEvent as AccelerationEvent).setDown()
                    }
                    //Five - all males drinks
                    4, 17, 30, 43 -> {
                        if (gender == "male") {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText("You have to take a drink")
                            (cardEvent as InfoAcceptEvent).setButtonText("I drank")
                            (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Every male have to take a drink")
                            (cardEvent as InfoEvent).sendCardActionDone()
                        }
                    }
                    //Six - all females drinks
                    5, 18, 31, 44 -> {
                        if (gender == "female") {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText("You have to take a drink")
                            (cardEvent as InfoAcceptEvent).setButtonText("I drank")
                            (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Every female have to take a drink")
                            (cardEvent as InfoEvent).sendCardActionDone()
                        }
                    }
                    //Seven - last person to raise their hand has to drink
                    6, 19, 32, 45 -> {
                        cardEvent = AccelerationEvent(this)
                        (cardEvent as AccelerationEvent).setUp()
                    }
                    //Eight - random players drinks
                    7, 20, 33, 46 -> {
                        if (Random.nextInt(0, 2) == 1) {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText("You have to take a drink")
                            (cardEvent as InfoAcceptEvent).setButtonText("I drank")
                            (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("You where lucky :)")
                            (cardEvent as InfoEvent).sendCardActionDone()
                        }
                    }
                    //Queen - pick player and ask him question
                    11, 24, 37, 50 -> {
                        if (current_player_id == playerKey) {
                            cardEvent = PlayerChooseEvent(this)
                            (cardEvent as PlayerChooseEvent).setPlayers(playerArray)
                            (cardEvent as PlayerChooseEvent).setKey("player_choose_event_queen")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Player ${playerArray.find { it.playerKey == current_player_id }?.name} chooses another player to answer question")
                        }
                    }
                    //King - all players finish drinks
                    12, 25, 38, 51 -> {
                        cardEvent = InfoAcceptEvent(this)
                        (cardEvent as InfoAcceptEvent).setText("You have to finish your drink")
                        (cardEvent as InfoAcceptEvent).setButtonText("I finish")
                        (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                    }
                }
                currentEvent.start()
            }
            "Drinks" -> {
                Log.e("Client ", "Drinks")
                if (playerKey == snapshot.child("players_to_drink").value.toString()) {
                    currentEvent = InfoAcceptEvent(this)
                    (currentEvent as InfoAcceptEvent).setText("You have to take a drink")
                    (currentEvent as InfoAcceptEvent).setButtonText("I finish")
                    (currentEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                } else {
                    currentEvent = InfoEvent(this)
                    (currentEvent as InfoEvent).setText("Player  ${playerArray.find { it.playerKey == snapshot.child("players_to_drink").value.toString() }?.name} have to take a drink")

                }
                currentEvent.start()
            }
            "Question" -> {
                Log.e("Client ", "Question")
                if (playerKey == snapshot.child("selected_player_for_question").value.toString()) {
                    currentEvent.end()
                    currentEvent = TextInputEvent(this)
                    (currentEvent as TextInputEvent).setText(snapshot.child("question").value.toString())
                    (currentEvent as TextInputEvent).setHint("answer")
                    (currentEvent as TextInputEvent).setButtonText("Answer")
                    (currentEvent as TextInputEvent).setKey("text_input_event_answer")
                    (currentEvent as TextInputEvent).start()

                } else {
                    currentEvent = InfoEvent(this)
                    (currentEvent as InfoEvent).setText(
                        "Player ${
                            playerArray.find {
                                it.playerKey == snapshot.child(
                                    "selected_player_for_question"
                                ).value.toString()
                            }?.name
                        } answers question"
                    )
                    currentEvent.start()
                }
            }
            "Answer" -> {
                Log.e("Client ", "Answer")
                currentEvent = InfoEvent(this)
                (currentEvent as InfoEvent).setText("Question: ${snapshot.child("question").value.toString()}\n Answer: ${snapshot.child("answer").value.toString()}")
                currentEvent.start()
                Timer("task", false).schedule(5000) {
                    sendResponse("CardActionDone", "")
                }
            }

            "AcceptThisRound" -> {
                Log.e("Client ", "AcceptThisRound")
                currentEvent = InfoAcceptEvent(this)
                (currentEvent as InfoAcceptEvent).setText("Ready for next?")
                (currentEvent as InfoAcceptEvent).setButtonText("Yes")
                (currentEvent as InfoAcceptEvent).setKey("info_accept_event_accept")
                currentEvent.start()
            }
            "FinishGame" ->{
                Log.e("Client ", "FinishGame")
                referenceGames.child(gameKey).child("players").removeEventListener(listenerToPlayers)
                referenceGames.child(gameKey).child("gamedata").removeEventListener(listenerToGameData)
                referenceGames.child(gameKey).child("activity/tick").removeEventListener(listenerToActivityTick)
                context.startEndGameActivity()
            }
        }
    }


    //function called by events
    fun respond(key: String, value: Any) {
        when (key) {
            "deck_setup_event_done" -> {
                sendResponse("Join", "")
            }

            "shuffle_event_drawn" -> {
                currentEvent.end()
                sendResponse("Drawn", "")
            }

            "draw_event_done" -> {
                currentEvent.end()
                currentEvent = cardEvent
                if (currentEvent is AccelerationEvent) {
                    currentEvent.start()
                } else {
                    Timer("task", false).schedule(2000) {
                        currentEvent.start()
                    }
                }
            }

            "info_accept_event_drink" -> {
                sendResponse("CardActionDone", "")
                currentEvent.end()
            }

            "info_accept_event_accept" -> {
                currentEvent.end()
                currentEvent = RemoveCardEvent(this)
                (currentEvent as RemoveCardEvent).setCard(current_card_id)
                currentEvent.start()
                sendResponse("Accepted", "")
            }


            "player_choose_event_drink" -> {
                currentEvent.end()
                sendResponse("PickedPlayer", value.toString())
            }

            "player_choose_event_queen" -> {
                pickedPlayer = value.toString()
                currentEvent.end()
                currentEvent = TextInputEvent(this)
                (currentEvent as TextInputEvent).setButtonText("Ask")
                (currentEvent as TextInputEvent).setText("Enter question")
                (currentEvent as TextInputEvent).setKey("text_input_event_question")
                (currentEvent as TextInputEvent).setHint("question")
                (currentEvent as TextInputEvent).start()
            }

            "text_input_event_question"-> {
                currentEvent.end()
                sendResponse("PickedPlayer", pickedPlayer, value.toString())
            }

            "text_input_event_answer"-> {
                currentEvent.end()
                sendResponse("Answer", value.toString())
            }

            "acceleration_event_time" -> {
                Log.e("AccelerationEvent", value.toString())
                currentEvent.end()
                currentEvent = InfoEvent(this)
                with(value as Long) {
                    if (this == 0L) {
                        (currentEvent as InfoEvent).setText("Wrong Move :(")
                        Timer("task", false).schedule(2000) {
                            sendResponse("Time", "5000")
                        }
                    } else if(this == 5000L){
                        (currentEvent as InfoEvent).setText("Timeout :(")
                        Timer("task", false).schedule(2000) {
                            sendResponse("Time", "5000")
                        }
                    } else {
                        (currentEvent as InfoEvent).setText("Your time: " + this.toString() + "ms")
                        Timer("task", false).schedule(2000) {
                            sendResponse("Time", value.toString())
                        }
                    }
                }
                currentEvent.start()
            }
            "CardActionDone"->{
                Timer("task", false).schedule(2000) {
                    sendResponse("CardActionDone", "")
                }
            }

            else -> Log.e("GameClient", "Unhandled event response key! [$key]")
        }
    }
    data class Player(
        val playerKey: String,
        var isOnline: Boolean = true,
        var name: String,
    )
}