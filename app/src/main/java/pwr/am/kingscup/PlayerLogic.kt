package pwr.am.kingscup

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random.Default.nextInt

class PlayerLogic(var gameKey: String, val playerKey: String, ) {

    private val database = Firebase.database
    private var referenceGames = database.getReference("games")
    private var gameTick = 1
    private var gender = ""
    private lateinit var listenerToGameData: ChildEventListener

    //todo player Listener
    //todo disconnect


    private fun sendResponse(data: String, additionalData: String) {
        referenceGames.child(gameKey).child("responses").push()
            .setValue(GameLogic.Response(playerKey, gameTick, data, additionalData))
    }


    //todo remove  Listener
    fun addListenerToGameData() {
        referenceGames.child(gameKey).child("gamedata")
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    if (gameTick < (snapshot.child("server_tick").value as Long).toInt()) {
                        gameTick = (snapshot.child("server_tick").value as Long).toInt()
                        handleServerUpdate(snapshot)
                    }
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
            })
    }

    fun getPlayerGender() {
        referenceGames.child(gameKey).child("players").child(playerKey).child("gender").get()
            .addOnSuccessListener {
                gender = it.value.toString()
            }
    }

    fun handleServerUpdate(snapshot: DataSnapshot) {
        val current_player_id = snapshot.child("current_player_id").value.toString()
        val current_card_id = (snapshot.child("current_card_id").value as Long).toInt()
        val game_status = snapshot.child("game_status").value.toString()
        when (game_status) {
            "DrawCard" -> {
                if (current_player_id == playerKey) {
                    //todo Shuffle_Event(True);
                    //todo on result sendResponse("Drawn", "")
                }
            }
            "CardAction" -> {
                //todo Draw_Event(id);
                //TODO MAYBE WE WAIT FOR ANIMATION TO FINISH IDK
                when (current_card_id) {
                    //Ace - everyone without current player drinks
                    0, 13, 26, 39 -> {
                        if (current_player_id == playerKey) {
                            sendResponse("CardActionDone", "")
                            //todo event other players drink
                        } else {
                            //todo Info_Accept_Event("You have to take a drink");
                            //todo on result sendResponse("CardActionDone","")
                        }
                    }
                    //Two - player chooses other player to take a drink
                    1, 14, 27, 40 -> {
                        if (current_player_id == playerKey) {
                            //todo Info_Accept_Event("You have to choose other player to take a drink");
                            //todo on result sendResponse("PickedPlayer", playerKey)
                        } else {
                            //todo Info_Event("Player $current_player_id have to choose other player");
                        }
                    }
                    //Three - only current player drinks
                    2, 15, 28, 41 -> {
                        if (current_player_id == playerKey) {
                            //todo Info_Accept_Event("You have to take a drink");
                            //todo on result sendResponse("CardActionDone","")
                        } else {
                            sendResponse("CardActionDone", "")
                            //todo Info_Event("Player $current_player_id have to take a drink");
                        }
                    }
                    //Four - last player to touch the floor has to drink
                    3, 16, 29, 42 -> {
                        //todo Four_Floor_Event();
                        //todo on result sendResponse("Time", time)
                    }
                    //Five - all males drinks
                    4, 17, 30, 43 -> {
                        if (gender == "male") {
                            //todo Info_Accept_Event("You have to take a drink");
                            //todo on result sendResponse("CardActionDone","")
                        } else {
                            sendResponse("CardActionDone", "")
                            //todo Info_Event("Every male have to take a drink");
                        }
                    }
                    //Six - all females drinks
                    5, 18, 31, 44 -> {
                        if (gender == "female") {
                            //todo Info_Accept_Event("You have to take a drink");
                            //todo on result sendResponse("CardActionDone","")
                        } else {
                            sendResponse("CardActionDone", "")
                            //todo  Info_Event("Every female have to take a drink");
                        }
                    }
                    //Seven - last person to raise their hand has to drink
                    6, 19, 32, 45 -> {
                        //todo Seven_Haven_Event();
                        //todo on result sendResponse("Time", time)
                    }
                    //Eight - random players drinks
                    //TODO we can do this on server but this is easier
                    7, 20, 33, 46 -> {
                        if (nextInt(0, 2) == 1) {
                            //todo Info_Accept_Event("You have to take a drink");
                            //todo on result  sendResponse("CardActionDone", "")
                        } else {
                            sendResponse("CardActionDone", "")
                            //todo Info_Event("You where lucky :)");
                        }
                    }
                    //Queen - pick player and ask him question
                    11, 24, 37, 50 -> {
                        if (current_player_id == playerKey) {
                            //todo Info_Accept_Event("Chose player have to ask a question");
                            //todo Player_Choose_Event(playerList);
                            //todo Text_Input_Event();
                            //todo on result sendResponse("PickedPlayer", "playerKey|question")
                        } else {
                            //todo Info_Event("Player $current_player_id have to choose other player");
                        }
                    }
                    //King - all players finish drinks
                    12, 25, 38, 51  ->{
                        //todo Info_Accept_Event("You have to finish your drink");
                        sendResponse("CardActionDone", "")
                    }
                }
            }
            "Drinks" -> {
                if (playerKey == snapshot.child("players_to_drink").value.toString()) {
                    //todo Info_Accept_Event("You have to take a drink");
                    //todo on result sendResponse("CardActionDone", "")
                } else {
                    //todo Info_Event("Player $current_player_id have to take a drink");
                }
            }
            "Question" ->{
                //todo
                //todo Info_Accept_Event("QuestionAndResponse!");
            }

            "AcceptThisRound" -> {
                //todo Info_Accept_Event("Done!");
                //todo on result sendResponse("Accepted", "")
            }
        }
    }
}