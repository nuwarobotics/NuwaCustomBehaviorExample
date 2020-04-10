package com.nuwarobotics.service.nuwaextservice.sub;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.nuwarobotics.service.nuwaextservice.R;
import com.nuwarobotics.service.nuwaextservice.basic.BasicBehaviorActivity;
import com.nuwarobotics.service.nuwaextservice.utils.utils;

import java.io.InputStream;

public class MeetingInfoActivity extends BasicBehaviorActivity {
    private final String TAG = "MeetingInfoActivity";

    private Context mContext;

    private ImageView iv_meeting_room;
    private TextView iv_meeting_info;
    private TextView iv_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_info_view);

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
        iv_meeting_room = (ImageView) findViewById(R.id.imgview_meeting_room);
        iv_meeting_info = (TextView) findViewById(R.id.meeting_info);
        iv_title = (TextView) findViewById(R.id.title);

        if (getIntent().hasExtra("name")) {
            mNickname = getIntent().getStringExtra("name");
        }

        //Get image
        if (getIntent().hasExtra("img")) {
            String imgpath = getIntent().getStringExtra("img");
            new ImageDownloadTask().execute(imgpath);
        }

        if (getIntent().hasExtra("msg")) {
            String msg = getIntent().getStringExtra("msg");
            StringBuilder sb = new StringBuilder();
            sb.append(msg);
            iv_meeting_info.setText(sb.toString());
        }

        if (getIntent().hasExtra("staff")) {
            String staff = getIntent().getStringExtra("staff");
            mParam2 = staff;
        }

        if (getIntent().hasExtra("title")) {
            String title = getIntent().getStringExtra("title");
            iv_title.setText(title);
            mParam3 = title;
        }

        if (getIntent().hasExtra("date")) {
            String date = getIntent().getStringExtra("date");
            mParam1 = date;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                BehaviorDo(utils.readJson(getApplicationContext(), "meeting_info"));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    public void BtnQuit(View view) {
        Intent intent = new Intent();
        Log.d(TAG, "BtnQuit");
        setResult(100, intent);
        finish();
    }


    private class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {

        public ImageDownloadTask() {
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap img = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                img = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
            return img;
        }

        protected void onPostExecute(Bitmap img) {
            iv_meeting_room.setImageBitmap(img);
            iv_meeting_room.setVisibility(View.VISIBLE);
        }
    }
}
