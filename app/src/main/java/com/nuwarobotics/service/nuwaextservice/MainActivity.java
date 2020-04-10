package com.nuwarobotics.service.nuwaextservice;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private boolean DEBUG = false;

    private Context mContext;

    private TextView mTVInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Example of a call to a native method
        mTVInfo = (TextView) findViewById(R.id.tv_info);
        //mTVInfo.setText(stringFromJNI());
        Log.d(TAG, "onCreate");
        mContext = this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void BtnReception(View view) {
        mTVInfo.append("Reception\n");
        startService("ExtBehaviorService");
    }

    public void BtnTest(View view) {
        mTVInfo.append("Test Func.\n");
        //Intent intent = new Intent(this, FaceRecogActivity.class);
        //Intent intent = new Intent(this, BasicBehaviorActivity.class);
/*
        Intent intent = new Intent(this, MeetingRoomActivity.class);
        intent.putExtra("img", "https://www.sbj.or.jp/2016/wp-content/uploads/file/map/2016/conf_map-3fe.jpg");
        intent.putExtra("msg", "田中さま，本日14時より社長どの打ち合わせがございます。第一会議室にご案内いたします。");
        startActivity(intent);
*/
        //Intent intent = new Intent(this, MeetingActivity.class);
        Intent intent = new Intent(this, PersonVisitActivity.class);
        intent.putExtra("faceid", Long.parseLong("1000"));
        intent.putExtra("nickname", "妹妹2");
        startActivity(intent);

    }

    public void BtnTest2(View view) {
        mTVInfo.append("Test Func2.\n");
        Intent intent = new Intent(this, MeetingActivity.class);
        intent.putExtra("faceid", Long.parseLong("1000"));
        intent.putExtra("nickname", "妹妹2");
        startActivity(intent);

    }

    public void BtnTest3(View view) {
        mTVInfo.append("Test Func2.\n");
        Intent intent = new Intent(this, VisitAndMeetingActivity.class);
        intent.putExtra("faceid", Long.parseLong("1000"));
        intent.putExtra("nickname", "妹妹2");
        startActivity(intent);

    }
    public void BtnStop(View view) {
        mTVInfo.append("Stop all\n");
        stopService("ExtBehaviorService");
    }

    public void BtnWelcome(View view) {
        mTVInfo.append("Welcome\n");
    }

    private void startService(String servicename) {
        Intent intent = new Intent();
        intent.setClassName("com.nuwarobotics.service.nuwaextservice", "com.nuwarobotics.service.nuwaextservice.service." + servicename);
        startForegroundService(intent);
    }

    private void stopService(String servicename) {
        Intent intent = new Intent();
        intent.setClassName("com.nuwarobotics.service.nuwaextservice", "com.nuwarobotics.service.nuwaextservice.service." + servicename);
        stopService(intent);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
