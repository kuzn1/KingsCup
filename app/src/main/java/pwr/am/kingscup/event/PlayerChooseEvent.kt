package pwr.am.kingscup.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pwr.am.kingscup.Game
import pwr.am.kingscup.R
import pwr.am.kingscup.databinding.PlayerViewRowBinding
import pwr.am.kingscup.databinding.PlayetChooseViewBinding

class PlayerChooseEvent(game: Game): Event(game) {

    private lateinit var binding: PlayetChooseViewBinding
    private lateinit var players: ArrayList<Pair<String, String>>
    private var chosen = false

    fun setPlayers(playerList: ArrayList<Pair<String, String>>) {
        players = playerList
    }

    override fun start() {
        binding = PlayetChooseViewBinding.inflate(LayoutInflater.from(game.context))

        binding.playerList.removeAllViews()
        for (player: Pair<String, String> in players) {
            val row = PlayerViewRowBinding.inflate(LayoutInflater.from(game.context))
            row.nickName.text = player.first
            row.kickButton.visibility = View.VISIBLE
            row.kickButton.text = game.context.getString(R.string.choose)
            row.kickButton.setOnClickListener{
                if(!chosen) {
                    chosen = true
                    game.respond("player", player.second)
                }
            }
            binding.playerList.addView(row.root)
        }

        game.context.addContentView(
            binding.root,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    override fun end() {
        (binding.root.parent as ViewGroup).removeView(binding.root)
    }
}