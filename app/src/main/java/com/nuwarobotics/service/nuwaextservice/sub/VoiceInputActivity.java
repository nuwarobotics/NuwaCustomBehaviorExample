package com.nuwarobotics.service.nuwaextservice.sub;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.nuwarobotics.service.nuwaextservice.R;
import com.nuwarobotics.service.nuwaextservice.basic.BasicBehaviorActivity;


public class VoiceInputActivity extends BasicBehaviorActivity {
    private final String TAG = "VoiceInputActivity";

    private Context mContext;

    TextView tv_Title;
    TextView tv_NoInput;
    EditText et_Input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_input_view);

        //FIX status bar was shown after DialogFragment.show
        final View decorView = this.getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        hideSystemUi();
                    }
                });

        mContext = this;

        tv_Title = (TextView) findViewById(R.id.title);
        String title = "Input";
        if (getIntent().hasExtra("title")) {
            title = getIntent().getStringExtra("title");
        }
        tv_Title.setText(title);

        tv_NoInput = (TextView) findViewById(R.id.not_input);
        et_Input = (EditText) findViewById(R.id.input_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
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

    public void BtnNext(View view) {
        if (et_Input.getText().length() == 0) {
            tv_NoInput.setVisibility(View.VISIBLE);
        } else {
            Intent intent = new Intent();
            String input_text = et_Input.getText().toString();
            Log.d(TAG, "BtnNext, input_text=" + input_text);
            intent.putExtra("input", input_text);
            setResult(2, intent);
            finish();
        }
    }

    public void BtnQuit(View view) {
        finish();
    }

}
