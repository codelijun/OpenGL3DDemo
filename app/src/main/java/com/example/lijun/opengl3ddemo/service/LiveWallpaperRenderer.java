package com.example.lijun.opengl3ddemo.service;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.lijun.opengl3ddemo.objectgl.AdjustBrightness;
import com.example.lijun.opengl3ddemo.objectgl.Circle;
import com.example.lijun.opengl3ddemo.objectgl.Cube;
import com.example.lijun.opengl3ddemo.objectgl.Cylinder;
import com.example.lijun.opengl3ddemo.objectgl.Point;
import com.example.lijun.opengl3ddemo.objectgl.Rectangle;
import com.example.lijun.opengl3ddemo.objectgl.TextureCylinder;
import com.example.lijun.opengl3ddemo.objectgl.TextureRectangle;
import com.example.lijun.opengl3ddemo.objectgl.Triangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LiveWallpaperRenderer implements GLSurfaceView.Renderer {

    private Context mContext;

    private Cylinder mCylinder;
    private Circle mCircle;
    private Rectangle mRectangle;
    private Triangle mTriangle;
    private Cube mCube;
    private Point mPoint;
    private TextureCylinder mTextureCylinder;
    private TextureRectangle mTextureRectangle;
    private AdjustBrightness mAdjustBrightness;

    public LiveWallpaperRenderer(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA,
                GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE, GLES20.GL_ONE);
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        //启用表面剔除功能
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        //指定哪些面不绘制
        GLES20.glCullFace(GLES20.GL_BACK);

//        mTriangle = new Triangle();
//        mRectangle = new Rectangle();
//        mCube = new Cube();
//        mCylinder = new Cylinder();
//        mCircle = new Circle(mContext);
//        mTextureCylinder = new TextureCylinder(mContext);
//        mTextureRectangle = new TextureRectangle(mContext);
//        mPoint = new Point();
        mAdjustBrightness = new AdjustBrightness(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (height == 0) {
            height = 1;
        }
        GLES20.glViewport(0, 0, width, height);
//        mRectangle.onSurfaceChanged(gl, width, height);
//        mTriangle.onSurfaceChanged(gl, width, height);
//        mCube.onSurfaceChanged(gl, width, height);
//        mCylinder.onSurfaceChanged(gl, width, height);
//        mCircle.onSurfaceChanged(gl, width, height);
//        mTextureCylinder.onSurfaceChanged(gl, width, height);
//        mTextureRectangle.onSurfaceChanged(gl, width, height);
//        mPoint.onSurfaceChanged(gl, width, height);
        mAdjustBrightness.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//        mRectangle.drawSelf();
//        mTriangle.drawSelf();
//        mCube.drawSelf();
//        mCylinder.drawSelf();
//        mCircle.drawSelf();
//        mTextureCylinder.drawSelf();
//        mTextureRectangle.drawSelf();
//        mPoint.drawSelf();
        mAdjustBrightness.drawSelf();
    }
}
