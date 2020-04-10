package com.nuwarobotics.service.nuwaextservice.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nuwarobotics.service.custombehavior.BaseBehaviorService;
import com.nuwarobotics.service.custombehavior.CustomBehavior;
import com.nuwarobotics.service.nuwaextservice.ACTION;
import com.nuwarobotics.service.nuwaextservice.MeetingActivity;
import com.nuwarobotics.service.nuwaextservice.PersonVisitActivity;
import com.nuwarobotics.service.nuwaextservice.R;
import com.nuwarobotics.service.nuwaextservice.ReservedActivity;
import com.nuwarobotics.service.nuwaextservice.VisitAndMeetingActivity;
import com.nuwarobotics.service.nuwaextservice.contact.ContactHelper;
import com.nuwarobotics.service.nuwaextservice.utils.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ExtBehaviorService extends BaseBehaviorService {

    public static final String TAG = "ExtBehaviorService";

    public static final int msgBehaviorCompleted = 0;
    public static final int msgBehaviorQueue = 1;
    public static final int msgExtBehavior = 2;

    private static final int BEHAVIOR_NONE = 0;
    private static final int BEHAVIOR_EXTBEHAVIOR = 1 << 1; //1

    public static final String INTENT_EXT_FINISH = "com.nuwarobotics.service.nuwaextservice.ExtBehaviorService.EXT_FINISH";

    private final static int BEHAVIOR_DELAY = 500; //ms
    private Context mContext;

    private Handler mHandler;
    private HandlerThread mThread;

    private int mBehavior = BEHAVIOR_NONE;
    private long mFaceID = 0;
    private String mNickname = "";
    private String mASRResult = "";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate.");
        mContext = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");

        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand: intent=" + intent + " flags=" + flags + " startId=" + startId);

        SetForeground();

        initHandle();

        intentRegister();

        return START_STICKY;
    }

    private void SetForeground() {
        String NOTIFICATION_CHANNEL_ID = getApplicationContext().getPackageName();
        String channelName = getClass().getSimpleName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel chan = new android.app.NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            chan.setShowBadge(false);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        } else {
            Log.e(TAG, "system below Oreo, no NotificationChannel");
            return;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("ExtService is running in background") // notification text
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }

    private void intentRegister() {
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(INTENT_EXT_FINISH);

        registerReceiver(mBroadcastReceiver, intentfilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equalsIgnoreCase(INTENT_EXT_FINISH)) {
                BehaviorDone(BEHAVIOR_EXTBEHAVIOR);
            }
        }
    };

    //Basic behavior
    @Override
    public void onInitialize() {
        try {
            mSystemBehaviorManager.setWelcomeSentence(getResources().getStringArray(R.array.welcom_sentence));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CustomBehavior createCustomBehavior() {
        return new CustomBehavior.Stub() {
            @Override
            public void onWelcome(String name, long faceid) {
                Log.d(TAG, "onWelcome: name=" + name + ", faceid=" + faceid);
                if (faceid > 0) {
                    SQLiteDatabase db = new ContactHelper(mContext).getWritableDatabase();
                    ContentValues contactdata = new ContentValues();
                    contactdata.put("faceid", faceid);
                    contactdata.put("nickname", name);
                    db.update(ContactHelper.TABLE_NAME, contactdata, android.provider.BaseColumns._ID + "=" + faceid, null);
                    mNickname = name;
                    mFaceID = faceid;

                } else {
                    mNickname = "";
                    mFaceID = faceid;
                }
/*
                //TODO
                Message msg = new Message();
                msg.what = msgExtBehavior;
                Bundle bundle = new Bundle();
                bundle.putString("action", "reserved_behavior");
                msg.setData(bundle);
                mHandler.sendMessageDelayed(msg, BEHAVIOR_DELAY);

 */
            }

            @Override
            public void prepare(final String parameter) {

            }

            @Override
            public void process(final String parameter) {
                Log.d(TAG, "process: " + parameter);
                mBehavior = BEHAVIOR_NONE;
                boolean ret = BehaviorDo(parameter);
                if (!ret) {
                    mBehavior = BEHAVIOR_NONE;
                    mHandler.sendEmptyMessage(msgBehaviorCompleted);
                }
            }

            @Override
            public void finish(final String parameter) {
            }
        };
    }

    private void initHandle() {
        //initial
        mThread = new HandlerThread("BehaviorThread");
        mThread.start();

        mHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                    case msgBehaviorCompleted:
                        try {
                            initParam();
                            notifyBehaviorFinished(); //
                        } catch (RemoteException RE) {
                            Log.e(TAG, "handleMessage, RemoteException", RE);
                        }
                        break;
                    case msgExtBehavior:
                        mBehavior |= BEHAVIOR_EXTBEHAVIOR;
                        String act = msg.getData().getString("action");
                        if (act.equalsIgnoreCase(ACTION.BEHAVIOR_PERSON_VISIT)) {
                            Intent intent = new Intent(mContext, PersonVisitActivity.class);
                            intent.putExtra("faceid", mFaceID);
                            intent.putExtra("nickname", mNickname);
                            startActivity(intent);
                        } else if (act.equalsIgnoreCase(ACTION.BEHAVIOR_MEETING_ROOM)) {
                            Intent intent = new Intent(mContext, MeetingActivity.class);
                            intent.putExtra("faceid", mFaceID);
                            intent.putExtra("nickname", mNickname);
                            startActivity(intent);
                        } else if (act.equalsIgnoreCase(ACTION.BEHAVIOR_RESERVED_ACTION)) {
                            Intent intent = new Intent(mContext, ReservedActivity.class);
                            intent.putExtra("faceid", mFaceID);
                            intent.putExtra("nickname", mNickname);
                            startActivity(intent);
                        } else if (act.equalsIgnoreCase(ACTION.BEHAVIOR_VISIT_AND_MEETING)) {
                            Intent intent = new Intent(mContext, VisitAndMeetingActivity.class);
                            intent.putExtra("faceid", mFaceID);
                            intent.putExtra("nickname", mNickname);
                            startActivity(intent);
                        } else {
                            BehaviorDone(BEHAVIOR_EXTBEHAVIOR);
                        }
                        break;
                }
            }
        };
    }

    private void initParam() {
        mBehavior = BEHAVIOR_NONE;

        mNickname = "";
        mASRResult = "";
    }

    private boolean BehaviorDo(String action) {
        boolean ret = true;
        try {
            JSONObject intention = new JSONObject(action);

            //ToBehavior
            if (intention.has("ToAction")) {
                String act = intention.getString("ToAction");
                Message msg = new Message();
                msg.what = msgExtBehavior;
                Bundle bundle = new Bundle();
                bundle.putString("action", (String) act);
                msg.setData(bundle);
                mHandler.sendMessageDelayed(msg, BEHAVIOR_DELAY);
            }
        } catch (JSONException e) {
            Log.e(TAG, "process: JSONException", e);
            ret = false;
        }
        return ret;
    }

    private int BehaviorDone(int behavior) {
        int postBehavior = mBehavior & (~behavior);

        Log.d(TAG, "checkBehaviorDone, mBehavior=" + mBehavior + ", postBehavior=" + postBehavior);
        mBehavior = postBehavior;

        if (mBehavior == BEHAVIOR_NONE) {
            Log.d(TAG, "checkBehaviorDone, All behavior done and send completed");
            mHandler.sendEmptyMessage(msgBehaviorCompleted);
        }

        return 0;
    }

    private String loadJson(String filename) {
        return utils.readJson(this, filename);
    }

}
