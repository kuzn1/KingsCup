package pwr.am.kingscup.event

import android.media.MediaPlayer
import android.os.SystemClock
import pwr.am.kingscup.R
import pwr.am.kingscup.services.GameClient
import pwr.am.kingscup.render.Animation
import pwr.am.kingscup.render.Drawable
import java.util.*
import kotlin.concurrent.schedule
import kotlin.random.Random

class ShuffleEvent(game : GameClient) : Event(game) {
    private var clickable = false
    private var animate = false

    fun enableDraw(){
        clickable = true
    }

    override fun start() {
        val mediaPlayer = MediaPlayer.create(game.context, R.raw.slide)
        mediaPlayer.setOnPreparedListener {
            if(game.enableSfxSound) mediaPlayer.start()
            for (i in 1 until game.drawables.size) {
                val rand = Random.Default
                game.drawables[i].animate(
                    Animation(
                        rand.nextFloat() * 6.0f - 3.0f,
                        rand.nextFloat() * 6.0f - 3.0f,
                        0.00f, 0.0f, 0.0f, 0.0f,
                        300,
                        false
                    )
                )
                game.drawables[i].animate(
                    Animation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1000 + 150 * i.toLong(), false).also {
                        it.after {
                            if(game.enableSfxSound) {
                                mediaPlayer.pause()
                                mediaPlayer.seekTo(0)
                                mediaPlayer.start()
                            }
                            Unit
                        }
                    })
                game.drawables[i].animate(Animation(0.0f, 0.0f, -6.0f, 0.0f, 180.0f, 0.0f, 150))
            }
            if (clickable) {
                Timer("shuffle_click_listener_delay", false).schedule(game.drawables.size * 150L + 1000L) {
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
        }
    }

    override fun end() {
    }

    fun reminderAnimation(card : Drawable){
        if(animate){
            card.animate(Animation(0.0f,0.0f,0.5f,0.0f,0.0f,0.0f,100, false))
            card.animate(Animation(0.0f,0.0f,-0.5f,0.0f,0.0f,0.0f,100, false))
            card.animate(Animation(0.0f,0.0f,0.5f,0.0f,0.0f,0.0f,100, false))
            card.animate(Animation(0.0f,0.0f,-0.5f,0.0f,0.0f,0.0f,100, false).also {
                it.after {
                    Timer("draw_reminder", false).schedule(2000) {
                        reminderAnimation(card)
                    }
                    Unit
                }
            })
        }
    }
}