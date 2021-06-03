package pwr.am.kingscup.event

import pwr.am.kingscup.Game
import pwr.am.kingscup.PlayerLogic
import pwr.am.kingscup.render.Animation
import pwr.am.kingscup.render.Card

class DeckSetupEvent(game : PlayerLogic) : Event(game) {
    private lateinit var deck : ArrayList<Int>
    private var fastEnd = false

    fun deckInit(cards : ArrayList<Int>){
        deck = cards
    }

    override fun start() {
        addNextCard(0)
    }

    override fun end() {
        fastEnd = true
    }

    private fun addNextCard(index : Int) {
        if(fastEnd){
            for(i in index until deck.size){
                val card = Card(deck[i])
                card.move(0.0f, 0.0f, -6.0f)
                card.rotateY(180.0f)
                game.drawables.add(card)
            }
        }else if(index < deck.size){
            val card = Card(deck[index])

            card.move(0.0f, -4.0f, -1.6f)
            card.animate(
                Animation(0.0f, 0.0f, -1.6f, 0.0f, 0.0f, 0.0f, 300)
            )
            card.animate(
                Animation(0.0f, 0.0f, -1.6f, 0.0f, 0.0f, 0.0f, 500)
            )
            card.animate(
                Animation(0.0f, 0.0f, -6.0f, 0.0f, 180.0f, 0.0f, 300).also {
                    it.after {
                        addNextCard(index + 1)
                    }
                }
            )

            game.drawables.add(card)
            return
        }
        game.respond("deck_setup_event_done", true)
    }
}