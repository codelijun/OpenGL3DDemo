package com.example.lijun.opengl3ddemo.util;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;

public class ScreenUtil {
    public static int getScreenWidth(@NonNull Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(@NonNull Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int dip2px(@NonNull Context context, float dpValue) {
        float var2 = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * var2 + 0.5F);
    }

    /**
     * 输入起点、长度、旋转角度计算终点
     *
     * @param startPoint 起点
     * @param length     长度
     * @param angle      旋转角度
     * @return 终点
     */
    public static PointF calculatPoint(PointF startPoint, float length, float angle) {
        float deltaX = (float) Math.cos(Math.toRadians(angle)) * length;
        //符合Android坐标的y轴朝下的标准
        float deltaY = (float) Math.sin(Math.toRadians(angle - 180)) * length;
        return new PointF(startPoint.x + deltaX, startPoint.y + deltaY);
    }
}
