package com.example.lijun.opengl3ddemo.objectgl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.lijun.opengl3ddemo.BuildConfig;
import com.example.lijun.opengl3ddemo.util.GLUtil;
import com.example.lijun.opengl3ddemo.util.ShaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class AdjustBrightness {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "AdjustBrightness";

    private int glProgramId;
    private int glUMatrix;
    private int glULightPosition;
    private int glAPosition;
    private int glANormal;
    private int glUDiffuseStrength;
    private int glULightColor;

    private int mTexCoordHandle;
    private int mTexUniformHandle;

    private int mTextureId[] = new int[1];
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    protected float[] mVertexRectangle;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer mTexCoorBuffer;//顶点纹理坐标数据缓冲

    private float mCurrentValue;
    private float mLightValue;
    private float mBitmapAspectRatio;

    private final float[] normal = new float[]{
            0f, 0f, -1f,
            0f, 0f, -1f,
            0f, 0f, -1f,
            0f, 0f, -1f,
    };

    private static final float[] VERTEX_TEXTURE = {
            0, 0,
            0, 1,
            1, 0,
            1, 1,
    };

    private float lx, ly, lz;

    public AdjustBrightness(Context context) {
        loadTexture(context);
        initVertexData();
        initGL(context);
    }

    protected void initVertexData() {
        //根据纹理的宽高比绘制矩形,使得纹理不变形
        //此时矩形x轴的范围:[-1,1], y轴的范围:[-mBitmapAspectRatio, mBitmapAspectRatio].
        mVertexRectangle = new float[]{
                -1f, mBitmapAspectRatio, 0f,
                -1f, -mBitmapAspectRatio, 0f,
                1f, mBitmapAspectRatio, 0,
                1f, -mBitmapAspectRatio, 0f,
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

        normalBuffer = ByteBuffer.allocateDirect(normal.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(normal);
        normalBuffer.position(0);
    }


    private void loadTexture(Context context) {
        InputStream is = null;
        try {
            is = context.getAssets().open("wallpaper.jpg");
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

        mTextureId[0] = GLUtil.loadTextures(0, bitmap);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.gc();
    }

    public void initGL(Context context) {
        glProgramId = ShaderUtils.createProgram(context.getResources(), "light/light.vert", "light/light.frag");
        glAPosition = GLES20.glGetAttribLocation(glProgramId, "aPosition");
        glANormal = GLES20.glGetAttribLocation(glProgramId, "aNormal");
        glUMatrix = GLES20.glGetUniformLocation(glProgramId, "uMVPMatrix");
        glULightPosition = GLES20.glGetUniformLocation(glProgramId, "uLightPosition");
        glUDiffuseStrength = GLES20.glGetUniformLocation(glProgramId, "uDiffuseStrength");
        glULightColor = GLES20.glGetUniformLocation(glProgramId, "uLightColor");

        //纹理相关
        mTexCoordHandle = GLES20.glGetAttribLocation(glProgramId, "aTexcoord");
        mTexUniformHandle = GLES20.glGetUniformLocation(glProgramId, "uTexture");

        GLES20.glUseProgram(glProgramId);

        //传入顶点信息

//        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(glAPosition, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        //传入法线信息
//        mVertexBuffer.position(3);
        GLES20.glVertexAttribPointer(glANormal, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);


        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexCoorBuffer);

    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        lx = 0f;
        ly = 0f;
        lz = 4f;

        float screenAspectRatio = (float) width / (float) height;
        //纹理刚好以高为基准充满屏幕,宽自适应
        if (screenAspectRatio < 1 / mBitmapAspectRatio) {
            //屏幕右测在模型坐标系X轴的位置
            float screenRightPositionX = screenAspectRatio * mBitmapAspectRatio;
            Matrix.frustumM(mProjectionMatrix, 0, -screenRightPositionX, screenRightPositionX,
                    -mBitmapAspectRatio, mBitmapAspectRatio, 1f, 100f);
        } else {
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1,
                    -1 / screenAspectRatio, 1 / screenAspectRatio, 1f, 100f);
        }
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 1,
                0, 0, 0f, 0, 1, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //光源颜色
        GLES20.glUniform3f(glULightColor, 1.0f, 1.0f, 1.0f);
        //光源位置
        GLES20.glUniform3f(glULightPosition, lx, ly, lz);
    }

    public void drawSelf() {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        if (mCurrentValue > 360) {
            mCurrentValue = 0;
        }
        mCurrentValue+=5;
        mLightValue = (float) ((Math.sin(Math.toRadians(mCurrentValue)) + 1) * 0.5f) + 1;//[1,2]

        GLES20.glEnableVertexAttribArray(glAPosition);
        GLES20.glEnableVertexAttribArray(glANormal);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glUniform1i(mTexUniformHandle, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);

        //漫反射光强度
        GLES20.glUniform1f(glUDiffuseStrength, mLightValue);

        GLES20.glUniformMatrix4fv(glUMatrix, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexRectangle.length / 3);

        GLES20.glDisableVertexAttribArray(glAPosition);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
        GLES20.glDisableVertexAttribArray(glANormal);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }
}
