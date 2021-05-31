package pwr.am.kingscup.event

import android.os.SystemClock
import pwr.am.kingscup.Game
import pwr.am.kingscup.render.Animation
import pwr.am.kingscup.render.Drawable
import kotlin.random.Random

class ShuffleEvent(game : Game) : Event(game) {
    private var clickable = false

    fun enableDraw(){
        clickable = true
    }

    override fun start() {
        for(i in 1 until game.drawables.size) {
            val rand = Random.Default
            game.drawables[i].animate(Animation(
                rand.nextFloat()*6.0f - 3.0f,
                rand.nextFloat()*6.0f - 3.0f,
                0.00f , 0.0f, 0.0f, 0.0f,
                300,
                false
            ))
            game.drawables[i].animate(Animation(0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,1000+300*i.toLong(), false))
            game.drawables[i].animate(Animation(0.0f,0.0f,-6.0f,0.0f,180.0f,0.0f,300))
        }
        if(clickable) {
            reminderAnimation(game.drawables.last())
            game.context.runOnUiThread {
                game.context.glView.setOnClickListener {
                    game.respond("done", true)
                    game.context.glView.setOnClickListener {}
                    clickable = false
                }
            }
        }
    }

    override fun end() {
        if (clickable) {
            game.context.runOnUiThread {
                game.context.glView.setOnClickListener {}
            }
            clickable = false
        }
    }

    fun reminderAnimation(card : Drawable){
        SystemClock.sleep(2000)
        if(clickable){
            card.animate(Animation(0.0f,0.0f,0.5f,0.0f,0.0f,0.0f,100, false))
            card.animate(Animation(0.0f,0.0f,-0.5f,0.0f,0.0f,0.0f,100, false))
            card.animate(Animation(0.0f,0.0f,0.5f,0.0f,0.0f,0.0f,100, false))
            card.animate(Animation(0.0f,0.0f,-0.5f,0.0f,0.0f,0.0f,100, false).also {
                it.after { reminderAnimation(card) }
            })
        }
    }
}