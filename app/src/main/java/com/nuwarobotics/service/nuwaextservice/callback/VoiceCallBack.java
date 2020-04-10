package com.nuwarobotics.service.nuwaextservice.callback;

public interface VoiceCallBack {
    void onSynthesisDone(boolean isSuccessful);

    void onListenDone(boolean isSuccessful, String result);
}