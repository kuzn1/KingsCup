package pwr.am.kingscup

import pwr.am.kingscup.event.*
import pwr.am.kingscup.render.Drawable
import kotlin.collections.ArrayList
class Game (val context : GameBoardActivity, val drawables : ArrayList<Drawable>){

    private var currentEvent : Event = DeckSetupEvent(this)
    private var additionalEvent : Event = DeckSetupEvent(this)


    //placeholder just to test all events
    fun start(k : Int){
        when(k){
            // adb shell input keyevent 7
            0->{
                currentEvent.end()
                currentEvent = DeckSetupEvent(this)
                (currentEvent as DeckSetupEvent).deckInit(arrayOf(1, 1, 2, 3, 4, 5, 6, 26, 13))
                currentEvent.start()
            }
            // adb shell input keyevent 85
            1->{
                currentEvent.end()
                currentEvent = ShuffleEvent(this)
                (currentEvent as ShuffleEvent).enableDraw()
                currentEvent.start()
            }
            // adb shell input keyevent 9
            2->{
                currentEvent.end()
                currentEvent = DrawEvent(this)
                (currentEvent as DrawEvent).setCard(1)
                currentEvent.start()
            }
            // adb shell input keyevent 10
            3-> {
                currentEvent.end()
                currentEvent = InfoEvent(this)
                (currentEvent as InfoEvent).setText("Example text please wait")
                currentEvent.start()
            }
            // adb shell input keyevent 11
            4->{
                currentEvent.end()
                currentEvent = InfoAcceptEvent(this)
                (currentEvent as InfoAcceptEvent).setText("Example text please wait")
                (currentEvent as InfoAcceptEvent).setButtonText("Click me!")
                currentEvent.start()
            }
            // adb shell input keyevent 12
            5->{
                currentEvent.end()
                currentEvent = RemoveCardEvent(this)
                (currentEvent as RemoveCardEvent).setCard(1)
                currentEvent.start()
            }
            // adb shell input keyevent 13
            6->{
                currentEvent.end()
                currentEvent = AccelerationEvent(this)
                (currentEvent as AccelerationEvent).setUp()

                additionalEvent = InfoEvent(this)
                (additionalEvent as InfoEvent).setText("7 Heven")

                additionalEvent.start()
                currentEvent.start()
            }
            // adb shell input keyevent 14
            7->{
                currentEvent.end()
                currentEvent = AccelerationEvent(this)
                (currentEvent as AccelerationEvent).setDown()

                additionalEvent = InfoEvent(this)
                (additionalEvent as InfoEvent).setText("4 Floor")

                additionalEvent.start()
                currentEvent.start()
            }
            // adb shell input keyevent 15
            8->{
                currentEvent.end()
                currentEvent = PlayerChooseEvent(this)
                (currentEvent as PlayerChooseEvent).setPlayers(arrayListOf(
                    Pair("NAME1","ID1"),
                    Pair("NAME2","ID2"),
                    Pair("NAME3","ID3"),
                    Pair("NAME4","ID4"),
                    Pair("NAME5","ID5")
                ))
                currentEvent.start()
            }
            // adb shell input keyevent 16
            9->{
                currentEvent.end()
                currentEvent = TextInputEvent(this)
                (currentEvent as TextInputEvent).setButtonText("Example")
                (currentEvent as TextInputEvent).setHint("type here")
                (currentEvent as TextInputEvent).start()
            }
        }
    }

    //TODO implement database listeners and game logic linked to listeners

    fun respond(key : String, value : Any){
        if(currentEvent is AccelerationEvent){
            additionalEvent.end()
            currentEvent.end()
            currentEvent = InfoEvent(this)
            with(value as Long){
                if(this == 0L){
                    (currentEvent as InfoEvent).setText("Wrong Move :(")
                }else{
                    (currentEvent as InfoEvent).setText("Your time: " + this.toString() + "ms")
                }
            }
            currentEvent.start()
        }else if(currentEvent is PlayerChooseEvent){
            currentEvent.end()
            currentEvent = InfoEvent(this)
            (currentEvent as InfoEvent).setText("Chosen player with id: " + (value as String))
            currentEvent.start()
        }else if(currentEvent is ShuffleEvent){
            currentEvent.end()
            currentEvent = DrawEvent(this)
            (currentEvent as DrawEvent).setCard(1)
            currentEvent.start()
        } else if(currentEvent is DrawEvent){
            currentEvent.end()
        }

    }
}