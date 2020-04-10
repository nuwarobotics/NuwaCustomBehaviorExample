package com.nuwarobotics.service.nuwaextservice.sub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.util.Log;

import com.nuwarobotics.service.nuwaextservice.R;
import com.nuwarobotics.service.nuwaextservice.basic.BasicBehaviorActivity;
import com.nuwarobotics.service.nuwaextservice.utils.utils;

public class FuncSelectActivity extends BasicBehaviorActivity implements View.OnClickListener {
    private ImageButton mBtnClose;
    private int[] mBtnResList = {R.id.imageButton4, R.id.imageButton5, R.id.imageButton6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_function_list);

        mBtnClose = findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(this);

        for (int resId : mBtnResList) {
            View view = findViewById(resId);

            if (view != null) {
                view.setOnClickListener(this);
            }
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                BehaviorDo(utils.readJson(getApplicationContext(), "func_select"));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton4:
                Log.d("Test", "Click the Telephone button...");
                fisishAction();
                break;
            case R.id.imageButton5:
                Log.d("Test", "Click the Email button...");
                fisishAction();
                break;
            case R.id.imageButton6:
                Log.d("Test", "Click the Message button...");
                fisishAction();
                break;
            case R.id.btn_close:
                fisishAction();
                break;
            default:
                return;
        }
    }

    protected void hideSystemUi() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void fisishAction() {
        Intent intent = new Intent();
        setResult(200, intent);
        finish();
    }
}
