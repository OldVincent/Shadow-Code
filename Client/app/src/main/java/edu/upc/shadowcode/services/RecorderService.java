package edu.upc.shadowcode.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.databinding.ObservableArrayList;

import java.util.Timer;
import java.util.TimerTask;

import edu.upc.shadowcode.models.DeviceModel;
import edu.upc.shadowcode.models.Exposure;
import edu.upc.shadowcode.views.MainActivity;

public class RecorderService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Recorder", "Created.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("Recorder", "Destroyed.");
    }

    private boolean isRunning = false;

    private Timer recorderTimer = null;

    private final static int ExposureTimeout = 1000;
    private final static int RecordMaxTime = 1000;

    // 检查列表中是否存在超时接触
    public static void verifyExposures(ObservableArrayList<Exposure> exposures) {
        ObservableArrayList<Exposure> list = (ObservableArrayList<Exposure>) exposures.clone();
        for (Exposure exposure : list) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - exposure.endingTime.get().getTime() > ExposureTimeout) {
                MainActivity.get().runOnUiThread(() -> DeviceModel.get().onDeviceExpired(exposure));
            } else if (currentTime - exposure.beginningTime.get().getTime() > ExposureTimeout) {
                MainActivity.get().runOnUiThread(() -> DeviceModel.get().recordDevice(exposure));
            }

        }
    }

    public static void clearExposures(ObservableArrayList<Exposure> exposures) {
        ObservableArrayList<Exposure> list = (ObservableArrayList<Exposure>) exposures.clone();
        for (Exposure exposure : list) {
            MainActivity.get().runOnUiThread(() -> DeviceModel.get().onDeviceExpired(exposure));
        }
    }

    // 该定时任务将重启扫描器
    static class RecorderTask extends TimerTask {
        public void run() {
            verifyExposures(DeviceModel.get().currentFarExposures);
            verifyExposures(DeviceModel.get().currentMediumExposures);
            verifyExposures(DeviceModel.get().currentNearExposures);
        }
    }

    public void start() {
        if (isRunning) return;

        recorderTimer = new Timer();
        recorderTimer.schedule(new RecorderTask(), 0, 500);

        isRunning = true;

        Log.d("Recorder", "Started.");
    }

    public void stop() {
        if (!isRunning) return;

        if (recorderTimer != null) {
            recorderTimer.cancel();
            recorderTimer = null;
        }

        clearExposures(DeviceModel.get().currentFarExposures);
        clearExposures(DeviceModel.get().currentMediumExposures);
        clearExposures(DeviceModel.get().currentNearExposures);

        isRunning = false;

        Log.d("Recorder", "Stopped.");
    }

    public static class RecorderBinder extends Binder {
        private final RecorderService instance;

        public RecorderBinder(RecorderService service){
            instance = service;
        }

        public void start(){
            instance.start();
        }

        public void stop(){
            instance.stop();
        }

        public boolean isRunning(){
            return instance.isRunning;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new RecorderBinder(this);
    }
}
