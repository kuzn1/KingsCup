package pwr.am.kingscup

import android.app.Activity
import android.os.Bundle
import android.view.View
import pwr.am.kingscup.databinding.ActivityPlayerViewBinding
import android.view.LayoutInflater
import android.widget.TableRow
import android.widget.Toast
import pwr.am.kingscup.databinding.PlayerViewRowBinding


class PlayerViewActivity : Activity() {
    private lateinit var binding: ActivityPlayerViewBinding
    private lateinit var players : Array<Pair<String,Int>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadPlayers(intent.getBooleanExtra("OWNER", false))
    }

    fun loadPlayers(owner : Boolean){
        //todo get players from database
        players = arrayOf(Pair("PLAYER_1", 1),Pair("PLAYER_2", 2),Pair("PLAYER_3", 3),Pair("PLAYER_4", 4),
            Pair("PLAYER_5", 5),Pair("PLAYER_6", 6),Pair("PLAYER_7", 7),Pair("PLAYER_8", 8),Pair("PLAYER_9", 9),
            Pair("PLAYER_10", 10),Pair("PLAYER_11", 11),Pair("PLAYER_12", 12))

        for(player : Pair<String,Int> in  players){
            val row = PlayerViewRowBinding.inflate(LayoutInflater.from(this))
            row.nickName.text = player.first
            if(owner){
                row.kickButton.visibility = View.VISIBLE
                row.kickButton.setOnClickListener{ kickPlayer(player.second) }
            }
            binding.playerList.addView(row.root)
        }

    }

    fun kickPlayer(playerID : Int){
        //todo send player kick to server
        Toast.makeText(this,"Kick player:" + playerID, Toast.LENGTH_SHORT).show()
    }

    fun back(view: View) {
        finish()
    }

}