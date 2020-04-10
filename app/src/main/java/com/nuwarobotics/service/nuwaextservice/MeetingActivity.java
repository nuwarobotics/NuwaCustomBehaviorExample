package com.nuwarobotics.service.nuwaextservice;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.nuwarobotics.service.nuwaextservice.sub.MeetingInfoActivity;
import com.nuwarobotics.service.nuwaextservice.sub.RoomListActivity;

import static com.nuwarobotics.service.nuwaextservice.service.ExtBehaviorService.INTENT_EXT_FINISH;


public class MeetingActivity extends Activity {
    private final String TAG = "MeetingActivity";

    private final int ACTIVITY_FACE_RECOGNITION = 1;
    private final int ACTIVITY_ORGANIZATION = 2;
    private final int ACTIVITY_TITLE = 3;
    private final int ACTIVITY_ROOM_LIST = 4;
    private final int ACTIVITY_MEETING_INFO = 5;

    private String mName = "";
    private long mFaceID = 0;
    private String mOrganization = "";
    private String mTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("faceid") && getIntent().getLongExtra("faceid", 0) > 0) {
            //have person
            mName = getIntent().getStringExtra("nickname");
            lunchRoomListActivity(mName);
        } else {
            Log.d(TAG, "onCreate: " + getIntent().getLongExtra("faceid", 0));
            //no person
            lunchFaceRecogActivity();
        }
    }

    private void lunchFaceRecogActivity() {
        Intent intent = new Intent("com.nuwarobotics.action.FACE_REC");
        intent.setPackage("com.nuwarobotics.app.facerecognition2");
        intent.putExtra("EXTRA_3RD_REC_ONCE", true);
        startActivityForResult(intent, ACTIVITY_FACE_RECOGNITION);
    }

/*    private void startInputOganization() {
        Log.d(TAG, "startInputOganization");
        Intent intent = new Intent(this, VoiceInputActivity.class);
        intent.putExtra("title", "請輸入組織");
        startActivityForResult(intent, ACTIVITY_ORGANIZATION);
    }

    private void startInputTitle() {
        Log.d(TAG, "startInputTitle");
        Intent intent = new Intent(this, VoiceInputActivity.class);
        intent.putExtra("title", "請輸入職稱");
        startActivityForResult(intent, ACTIVITY_TITLE);
    }*/

    private void lunchRoomListActivity(String visitor) {
        Intent intent = new Intent(this, RoomListActivity.class);
        intent.putExtra("visitor", visitor);
        startActivityForResult(intent, ACTIVITY_ROOM_LIST);
    }

    private void lunchRoomInfoActivity(String room) {
        Intent intent = new Intent(this, MeetingInfoActivity.class);
        intent.putExtra("title", room);
        intent.putExtra("name", mName);
        intent.putExtra("msg", String.format(getResources().getString(R.string.meeting_info), room));
        intent.putExtra("img", "https://www.sbj.or.jp/2016/wp-content/uploads/file/map/2016/conf_map-3fe.jpg");
        startActivityForResult(intent, ACTIVITY_MEETING_INFO);
    }

    /**
     * 为了得到传回的数据，必须在前面的Activity中（指MainActivity类）重写onActivityResult方法
     * <p>
     * requestCode 请求码，即调用startActivityForResult()传递过去的值
     * resultCode 结果码，结果码用于标识返回数据来自哪个新Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult, requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (resultCode > 0) {
            switch (requestCode) {
                case ACTIVITY_FACE_RECOGNITION:
                    mFaceID = data.getLongExtra("EXTRA_RESULT_FACEID", 0);
                    mName = data.getStringExtra("EXTRA_RESULT_NAME");
                    Log.d(TAG, "onActivityResult, faceid=" + mFaceID + ", nickname=" + mName);
                    lunchRoomListActivity(mName);
                    break;

/*                case ACTIVITY_ORGANIZATION:
                    mOrganization = data.getStringExtra("input");
                    Log.d(TAG, "onActivityResult, mOrganization=" + mOrganization);
                    startInputTitle();
                    break;
                case ACTIVITY_TITLE:
                    mTitle = data.getStringExtra("input");
                    Log.d(TAG, "onActivityResult, mTitle=" + mTitle);
                    boardcastIntent();
                    finish();
                    break;
*/
                case ACTIVITY_ROOM_LIST:
                    String room = data.getStringExtra("room");
                    lunchRoomInfoActivity(room);
                    break;
                case ACTIVITY_MEETING_INFO:
                    boardcastIntent();
                    break;
            }
        } else {
            Log.d(TAG, "unexception exit");
            boardcastIntent();
        }
    }

    public void boardcastIntent() {
        try {
            //TODO
            Intent intent = new Intent(INTENT_EXT_FINISH);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setPackage(getPackageName());
            }
            sendBroadcast(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Activity not found", e);
        }
        finish();
    }
}
