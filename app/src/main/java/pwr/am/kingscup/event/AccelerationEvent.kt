package pwr.am.kingscup.event

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import pwr.am.kingscup.Game
import pwr.am.kingscup.PlayerLogic
import kotlin.math.absoluteValue

class AccelerationEvent(game: PlayerLogic) : Event(game), SensorEventListener {

    private var state = 1
    private var time = 0L
    private var count = 0

    fun setUp(){
        state = 1
    }

    fun setDown(){
        state = 2
    }


    override fun start() {
        time = SystemClock.uptimeMillis()
        val sensorManager = (game.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun end() {
        val sensorManager = (game.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
        sensorManager.unregisterListener(this)
    }

    private fun up(){
        if(state == 1) {
            state = 3
        }else if(state == 2){
            state = 0
            game.respond("time", 0L)
        }
        count = 0
    }

    private fun down(){
        if(state == 2) {
            state = 3
        }else if(state == 1){
            state = 0
            game.respond("time", 0L)
        }
        count = 0
    }

    private fun still(){
        if(state == 3) {
            count ++
            if(count > 10) {
                time = SystemClock.uptimeMillis() - time
                state = 0
                game.respond("time", time)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            if (state != 0) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val d = (x.absoluteValue + y.absoluteValue + z.absoluteValue)

                if (7.0 < d && d < 18.0) {
                    still()
                } else if (y < 2.0 && d < 2.0 || z < -2.0) {
                    down()
                } else if (d > 30.0) {
                    up()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}