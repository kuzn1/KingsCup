package pwr.am.kingscup.render

import android.opengl.GLES31
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class Background(): Drawable() {

    companion object{
        var shaderProgram : Int = 0
        lateinit var vertexBuffer : FloatBuffer
        lateinit var colorMapBuffer : FloatBuffer

        var vertexArray =
            floatArrayOf(
                0.0f, 0.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f
            )
        var colorMapArray =
            floatArrayOf(
                0.105f, 0.639f, 0.537f,
                0.035f, 0.2f, 0.168f,
                0.035f, 0.2f, 0.168f,
                0.035f, 0.2f, 0.168f,
                0.035f, 0.2f, 0.168f,
                0.035f, 0.2f, 0.168f
            )

        fun initialize(programHandle: Int){
            shaderProgram = programHandle
            initializeBuffers()
        }


        private fun initializeBuffers() {
            vertexBuffer = ByteBuffer.allocateDirect(vertexArray.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder()).asFloatBuffer()
                asFloatBuffer().apply {
                    put(vertexArray)
                    position(0)
                }
            }

            colorMapBuffer = ByteBuffer.allocateDirect(colorMapArray.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder()).asFloatBuffer()
                asFloatBuffer().apply {
                    put(colorMapArray)
                    position(0)
                }
            }
        }
    }

    override fun draw(viewMatrix : FloatArray, projectionMatrix : FloatArray) {
        GLES31.glUseProgram(shaderProgram)

        val vertexHandle = GLES31.glGetAttribLocation(shaderProgram, "aVertexPosition")
        GLES31.glVertexAttribPointer(
            vertexHandle,
            3,
            GLES31.GL_FLOAT,
            false,
            0,
            vertexBuffer
        )
        GLES31.glEnableVertexAttribArray(vertexHandle)

        val colorMapHandle = GLES31.glGetAttribLocation(shaderProgram, "aVertexColor")
        GLES31.glVertexAttribPointer(
            colorMapHandle,
            3,
            GLES31.GL_FLOAT,
            false,
            0,
            colorMapBuffer
        )
        GLES31.glEnableVertexAttribArray(colorMapHandle)

        GLES31.glDrawArrays(GLES31.GL_TRIANGLE_FAN, 0, 6)

        GLES31.glDisableVertexAttribArray(vertexHandle)
        GLES31.glDisableVertexAttribArray(colorMapHandle)
    }
}