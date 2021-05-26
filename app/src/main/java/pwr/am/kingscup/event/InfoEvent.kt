package pwr.am.kingscup.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pwr.am.kingscup.Game
import pwr.am.kingscup.databinding.InfoViewBinding

class InfoEvent(game: Game): Event(game) {

    private lateinit var binding: InfoViewBinding

    private var text = ""

    fun setText(t : String){
        text = t
    }

    override fun start() {
        binding = InfoViewBinding.inflate(LayoutInflater.from(game.context))
        binding.button.visibility = View.INVISIBLE
        binding.textView.text = text
        game.context.addContentView(
            binding.root,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    override fun end() {
        (binding.root.parent as ViewGroup).removeView(binding.root)
    }
}