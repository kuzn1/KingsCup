package pwr.am.kingscup.event

import android.view.LayoutInflater
import android.view.ViewGroup
import pwr.am.kingscup.services.GameClient
import pwr.am.kingscup.databinding.InfoViewBinding
import java.util.*
import kotlin.concurrent.schedule

class InfoAcceptEvent(game: GameClient): Event(game) {

    private lateinit var binding: InfoViewBinding

    private var text = ""
    private var buttonText = ""
    private var key = "info_accept_event_done"
    private var initialized = false

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
            initialized = true
        }
    }

    override fun end() {
        if (initialized)
            game.context.runOnUiThread {if(binding.root.parent != null) (binding.root.parent as ViewGroup).removeView(binding.root)}
        else Timer("Delete IAE", true).schedule(200){
            if (initialized)
                game.context.runOnUiThread {
                    if(binding.root.parent != null) (binding.root.parent as ViewGroup).removeView(binding.root)
                }
        }
    }
}