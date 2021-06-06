package pwr.am.kingscup.event

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import pwr.am.kingscup.R
import pwr.am.kingscup.services.GameClient
import pwr.am.kingscup.render.Animation
import java.util.*

class DrawEvent(game: GameClient) : Event(game) , TextToSpeech.OnInitListener{
    private var textToSpeech  = TextToSpeech(game.context, this)
    private var tts = false
    private var index = 0

    fun setIndex(i : Int){
        index = i
    }

    fun setCard(id : Int){
        for(i in 0 .. game.drawables.lastIndex){
            if(game.drawables[i].id == id){
                index = i
            }
        }
    }

    override fun start() {
        if (index == 0) index = game.drawables.lastIndex

        MediaPlayer.create(game.context, R.raw.draw).setOnPreparedListener {
            if(game.enableSfxSound) it.start()
            game.drawables[index].animate(
                Animation(0.0f, 0.0f, -2.0f, 0.0f, 180.0f, 0.0f, 1000).also {
                    it.after {
                        game.respond("draw_event_done", true)
                        if (tts) {
                            val cardNames = game.context.resources.getStringArray(R.array.card_name)
                            val text = cardNames[game.drawables[index].id % 13]
                            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
                        }
                    }
                }
            )
        }
    }

    override fun end() {
        game.drawables[index].animate(
            Animation(0.0f,0.0f,-1.8f, 0.0f,0.0f,0.0f,100)
        )
    }

    override fun onInit(status: Int) {
        if(status != TextToSpeech.ERROR){
            if(game.enableCardSound) tts = true
            textToSpeech.language = Locale.ENGLISH
        }
    }
}
