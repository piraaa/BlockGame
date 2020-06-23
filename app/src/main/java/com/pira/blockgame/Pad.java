package com.pira.blockgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Pad implements DrawableItem {
    private final float mTop;
    private float mLeft;
    private final float mBottom;
    private float mRight;

    public Pad(float top, float bottom){
        mTop = top;
        mBottom = bottom;
    }

    public void SetLR(float left, float right){
        mLeft = left;
        mRight = right;
    }

    public void draw(Canvas canvas, Paint paint){
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(mLeft, mTop, mRight, mBottom, paint);
    }

    public float getTop() {
        return mTop;
    }
}
