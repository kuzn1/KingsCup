package pwr.am.kingscup.event

import android.view.LayoutInflater
import android.view.ViewGroup
import pwr.am.kingscup.Game
import pwr.am.kingscup.databinding.InfoViewBinding

class InfoAcceptEvent(game: Game): Event(game) {

    private lateinit var binding: InfoViewBinding

    private var text = ""
    private var buttonText = ""

    fun setText(t : String){
        text = t
    }

    fun setButtonText(t : String){
        buttonText = t
    }

    override fun start() {
        binding = InfoViewBinding.inflate(LayoutInflater.from(game.context))
        binding.textView.text = text
        binding.button.text = buttonText
        game.context.addContentView(
            binding.root,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        binding.button.setOnClickListener {
            game.respond("done", true)
            binding.button.isEnabled = false
        }
    }

    override fun end() {
        (binding.root.parent as ViewGroup).removeView(binding.root)
    }
}