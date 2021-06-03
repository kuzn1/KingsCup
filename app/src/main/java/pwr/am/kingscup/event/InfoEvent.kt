package pwr.am.kingscup.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pwr.am.kingscup.services.GameClient
import pwr.am.kingscup.databinding.InfoViewBinding

class InfoEvent(game: GameClient): Event(game) {

    private var binding: InfoViewBinding = InfoViewBinding.inflate(LayoutInflater.from(game.context))

    private var text = ""
    private var send = false

    fun setText(t : String){
        text = t
    }
    fun sendCardActionDone(){
        send = true
    }

    override fun start() {
        game.context.runOnUiThread {
            //binding = InfoViewBinding.inflate(LayoutInflater.from(game.context))
            binding.button.visibility = View.INVISIBLE
            binding.textView.text = text
            game.context.addContentView(
                binding.root,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }
        if(send){
            game.respond("CardActionDone", "")
        }
    }

    override fun end() {
        send = false
        game.context.runOnUiThread {
            if (binding.root.parent != null)
                (binding.root.parent as ViewGroup).removeView(binding.root)
        }
    }
}