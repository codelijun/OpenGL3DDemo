package com.example.lijun.opengl3ddemo.objectgl;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import static com.example.lijun.opengl3ddemo.util.GLUtil.loadShader;

public class Cylinder {
    private static final String VERTEX_SHADER =
            "attribute vec4 vPosition;\n"
                    + "uniform mat4 uMVPMatrix;\n"
                    + "void main() {\n"
                    + " gl_Position = uMVPMatrix * vPosition;\n"
                    + "}";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "void main() {\n"
            + "  gl_FragColor = vec4(1,0.5f,0.5f,1);\n"
            + "}";

    private final float[] mMVPMatrix = new float[16];

    private FloatBuffer mVertexBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mMatrixHandle;

    private int vCount;


    public Cylinder() {
        initVertexData(0.5f, 10, 20, 360);
        initGL();
    }

    public void initVertexData(float scale,    //大小
                               float r,        //半径
                               float h,        //高度
                               int n) {        //切分的份数

        r = scale * r;
        h = scale * h;

        float angdegSpan = 360.0f / n;
        vCount = 3 * n * 4;//顶点个数，共有3*n*4个三角形，每个三角形都有三个顶点
        //坐标数据初始化
        float[] vertices = new float[vCount * 3];
        float[] textures = new float[vCount * 2];//顶点纹理S、T坐标值数组
        //坐标数据初始化
        int count = 0;
        int stCount = 0;
        for (float angdeg = 0; Math.ceil(angdeg) < 360; angdeg += angdegSpan)//侧面
        {
            double angrad = Math.toRadians(angdeg);//当前弧度
            double angradNext = Math.toRadians(angdeg + angdegSpan);//下一弧度
            //底圆当前点---0
            vertices[count++] = (float) (-r * Math.sin(angrad));
            vertices[count++] = 0;
            vertices[count++] = (float) (-r * Math.cos(angrad));

            textures[stCount++] = (float) (angrad / (2 * Math.PI));//st坐标
            textures[stCount++] = 1;
            //顶圆下一点---3
            vertices[count++] = (float) (-r * Math.sin(angradNext));
            vertices[count++] = h;
            vertices[count++] = (float) (-r * Math.cos(angradNext));

            textures[stCount++] = (float) (angradNext / (2 * Math.PI));//st坐标
            textures[stCount++] = 0;
            //顶圆当前点---2
            vertices[count++] = (float) (-r * Math.sin(angrad));
            vertices[count++] = h;
            vertices[count++] = (float) (-r * Math.cos(angrad));

            textures[stCount++] = (float) (angrad / (2 * Math.PI));//st坐标
            textures[stCount++] = 0;

            //底圆当前点---0
            vertices[count++] = (float) (-r * Math.sin(angrad));
            vertices[count++] = 0;
            vertices[count++] = (float) (-r * Math.cos(angrad));

            textures[stCount++] = (float) (angrad / (2 * Math.PI));//st坐标
            textures[stCount++] = 1;
            //底圆下一点---1
            vertices[count++] = (float) (-r * Math.sin(angradNext));
            vertices[count++] = 0;
            vertices[count++] = (float) (-r * Math.cos(angradNext));

            textures[stCount++] = (float) (angradNext / (2 * Math.PI));//st坐标
            textures[stCount++] = 1;
            //顶圆下一点---3
            vertices[count++] = (float) (-r * Math.sin(angradNext));
            vertices[count++] = h;
            vertices[count++] = (float) (-r * Math.cos(angradNext));

            textures[stCount++] = (float) (angradNext / (2 * Math.PI));//st坐标
            textures[stCount++] = 0;
        }
        //法向量数据初始化
        float[] normals = new float[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            if (i % 3 == 1) {
                normals[i] = 0;
            } else {
                normals[i] = vertices[i];
            }
        }

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);//创建顶点坐标数据缓冲
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序为本地操作系统顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为float型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置
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

        GLES20.glUseProgram(mProgram);

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                0, mVertexBuffer);

    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float screenAspectRatio = (float) width / (float) height;

        Matrix.perspectiveM(mMVPMatrix, 0, 90, screenAspectRatio, 5, 100f);
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -30f);
    }

    public void drawSelf() {
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        Matrix.rotateM(mMVPMatrix, 0, 1f, 1, 0, 0);
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
