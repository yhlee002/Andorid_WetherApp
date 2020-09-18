package com.project.mywetherapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.project.mywetherapp.R;
// 읍면동 이름을 검색조건으로 기준좌표(TM좌표) 정보 제공
public class TmLocationService extends Service {

    private static RequestQueue requestQueue;

    private static String getTMStdrCrdnt = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getTMStdrCrdnt";
    private static String ServiceKey;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel("03");
            if (channel == null) {
                channel = new NotificationChannel("03", getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }
            Notification noti = new NotificationCompat.Builder(this, "03").build();
            startForeground(1, noti);
            manager.cancelAll();
        }

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        ServiceKey = getString(R.string.air_api_key);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final ResultReceiver receiver = intent.getParcelableExtra("resultReceiver");
        
        String umdName = intent.getStringExtra("umdName");
        String url = createUri(umdName);
        makeRequest(url, receiver);

        return super.onStartCommand(intent, flags, startId);
    }

    private String createUri(String umdName) {
        StringBuilder builder = new StringBuilder();
        builder.append(getTMStdrCrdnt+"?")
                .append("ServiceKey="+ServiceKey)
                .append("umdName="+umdName);

        Log.i("[T Service - createUri]", "생성된 uri : "+builder.toString());
        return builder.toString();
    }

    private void makeRequest(String url, ResultReceiver resultReceiver) {
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Bundle bundle = dataAdapter(response);
                resultReceiver.send(6, bundle);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(500000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        requestQueue.add(request);
        stopSelf();
    }

    private Bundle dataAdapter(String response) {
        Gson gson = new Gson();
        JsonObject result = gson.fromJson(response, JsonObject.class);

        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}