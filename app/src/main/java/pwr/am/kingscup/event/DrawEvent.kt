package pwr.am.kingscup.event

import pwr.am.kingscup.Game
import pwr.am.kingscup.PlayerLogic
import pwr.am.kingscup.render.Animation

class DrawEvent(game: PlayerLogic) : Event(game) {
    private var index = 0

    fun setIndex(i : Int){
        index = i
    }

    fun setCard(id : Int){
        for(i in 0 .. game.drawables.lastIndex){
            if(game.drawables[i].id == id){
                index = i
                return
            }
        }
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
            Animation(0.0f,0.0f,-1.7f, 0.0f,0.0f,0.0f,100)
        )
    }
}
