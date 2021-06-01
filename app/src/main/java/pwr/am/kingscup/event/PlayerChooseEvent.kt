package pwr.am.kingscup.event

import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pwr.am.kingscup.Game
import pwr.am.kingscup.PlayerLogic
import pwr.am.kingscup.R
import pwr.am.kingscup.databinding.PlayerViewRowBinding
import pwr.am.kingscup.databinding.PlayetChooseViewBinding
import java.util.*
import kotlin.collections.ArrayList

class PlayerChooseEvent(game: PlayerLogic): Event(game) {

    private var binding = PlayetChooseViewBinding.inflate(LayoutInflater.from(game.context))
    private lateinit var players: ArrayList<Pair<String, String>>
    private var chosen = false


    fun setPlayers(playerList: ArrayList<Pair<String, String>>) {
        players = playerList
    }
    private var key = "player"
    fun setKey(key :String){
        this.key = key
    }

    override fun start() {
        game.context.runOnUiThread {

            binding.playerList.removeAllViews()
            for (player: Pair<String, String> in players) {
                val row = PlayerViewRowBinding.inflate(LayoutInflater.from(game.context))
                row.nickName.text = player.second
                row.kickButton.visibility = View.VISIBLE
                row.kickButton.text = game.context.getString(R.string.choose)
                row.kickButton.setOnClickListener{
                    if(!chosen) {
                        chosen = true
                        game.respond(key, player.first)
                    }
                }
                binding.playerList.addView(row.root)
            }

            game.context.addContentView(
                binding.root,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }
    }

    override fun end() {
        game.context.runOnUiThread {
            if (binding.root.parent != null)
                (binding.root.parent as ViewGroup).removeView(binding.root)
        }
    }
}