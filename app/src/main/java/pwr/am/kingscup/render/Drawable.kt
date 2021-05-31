package pwr.am.kingscup.render

import java.util.*

abstract class Drawable (){
    protected var modelMatrixChange = true
    protected val modelMatrix = FloatArray(16)
    protected val position = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    protected var animationQueue : Queue<Animation> = LinkedList()
    var id = -1
    var deleteFlag = false

    fun move(dx : Float, dy: Float, dz : Float){
        position[0] += dx
        position[1] += dy
        position[2] += dz
        modelMatrixChange = true
    }

    fun rotateX(angle : Float){
        position[3] += angle
        position[3] %= 360.0f
        modelMatrixChange = true
    }

    fun rotateY(angle : Float){
        position[4] += angle
        position[4] %= 360.0f
        modelMatrixChange = true
    }

    fun rotateZ(angle : Float){
        position[5] += angle
        position[5] %= 360.0f
        modelMatrixChange = true
    }

    fun calculateAnimations(time : Long){
        var ticks = time
        while(ticks>0){
            if(animationQueue.isEmpty()) break
            ticks = animationQueue.peek().animate(position, ticks)
            if(ticks > 0) animationQueue.poll()
            modelMatrixChange = true
        }
    }

    fun animate(animation : Animation){
        animationQueue.add(animation)
    }

    abstract fun draw(time: Long, viewMatrix : FloatArray, projectionMatrix : FloatArray)
}