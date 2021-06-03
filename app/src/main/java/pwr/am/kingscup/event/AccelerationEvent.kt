package pwr.am.kingscup.event

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.view.ViewGroup
import android.widget.ProgressBar
import pwr.am.kingscup.services.GameClient
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.absoluteValue

class AccelerationEvent(game: GameClient) : Event(game), SensorEventListener {

    private var state = 1
    private var time = 0L
    private var count = 0
    private lateinit var progressBar : ProgressBar
    private var progress = 0

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
        game.context.runOnUiThread{
            progressBar = ProgressBar(game.context,null,android.R.attr.progressBarStyleHorizontal)
            progressBar.scaleY = 2f
            game.context.addContentView(
                progressBar,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            )
            startProgressTimer()
        }
    }

    private fun startProgressTimer() {
        if(state != 0) {
            Timer("progress", false).schedule(50) {
                progress++
                progressBar.progress = progress
                if (progress == 100) {
                    state = 0
                    game.respond("acceleration_event_time", 5000L)
                } else {
                    startProgressTimer()
                }
            }
        }
    }

    override fun end() {
        val sensorManager = (game.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
        sensorManager.unregisterListener(this)
        game.context.runOnUiThread {
            if (progressBar != null)
                (progressBar.parent as ViewGroup).removeView(progressBar)
        }
    }

    private fun up(){
        if(state == 1) {
            state = 3
        }else if(state == 2){
            state = 0
            game.respond("acceleration_event_time", 0L)
        }
        count = 0
    }

    private fun down(){
        if(state == 2) {
            state = 3
        }else if(state == 1){
            state = 0
            game.respond("acceleration_event_time", 0L)
        }
        count = 0
    }

    private fun still(){
        if(state == 3) {
            count ++
            if(count > 10) {
                time = SystemClock.uptimeMillis() - time
                state = 0
                game.respond("acceleration_event_time", time)
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