package pwr.am.kingscup.event

import pwr.am.kingscup.Game

abstract class Event (val game : Game){

    abstract fun start()

    abstract fun end()
}