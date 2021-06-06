package pwr.am.kingscup.event

import android.view.LayoutInflater
import android.view.ViewGroup
import pwr.am.kingscup.services.GameClient
import pwr.am.kingscup.databinding.TextInputViewBinding
import java.util.*
import kotlin.concurrent.schedule

class TextInputEvent(game: GameClient): Event(game) {

    private lateinit var binding: TextInputViewBinding
    private var initialized = false

    private var hint = ""
    private var text = ""
    private var buttonText = ""
    private var key = "text_input_event_text"

    fun setHint(t : String){
        hint = t
    }

    fun setText(t : String){
        text = t
    }

    fun setButtonText(t : String){
        buttonText = t
    }

    fun setKey(key :String){
        this.key = key
    }

    override fun start() {
        game.context.runOnUiThread {
            binding = TextInputViewBinding.inflate(LayoutInflater.from(game.context))
            binding.editText.hint = hint
            binding.button.text = buttonText
            binding.textView.text = text
            game.context.addContentView(
                binding.root,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            binding.button.setOnClickListener {
                binding.button.isEnabled = false
                binding.editText.isEnabled = false
                game.respond(key, binding.editText.text.toString())
            }
            initialized = true
        }
    }

    override fun end() {
        if (initialized)
            game.context.runOnUiThread {
                if (binding.root.parent != null)
                    (binding.root.parent as ViewGroup).removeView(binding.root)
            }
        else Timer("Delete TIE", true).schedule(200){
            if (initialized)
                game.context.runOnUiThread {
                    if (binding.root.parent != null)
                        (binding.root.parent as ViewGroup).removeView(binding.root)
                }
        }
    }
}