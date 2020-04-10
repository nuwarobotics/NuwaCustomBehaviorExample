package com.nuwarobotics.service.nuwaextservice.sub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nuwarobotics.service.nuwaextservice.R;
import com.nuwarobotics.service.nuwaextservice.basic.BasicBehaviorActivity;
import com.nuwarobotics.service.nuwaextservice.utils.utils;

public class RoomListActivity extends BasicBehaviorActivity {
    private static String TAG = "RoomListActivity";
    private String[] mVisitorNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        mVisitorNameList = getResources().getStringArray(R.array.conference_room_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.listitem_view, R.id.txt_item_name, mVisitorNameList);

        ListView listview = (ListView) findViewById(R.id.list_vistiors);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: " + listview.getAdapter().getItem(position));
                Intent intent = new Intent();
                intent.putExtra("room", listview.getAdapter().getItem(position).toString());
                setResult(100, intent);
                finish();
            }
        });

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mNickname = getIntent().getStringExtra("visitor");
                BehaviorDo(utils.readJson(getApplicationContext(), "room_list"));
            }
        });
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

}
