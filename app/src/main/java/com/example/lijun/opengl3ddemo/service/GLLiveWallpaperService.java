package com.example.lijun.opengl3ddemo.service;

import android.util.Log;

import com.example.lijun.opengl3ddemo.BuildConfig;
import com.example.livewallpapergllibrary.net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class GLLiveWallpaperService extends GLWallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new LiveWallpaperEngine();
    }

    private class LiveWallpaperEngine extends GLEngine {
        private final boolean DEBUG = BuildConfig.DEBUG;
        private static final String TAG = "LiveWallpaperEngine";
        public static final int SENSOR_RATE = 60;
        private LiveWallpaperRenderer mRenderer;

        public LiveWallpaperEngine() {
            this.mRenderer = new LiveWallpaperRenderer(getApplicationContext());
            setEGLContextClientVersion(2);
            setRenderer(mRenderer);
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
        }
    }
}
