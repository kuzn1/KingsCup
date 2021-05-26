package pwr.am.kingscup.event

import pwr.am.kingscup.Game
import pwr.am.kingscup.render.Animation

class DrawEvent(game: Game) : Event(game) {
    private var index = 0

    fun setIndex(i : Int){
        index = i
    }

    override fun start() {
        if(index == 0) index = game.drawables.lastIndex
        game.drawables[index].animate(
            Animation(0.0f,0.0f,-2.0f, 0.0f,180.0f,0.0f,1000).also {
                it.after { game.respond("done", true) }
            }
        )
    }

    override fun end() {
        game.drawables[index].animate(
            Animation(0.0f,0.0f,-1.6f, 0.0f,0.0f,0.0f,100)
        )
    }
}
