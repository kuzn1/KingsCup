package pwr.am.kingscup.render

import android.content.Context

abstract class Drawable (){
    protected var modelMatrixChange = true
    protected val modelMatrix = FloatArray(16)
    protected val offset = floatArrayOf(0.0f, 0.0f, 0.0f)
    protected val rotation = floatArrayOf(0.0f, 0.0f, 0.0f)


    fun move(dx : Float, dy: Float, dz : Float){
        offset[0] += dx
        offset[1] += dy
        offset[2] += dz
        modelMatrixChange = true
    }

    fun rotateX(angle : Float){
        rotation[0] += angle
        rotation[0] %= 360.0f
        modelMatrixChange = true
    }

    fun rotateY(angle : Float){
        rotation[1] += angle
        rotation[1] %= 360.0f
        modelMatrixChange = true
    }

    fun rotateZ(angle : Float){
        rotation[2] += angle
        rotation[2] %= 360.0f
        modelMatrixChange = true
    }

    abstract fun draw(viewMatrix : FloatArray, projectionMatrix : FloatArray)
}