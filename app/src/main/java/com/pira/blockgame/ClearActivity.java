package com.pira.blockgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.preference.PreferenceManager;

public class ClearActivity extends AppCompatActivity {
    public static final String EXTRA_IS_CLEAR = "com.pira.blockgame.EXTRA_IS_CLEAR";
    public static final String EXTRA_BLOCK_COUNT = "com.pira.blockgame.EXTRA_BLOCK_COUNT";
    public static final String EXTRA_TIME = "com.pira.blockgame.EXTRA_TIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear);

        Intent receiveIntent = getIntent();
        if(receiveIntent == null){
            finish();
        }

        Bundle receiveExtras = receiveIntent.getExtras();
        if(receiveExtras == null){
            finish();
        }

        boolean isClear = receiveExtras.getBoolean(EXTRA_IS_CLEAR, false);
        int blockCount = receiveExtras.getInt(EXTRA_BLOCK_COUNT, 0);
        long clearTime = receiveExtras.getLong(EXTRA_TIME, 0);

        TextView textTitle = (TextView)findViewById(R.id.textTitle);
        TextView textBlockCount = (TextView)findViewById(R.id.textBlockCount);
        TextView textCleatTime = (TextView)findViewById(R.id.textClearTime);
        final Button startButton = (Button)findViewById(R.id.startButton);

        if(isClear){
            textTitle.setText(R.string.clear);
        }else{
            textTitle.setText(R.string.gameover);
        }
        textBlockCount.setText(getString(R.string.block_count, blockCount));
        textCleatTime.setText(getString(R.string.time, clearTime/1000, clearTime%1000));

        // スコア
        TextView textScore = (TextView)findViewById(R.id.textScore);
        final long score = (blockCount) * clearTime;
        textScore.setText(getString(R.string.score, score));

        // ハイスコア
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long highscore = sp.getLong("high_score", 0);
        if(highscore < score){
            highscore = score;
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong("high_score", highscore);
            editor.commit();
        }
        TextView textHighScore = (TextView)findViewById(R.id.textHighScore);
        textHighScore.setText(getString(R.string.high_score, highscore));

        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClearActivity.this, GameActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); //Activityの履歴に記録しないことで端末の戻るボタンで戻れなくする
                startActivity(intent);
            }
        });
    }
}
