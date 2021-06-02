package pwr.am.kingscup

import android.util.Log
import android.util.Pair
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.event.*
import pwr.am.kingscup.render.Drawable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.random.Random

class PlayerLogic(
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
    private var currentEvent: Event = DeckSetupEvent(this)
    private var cardEvent: Event = DeckSetupEvent(this)
    private var playerArray = ArrayList<Pair<String, String>>()
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
                GameLogic.Response(
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
                        Pair(
                            snapshot.key.toString(),
                            snapshot.child("name").value.toString()
                        )
                    )
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    if (snapshot.key.toString() == playerKey) {
                        //TODO END CLIENT
                    }
                    for (player in playerArray) {
                        if (player.first == snapshot.key.toString())
                            playerArray.remove(player)
                    }
                }
            })
    }

    fun addListenerToGameData() {
        listenerToGameData = referenceGames.child(gameKey).child("gamedata")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.e("POGO", snapshot.toString())
                    if (gameTick < (snapshot.child("server_tick").value as Long).toInt()) {
                        gameTick = (snapshot.child("server_tick").value as Long).toInt()
                        handleServerUpdate(snapshot)
                    }
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
                if (current_player_id == playerKey) {

                    currentEvent = ShuffleEvent(this)
                    (currentEvent as ShuffleEvent).enableDraw()
                    currentEvent.start()
                }
            }
            "CardAction" -> {
                currentEvent = DrawEvent(this)
                (currentEvent as DrawEvent).setCard(current_card_id)
                when (current_card_id) {
                    //Ace - everyone without current player drinks
                    0, 13, 26, 39 -> {
                        if (current_player_id == playerKey) {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Other players drink")
                            sendResponse("CardActionDone", "")
                        } else {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText("You have to take a drink")
                            (cardEvent as InfoAcceptEvent).setButtonText("I drank")
                            (cardEvent as InfoAcceptEvent).setResponce("drink")
                        }
                    }
                    //Two - player chooses other player to take a drink
                    1, 14, 27, 40 -> {
                        if (current_player_id == playerKey) {
                            cardEvent = PlayerChooseEvent(this)
                            (cardEvent as PlayerChooseEvent).setPlayers(playerArray)
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Player ${playerArray.find { it.first == current_player_id }?.second} chooses another player")
                        }
                    }
                    //Three - only current player drinks
                    2, 15, 28, 41 -> {
                        if (current_player_id == playerKey) {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText("You have to take a drink")
                            (cardEvent as InfoAcceptEvent).setButtonText("I drank")
                            (cardEvent as InfoAcceptEvent).setResponce("drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Player ${playerArray.find { it.first == current_player_id }?.second} have to take a drink")
                            sendResponse("CardActionDone", "")
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
                            (cardEvent as InfoAcceptEvent).setResponce("drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Every male have to take a drink")
                            sendResponse("CardActionDone", "")
                        }
                    }
                    //Six - all females drinks
                    5, 18, 31, 44 -> {
                        if (gender == "female") {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText("You have to take a drink")
                            (cardEvent as InfoAcceptEvent).setButtonText("I drank")
                            (cardEvent as InfoAcceptEvent).setResponce("drink")
                        } else {
                            sendResponse("CardActionDone", "")
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Every female have to take a drink")
                            sendResponse("CardActionDone", "")
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
                            (cardEvent as InfoAcceptEvent).setResponce("drink")
                        } else {
                            sendResponse("CardActionDone", "")
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("You where lucky :)")
                            sendResponse("CardActionDone", "")
                        }
                    }
                    //Queen - pick player and ask him question
                    11, 24, 37, 50 -> {
                        if (current_player_id == playerKey) {
                            cardEvent = PlayerChooseEvent(this)
                            (cardEvent as PlayerChooseEvent).setPlayers(playerArray)
                            (cardEvent as PlayerChooseEvent).setKey("text")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText("Player ${playerArray.find { it.first == current_player_id }?.second} chooses another player to answer question")
                        }
                    }
                    //King - all players finish drinks
                    12, 25, 38, 51 -> {
                        cardEvent = InfoAcceptEvent(this)
                        (cardEvent as InfoAcceptEvent).setText("You have to finish your drink")
                        (cardEvent as InfoAcceptEvent).setButtonText("I finish")
                        (cardEvent as InfoAcceptEvent).setResponce("drink")
                    }
                }
                currentEvent.start()
            }
            "Drinks" -> {
                if (playerKey == snapshot.child("players_to_drink").value.toString()) {
                    currentEvent = InfoAcceptEvent(this)
                    (currentEvent as InfoAcceptEvent).setText("You have to take a drink")
                    (currentEvent as InfoAcceptEvent).setButtonText("I finish")
                    (currentEvent as InfoAcceptEvent).setResponce("drink")
                } else {
                    currentEvent = InfoEvent(this)
                    (currentEvent as InfoEvent).setText("Player  ${playerArray.find { it.first == current_player_id }?.second} have to take a drink")

                }
                currentEvent.start()
            }
            "Question" -> {
                if (playerKey == snapshot.child("selected_player_for_question").value.toString()) {
                    currentEvent.end()
                    currentEvent = TextInputEvent(this)
                    (currentEvent as TextInputEvent).setHint(snapshot.child("question").value.toString())
                    (currentEvent as TextInputEvent).setButtonText("Answer")
                    (currentEvent as TextInputEvent).setKey("answer")
                    (currentEvent as TextInputEvent).start()

                } else {
                    currentEvent = InfoEvent(this)
                    (currentEvent as InfoEvent).setText(
                        "Player ${
                            playerArray.find {
                                it.first == snapshot.child(
                                    "selected_player_for_question"
                                ).value.toString()
                            }?.second
                        } answers question"
                    )
                    currentEvent.start()
                }
            }
            "Answer" -> {
                currentEvent = InfoEvent(this)
                (currentEvent as InfoEvent).setText("Question: ${snapshot.child("question").value.toString()}\n Answer: ${snapshot.child("answer").value.toString()}")
                currentEvent.start()
                Timer("task", false).schedule(5000) {
                    sendResponse("CardActionDone", "")
                }
            }

            "AcceptThisRound" -> {
                currentEvent = InfoAcceptEvent(this)
                (currentEvent as InfoAcceptEvent).setText("Ready for next?")
                (currentEvent as InfoAcceptEvent).setButtonText("Yes")
                (currentEvent as InfoAcceptEvent).setResponce("accept")
                currentEvent.start()
            }
        }
    }


    //function called by events
    fun respond(key: String, value: Any) {
        when (currentEvent) {
            is DeckSetupEvent -> {
                sendResponse("Join", "")
            }
            is ShuffleEvent -> {
                currentEvent.end()
                sendResponse("Drawn", "")
            }
            is DrawEvent -> {
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
            is InfoAcceptEvent -> {
                if (key == "drink") {
                    sendResponse("CardActionDone", "")
                    currentEvent.end()
                }
                if (key == "accept") {
                    currentEvent.end()
                    currentEvent = RemoveCardEvent(this)
                    (currentEvent as RemoveCardEvent).setCard(current_card_id)
                    currentEvent.start()
                    sendResponse("Accepted", "")
                }
            }
            is PlayerChooseEvent -> {
                if (key == "player") {
                    currentEvent.end()
                    sendResponse("PickedPlayer", value.toString())
                } else if (key == "text") {
                    pickedPlayer = value.toString()
                    currentEvent.end()
                    currentEvent = TextInputEvent(this)
                    (currentEvent as TextInputEvent).setButtonText("Write question")
                    (currentEvent as TextInputEvent).setHint("")
                    (currentEvent as TextInputEvent).start()
                }
            }
            is TextInputEvent -> {
                if (key == "text") {
                    currentEvent.end()
                    sendResponse("PickedPlayer", pickedPlayer, value.toString())
                } else if (key == "answer") {
                    currentEvent.end()
                    sendResponse("Answer", value.toString())
                }
            }

            is AccelerationEvent -> {
                Log.e("AccelerationEvent", value.toString())
                currentEvent.end()
                currentEvent = InfoEvent(this)
                with(value as Long) {
                    if (this == 0L) {
                        (currentEvent as InfoEvent).setText("Wrong Move :(")
                        sendResponse("Time", "1111111111")
                    } else {
                        sendResponse("Time", value.toString())
                        (currentEvent as InfoEvent).setText("Your time: " + this.toString() + "ms")
                    }
                }
                currentEvent.start()
            }
        }
    }
}