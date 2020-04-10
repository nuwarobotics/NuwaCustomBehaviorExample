package com.nuwarobotics.service.nuwaextservice.sub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ImageButton;

import com.nuwarobotics.service.nuwaextservice.R;
import com.nuwarobotics.service.nuwaextservice.basic.BasicBehaviorActivity;
import com.nuwarobotics.service.nuwaextservice.callback.VoiceCallBack;
import com.nuwarobotics.service.nuwaextservice.utils.utils;

public class EmployeeListActivity extends BasicBehaviorActivity implements VoiceCallBack {
    private static String TAG = "EmployeeListActivity";
    private String[] mEmployeeNameList;

    private ListView listview;
    private int mPosition = -1;
    private boolean waitTTS = false;
    private boolean isFinal = false;
    private boolean isResponsed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        mEmployeeNameList = getResources().getStringArray(R.array.employee_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.listitem_view, R.id.txt_item_name, mEmployeeNameList);

        listview = (ListView) findViewById(R.id.list_vistiors);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: " + listview.getAdapter().getItem(position));
                if(!isFinal) {
                    mPosition = position;
                    mNickname = listview.getAdapter().getItem(mPosition).toString();
                    if(isResponsed) {
                        finalSpeak(String.format(getResources().getString(R.string.visitorlist_contact), listview.getAdapter().getItem(mPosition).toString()), true);
                        isFinal = true;
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                destoryActivity();
                            }
                        });
                    }
                }
            }
        });

        if(getIntent().hasExtra("response")) {
            isResponsed = getIntent().getBooleanExtra("response", true);
            Log.d(TAG, "Response="+isResponsed);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mNickname = getIntent().getStringExtra("visitor");
                BehaviorDo(utils.readJson( getApplicationContext(),"employee_list"));
            }
        });

        setVoiceCallback(this);
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

    private void destoryActivity() {
        Intent intent = new Intent();
        intent.putExtra("staff", mNickname);
        setResult(100, intent);
        finish();
    }

    private void finalSpeak(String tts, boolean isDestory) {
        Message msg = new Message();
        msg.what = msgTTSPlay;
        Bundle bundle = new Bundle();
        bundle.putString("speak", tts);
        msg.setData(bundle);
        mHandler.sendMessageDelayed(msg, 500);
        if(isDestory)
            waitTTS = true;
    }

    public void onSynthesisDone(boolean isSuccessful) {
        Log.d(TAG, "onSynthesisDone, isSuccessful=" + isSuccessful);

        if(waitTTS) {
            waitTTS = false;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    destoryActivity();
                }
            });
        }
    }

    public void onListenDone(boolean isSuccessful, String result) {
        Log.d(TAG, "onListenDone, isSuccessful=" + isSuccessful + ", result=" + result);
        boolean b = true;

        if(!isFinal) {
            if (result != null && !result.isEmpty()) {
                for (String visit : mEmployeeNameList) {
                    if (visit.replaceAll("\\s+", "").equals(result.replaceAll("\\s+", ""))) {
                        b = true;
                        mNickname = visit;
                        if(isResponsed) {
                            finalSpeak(String.format(getResources().getString(R.string.visitorlist_contact), visit), true);
                            isFinal = true;
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    destoryActivity();
                                }
                            });
                        }
                    }
                }
            } else {
                b = true;
                finalSpeak(getResources().getString(R.string.visitorlist_not_found), false);
            }

            if (!b)
                finalSpeak(getResources().getString(R.string.visitorlist_not_found), false);
        }

    }
}
