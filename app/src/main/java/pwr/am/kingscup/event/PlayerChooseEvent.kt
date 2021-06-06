package pwr.am.kingscup.event

import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pwr.am.kingscup.services.GameClient
import pwr.am.kingscup.R
import pwr.am.kingscup.databinding.PlayerViewRowBinding
import pwr.am.kingscup.databinding.PlayetChooseViewBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class PlayerChooseEvent(game: GameClient) : Event(game) {

    private var binding = PlayetChooseViewBinding.inflate(LayoutInflater.from(game.context))
    private var initialized = false
    private lateinit var players: ArrayList<GameClient.Player>
    private var key = "player_choose_event"
    private var chosen = false

    fun setPlayers(playerList: ArrayList<GameClient.Player>) {
        players = playerList
    }

    fun setKey(key: String) {
        this.key = key
    }

    override fun start() {
        game.context.runOnUiThread {

            binding.playerList.removeAllViews()
            for (player in players) {
                if (player.isOnline) {
                    val row = PlayerViewRowBinding.inflate(LayoutInflater.from(game.context))
                    row.nickName.text = player.name
                    row.nickName.textSize = 14f
                    row.kickButton.textSize = 14f
                    row.kickButton.visibility = View.VISIBLE
                    row.kickButton.text = game.context.getString(R.string.choose)
                    row.kickButton.setOnClickListener {
                        if (!chosen) {
                            chosen = true
                            game.respond(key, player.playerKey)
                        }
                    }
                    binding.playerList.addView(row.root)
                }
            }

            game.context.addContentView(
                binding.root,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            initialized = true
        }
    }

    override fun end() {
        if (initialized)
            game.context.runOnUiThread {
                if (binding.root.parent != null)
                    (binding.root.parent as ViewGroup).removeView(binding.root)
            }
        else Timer("Delete PCE", true).schedule(200){
            if (initialized)
                game.context.runOnUiThread {
                    if (binding.root.parent != null)
                        (binding.root.parent as ViewGroup).removeView(binding.root)
                }
        }
    }
}