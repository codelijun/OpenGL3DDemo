package com.example.lijun.opengl3ddemo.objectgl;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import static com.example.lijun.opengl3ddemo.util.GLUtil.loadShader;

public class Point {
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

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mCubeColorsBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mMatrixHandle;
    private int mColorHandle;
    private int vCount;


    public Point() {
        float n = 10f;
        float r = 0.1f;
        float angdegSpan = 360.0f / n;//顶角的读书
        vCount = (int) (3 * n);//顶点个数,共有n个三角形，每个三角形都有3个顶点

        float[] vertices = new float[vCount * 3];//顶点坐标数据
        float[] color = new float[vCount * 4];

        //坐标数据初始化
        int count = 0;
        int colorIndex = 0;
        for (float angdeg = 0; Math.ceil(angdeg) < 360; angdeg += angdegSpan) {
            double angrad = Math.toRadians(angdeg);//当前弧度
            double angradNext = Math.toRadians(angdeg + angdegSpan);//下一弧度

            //中心点
            vertices[count++] = 0;//顶点坐标
            vertices[count++] = 0;
            vertices[count++] = 0;

            color[colorIndex ++] = 1;
            color[colorIndex ++] = 0;
            color[colorIndex ++] = 0;
            color[colorIndex ++] = 1;


            //当前点
            vertices[count++] = (float) (-r * Math.sin(angrad));//顶点坐标
            vertices[count++] = (float) (r * Math.cos(angrad));
            vertices[count++] = 0;

            color[colorIndex ++] = 1;
            color[colorIndex ++] = 0;
            color[colorIndex ++] = 0;
            color[colorIndex ++] = 0f;


            //下一点
            vertices[count++] = (float) (-r * Math.sin(angradNext));//顶点坐标
            vertices[count++] = (float) (r * Math.cos(angradNext));
            vertices[count++] = 0;

            color[colorIndex ++] = 1;
            color[colorIndex ++] = 0;
            color[colorIndex ++] = 0;
            color[colorIndex ++] = 0f;
        }

        mVertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices);
        mVertexBuffer.position(0);

        mCubeColorsBuffer = ByteBuffer.allocateDirect(color.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(color);
        mCubeColorsBuffer.position(0);

        initGL();
    }

    private void initGL() {
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
//        GLES20.glBindAttribLocation(mProgram, 0, "a_Color");

        GLES20.glUseProgram(mProgram);

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, mCubeColorsBuffer);

    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float screenAspectRatio = (float) width / (float) height;
        Matrix.frustumM(mProjectionMatrix, 0, -screenAspectRatio,
                screenAspectRatio, -1, 1, 2f, 7f);
    }

    public void drawSelf() {
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 4f, 0f, 0, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vCount);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
