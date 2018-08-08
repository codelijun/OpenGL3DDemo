package com.example.lijun.opengl3ddemo.objectgl;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import static com.example.lijun.opengl3ddemo.util.GLUtil.loadShader;

public class Rectangle {
    private static final String VERTEX_SHADER =
            "attribute vec4 vPosition;\n"
                    + "uniform mat4 uMVPMatrix;\n"
                    + "void main() {\n"
                    + " gl_Position = uMVPMatrix * vPosition;\n"
                    + "}";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "void main() {\n"
            + "  gl_FragColor = vec4(1,0,0,1);\n"
            + "}";

    private static final float[] VERTEX_RECTANGLE = {   // in counterclockwise order:
            -1f, 1f, 0f,
            -1f, -1f, 0f,
            1f, 1f, 0f,
            1f, -1f, 0f,
    };

    private final FloatBuffer mVertexBuffer;

    private int mProgram;

    private int mMatrixHandle;
    private int mPositionHandle;

    private final float[] mMVPMatrix = new float[16];
//    private final float[] mProjectionMatrix = new float[16];
//    private final float[] mViewMatrix = new float[16];

    public Rectangle() {
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX_RECTANGLE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX_RECTANGLE);
        mVertexBuffer.position(0);
        initGL();
    }

    public void initGL() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                0, mVertexBuffer);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height){
        float screenAspectRatio = (float) width / (float) height;
//        Matrix.frustumM(mProjectionMatrix, 0, -screenAspectRatio,
//                screenAspectRatio, -1, 1, 2f, 8f);

        Matrix.perspectiveM(mMVPMatrix, 0, 90, screenAspectRatio, 0, 100f);
        Matrix.translateM(mMVPMatrix, 0, -1.5f, 0f, -6f);
    }

    public void drawSelf(){
        GLES20.glEnableVertexAttribArray(mPositionHandle);
//        Matrix.setLookAtM(mViewMatrix, 0, 1f, 0, 6.5f, 1f, 0, 0f, 0f, 1f, 0f);
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_RECTANGLE.length / 3);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
