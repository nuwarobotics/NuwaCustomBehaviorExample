package com.nuwarobotics.service.nuwaextservice.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class utils {
    private static final String TAG = "utils";

    public static String readJson(Context context, String filename) {
        AssetManager assetManager = context.getAssets();
        StringBuilder sb = new StringBuilder();

        Log.v(TAG, "readJson, " + filename);
        try {
            InputStream in = assetManager.open(filename + ".json");

            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line.trim());
            }
        } catch (IOException ioe) {
            Log.e(TAG, "readJson, IOException, ", ioe);
        } catch (Exception e) {
            Log.e(TAG, "readJson, Exception, ", e);
        }
        Log.v(TAG, "readJson, sb=" + sb.toString());
        return sb.toString();
    }
}