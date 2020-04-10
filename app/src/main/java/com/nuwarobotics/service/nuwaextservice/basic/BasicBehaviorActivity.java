package com.nuwarobotics.service.nuwaextservice.basic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nuwarobotics.service.IClientId;
import com.nuwarobotics.service.agent.NuwaRobotAPI;
import com.nuwarobotics.service.agent.RobotEventListener;
import com.nuwarobotics.service.agent.SimpleGrammarData;
import com.nuwarobotics.service.agent.VoiceEventListener;
import com.nuwarobotics.service.agent.VoiceResultJsonParser;
import com.nuwarobotics.service.nuwaextservice.R;
import com.nuwarobotics.service.nuwaextservice.callback.VoiceCallBack;
import com.nuwarobotics.service.nuwaextservice.contact.ContactHelper;
import com.nuwarobotics.service.nuwaextservice.utils.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Iterator;

public class BasicBehaviorActivity extends Activity {

    public static final String TAG = "BasicBehaviorActivity";

    public static final int msgBehaviorCompleted = 0;
    public static final int msgBehaviorQueue = 1;
    public static final int msgTTSPlay = 2;
    public static final int msgMotionPlay = 3;
    public static final int msgImageShow = 4;
    public static final int msgImageUpdated = 5;
    public static final int msgMenuShow = 6;
    public static final int msgLocalCMDListen = 7;
    public static final int msgASRListen = 8;
    public static final int msgFacecheck = 9;

    private static final int BEHAVIOR_NONE = 0;
    private static final int BEHAVIOR_TTS = 1 << 0; //1
    private static final int BEHAVIOR_MOTION = 1 << 1; //2
    private static final int BEHAVIOR_IMAGE = 1 << 2; //4
    private static final int BEHAVIOR_LOCALCMD = 1 << 3; //8
    private static final int BEHAVIOR_ASR = 1 << 4; //16
    private static final int BEHAVIOR_MENU = 1 << 5; //32
    private static final int BEHAVIOR_FACECHECK = 1 << 6; //64

    public static final String INTENT_FACE_REC = "com.nuwarobotics.service.nuwaextservice.ExtBehaviorService.FACE_REC";

    protected final static int BEHAVIOR_DELAY = 500; //ms
    private NuwaRobotAPI mRobotAPI;
    private IClientId mClientId;
    private Context mContext;

    boolean mSDKinit = false;

    protected Handler mHandler;
    private HandlerThread mThread;
    private JSONArray mBehaviorQueue;
    private VoiceCallBack mVoiceCallback;

    private int mBehavior = BEHAVIOR_NONE;
    private boolean isLocalCMD = false;
    private boolean haveFace = false;
    protected String mNickname = "";
    private String mASRResult = "";
    protected String mParam1 = "";
    protected String mParam2 = "";
    protected String mParam3 = "";

    private WindowManager windowManager;
    private LayoutInflater layoutInflater;
    private WindowManager.LayoutParams wmLayoutParam;
    private View mFloatyView;
    private Bitmap mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate.");
        mContext = this;

        initHandle();
        KiwiSDKInit();

        intentRegister();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");

