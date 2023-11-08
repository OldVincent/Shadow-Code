package edu.upc.shadowcode.services;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.Observable;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.models.DeviceModel;
import edu.upc.shadowcode.models.UserModel;
import edu.upc.shadowcode.modules.MessageModule;
import edu.upc.shadowcode.modules.PermissionModule;
import edu.upc.shadowcode.views.MainActivity;
import edu.upc.shadowcode.views.RequestFragment;

public class AdvertiserService extends Service {

    private BluetoothLeAdvertiser advertiser = null;
    private AdvertiseData advertiseData = null;

    private final AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0)
            .build();
    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d("Advertiser", "Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d("Advertiser", "Failed to start.");
            DeviceModel.get().notifyFailure();
        }
    };

    private static RequestFragment.RequestHandler goToSettings = new RequestFragment.RequestHandler() {
        @Override
        public void confirm() {
            MainActivity.get().runOnUiThread(()-> {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + MainActivity.get().getPackageName()));
                MainActivity.get().startActivity(intent);
            });
        }

        @Override
        public void cancel() {

        }
    };

    private boolean isRunning = false;

    private final Observable.OnPropertyChangedCallback userWatcher = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            updateAdvertiseData();
            stop();
            start();
        }
    };

    private void updateAdvertiseData() {
        advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceData(DeviceModel.get().userServiceId,
                        UserModel.get().deviceIdBytes)
                .addServiceData(DeviceModel.get().riskServiceId,
                        new byte[]{(byte)UserModel.get().risk.get().ordinal()})
                .addServiceData(DeviceModel.get().typeServiceId,
                        new byte[]{(byte)UserModel.get().deviceType.get().ordinal()})
                .build();
    }

    public void start() {
        if (isRunning) return;

        // 检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ActivityCompat.checkSelfPermission(Controller.getContext(),
                    Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                RequestFragment.displayRequest(new RequestFragment.Request(
                        Controller.getContext().getString(R.string.request_permission_title),
                        Controller.getContext().getString(R.string.request_permission_bluetooth),
                        PermissionModule.goToSettings));
                DeviceModel.get().notifyFailure();
                return;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(Controller.getContext(),
                    Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                RequestFragment.displayRequest(new RequestFragment.Request(
                        Controller.getContext().getString(R.string.request_permission_title),
                        Controller.getContext().getString(R.string.request_permission_bluetooth),
                        PermissionModule.goToSettings));
                DeviceModel.get().notifyFailure();
                return;
            }
        }

        BluetoothManager manager = (BluetoothManager)
                Controller.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        advertiser = adapter.getBluetoothLeAdvertiser();
        if (!adapter.isEnabled() || advertiser == null) {

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(Controller.getContext(),
                            Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                RequestFragment.displayRequest(new RequestFragment.Request(
                        Controller.getContext().getString(R.string.request_permission_title),
                        Controller.getContext().getString(R.string.request_permission_bluetooth),
                        PermissionModule.goToSettings));

                DeviceModel.get().notifyFailure();
                return;
            }
            Controller.getContext().startActivity(intent);
            return;
        }

        advertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);

        isRunning = true;
    }

    public void stop() {
        if (!isRunning) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ActivityCompat.checkSelfPermission(Controller.getContext(),
                    Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                RequestFragment.displayRequest(new RequestFragment.Request(
                        Controller.getContext().getString(R.string.request_permission_title),
                        Controller.getContext().getString(R.string.request_permission_bluetooth),
                        PermissionModule.goToSettings));
                DeviceModel.get().notifyFailure();
                return;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(Controller.getContext(),
                    Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                RequestFragment.displayRequest(new RequestFragment.Request(
                        Controller.getContext().getString(R.string.request_permission_title),
                        Controller.getContext().getString(R.string.request_permission_bluetooth),
                        PermissionModule.goToSettings));
                DeviceModel.get().notifyFailure();
                return;
            }
        }
        advertiser.stopAdvertising(advertiseCallback);

        isRunning = false;

        Log.d("Advertiser", "Stopped.");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        UserModel.get().risk.addOnPropertyChangedCallback(userWatcher);
        UserModel.get().deviceType.addOnPropertyChangedCallback(userWatcher);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            Controller.getModule(PermissionModule.class).ensurePermission(
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_ADVERTISE
            }, getString(R.string.request_permission_bluetooth));
        }
        else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                Controller.getModule(PermissionModule.class).ensurePermission(
                        new String[]{
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, getString(R.string.request_permission_location));
            } else {
                Controller.getModule(PermissionModule.class).ensurePermission(
                        new String[]{
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, getString(R.string.request_permission_location));
            }
        }

        updateAdvertiseData();

        Log.d("Advertiser", "Created.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UserModel.get().risk.removeOnPropertyChangedCallback(userWatcher);
        UserModel.get().deviceType.removeOnPropertyChangedCallback(userWatcher);
        Log.d("Advertiser", "Destroyed.");
    }

    public static class AdvertiserBinder extends Binder {
        private final AdvertiserService instance;

        public AdvertiserBinder(AdvertiserService service){
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
        return new AdvertiserBinder(this);
    }
}
