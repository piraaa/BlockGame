package com.pira.blockgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Ball implements DrawableItem {
    private float mX;
    private float mY;
    private float mSpeedX;
    private float mSpeedY;
    private final float mRadius;

    // 初期値
    private final float mInitialX, mInitialY;
    private final float mInitialSpeedX, mInitialSpeedY;

    public Ball(float radius, float initialX, float initialY){
        mInitialX = initialX;
        mInitialY = initialY;
        mInitialSpeedX = radius/5;
        mInitialSpeedY = -1 * radius/5;
        mRadius = radius;
        mX = mInitialX;
        mY = mInitialY;
        mSpeedX = mInitialSpeedX;
        mSpeedY = mInitialSpeedY;
    }

    public void move(){
        mX += mSpeedX;
        mY += mSpeedY;
    }

    public void draw(Canvas canvas, Paint paint){
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mX, mY, mRadius, paint);
    }

    public void reset(){
        mX = mInitialX;
        mY = mInitialY;
        mSpeedX = mInitialSpeedX;
        mSpeedY = mInitialSpeedY;

    }

    public float getSpeedX() {
        return mSpeedX;
    }

    public float getSpeedY() {
        return mSpeedY;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public void setSpeedX(float speedX) {
        this.mSpeedX = speedX;
    }

    public void setSpeedY(float speedY) {
        this.mSpeedY = speedY;
    }
}
