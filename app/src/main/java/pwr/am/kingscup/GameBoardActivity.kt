package pwr.am.kingscup

import android.app.Activity
import android.os.Bundle
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.databinding.ActivityGameBoardBinding

class GameBoardActivity : Activity() {
    private lateinit var binding: ActivityGameBoardBinding
    private var owner: Boolean = false
    private lateinit var gameKey: String
    private lateinit var playerKey: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBoardBinding.inflate(layoutInflater)
        owner = intent.getBooleanExtra("OWNER", false)
        playerKey = intent.getStringExtra("playerKey").toString()
        gameKey = intent.getStringExtra("gameKey").toString()

        if(owner){
            //TODO Start GameLogicClass
            //temp
            Firebase.database.getReference("games").child(gameKey).child("gamedata").child("server_tick").setValue(1)
        }
        //TODO Start BoardClass


        setContentView(binding.root)
    }
}