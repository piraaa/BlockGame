package com.pira.blockgame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import java.util.ArrayList;

// ゲーム画面を描画するためのTextureView

public class GameView extends TextureView implements TextureView.SurfaceTextureListener, View.OnTouchListener {
    private Thread mthread;
    volatile private boolean mIsRunnable;
    volatile private float mTouchedX;
    volatile private float mTouchedY;

    private ArrayList<DrawableItem> mItemList;
    private ArrayList<Block> mBlockList;

    private float mBlockWidth, mBlockHeight;

    private Pad mPad;
    private float mPadHalfWidth;

    private Ball mBall;
    private float mBallRadius;

    private int mLife;

    private long mGameStartTime;

    private Handler mHandler;

    static final int BLOCK_NUM = 100;
    static final int LIFE_NUM = 5;

    public GameView(final Context context){
        super(context);
        setSurfaceTextureListener(this);
        setOnTouchListener(this);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Intent intent = new Intent(context, ClearActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.putExtras(msg.getData());
                context.startActivity(intent);
            }
        };
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        readyObjects(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        readyObjects(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        synchronized (this){
            return true;
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    public void start(){

        mthread = new Thread(new Runnable() {
            @Override
            public void run() {
                Paint paint = new Paint();
                ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);

                float padLeft, padRight;
                float ballTop, ballLeft, ballBottom, ballRight;
                Block tmp;
                float padTop, ballSpeedY;
                boolean isCollision;

                // ブロック衝突音関係
                int collisionTime = 0;
                int soundIndex = ToneGenerator.TONE_DTMF_0; // 0

                while (true) {
                    long startTime = System.currentTimeMillis();

                    synchronized (GameView.this) {
                        if (!mIsRunnable) {
                            break;
                        }

                        Canvas canvas = lockCanvas();
                        if (canvas == null) {
                            continue;
                        }

                        canvas.drawColor(Color.BLACK);

                        padLeft = mTouchedX - mPadHalfWidth;
                        padRight = mTouchedX + mPadHalfWidth;
                        mPad.SetLR(padLeft, padRight);
                        // mPad.draw(canvas, paint);

                        mBall.move();
                        ballTop = mBall.getY() - mBallRadius;
                        ballLeft = mBall.getX() - mBallRadius;
                        ballBottom = mBall.getY() + mBallRadius;
                        ballRight = mBall.getX() + mBallRadius;

                        // 壁衝突判定
                        if ((ballLeft < 0 && mBall.getSpeedX() < 0) || (ballRight >= getWidth() && mBall.getSpeedX() > 0)) {
                            mBall.setSpeedX(-1 * mBall.getSpeedX());
                            tg.startTone(ToneGenerator.TONE_DTMF_0, 10); // 第2引数=再生時間 10[ms]
                        }
                        if (ballTop < 0) {
                            mBall.setSpeedY(-1 * mBall.getSpeedY());
                            tg.startTone(ToneGenerator.TONE_DTMF_0, 10); // 第2引数=再生時間 10[ms]
                        }

                        // 死亡判定
                        if (ballTop > getHeight()) {
                            if (mLife > 0) {
                                mLife--;
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {

                                }
                                mBall.reset();
                            } else {
                                tg.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE);
                                unlockCanvasAndPost(canvas);
                                Message message = Message.obtain();
                                Bundle bundle = new Bundle();
                                bundle.putBoolean(ClearActivity.EXTRA_IS_CLEAR, false);
                                bundle.putInt(ClearActivity.EXTRA_BLOCK_COUNT, BLOCK_NUM - getBlockCount());
                                bundle.putLong(ClearActivity.EXTRA_TIME, System.currentTimeMillis() - mGameStartTime);
                                message.setData(bundle);
                                mHandler.sendMessage(message);
                                return;
                            }
                        }

                        // ブロック衝突判定
                        isCollision = false;
                        tmp = getBlock(ballLeft, mBall.getY());
                        if (tmp != null) {
                            mBall.setSpeedX(-1 * mBall.getSpeedX());
                            tmp.collision();
                            isCollision = true;
                        }
                        tmp = getBlock(mBall.getX(), ballTop);
                        if (tmp != null) {
                            mBall.setSpeedY(-1 * mBall.getSpeedY());
                            tmp.collision();
                            isCollision = true;
                        }
                        tmp = getBlock(ballRight, mBall.getY());
                        if (tmp != null) {
                            mBall.setSpeedX(-1 * mBall.getSpeedX());
                            tmp.collision();
                            isCollision = true;
                        }
                        tmp = getBlock(mBall.getX(), ballBottom);
                        if (tmp != null) {
                            mBall.setSpeedY(-1 * mBall.getSpeedY());
                            tmp.collision();
                            isCollision = true;
                        }

                        // ブロック衝突時の音
                        if(isCollision){
                            if(collisionTime>0){
                                if(soundIndex<ToneGenerator.TONE_DTMF_D) { // 15
                                    soundIndex++;
                                }
                            }else{
                                soundIndex=ToneGenerator.TONE_DTMF_0;
                            }
                            collisionTime = 10;
                            tg.startTone(soundIndex, 10);
                        }else if(collisionTime>0){
                            collisionTime--;
                        }

                        // パッド衝突判定
                        padTop = mPad.getTop();
                        ballSpeedY = mBall.getSpeedY();
                        if (ballBottom > padTop && (ballBottom - ballSpeedY) < padTop && padLeft < ballRight && padRight > ballLeft) {
                            if (ballSpeedY < mBlockHeight / 3) {
                                ballSpeedY = -1 * ballSpeedY * 1.05f;
                            } else {
                                ballSpeedY = -1 * ballSpeedY;
                            }
                            mBall.setSpeedY(ballSpeedY);
                            tg.startTone(ToneGenerator.TONE_DTMF_0, 10); // 第2引数=再生時間 10[ms]
                        }

                        for (DrawableItem item : mItemList) {
                            item.draw(canvas, paint);

                        }

                        // paint.setColor(Color.RED);
                        // paint.setStyle(Paint.Style.FILL);
                        // canvas.drawCircle(mTouchedX, mTouchedY, 50, paint);

                        unlockCanvasAndPost(canvas);

                        if (isCollision && getBlockCount() == 0) {
                            tg.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE);
                            Message message = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(ClearActivity.EXTRA_IS_CLEAR, true);
                            bundle.putInt(ClearActivity.EXTRA_BLOCK_COUNT, BLOCK_NUM);
                            bundle.putLong(ClearActivity.EXTRA_TIME, System.currentTimeMillis() - mGameStartTime);
                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        }

                        long sleepTime = 16 - System.currentTimeMillis() - startTime; // 16[ms] = 1/60[s]
                        if (sleepTime > 0) {
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {

                            }
                        }
                    }
                }
                tg.release();
            }
        });
        mIsRunnable = true;
        mthread.start();
    }

