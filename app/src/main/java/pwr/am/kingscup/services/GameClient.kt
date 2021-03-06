package pwr.am.kingscup.services

import android.content.Intent
import android.speech.tts.TextToSpeech
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
) : TextToSpeech.OnInitListener{

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
    lateinit var textToSpeech  : TextToSpeech
    var ttsInitialized = false
    var enableSfxSound = true
    var enableCardSound = true


    override fun onInit(status: Int) {
        if(status != TextToSpeech.ERROR){
            textToSpeech.language = Locale.ENGLISH
            if(enableCardSound) ttsInitialized = true
        }
    }

    fun initTTS(){
        textToSpeech = TextToSpeech(context, this)
    }

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
                        removeAllListeners()
                        Toast.makeText(
                            context,
                            context.getString(R.string.server_down),
                            Toast.LENGTH_LONG
                        ).show()
                        context.runOnUiThread {
                            val intent = Intent(
                                context,
                                MainActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            context.startActivity(intent)
                        }
                    }
                    else {
                        playerArray.find { it.playerKey == snapshot.key.toString() }?.isOnline = false
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.playerStringHasDisconnected,
                                snapshot.child("name").value.toString()
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }

    fun addListenerToGameData() {
        listenerToGameData = referenceGames.child(gameKey).child("gamedata")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.e("GameClient", "snapshot received: $snapshot")
                    if (snapshot.child("server_tick").value != null) {
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
                    if (snapshot.value != null)
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

        referenceCards.get().addOnSuccessListener {
            val cardIdList: ArrayList<Int> = ArrayList()
            for (i in 0..51) {
                if ((it.child(i.toString()).value as Long).toInt() > 0)
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
                            (cardEvent as InfoEvent).setText(context.getString(R.string.otherPlayersDrink))
                            (cardEvent as InfoEvent).sendCardActionDone()
                        } else {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText(context.getString(R.string.youHaveToTakeADrink))
                            (cardEvent as InfoAcceptEvent).setButtonText(context.getString(R.string.done))
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
                            (cardEvent as InfoEvent).setText(
                                context.getString(
                                    R.string.playerChoosesAnotherPlayer,
                                    playerArray.find { it.playerKey == current_player_id }?.name
                                )
                            )
                        }
                    }
                    //Three - only current player drinks
                    2, 15, 28, 41 -> {
                        if (current_player_id == playerKey) {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText(context.getString(R.string.youHaveToTakeADrink))
                            (cardEvent as InfoAcceptEvent).setButtonText(context.getString(R.string.done))
                            (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText(
                                context.getString(
                                    R.string.playerStringHaveToTakeADrink,
                                    playerArray.find { it.playerKey == current_player_id }?.name
                                )
                            )
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
                            (cardEvent as InfoAcceptEvent).setText(context.getString(R.string.youHaveToTakeADrink))
                            (cardEvent as InfoAcceptEvent).setButtonText(context.getString(R.string.done))
                            (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText(context.getString(R.string.everyMaleHasToTakeADrink))
                            (cardEvent as InfoEvent).sendCardActionDone()
                        }
                    }
                    //Six - all females drinks
                    5, 18, 31, 44 -> {
                        if (gender == "female") {
                            cardEvent = InfoAcceptEvent(this)
                            (cardEvent as InfoAcceptEvent).setText(context.getString(R.string.youHaveToTakeADrink))
                            (cardEvent as InfoAcceptEvent).setButtonText(context.getString(R.string.done))
                            (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText(context.getString(R.string.everyFemaleHasToTakeADrink))
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
                            (cardEvent as InfoAcceptEvent).setText(context.getString(R.string.youHaveToTakeADrink))
                            (cardEvent as InfoAcceptEvent).setButtonText(context.getString(R.string.done))
                            (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                        } else {
                            cardEvent = InfoEvent(this)
                            (cardEvent as InfoEvent).setText(context.getString(R.string.randomPlayersDrink))
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
                            (cardEvent as InfoEvent).setText(
                                context.getString(
                                    R.string.playerStringChoosesAnotherPlayer,
                                    playerArray.find { it.playerKey == current_player_id }?.name
                                )
                            )
                        }
                    }
                    //King - all players finish drinks
                    12, 25, 38, 51 -> {
                        cardEvent = InfoAcceptEvent(this)
                        (cardEvent as InfoAcceptEvent).setText(context.getString(R.string.youHaveToFinishDrink))
                        (cardEvent as InfoAcceptEvent).setButtonText(context.getString(R.string.done))
                        (cardEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                    }
                }
                currentEvent.start()
            }
            "Drinks" -> {
                Log.e("Client ", "Drinks")
                val playersToDrink = snapshot.child("players_to_drink").value.toString().split("|")

                if (playersToDrink.find { it == playerKey } != null) {
                    currentEvent = InfoAcceptEvent(this)
                    (currentEvent as InfoAcceptEvent).setText(context.getString(R.string.youHaveToTakeADrink))
                    (currentEvent as InfoAcceptEvent).setButtonText(context.getString(R.string.done))
                    (currentEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                } else {
                    currentEvent = InfoEvent(this)
                    var string = ""
                    for (player in playersToDrink)
                        if (playerArray.find { it.playerKey == player }?.name != null) {
                            string = string.plus(playerArray.find { it.playerKey == player }?.name)
                            string = string.plus(", ")
                        }
                    string = string.dropLast(2)


                    (currentEvent as InfoEvent).setText(context.getString(R.string.playerStringHaveToTakeADrink,string))

                }
                currentEvent.start()
            }
            "MultipleDrinks" -> {
                Log.e("Client ", "Drinks")
                val input = snapshot.child("players_to_drink_with_time").value.toString().split("|")
                val playersToDrink = snapshot.child("players_to_drink").value.toString().split("|")

                val playersToDrinkWithTime = ArrayList<Pair<String, String>>()
                for (i in input.indices step 2) {
                    playersToDrinkWithTime.add(Pair(input[i], input[i + 1]))
                }
                var string = ""
                for (player in playersToDrinkWithTime)
                    if (playerArray.find { it.playerKey == player.first }?.name != null) {
                        string =
                            string.plus(playerArray.find { it.playerKey == player.first }?.name)
                        string = string.plus(" - ")
                        string = string.plus(player.second)
                        string = string.plus("\n")
                    }


                if (playersToDrink.find { it == playerKey } != null) {
                    currentEvent = InfoAcceptEvent(this)
                    (currentEvent as InfoAcceptEvent).setText(context.getString(R.string.youHaveToTakeADrink) + "\n\n $string")
                    (currentEvent as InfoAcceptEvent).setButtonText(context.getString(R.string.done))
                    (currentEvent as InfoAcceptEvent).setKey("info_accept_event_drink")
                } else {
                    currentEvent = InfoEvent(this)
                    (currentEvent as InfoEvent).setText(context.getString(R.string.youAreNoTheLastOne, string))

                }
                currentEvent.start()
            }

            "Question" -> {
                Log.e("Client ", "Question")
                if (playerKey == snapshot.child("selected_player_for_question").value.toString()) {
                    currentEvent.end()
                    currentEvent = TextInputEvent(this)
                    (currentEvent as TextInputEvent).setText(snapshot.child("question").value.toString())
                    (currentEvent as TextInputEvent).setHint(context.getString(R.string.answer))
                    (currentEvent as TextInputEvent).setButtonText(context.getString(R.string.answer))
                    (currentEvent as TextInputEvent).setKey("text_input_event_answer")
                    (currentEvent as TextInputEvent).start()

                } else {
                    currentEvent = InfoEvent(this)
                    (currentEvent as InfoEvent).setText(
                        context.getString(
                            R.string.playerAnswersQuestion,
                            playerArray.find {
                                it.playerKey == snapshot.child(
                                    "selected_player_for_question"
                                ).value.toString()
                            }?.name
                        )
                    )
                    currentEvent.start()
                }
            }
            "Answer" -> {
                Log.e("Client ", "Answer")
                currentEvent = InfoEvent(this)
                (currentEvent as InfoEvent).setText(
                    context.getString(
                        R.string.questionAnswer,
                        snapshot.child("question").value.toString(),
                        snapshot.child("answer").value.toString()
                    )
                )
                currentEvent.start()
                Timer("task", false).schedule(5000) {
                    sendResponse("CardActionDone", "")
                }
            }
            "AcceptThisRound" -> {
                Log.e("Client ", "AcceptThisRound")
                currentEvent = InfoAcceptEvent(this)
                (currentEvent as InfoAcceptEvent).setText(context.getString(R.string.readyForNext))
                (currentEvent as InfoAcceptEvent).setButtonText(context.getString(R.string.yes))
                (currentEvent as InfoAcceptEvent).setKey("info_accept_event_accept")
                currentEvent.start()
            }
            "FinishGame" -> {
                Log.e("Client ", "FinishGame")
                removeAllListeners()
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
                Timer("task", false).schedule(1000) {
                    sendResponse("Accepted", "")
                }
            }


            "player_choose_event_drink" -> {
                currentEvent.end()
                sendResponse("PickedPlayer", value.toString())
            }

            "player_choose_event_queen" -> {
                pickedPlayer = value.toString()
                currentEvent.end()
                currentEvent = TextInputEvent(this)
                (currentEvent as TextInputEvent).setButtonText(context.getString(R.string.done))
                (currentEvent as TextInputEvent).setText(context.getString(R.string.enterQuestion))
                (currentEvent as TextInputEvent).setKey("text_input_event_question")
                (currentEvent as TextInputEvent).setHint(context.getString(R.string.question))
                (currentEvent as TextInputEvent).start()
            }

            "text_input_event_question" -> {
                currentEvent.end()
                sendResponse("PickedPlayer", pickedPlayer, value.toString())
            }

            "text_input_event_answer" -> {
                currentEvent.end()
                sendResponse("Answer", value.toString())
            }

            "acceleration_event_time" -> {
                Log.e("AccelerationEvent", value.toString())
                currentEvent.end()
                currentEvent = InfoEvent(this)
                with(value as Long) {
                    if (this == 0L) {
                        (currentEvent as InfoEvent).setText(context.getString(R.string.wrongMove))
                        Timer("task", false).schedule(2000) {
                            sendResponse("Time", "5000")
                        }
                    } else if(this == 5000L){
                        (currentEvent as InfoEvent).setText(context.getString(R.string.timeout))
                        Timer("task", false).schedule(2000) {
                            sendResponse("Time", "5000")
                        }
                    } else {
                        (currentEvent as InfoEvent).setText(
                            context.getString(
                                R.string.yourTimems,
                                this.toString()
                            )
                        )
                        Timer("task", false).schedule(2000) {
                            sendResponse("Time", value.toString())
                        }
                    }
                }
                currentEvent.start()
            }
            "CardActionDone" -> {
                Timer("task", false).schedule(2000) {
                    sendResponse("CardActionDone", "")
                }
            }

            else -> Log.e("GameClient", "Unhandled event response key! [$key]")
        }
    }

    fun removeAllListeners() {
        Log.e("Client ", "removeAllListeners")
        referenceGames.child(gameKey).child("players").removeEventListener(listenerToPlayers)
        referenceGames.child(gameKey).child("gamedata").removeEventListener(listenerToGameData)
        referenceGames.child(gameKey).child("activity/tick").removeEventListener(listenerToActivityTick)
    }

    data class Player(
        val playerKey: String,
        var isOnline: Boolean = true,
        var name: String,
    )
}