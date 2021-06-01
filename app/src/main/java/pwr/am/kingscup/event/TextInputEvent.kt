package pwr.am.kingscup.event

import android.view.LayoutInflater
import android.view.ViewGroup
import pwr.am.kingscup.Game
import pwr.am.kingscup.PlayerLogic
import pwr.am.kingscup.databinding.TextInputViewBinding

class TextInputEvent(game: PlayerLogic): Event(game) {

    private lateinit var binding: TextInputViewBinding

    private var hint = ""
    private var buttonText = ""

    fun setHint(t : String){
        hint = t
    }

    fun setButtonText(t : String){
        buttonText = t
    }

    private var key = "text"
    fun setKey(key :String){
        this.key = key
    }

    override fun start() {
        game.context.runOnUiThread {
            binding = TextInputViewBinding.inflate(LayoutInflater.from(game.context))
            binding.editText.hint = hint
            binding.button.text = buttonText
            game.context.addContentView(
                binding.root,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            binding.button.setOnClickListener {
                binding.button.isEnabled = false
                binding.editText.isEnabled = false
                game.respond(key, binding.editText.text.toString())
            }
        }
    }

    override fun end() {
        game.context.runOnUiThread {
            if (binding.root.parent != null)
                (binding.root.parent as ViewGroup).removeView(binding.root)
        }
    }
}