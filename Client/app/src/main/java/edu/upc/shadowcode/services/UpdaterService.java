package edu.upc.shadowcode.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import edu.upc.shadowcode.Client;
import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.models.UserModel;
import edu.upc.shadowcode.modules.MessageModule;
import edu.upc.shadowcode.views.MainActivity;

public class UpdaterService extends Service {

    // 扫描器定时重启计时器
    private Timer updaterTimer = null;

    // 该定时任务将重启扫描器
    class UpdaterTask extends TimerTask {
        public void run() {
            update();
            Log.d("Updater", "Updated.");
        }
    }

    private ConnectivityManager connectivityManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Updater", "Created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Updater", "Destroyed.");
    }

    private boolean isRunning = false;

    public void start() {
        if (isRunning) return;

        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager)MainActivity.get()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        isRunning = true;

        updaterTimer = new Timer();
        updaterTimer.schedule(new UpdaterTask(), 0, 3 * 1000);

        Log.d("Updater", "Started.");
    }

    public void stop() {
        if (!isRunning) return;

        // 取消定时重启任务
        if (updaterTimer != null) {
            updaterTimer.cancel();
            updaterTimer = null;
        }

        isRunning = false;

        Log.d("Updater", "Stopped.");
    }

    private void update(){
        NetworkInfo network = connectivityManager.getActiveNetworkInfo();
        if (network == null || !network.isAvailable()) {
            return;
        }
        UserModel.get().updateRisk(false);
    }



    public static class UpdaterBinder extends Binder {
        private final UpdaterService instance;

        public UpdaterBinder(UpdaterService service){
            instance = service;
        }
        public void start(){
            instance.start();
        }
        public void stop(){
            instance.stop();
        }
        public void update() {instance.update();}
        public boolean isRunning(){
            return instance.isRunning;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new UpdaterBinder(this);
    }
}
