package com.example.lijun.opengl3ddemo.objectgl;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.lijun.opengl3ddemo.util.GLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import static com.example.lijun.opengl3ddemo.util.GLUtil.loadShader;

public class Circle {

//    private static final String VERTEX_SHADER =
//            "attribute vec4 vPosition;\n"
//                    + "uniform mat4 uMVPMatrix;\n"
//                    + "void main() {\n"
//                    + " gl_Position = uMVPMatrix * vPosition;\n"
//                    + "}";
//
//    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
//            + "void main() {\n"
//            + "  gl_FragColor = vec4(0.5,0.5f,0.5f,1);\n"
//            + "}";

    //纹理相关
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

    private Context mContext;

    int mProgram;//自定义渲染管线着色器程序id
    int muMVPMatrixHandle;//总变换矩阵引用
    int maPositionHandle; //顶点位置属性引用
    private int mTexCoordHandle;
    private int mTexUniformHandle;

    FloatBuffer mVertexBuffer;//顶点坐标数据缓冲
    FloatBuffer mTexCoorBuffer;//顶点纹理坐标数据缓冲
    FloatBuffer mNormalBuffer;//顶点法向量数据缓冲
    int vCount = 0;
    private final float[] mMVPMatrix = new float[16];
    private int mTextureId[] = new int[1];

    public Circle(Context context) {
        mContext = context;
        //调用初始化顶点数据的initVertexData方法
        initVertexData(0.5f, 10, 360);
        //调用初始化着色器的intShader方法
        initShader();
        loadTexture();
    }

    public void initVertexData(
            float scale,//大小
            float r,//半径
            int n //切分的份数
    ) {
        r = r * scale;
        float angdegSpan = 360.0f / n;//顶角的读书
        vCount = 3 * n;//顶点个数,共有n个三角形，每个三角形都有3个顶点

        float[] vertices = new float[vCount * 3];//顶点坐标数据
        float[] textures = new float[vCount * 2];//顶点纹理S,T坐标值数组

        //坐标数据初始化
        int count = 0;
        int stCount = 0;
        for (float angdeg = 0; Math.ceil(angdeg) < 360; angdeg += angdegSpan) {
            double angrad = Math.toRadians(angdeg);//当前弧度
            double angradNext = Math.toRadians(angdeg + angdegSpan);//下一弧度

            //中心点
            vertices[count++] = 0;//顶点坐标
            vertices[count++] = 0;
            vertices[count++] = 0;

            textures[stCount++] = 0.5F;//ST坐标
            textures[stCount++] = 0.5f;

            //当前点
            vertices[count++] = (float) (-r * Math.sin(angrad));//顶点坐标
            vertices[count++] = (float) (r * Math.cos(angrad));
            vertices[count++] = 0;

            textures[stCount++] = (float) (0.5 - 0.5 * Math.sin(angrad));//st坐标
            textures[stCount++] = (float) (0.5 - 0.5 * Math.cos(angrad));

            //下一点
            vertices[count++] = (float) (-r * Math.sin(angradNext));//顶点坐标
            vertices[count++] = (float) (r * Math.cos(angradNext));
            vertices[count++] = 0;

            textures[stCount++] = (float) (0.5 - 0.5 * Math.sin(angradNext));//st坐标
            textures[stCount++] = (float) (0.5 - 0.5 * Math.cos(angradNext));
        }

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);//创建顶点坐标数据缓冲
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序为本地操作系统顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为float型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置
        //法向量数据初始化-用于计算光照
        float[] normals = new float[vertices.length];
        for (int i = 0; i < normals.length; i += 3) {
            normals[i] = 0;
            normals[i + 1] = 0;
            normals[i + 2] = 1;
        }
        ByteBuffer nbb = ByteBuffer.allocateDirect(normals.length * 4);//创建顶点法向量数据缓冲
        nbb.order(ByteOrder.nativeOrder());//设置字节顺序为本地操作系统顺序
        mNormalBuffer = nbb.asFloatBuffer();//转换为float型缓冲
        mNormalBuffer.put(normals);//向缓冲区中放入顶点法向量数据
        mNormalBuffer.position(0);//设置缓冲区起始位置

        //纹理坐标数据初始化
        ByteBuffer cbb = ByteBuffer.allocateDirect(textures.length * 4);//创建顶点纹理数据缓冲
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序为本地操作系统顺序
        mTexCoorBuffer = cbb.asFloatBuffer();//转换为float型缓冲
        mTexCoorBuffer.put(textures);//向缓冲区中放入顶点纹理数据
        mTexCoorBuffer.position(0);//设置缓冲区起始位置
    }

    private void loadTexture() {
            InputStream is = null;
            try {
                is = mContext.getAssets().open("image.jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (is == null) {
                return;
            }
            mTextureId[0] = GLUtil.loadTextures(0, BitmapFactory.decodeStream(is));
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.gc();
    }

    //自定义初始化着色器initShader方法
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

        //传送顶点位置数据
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);
//        纹理
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoorBuffer);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float screenAspectRatio = (float) width / (float) height;

        Matrix.perspectiveM(mMVPMatrix, 0, 90, screenAspectRatio, 5, 100f);
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -30f);
    }

    public void drawSelf() {

//        Matrix.rotateM(mMVPMatrix, 0, 1f, 1, 0, 0);

        //将最终变换矩阵传入shader程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);



        //启用顶点位置数据
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        //启用顶点纹理数据
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);


        //绑定纹理
        GLES20.glUniform1i(mTexUniformHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);

        //绘制纹理矩形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vCount);

        GLES20.glDisableVertexAttribArray(maPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
    }
}
