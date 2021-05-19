package pwr.am.kingscup.render

import android.content.Context
import android.opengl.GLSurfaceView
import kotlin.collections.ArrayList

class OpenGLView(context: Context) : GLSurfaceView(context) {

    private val renderer : OpenGlRenderer

    val drawables : ArrayList<Drawable>

    init {
        drawables = ArrayList()
        setEGLContextClientVersion(3)
        renderer = OpenGlRenderer(context, drawables)
        setRenderer(renderer)
    }
}