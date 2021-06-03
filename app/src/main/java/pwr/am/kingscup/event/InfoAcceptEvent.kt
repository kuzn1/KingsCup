package pwr.am.kingscup.event

import android.view.LayoutInflater
import android.view.ViewGroup
import pwr.am.kingscup.Game
import pwr.am.kingscup.PlayerLogic
import pwr.am.kingscup.databinding.InfoViewBinding

class InfoAcceptEvent(game: PlayerLogic): Event(game) {

    private lateinit var binding: InfoViewBinding

    private var text = ""
    private var buttonText = ""
    private var key = "info_accept_event_done"

    fun setText(t : String){
        text = t
    }

    fun setKey(key : String){
        this.key = key
    }

    fun setButtonText(t : String){
        buttonText = t
    }

    override fun start() {
        game.context.runOnUiThread {
            binding = InfoViewBinding.inflate(LayoutInflater.from(game.context))
            binding.textView.text = text
            binding.button.text = buttonText
            game.context.addContentView(
                binding.root,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            binding.button.setOnClickListener {
                game.respond(key, true)
                binding.button.isEnabled = false
            }
        }
    }

    override fun end() {
        game.context.runOnUiThread {
            if(binding.root.parent != null)
                (binding.root.parent as ViewGroup).removeView(binding.root)
        }
    }
}