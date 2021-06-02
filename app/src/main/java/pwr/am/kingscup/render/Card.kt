package pwr.am.kingscup.render

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.opengl.GLES31
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import pwr.am.kingscup.R


class Card(id : Int): Drawable(id) {
    private var textureIndex : Int

    companion object{
        private var cardResource = R.drawable.deck_default_320x498
        private var cardWidth = 320
        private var cardHeight = 498

        var shaderProgram : Int = 0
        lateinit var vertexBuffer : FloatBuffer
        lateinit var mapBuffer : FloatBuffer
        lateinit var texturesHandle : IntArray

        var vertexArray =
            floatArrayOf(
                -0.64228367529f, -1.0f, 0.0f,
                0.64228367529f, 1.0f, 0.0f,
                -0.64228367529f, 1.0f, 0.0f,
                -0.64228367529f, -1.0f, 0.0f,
                0.64228367529f, -1.0f, 0.0f,
                0.64228367529f, 1.0f, 0.0f,
            )
        var textureMapArray =
            floatArrayOf(
                0.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
            )

        fun initialize(context : Context, programHandle: Int){
            shaderProgram = programHandle
            initializeBuffers()
            loadConfig(context)
            loadTextures(context)
        }

        private fun loadConfig(context : Context){
            val config = context.getSharedPreferences("KingsCupConfig", Activity.MODE_PRIVATE)
            cardWidth = config.getInt("card_width", 320)
            cardHeight = config.getInt("card_height", 498)

            when (config.getString("texture", "Default")) {
                "Default" -> when (config.getString("quality", "Low")) {
                    "Low" -> cardResource = R.drawable.deck_default_320x498
                    "Medium" -> cardResource = R.drawable.deck_default_480x747
                    "High" -> cardResource = R.drawable.deck_default_720x1121
                }
                "Dark" -> when (config.getString("quality", "Low")) {
                    "Low" -> cardResource = R.drawable.deck_dark_320x498
                    "Medium" -> cardResource = R.drawable.deck_dark_480x747
                    "High" -> cardResource = R.drawable.deck_dark_720x1121
                }
            }
        }

        private fun initializeBuffers() {
            vertexBuffer = ByteBuffer.allocateDirect(vertexArray.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder()).asFloatBuffer()
                asFloatBuffer().apply {
                    put(vertexArray)
                    position(0)
                }
            }

            mapBuffer = ByteBuffer.allocateDirect(textureMapArray.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder()).asFloatBuffer()
                asFloatBuffer().apply {
                    put(textureMapArray)
                    position(0)
                }
            }
        }

        private fun loadTextures(context : Context) {
            //todo make async texture loading
            texturesHandle = IntArray(55)
            GLES31.glGenTextures(55, texturesHandle, 0)


            val input = context.resources.openRawResource(+cardResource)
            val decoder = BitmapRegionDecoder.newInstance(input, false)
            for(i in 0..54){
                val x = (i % 13) * cardWidth
                val y = (i / 13) * cardHeight
                val options = BitmapFactory.Options()
                options.inScaled = false
                val bitmap = decoder.decodeRegion(Rect(x, y, x + cardWidth, y + cardHeight), options)
                GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, texturesHandle[i])
                GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST)
                GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_NEAREST)
                //todo maybe change to glTexImage3D(GL_TEXTURE_2D_ARRAY, ...) to improve performance
                GLUtils.texImage2D(GLES31.GL_TEXTURE_2D, 0, bitmap, 0)
                bitmap.recycle()
            }
        }
    }

    init {
        position[2] = -1.6f
        //todo assign texture based on card ID
        textureIndex = id
    }

    override fun draw(time: Long, viewMatrix : FloatArray, projectionMatrix : FloatArray) {
        calculateAnimations(time)
        if(modelMatrixChange) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, position[0], position[1], position[2])

            Matrix.rotateM(modelMatrix, 0, position[3], 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(modelMatrix, 0, position[4], 0.0f, 1.0f, 0.0f)
            Matrix.rotateM(modelMatrix, 0, position[5], 0.0f, 0.0f, 1.0f)
            modelMatrixChange = false
        }

        GLES31.glUseProgram(shaderProgram)

        val samplerHandle = GLES31.glGetUniformLocation(shaderProgram, "uSampler")
        GLES31.glActiveTexture(GLES31.GL_TEXTURE0)
        //todo change to based on perspective not just angle
        if( 270.0f > position[3] && position[3] > 90.0f ||
            270.0f > position[4] && position[4] > 90.0f) GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, texturesHandle[54])
        else GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, texturesHandle[textureIndex])

        GLES31.glUniform1i(samplerHandle, 0)


        val MVPMatrix = FloatArray(16)
        Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVPMatrix, 0)

        val projectionMatrixHandle = GLES31.glGetUniformLocation(shaderProgram, "uModelViewProjectionMatrix")
        GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, MVPMatrix, 0)

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

        val mapHandle = GLES31.glGetAttribLocation(shaderProgram, "aTextureCoord")
        GLES31.glVertexAttribPointer(
            mapHandle,
            2,
            GLES31.GL_FLOAT,
            false,
            0,
            mapBuffer
        )
        GLES31.glEnableVertexAttribArray(mapHandle)

        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, 6)


        GLES31.glDisableVertexAttribArray(vertexHandle)
        GLES31.glDisableVertexAttribArray(mapHandle)
    }
}