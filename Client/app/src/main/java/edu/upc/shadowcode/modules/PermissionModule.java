package edu.upc.shadowcode.modules;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import java.util.Hashtable;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.Module;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.views.MainActivity;
import edu.upc.shadowcode.views.RequestFragment;
import kotlinx.coroutines.MainCoroutineDispatcher;

public class PermissionModule implements Module {
    @Override
    public void install() {

    }

    @Override
    public void uninstall() {

    }

    public void ensureBackgroundPermission() {
        PowerManager powerManager = (PowerManager) Controller.getContext().getSystemService(Context.POWER_SERVICE);
        if(powerManager != null) {
            if (powerManager.isIgnoringBatteryOptimizations(Controller.getContext().getPackageName()))
                return;
        }
        try{
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:"+ Controller.getContext().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Controller.getContext().startActivity(intent);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static RequestFragment.RequestHandler goToSettings = new RequestFragment.RequestHandler() {
        @Override
        public void confirm() {
            MainActivity.get().runOnUiThread(()-> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + MainActivity.get().getPackageName()));
                MainActivity.get().startActivity(intent);
            });
        }

        @Override
        public void cancel() {

        }
    };

    public static RequestFragment.RequestHandler goToLocationSettings = new RequestFragment.RequestHandler() {
        @Override
        public void confirm() {
            MainActivity.get().runOnUiThread(()-> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MainActivity.get().startActivity(intent);
            });
        }

        @Override
        public void cancel() {

        }
    };

    public void ensurePermission(String[] permissions, String description){
        boolean requestPermissions = false;
        boolean describePermissions = false;

        for (String permission: permissions) {
            if (ActivityCompat.checkSelfPermission(Controller.get(), permission)
                    != PackageManager.PERMISSION_GRANTED){
                requestPermissions = true;
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.get(), permission))
                    describePermissions = true;
            }
        }

        if (describePermissions) {
            RequestFragment.displayRequest(new RequestFragment.Request(
                    Controller.getContext().getString(R.string.request_permission_title),
                    description,
                    goToSettings));
        }

        if (requestPermissions) {
            ActivityCompat.requestPermissions(MainActivity.get(), permissions, 100);
        }
    }
}
