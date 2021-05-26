package pwr.am.kingscup.render

import android.content.Context
import android.opengl.*
import android.os.SystemClock
import java.time.Duration
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.collections.ArrayList

class OpenGlRenderer(val context : Context, private val drawables : ArrayList<Drawable>) : GLSurfaceView.Renderer {
    private var shaderProgram : Int = 0
    private var backgroundShaderProgram : Int = 0
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private var lastTime : Long = 0

    val vertexShaderCode =
        """
            attribute vec3 aVertexPosition;
            attribute vec2 aTextureCoord;
            
            uniform mat4 uModelViewProjectionMatrix;
           
            varying vec2 vTextureCoord;
            
            void main() {
                vTextureCoord = aTextureCoord;
                gl_Position = uModelViewProjectionMatrix * vec4(aVertexPosition, 1.0);
            }
        """
    val fragmentShaderCode =
        """
            precision mediump float;
            
            uniform sampler2D uSampler;
            
            varying highp vec2 vTextureCoord;
            
            void main() {
                gl_FragColor = texture2D(uSampler, vTextureCoord);
            }
        """
    val backgroundVertexShaderCode =
        """
            attribute vec3 aVertexPosition;
            attribute vec3 aVertexColor;
            
            varying vec3 vFragColor;
            void main() {
                vFragColor = aVertexColor;
                gl_Position = vec4(aVertexPosition, 1.0);
            }
        """
    val backgroundFragmentShaderCode =
        """
            precision mediump float;
            
            varying vec3 vFragColor;
            
            void main() {
                gl_FragColor = vec4(vFragColor, 1.0);
            }
        """

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        buildShaderProgram()
        //todo make loading screen and run async with result on gl_thread
        Card.initialize(context, shaderProgram)
        Background.initialize(backgroundShaderProgram)

        GLES31.glClearColor(0.0f,  0.0f, 0.0f, 1.0f)
        GLES31.glClearDepthf(1.0f)
        GLES31.glEnable(GLES31.GL_DEPTH_TEST)
        GLES31.glDepthFunc(GLES31.GL_LEQUAL)
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT)
        GLES31.glDisable(GLES31.GL_CULL_FACE)
        lastTime = SystemClock.uptimeMillis()
    }

    override fun onDrawFrame(gl: GL10?) {
        val currentTime = SystemClock.uptimeMillis()
        val deltaTime = currentTime - lastTime
        lastTime = currentTime
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)

        var i = 0
        var last = drawables.size
        while(i<last) {
            drawables[i].draw(deltaTime, viewMatrix, projectionMatrix)
            if(drawables[i].deleteFlag) {
                drawables.removeAt(i)
                last--
            }else{
                i++
            }
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES31.glViewport(0, 0, width, height)
        setupMatricies(width, height)
    }


    private fun buildShaderProgram() {
        val vertexShader = loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode)
        shaderProgram = GLES31.glCreateProgram().also {
            GLES31.glAttachShader(it, vertexShader)
            GLES31.glAttachShader(it, fragmentShader)
            GLES31.glLinkProgram(it)
        }
        val backgroundVertexShader = loadShader(GLES31.GL_VERTEX_SHADER, backgroundVertexShaderCode)
        val backgroundFragmentShader = loadShader(GLES31.GL_FRAGMENT_SHADER, backgroundFragmentShaderCode)
        backgroundShaderProgram = GLES31.glCreateProgram().also {
            GLES31.glAttachShader(it, backgroundVertexShader)
            GLES31.glAttachShader(it, backgroundFragmentShader)
            GLES31.glLinkProgram(it)
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES31.glCreateShader(type).also { shader ->
            GLES31.glShaderSource(shader, shaderCode)
            GLES31.glCompileShader(shader)
        }
    }

    private fun setupMatricies(width: Int, height: Int) {
        val ratio: Float = width.toFloat()/height.toFloat()

        val left = -ratio
        val bottom = -1.0f
        val top = 1.0f

        val near = 1.0f
        val far = 10.0f

        Matrix.frustumM(projectionMatrix, 0, left, ratio, bottom, top, near, far)

        val eyeX = 0.0f
        val eyeY = 0.0f
        val eyeZ = -0.5f

        val lookX = 0.0f
        val lookY = 0.0f
        val lookZ = -5.0f

        val upX = 0.0f
        val upY = 1.0f
        val upZ = 0.0f

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ)
    }
}
