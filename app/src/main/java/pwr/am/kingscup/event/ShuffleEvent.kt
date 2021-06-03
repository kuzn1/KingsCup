package pwr.am.kingscup.event

import android.os.SystemClock
import pwr.am.kingscup.PlayerLogic
import pwr.am.kingscup.render.Animation
import pwr.am.kingscup.render.Drawable
import kotlin.random.Random

class ShuffleEvent(game : PlayerLogic) : Event(game) {
    private var clickable = false
    private var animate = false

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
            game.drawables[i].animate(Animation(0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,1000+150*i.toLong(), false))
            game.drawables[i].animate(Animation(0.0f,0.0f,-6.0f,0.0f,180.0f,0.0f,150).also {
                if(i == game.drawables.lastIndex && clickable){
                    it.after {
                        game.context.runOnUiThread {
                            game.context.glView.setOnClickListener {
                                animate = false
                                game.respond("shuffle_event_drawn", true)
                                game.context.glView.setOnClickListener {}
                            }
                        }
                        animate = true
                        reminderAnimation(game.drawables.last())
                    }
                }
            })

        }
    }

    override fun end() {
    }

    fun reminderAnimation(card : Drawable){
        SystemClock.sleep(2000)
        if(animate){
            card.animate(Animation(0.0f,0.0f,0.5f,0.0f,0.0f,0.0f,100, false))
            card.animate(Animation(0.0f,0.0f,-0.5f,0.0f,0.0f,0.0f,100, false))
            card.animate(Animation(0.0f,0.0f,0.5f,0.0f,0.0f,0.0f,100, false))
            card.animate(Animation(0.0f,0.0f,-0.5f,0.0f,0.0f,0.0f,100, false).also {
                it.after { reminderAnimation(card) }
            })
        }
    }
}