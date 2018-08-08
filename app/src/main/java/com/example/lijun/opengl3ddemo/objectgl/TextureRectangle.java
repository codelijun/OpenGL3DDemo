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

public class TextureRectangle {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "TextureRectangle";

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

    private float[] mVertexRectangle;

    private static final float[] VERTEX_TEXTURE = {
//            0f, 0f,  // bottom left 1
//            0f, 1f,  // top left 0
//            1f, 0f,  // top right 3
//            0f, 1f,  // bottom left 1
//            1f, 1f,  // bottom right 2
//            1f, 0f,  // top right 3
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f,

            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
    };

    private Context mContext;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoorBuffer;//顶点纹理坐标数据缓冲


    private int mProgram;

    private int mMatrixHandle;
    private int mPositionHandle;
    private int mTexCoordHandle;
    private int mTexUniformHandle;
    private int mTextureId[] = new int[1];

    private float mBitmapAspectRatio;
    private float mScreenAspectRatio;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float mTranslationOffset;
    private float mCurrentPositionX;
    private float mHorizontalMaxOffset;
    private static final float mTranslationPixel = 4; //每一帧平移的像素值

    public TextureRectangle(Context context) {
        mContext = context;
        loadTexture();
        initVertexData();
        initGL();
    }

    private void initVertexData() {
        //根据纹理的宽高比绘制矩形,使得纹理不变形
        //此时矩形x轴的范围:[-1,1], y轴的范围:[-mBitmapAspectRatio, mBitmapAspectRatio].
        float y = mBitmapAspectRatio * 0.5f;
        mVertexRectangle = new float[]{
//                -1f, mBitmapAspectRatio, 0f,
//                -1f, -mBitmapAspectRatio, 0f,
//                1f, mBitmapAspectRatio, 0f,
//                -1f, -mBitmapAspectRatio, 0f,
//                1f, -mBitmapAspectRatio, 0f,
//                1f, mBitmapAspectRatio, 0f,

                -1f, y, 0f,
                -1f, -y, 0f,
                0f, y, 0f,
                0f, -y, 0f,

                0f, -y, 0f,
                1f, -y, 0f,
                0f, y, 0f,
                1f, y, 0,
        };

        mVertexBuffer = ByteBuffer.allocateDirect(mVertexRectangle.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexRectangle);
        mVertexBuffer.position(0);
        mTexCoorBuffer = ByteBuffer.allocateDirect(VERTEX_TEXTURE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX_TEXTURE);
        mTexCoorBuffer.position(0);
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

        //纹理相关
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        mTexUniformHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                0, mVertexBuffer);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoorBuffer);
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
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        mBitmapAspectRatio = (float) height / (float) width;
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
        mScreenAspectRatio = (float) width / (float) height;
        if (DEBUG) {
            Log.d(TAG, " onSurfaceChanged() mScreenAspectRatio== " + mScreenAspectRatio);
        }
        //屏幕右测在模型坐标系X轴的位置
        float screenPositionX = mScreenAspectRatio * mBitmapAspectRatio;

        //left,top,right,bottom都乘以纹理的宽高比,使得纹理刚好以高为基准充满屏幕,宽自适应
        Matrix.frustumM(mProjectionMatrix, 0, -screenPositionX,
                screenPositionX, -mBitmapAspectRatio, mBitmapAspectRatio, 1f, 8f);
        //setLookAtM() X轴的最大值
        mHorizontalMaxOffset = 1 - screenPositionX;
        //每一帧平移的量
        mTranslationOffset = (screenPositionX * 2 * mTranslationPixel) / (float) width;
        if (DEBUG) {
            Log.d(TAG, " onSurfaceChanged() mTranslationOffset== " + mTranslationOffset);
        }
    }

    /**
     * setLookAtM()的X轴范围为 [(mScreenAspectRatio * mBitmapAspectRatio)-1,1-(mScreenAspectRatio * mBitmapAspectRatio)]
     */
    public void drawSelf() {
//        mCurrentPositionX += mTranslationOffset;
//        if(mCurrentPositionX >= mHorizontalMaxOffset){
//            mCurrentPositionX = -mHorizontalMaxOffset;
//        }
        Matrix.setLookAtM(mViewMatrix, 0, mCurrentPositionX, 0, 4f, mCurrentPositionX, 0, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glUniform1i(mTexUniformHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);

        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexRectangle.length / 3);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
    }
}
