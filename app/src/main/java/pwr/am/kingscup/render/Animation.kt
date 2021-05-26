package pwr.am.kingscup.render

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Animation(
        private var x : Float,
        private var y : Float,
        private var z : Float,
        private var xr : Float,
        private var yr : Float,
        private var zr : Float,
        private var time : Long = 1,
        private val absolute : Boolean = true
){

    private var started : Boolean = false
    private lateinit var deltaPos : FloatArray
    private var afterAnimationCallback : () -> Unit? = {}

    fun after(later: () -> Unit?){
        afterAnimationCallback = later
    }

    fun animate(position: FloatArray, deltaTime : Long) : Long{
        if(!started){
            deltaPos =
                if(absolute) floatArrayOf(
                    (x - position[0])  / time,
                    (y - position[1])  / time,
                    (z - position[2])  / time,
                    (xr - position[3]) / time,
                    (yr - position[4]) / time,
                    (zr - position[5]) / time
                )
                else floatArrayOf(
                        x  / time,
                        y  / time,
                        z  / time,
                        xr / time,
                        yr / time,
                        zr / time
                    )
            if(!absolute){
                x += position[0]
                y += position[1]
                z += position[2]
                xr += position[3]
                yr += position[4]
                zr += position[5]
            }
            started = true
        } else if(time == 0L) return deltaTime


        if(time > deltaTime){
            for(i in 0..5)
                position[i] += deltaPos[i] * deltaTime.toFloat()
            time -=deltaTime
            return 0L
        }else{
            position[0] = x
            position[1] = y
            position[2] = z
            position[3] = xr
            position[4] = yr
            position[5] = zr
            val tmp = time
            time = 0
            GlobalScope.launch{
                afterAnimationCallback()
            }
            return deltaTime-tmp
        }
    }
}