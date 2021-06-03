package pwr.am.kingscup.event

import pwr.am.kingscup.services.GameClient
import pwr.am.kingscup.render.Animation

class RemoveCardEvent(game: GameClient) : Event(game) {
    private var index = 0
    private var done = false

    fun setIndex(i : Int){
        index = i
    }

    fun setCard(id : Int){
        for(i in 0 .. game.drawables.lastIndex){
            if(game.drawables[i].id == id){
                index = i
                break
            }
        }
    }

    override fun start() {
        if(index == 0) index = game.drawables.lastIndex
        game.drawables[index].animate(
            Animation(5.0f,0.0f,0.0f, 0.0f,0.0f,0.0f,2000, false).also {
                it.after {
                    if(!done) {
                        done = true
                        game.drawables[index].deleteFlag = true
                        game.respond("remove_card_event_done", true)
                    }
                }
            }
        )
    }

    override fun end() {
//        if(!done) {
//            done = true
//            game.drawables[index].deleteFlag = true
//        }
    }
}
