package pwr.am.kingscup.event

import pwr.am.kingscup.Game
import pwr.am.kingscup.PlayerLogic

abstract class Event (val game : PlayerLogic){

    abstract fun start()

    abstract fun end()
}