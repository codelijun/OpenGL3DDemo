package com.example.lijun.opengl3ddemo.objectgl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.lijun.opengl3ddemo.BuildConfig;
import com.example.lijun.opengl3ddemo.util.GLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import static com.example.lijun.opengl3ddemo.util.GLUtil.loadShader;

public class TextureCylinder {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "TextureCylinder";
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    " gl_Position = uMVPMatrix * vPosition;" +
                    " v_texCoord = a_texCoord;" +
                    "}";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    " gl_FragColor = texture2D(s_texture, v_texCoord);" +
                    "}";


//    int muMMatrixHandle;//位置、旋转、缩放变换矩阵
//    int maCameraHandle; //摄像机位置属性引用
//    int maNormalHandle; //顶点法向量属性引用
//    int maLightLocationHandle;//光源位置属性引用
//    FloatBuffer mNormalBuffer;//顶点法向量数据缓冲

    private Context mContext;

    int mProgram;//自定义渲染管线着色器程序id
    int muMVPMatrixHandle;//总变换矩阵引用
    int maPositionHandle; //顶点位置属性引用
    private int mTexCoordHandle;
    private int mTexUniformHandle;

    FloatBuffer mVertexBuffer;//顶点坐标数据缓冲
    FloatBuffer mTexCoorBuffer;//顶点纹理坐标数据缓冲

    private int vCount = 0;
    private final int radius = 20;
    private float mBitmapAspectRatio;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private int mTextureId[] = new int[1];

    public TextureCylinder(Context context) {
        this.mContext = context;

        loadTexture();

        //调用初始化顶点数据的initVertexData方法
        float height = (float) (2 * Math.PI * radius * 1 / mBitmapAspectRatio);
        initVertexData(1f, radius, height, 360);
        //调用初始化着色器的intShader方法
        initShader();
    }

    //自定义初始化顶点坐标数据的方法
    public void initVertexData(
            float scale,    //大小
            float r,        //半径
            float h,        //高度
            int n           //切分的份数
    ) {
        r = scale * r;
        h = scale * h;
        float positionY = h * 0.5f;
        float angdegSpan = 360f / n;
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
            vertices[count++] = positionY;
            vertices[count++] = (float) (-r * Math.cos(angrad));

            textures[stCount++] = (float) (angrad / (2 * Math.PI));//st坐标
            textures[stCount++] = 1f;
            //顶圆下一点---3
            vertices[count++] = (float) (-r * Math.sin(angradNext));
            vertices[count++] = positionY;
            vertices[count++] = (float) (-r * Math.cos(angradNext));

            textures[stCount++] = (float) (angradNext / (2 * Math.PI));//st坐标
            textures[stCount++] = 0;
            //顶圆当前点---2
            vertices[count++] = (float) (-r * Math.sin(angrad));
            vertices[count++] = positionY;
            vertices[count++] = (float) (-r * Math.cos(angrad));

            textures[stCount++] = (float) (angrad / (2 * Math.PI));//st坐标
            textures[stCount++] = 0;

            //底圆当前点---0
            vertices[count++] = (float) (-r * Math.sin(angrad));
            vertices[count++] = -positionY;
            vertices[count++] = (float) (-r * Math.cos(angrad));

            textures[stCount++] = (float) (angrad / (2 * Math.PI));//st坐标
            textures[stCount++] = 1f;
            //底圆下一点---1
            vertices[count++] = (float) (-r * Math.sin(angradNext));
            vertices[count++] = -positionY;
            vertices[count++] = (float) (-r * Math.cos(angradNext));

            textures[stCount++] = (float) (angradNext / (2 * Math.PI));//st坐标
            textures[stCount++] = 1f;
            //顶圆下一点---3
            vertices[count++] = (float) (-r * Math.sin(angradNext));
            vertices[count++] = positionY;
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

//        ByteBuffer nbb = ByteBuffer.allocateDirect(vertices.length * 4);//创建顶点法向量数据缓冲
//        nbb.order(ByteOrder.nativeOrder());//设置字节顺序为本地操作系统顺序
//        mNormalBuffer = nbb.asFloatBuffer();//转换为float型缓冲
//        mNormalBuffer.put(normals);//向缓冲区中放入顶点法向量数据
//        mNormalBuffer.position(0);//设置缓冲区起始位置

        //st坐标数据初始化
        ByteBuffer cbb = ByteBuffer.allocateDirect(textures.length * 4);//创建顶点纹理数据缓冲
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序为本地操作系统顺序
        mTexCoorBuffer = cbb.asFloatBuffer();//转换为float型缓冲
        mTexCoorBuffer.put(textures);//向缓冲区中放入顶点纹理数据
        mTexCoorBuffer.position(0);//设置缓冲区起始位置
    }

    //自定义初始化着色器的initShader方法
    public void initShader() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        //基于顶点着色器与片元着色器创建程序
        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        //制定使用某套shader程序
        GLES20.glUseProgram(mProgram);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);

        //获取程序中顶点位置属性引用id
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //获取程序中总变换矩阵引用id
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        //纹理相关
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        mTexUniformHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

//        //获取程序中顶点法向量属性引用id
//        maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
//        //获取程序中摄像机位置引用id
//        maCameraHandle = GLES20.glGetUniformLocation(mProgram, "uCamera");
//        //获取程序中光源位置引用id
//        maLightLocationHandle = GLES20.glGetUniformLocation(mProgram, "uLightLocation");
//        //获取位置、旋转变换矩阵引用id
//        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");

        //传送顶点位置数据
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);
//        纹理
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoorBuffer);

        //传送顶点法向量数据
//        GLES20.glVertexAttribPointer(maNormalHandle, 4, GLES20.GL_FLOAT, false, 3 * 4, mNormalBuffer);

    }

    private void loadTexture() {
        InputStream is = null;
        try {
            is = mContext.getAssets().open("image2.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (is == null) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        mBitmapAspectRatio = (float) width / (float) height;
        if (DEBUG) {
            Log.d(TAG, " loadTexture() width== " + width + " height== " + height + " mBitmapAspectRatio== " + mBitmapAspectRatio);
        }

        mTextureId[0] = GLUtil.loadTextures(0, bitmap);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.gc();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float screenAspectRatio = (float) width / (float) height;

//        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0, 2f, 0f, 0, 0f, 0f, 1f, 0f);
//
//        Matrix.frustumM(mProjectionMatrix, 0, -screenAspectRatio,
//                screenAspectRatio, -1, 1, 2f, 100f);
//
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, 0.0f, 0f, -40f);

        Matrix.perspectiveM(mMVPMatrix, 0, 90, screenAspectRatio, 5, 100f);
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 0f);
    }

    public void drawSelf() {
        Matrix.rotateM(mMVPMatrix, 0, -0.1f, 0.0f, 1.0f, 0.0f);
//        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        //将最终变换矩阵传入shader程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        //启用顶点位置数据
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        //启用顶点纹理数据
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        //启用顶点法向量数据
//        GLES20.glEnableVertexAttribArray(maNormalHandle);

        //绑定纹理
        GLES20.glUniform1i(mTexUniformHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);

        //绘制纹理矩形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vCount);

        GLES20.glDisableVertexAttribArray(maPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
//        GLES20.glDisableVertexAttribArray(maNormalHandle);
    }
}
