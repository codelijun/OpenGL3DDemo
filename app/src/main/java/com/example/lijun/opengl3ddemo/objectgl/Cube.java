package com.example.lijun.opengl3ddemo.objectgl;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import static com.example.lijun.opengl3ddemo.util.GLUtil.loadShader;

public class Cube {
    private static final String VERTEX_SHADER =
            "attribute vec4 vPosition;\n"
                    + "attribute vec4 a_Color; \n"
                    + "varying vec4 v_Color;\n"
                    + "uniform mat4 uMVPMatrix;\n"
                    + "void main() {\n"
                    + "v_Color = a_Color;\n"
                    + " gl_Position = uMVPMatrix * vPosition;\n"
                    + "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "varying vec4 v_Color;\n"
                    + "void main() {\n"
                    + "  gl_FragColor = v_Color;\n"
                    + "}";
    private static final float[] VERTEX = {   // in counterclockwise order:
            -1.0f,1.0f,1.0f,    //正面左上0
            -1.0f,-1.0f,1.0f,   //正面左下1
            1.0f,-1.0f,1.0f,    //正面右下2
            1.0f,1.0f,1.0f,     //正面右上3
            -1.0f,1.0f,-1.0f,    //反面左上4
            -1.0f,-1.0f,-1.0f,   //反面左下5
            1.0f,-1.0f,-1.0f,    //反面右下6
            1.0f,1.0f,-1.0f,     //反面右上7
    };

    final float[] CUBE_COLOR = {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.5f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.5f, 1.0f,
    };

    final short index[] = {
            6, 7, 4, 6, 4, 5,    //后面
            6, 3, 7, 6, 2, 3,    //右面
            6, 5, 1, 6, 1, 2,    //下面
            0, 3, 2, 0, 2, 1,    //正面
            0, 1, 5, 0, 5, 4,    //左面
            0, 7, 3, 0, 4, 7,    //上面
    };


    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
//    private float[] mModelMatrix = new float[16];

    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mCubeColorsBuffer;
    private final ShortBuffer mIndexBuffer;


    private int mProgram;
    private int mPositionHandle;
    private int mMatrixHandle;
    private int mColorHandle;


    public Cube() {
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexBuffer.position(0);

        mCubeColorsBuffer = ByteBuffer.allocateDirect(CUBE_COLOR.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(CUBE_COLOR);
        mCubeColorsBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(index.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(index);
        mIndexBuffer.position(0);

        initGL();
    }

    public void initGL() {
        mProgram = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);

        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
        GLES20.glBindAttribLocation(mProgram, 0, "a_Color");

        GLES20.glUseProgram(mProgram);

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                0, mVertexBuffer);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, mCubeColorsBuffer);

    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float screenAspectRatio = (float) width / (float) height;
//        Matrix.frustumM(mProjectionMatrix, 0, -screenAspectRatio,
//                screenAspectRatio, -1, 1, 2f, 7f);
        Matrix.perspectiveM(mMVPMatrix, 0, 90, screenAspectRatio, 0, 100f);
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -10f);
    }

    public void drawSelf() {
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mColorHandle);

//        Matrix.rotateM(mMVPMatrix, 0, 0.5f, 0, 1, 0);
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -4f);
//        Matrix.rotateM(mModelMatrix, 0, 45, 1,1,0);
//        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
//        Matrix.setLookAtM(mViewMatrix, 0, -1f, -2f, -6f, 0f, 0, 0f, 0f, 1f, 0f);
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,index.length, GLES20.GL_UNSIGNED_SHORT,mIndexBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
