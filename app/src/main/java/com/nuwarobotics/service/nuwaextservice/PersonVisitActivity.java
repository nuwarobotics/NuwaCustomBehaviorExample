package com.nuwarobotics.service.nuwaextservice;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.nuwarobotics.service.nuwaextservice.sub.EmployeeListActivity;
import com.nuwarobotics.service.nuwaextservice.sub.FuncSelectActivity;

import static com.nuwarobotics.service.nuwaextservice.service.ExtBehaviorService.INTENT_EXT_FINISH;


public class PersonVisitActivity extends Activity {
    private final String TAG = "PersonVisitActivity";

    private final int ACTIVITY_FACE_RECOGNITION = 1;
    private final int ACTIVITY_ORGANIZATION = 2;
    private final int ACTIVITY_TITLE = 3;
    private final int ACTIVITY_VISITOR_VISIT = 4;
    private final int ACTIVITY_FUNC_SELECT = 5;

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
            lunchVisitorListActivity(mName);
        } else {
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

    private void lunchVisitorListActivity(String visitor) {
        Intent intent = new Intent(this, EmployeeListActivity.class);
        intent.putExtra("visitor", visitor);
        startActivityForResult(intent, ACTIVITY_VISITOR_VISIT);
    }

    private void lunchFuncSelectActivity(String staff) {
        Intent intent = new Intent(this, FuncSelectActivity.class);
        intent.putExtra("staff", staff);
        startActivityForResult(intent, ACTIVITY_FUNC_SELECT);
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
                    lunchVisitorListActivity(mName);
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
                case ACTIVITY_VISITOR_VISIT:
                    String staff = data.getStringExtra("staff");
                    boardcastIntent();
                    //TODO
                    //lunchFuncSelectActivity(staff);
                    break;
                case ACTIVITY_FUNC_SELECT:
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