        mRobotAPI.stopListen();
        unregisterReceiver(mBroadcastReceiver);
        KiwiSDKDestory();
    }

    private void intentRegister() {
        IntentFilter intentfilter = new IntentFilter();

        intentfilter.addAction(INTENT_FACE_REC);

        registerReceiver(mBroadcastReceiver, intentfilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equalsIgnoreCase(INTENT_FACE_REC)) {
                String nickname = intent.getStringExtra("nickname");
                long faceid = intent.getLongExtra("faceid", 0);
                Log.d(TAG, "onActivityResult, faceid=" + faceid + ", name=" + nickname);
                ContentValues contactdata = new ContentValues();
                contactdata.put("faceid", faceid);
                contactdata.put("nickname", nickname);
                SQLiteDatabase db = new ContactHelper(mContext).getWritableDatabase();
                db.insert(ContactHelper.TABLE_NAME, null, contactdata);
                db.close();
                haveFace = true;
                mNickname = nickname;
                BehaviorDone(BEHAVIOR_FACECHECK);
            }
        }
    };

    RobotEventListener robotEventListener = new RobotEventListener() {
        @Override
        public void onWikiServiceStart() {
            // Nuwa Robot SDK is ready now, you call call Nuwa SDK API now.
            Log.d(TAG, "onWikiServiceStart, robot ready to be control");
            //Step 3 : Start Control Robot after Service ready.
            //Register Voice Callback event
            mRobotAPI.registerVoiceEventListener(voiceEventListener);//listen callback of robot voice related event
            //Allow user start demo after service ready
            mSDKinit = true;
        }

        @Override
        public void onWikiServiceStop() {

        }

        @Override
        public void onWikiServiceCrash() {

        }

        @Override
        public void onWikiServiceRecovery() {

        }

        @Override
        public void onStartOfMotionPlay(String s) {

        }

        @Override
        public void onPauseOfMotionPlay(String s) {

        }

        @Override
        public void onStopOfMotionPlay(String s) {

        }

        @Override
        public void onCompleteOfMotionPlay(String s) {
            Log.d(TAG, "onCompleteOfMotionPlay, " + s);
            BehaviorDone(BEHAVIOR_MOTION);
        }

        @Override
        public void onPlayBackOfMotionPlay(String s) {

        }

        @Override
        public void onErrorOfMotionPlay(int i) {

        }

        @Override
        public void onPrepareMotion(boolean b, String s, float v) {

        }

        @Override
        public void onCameraOfMotionPlay(String s) {

        }

        @Override
        public void onGetCameraPose(float v, float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8, float v9, float v10, float v11) {

        }

        @Override
        public void onTouchEvent(int i, int i1) {

        }

        @Override
        public void onPIREvent(int i) {

        }

        @Override
        public void onTap(int i) {

        }

        @Override
        public void onLongPress(int i) {

        }

        @Override
        public void onWindowSurfaceReady() {

        }

        @Override
        public void onWindowSurfaceDestroy() {

        }

        @Override
        public void onTouchEyes(int i, int i1) {

        }

        @Override
        public void onRawTouch(int i, int i1, int i2) {

        }

        @Override
        public void onFaceSpeaker(float v) {

        }

        @Override
        public void onActionEvent(int i, int i1) {

        }

        @Override
        public void onDropSensorEvent(int i) {

        }

        @Override
        public void onMotorErrorEvent(int i, int i1) {

        }
    };

    VoiceEventListener voiceEventListener = new VoiceEventListener() {
        @Override
        public void onWakeup(boolean isError, String score, float direction) {

        }

        @Override
        public void onTTSComplete(boolean isError) {
            Log.d(TAG, "onTTSComplete:" + !isError);
            controlFace(false);
            BehaviorDone(BEHAVIOR_TTS);
            if (mVoiceCallback != null)
                mVoiceCallback.onSynthesisDone(!isError);
        }

        @Override
        public void onSpeechRecognizeComplete(boolean isError, ResultType iFlyResult, String json) {

        }

        @Override
        public void onSpeech2TextComplete(boolean isError, String json) {
            Log.d(TAG, "onSpeech2TextComplete:" + !isError + ", json:" + json);
            mASRResult = VoiceResultJsonParser.parseVoiceResult(json);

            try {
                JSONObject responseList = (JSONObject) mBehaviorQueue.get(0);
                if (!isError && (json != null && !json.isEmpty())) {
                    mASRResult = VoiceResultJsonParser.parseVoiceResult(json);
                    if (responseList.has("__ASR__")) {
                        mBehaviorQueue.put(0, responseList.getJSONObject("__ASR__"));
                    }
                } else {
                    if (responseList.has("__ERROR__")) {
                        mBehaviorQueue.put(0, responseList.getJSONObject("__ERROR__"));
                    }
                }
            } catch (JSONException e) {
            }

            BehaviorDone(BEHAVIOR_ASR);

            if (mVoiceCallback != null)
                mVoiceCallback.onListenDone(!isError, mASRResult);
        }

        @Override
        public void onMixUnderstandComplete(boolean isError, ResultType resultType, String json) {
            Log.d(TAG, "onMixUnderstandComplete isError:" + !isError + ", resultType=" + resultType + ", json:" + json);
            if (!isError && (json != null && !json.isEmpty())) {
                mASRResult = VoiceResultJsonParser.parseVoiceResult(json);
            }
/*
            try {
                JSONObject responseList = (JSONObject) mBehaviorQueue.get(0);
                if (!isError && (json != null && !json.isEmpty())) {
                    String result_string = VoiceResultJsonParser.parseVoiceResult(json);
                    if (responseList.has(result_string)) {
                        mBehaviorQueue.put(0, responseList.getJSONObject(result_string));
                    }
                } else {
                    if (responseList.has("__ERROR__")) {
                        mBehaviorQueue.put(0, responseList.getJSONObject("__ERROR__"));
                    }
                }
            } catch (JSONException e) {
            }

 */
            isLocalCMD = false;
            BehaviorDone(BEHAVIOR_LOCALCMD);

            if (mVoiceCallback != null)
                mVoiceCallback.onListenDone(!isError, mASRResult);
        }

        @Override
        public void onSpeechState(ListenType listenType, SpeechState speechState) {

        }

        @Override
        public void onSpeakState(SpeakType speakType, SpeakState speakState) {
            Log.d(TAG, "onSpeakState:" + speakType + ", state:" + speakState);
        }

        @Override
        public void onGrammarState(boolean isError, String s) {
            if (!isError) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //if(!isLocalCMD) {
                        {
                            mRobotAPI.startLocalCommand();//Start listen without wakeup, callback on onMixUnderstandComplete
                            isLocalCMD = true;
                        }
                    }
                });
            }
        }

        @Override
        public void onListenVolumeChanged(ListenType listenType, int i) {

        }

        @Override
        public void onHotwordChange(HotwordState hotwordState, HotwordType hotwordType, String s) {

        }
    };

    private void initHandle() {
        //initial
        mThread = new HandlerThread("BehaviorThread");
        mThread.start();

        mHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                    case msgBehaviorCompleted:
                        initParam();
                        break;
                    case msgBehaviorQueue:
                        if (mBehaviorQueue.length() > 0) {
                            try {
                                String param = mBehaviorQueue.get(0).toString();
                                Log.d(TAG, "param : " + param);
                                mBehaviorQueue.remove(0);
                                BehaviorDo(param);
                            } catch (JSONException e) {

                            }
                        }
                        break;
                    case msgTTSPlay:
                        String tts = msg.getData().getString("speak");
                        startSpeak(tts);
                        break;
                    case msgMotionPlay:
                        String motion = msg.getData().getString("motion");
                        playMotion(motion);
                        break;
                    case msgImageShow:
                        String imageurl = msg.getData().getString("image");
                        int delaytime = msg.getData().getInt("time", 7);
                        if (msg.getData().getBoolean("clean", false))
                            showImage(false, imageurl, delaytime);
                        else
                            showImage(true, imageurl, delaytime);
                        break;
                    case msgImageUpdated:
                        ImageView iv = mFloatyView.findViewById(R.id.CustomImage);
                        iv.setImageBitmap(mImage);
                        break;
                    case msgMenuShow:
                        if (msg.getData().getBoolean("clean", false))
                            showMenu(false);
                        else
                            showMenu(true);
                        break;
                    case msgLocalCMDListen:
                        String[] cmdList = msg.getData().getStringArray("grammar");
                        SimpleGrammarData grammardata = new SimpleGrammarData("ExtBahavior");
                        for (String string : cmdList) {
                            grammardata.addSlot(string);
                            Log.d(TAG, "add string : " + string);
                        }
                        //generate grammar data
                        grammardata.updateBody();
                        startLocalCMD(grammardata);
                        break;
                    case msgASRListen:
                        startASR();
                        break;
                    case msgFacecheck:
                        mBehavior |= BEHAVIOR_FACECHECK;
                        BehaviorDone(BEHAVIOR_FACECHECK);
                        break;
                }
            }
        };
    }

    private void initParam() {
        mBehavior = BEHAVIOR_NONE;
        haveFace = false;
        mNickname = "";
        mASRResult = "";
    }

    protected boolean BehaviorDo(String action) {
        boolean ret = true;
        try {
            /*
            //Case 1: general case
                {
                  "ToSpeak": "Hello",
                  "ToMotion": "666_TA_DictateR",
                  "ToImage": {
                    "url": "https://www.aeon-laketown.jp/img/shopguide/floor/img-floor_kaze_1.gif",
                    "time": 10000 //ms
                  },
                  "ToMenu": true
                }
             //Case 2: combo case
                {
                  "ToBehavior": [
                    {
                      "ToSpeak": "詢問哪間會議室?"
                    },
                    {
                      "ToLocalCmd": [
                        "大會議室",
                        "小會議室"
                      ]
                    },
                    {
                      "大會議室": {
                        "ToSpeak": "大會議室走這邊",
                        "ToMotion": "666_TA_DictateR",
                        "ToImage": {
                          "url": "https://www.aeon-laketown.jp/img/shopguide/floor/img-floor_kaze_1.gif",
                          "time": 5000
                        }
                      },
                      "小會議室": {
                        "ToSpeak": "小會議室走這邊",
                        "ToMotion": "666_TA_DictateR",
                        "ToImage": {
                          "url": "https://www.aeon-laketown.jp/img/shopguide/floor/img-floor_kaze_1.gif",
                          "time": 10000
                        }
                      }
                    }
                  ]
                }
             */
            JSONObject intention = new JSONObject(action);

            //ToBehavior
            if (intention.has("ToBehavior")) {
                mBehaviorQueue = intention.getJSONArray("ToBehavior");
                mHandler.sendEmptyMessageDelayed(msgBehaviorQueue, BEHAVIOR_DELAY);
            } else {
                Iterator<String> keysItr = intention.keys();
                while (keysItr.hasNext()) {
                    String key = keysItr.next();
                    Object value = intention.get(key);
                    Log.d(TAG, "BehaviorDo, key=" + key + ", value=" + value);

                    if (key.equals("FaceCheck")) {
                        mHandler.sendEmptyMessage(msgFacecheck);
                    } else if (key.equals("ToSpeak")) {
                        Message msg = new Message();
                        msg.what = msgTTSPlay;
                        Bundle bundle = new Bundle();
                        bundle.putString("speak", (String) value);
                        msg.setData(bundle);
                        mHandler.sendMessageDelayed(msg, BEHAVIOR_DELAY);
                    } else if (key.equals("ToMotion")) {
                        Message msg = new Message();
                        msg.what = msgMotionPlay;
                        Bundle bundle = new Bundle();
                        bundle.putString("motion", (String) value);
                        msg.setData(bundle);
                        mHandler.sendMessageDelayed(msg, BEHAVIOR_DELAY);
                    } else if (key.equals("ToImage")) {
                        JSONObject imgobj = (JSONObject) value;
                        String url = imgobj.getString("url");
                        int delay = imgobj.getInt("time");
                        Message msg = new Message();
                        msg.what = msgImageShow;
                        Bundle bundle = new Bundle();
                        bundle.putString("image", url);
                        bundle.putInt("time", delay);
                        msg.setData(bundle);
                        mHandler.sendMessageDelayed(msg, BEHAVIOR_DELAY);
                    } else if (key.equals("ToMenu")) {
                        mHandler.sendEmptyMessageDelayed(msgMenuShow, BEHAVIOR_DELAY);
                    } else if (key.equals("ToLocalCmd")) {
                        JSONArray jsonArray = (JSONArray) value;
                        String[] grammar = new String[jsonArray.length()];
                        for (int i = 0; i < jsonArray.length(); i++)
                            grammar[i] = jsonArray.getString(i);
                        Message msg = new Message();
                        msg.what = msgLocalCMDListen;
                        Bundle bundle = new Bundle();
                        bundle.putStringArray("grammar", grammar);
                        msg.setData(bundle);
                        mHandler.sendMessageDelayed(msg, BEHAVIOR_DELAY);
                    } else if (key.equals("ToRecognized")) {
                        mHandler.sendEmptyMessageDelayed(msgASRListen, BEHAVIOR_DELAY);
                    }
                }
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
            if (mBehaviorQueue != null && mBehaviorQueue.length() > 0) {
                mHandler.sendEmptyMessageDelayed(msgBehaviorQueue, BEHAVIOR_DELAY);
            } else {
                Log.d(TAG, "checkBehaviorDone, All behavior done and send completed");
                mHandler.sendEmptyMessage(msgBehaviorCompleted);
            }
        }

        return 0;
    }

    protected void setVoiceCallback(VoiceCallBack voicecb) {
        mVoiceCallback = voicecb;
    }

    //Common
    private void KiwiSDKInit() {
        //Step 1 : Initial Nuwa API Object
        mClientId = new IClientId(this.getClass().getCanonicalName());
        mRobotAPI = new NuwaRobotAPI(this, mClientId);

        //Step 2 : Register receive Robot Event
        Log.d(TAG, "register EventListener ");
        mRobotAPI.registerRobotEventListener(robotEventListener);//listen callback of robot service event
    }

    private void KiwiSDKDestory() {
        if (mRobotAPI != null) {
            mRobotAPI.release();
            mRobotAPI = null;
        }
    }

    private String loadJson(String filename) {
        return utils.readJson(this, filename);
    }

    private boolean controlFace(boolean mouseon) {
/*
        if (mouseon)
            mRobotAPI.mouthOn(100);
        else
            mRobotAPI.mouthOff();
*/
        return true;
    }

    private boolean startLocalCMD(SimpleGrammarData grammardata) {
        if (!mSDKinit) {
            Log.d(TAG, "need to do SDK init first !!!");
            return false;
        }

        mBehavior |= BEHAVIOR_LOCALCMD;
        mRobotAPI.stopListen();
        mRobotAPI.createGrammar(grammardata.grammar, grammardata.body);
        return true;
    }

    private boolean startASR() {
        if (!mSDKinit) {
            Log.d(TAG, "need to do SDK init first !!!");
            return false;
        }

        mBehavior |= BEHAVIOR_ASR;
        mRobotAPI.stopListen();
        mRobotAPI.startSpeech2Text(false);
        return true;
    }

    private boolean startSpeak(String tts) {
        if (!mSDKinit) {
            Log.d(TAG, "need to do SDK init first !!!");
            return false;
        }

        controlFace(true);
        mBehavior |= BEHAVIOR_TTS;
        mRobotAPI.stopTTS();
        if (tts.contains("__ASR__")) {
            tts = tts.replace("__ASR__", mASRResult);
        }
        mRobotAPI.startTTS(String.format(tts, mNickname, mParam1, mParam2, mParam3));
        return true;
    }

    private boolean playMotion(String motion) {
        if (!mSDKinit) {
            Log.d(TAG, "need to do SDK init first !!!");
            return false;
        }

        mBehavior |= BEHAVIOR_MOTION;
        mRobotAPI.motionPlay(motion, false);
        return true;
    }

    private boolean showImage(boolean show, String imageurl, int delaytime) {
        // if you want to let this app get the back key event to leave your app, you should NOT add FLAG_NOT_FOCUSABLE
        if (windowManager == null)
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (layoutInflater == null)
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (show) {
            wmLayoutParam = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    0,
                    PixelFormat.TRANSLUCENT);
            wmLayoutParam.gravity = Gravity.CENTER;
            wmLayoutParam.x = 0;
            wmLayoutParam.y = 0;
            // if you want to let the other app get the touch event, you should add FLAG_NOT_TOUCH_MODAL
            wmLayoutParam.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            wmLayoutParam.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            wmLayoutParam.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            //wmLayoutParam.alpha = 0.5f;

            if (mFloatyView != null)
                windowManager.removeViewImmediate(mFloatyView);

            mFloatyView = layoutInflater.inflate(R.layout.image_window_layout, new FrameLayout(this));
            windowManager.addView(mFloatyView, wmLayoutParam);

            mBehavior |= BEHAVIOR_IMAGE;
            //Download image
            new ImageDownloadTask(mFloatyView.findViewById(R.id.CustomImage), delaytime).execute(imageurl);
        } else {
            if (null != mFloatyView) {
                windowManager.removeView(mFloatyView);
                mFloatyView = null;
            }
        }

        return true;
    }

    public void BtnDialar(View view) {
        Message msg = new Message();
        msg.what = msgTTSPlay;
        Bundle bundle = new Bundle();
        bundle.putString("speak", getResources().getString(R.string.action_dialar));
        msg.setData(bundle);
        mHandler.sendMessageDelayed(msg, BEHAVIOR_DELAY);

        windowManager.removeViewImmediate(mFloatyView);
        mFloatyView = null;
        BehaviorDone(BEHAVIOR_MENU);
    }

    public void BtnMessage(View view) {
        Message msg = new Message();
        msg.what = msgTTSPlay;
        Bundle bundle = new Bundle();
        bundle.putString("speak", getResources().getString(R.string.action_message));
        msg.setData(bundle);
        mHandler.sendMessageDelayed(msg, BEHAVIOR_DELAY);

        windowManager.removeViewImmediate(mFloatyView);
        mFloatyView = null;
        BehaviorDone(BEHAVIOR_MENU);
    }

    public void BtnContact(View view) {
        Message msg = new Message();
        msg.what = msgTTSPlay;
        Bundle bundle = new Bundle();
        bundle.putString("speak", getResources().getString(R.string.action_contact));
        msg.setData(bundle);
        mHandler.sendMessageDelayed(msg, BEHAVIOR_DELAY);

        windowManager.removeViewImmediate(mFloatyView);
        mFloatyView = null;
        BehaviorDone(BEHAVIOR_MENU);
    }

    private boolean showMenu(boolean show) {
        // if you want to let this app get the back key event to leave your app, you should NOT add FLAG_NOT_FOCUSABLE
        if (windowManager == null)
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (layoutInflater == null)
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (show) {
            wmLayoutParam = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    0,
                    PixelFormat.TRANSLUCENT);
            wmLayoutParam.gravity = Gravity.CENTER_HORIZONTAL;
            wmLayoutParam.x = 0;
            wmLayoutParam.y = 175;
            // if you want to let the other app get the touch event, you should add FLAG_NOT_TOUCH_MODAL
            //wmLayoutParam.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            wmLayoutParam.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //wmLayoutParam.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            //wmLayoutParam.alpha = 0.5f;

            if (mFloatyView != null)
                windowManager.removeViewImmediate(mFloatyView);

            mFloatyView = layoutInflater.inflate(R.layout.menu_window_layout, new FrameLayout(this));
            windowManager.addView(mFloatyView, wmLayoutParam);

            mBehavior |= BEHAVIOR_MENU;
        } else {
            if (null != mFloatyView) {
                windowManager.removeView(mFloatyView);
                mFloatyView = null;
            }
        }

        return true;
    }

    private class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        int delaytime;

        public ImageDownloadTask(ImageView bmImage, int delaytime) {
            this.bmImage = bmImage;
            this.delaytime = delaytime;
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
            mImage = img;
            //bmImage.setImageBitmap(result);
            mHandler.sendEmptyMessageDelayed(msgImageUpdated, BEHAVIOR_DELAY);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showImage(false, "", 0);
                    BehaviorDone(BEHAVIOR_IMAGE);
                }
            }, delaytime);
        }
    }
}
