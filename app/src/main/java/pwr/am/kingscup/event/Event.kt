package pwr.am.kingscup.event

import pwr.am.kingscup.services.GameClient

abstract class Event (val game : GameClient){

    abstract fun start()

    abstract fun end()
}