    public void stop(){
        mIsRunnable = false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mTouchedX = motionEvent.getX();
        mTouchedY = motionEvent.getY();
        return true;
    }

    public  void readyObjects(int width, int height){
        mBlockWidth = width / 10;
        mBlockHeight = height / 30;

        mItemList = new ArrayList<DrawableItem>();
        mBlockList = new ArrayList<Block>();

        for(int i=0; i<BLOCK_NUM; i++){
            float blockTop = i / 10 * mBlockHeight;
            float blockLeft = i % 10 * mBlockWidth;
            float blockBottom = blockTop + mBlockHeight;
            float blockRight = blockLeft + mBlockWidth;
            mBlockList.add(new Block(blockTop, blockLeft, blockBottom, blockRight));
        }
        mItemList.addAll(mBlockList);

        mPad = new Pad(height*0.8f, height*0.82f);
        mItemList.add(mPad);
        mPadHalfWidth = width / 7; // 画面サイズを基準にパッドの幅を決定

        mBallRadius = width<height ? width/40 : height/40;
        mBall = new Ball(mBallRadius, width/2, height/2);
        mItemList.add(mBall);

        mLife = LIFE_NUM;

        mGameStartTime = System.currentTimeMillis();
    }

    private Block getBlock(float x, float y){
        int index = (int)(y/mBlockHeight) * 10 + (int)(x/mBlockWidth);
        if (0<=index && index<BLOCK_NUM) {
            Block block = (Block) mItemList.get(index);
            if (block.isExist()) {
                return block;
            }
        }
        return null;
    }

    // 残ってるブロックの数を取得
    private int getBlockCount(){
        int count = 0;
        for(Block block : mBlockList){
            if(block.isExist()){
                count++;
            }
        }
        return count;
    }
}
