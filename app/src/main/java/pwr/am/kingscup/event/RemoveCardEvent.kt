package pwr.am.kingscup.event

import pwr.am.kingscup.Game
import pwr.am.kingscup.render.Animation

class RemoveCardEvent(game: Game) : Event(game) {
    private var index = 0
    private var done = false

    fun setIndex(i : Int){
        index = i
    }

    override fun start() {
        if(index == 0) index = game.drawables.lastIndex
        game.drawables[index].animate(
            Animation(5.0f,0.0f,0.0f, 0.0f,0.0f,0.0f,2000, false).also {
                it.after {
                    if(!done) {
                        done = true
                        game.drawables[index].deleteFlag = true
                        game.respond("done", true)
                    }
                }
            }
        )
    }

    override fun end() {
        if(!done) {
            done = true
            game.drawables[index].deleteFlag = true
        }
    }
}
