package com.pira.blockgame;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

public class GameActivity extends AppCompatActivity {

    private GameView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new GameView(this);
        setContentView(mView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.stop();
    }
}
