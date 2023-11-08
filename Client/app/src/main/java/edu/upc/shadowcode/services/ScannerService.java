package edu.upc.shadowcode.services;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.models.DeviceModel;
import edu.upc.shadowcode.models.DeviceType;
import edu.upc.shadowcode.models.DistanceType;
import edu.upc.shadowcode.models.RiskType;
import edu.upc.shadowcode.models.UserModel;
import edu.upc.shadowcode.modules.MessageModule;
import edu.upc.shadowcode.modules.PermissionModule;
import edu.upc.shadowcode.views.RequestFragment;

public class ScannerService extends Service {

    private BluetoothLeScanner scanner;
    private LinkedList<ScanFilter> scanFilters = null;
    private final ScanSettings scanSettings = new ScanSettings.Builder()
            //设置低功耗模式
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build();

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // 扫描的关闭具有滞后性，如果扫描器已经关闭，则不再进行后续处理
            if (!isRunning)
                return;

            // 过滤掉可连接广播
            if (result.isConnectable())
                return;


            ScanRecord record = result.getScanRecord();
            if (record == null)
                return;

            Log.d("Scanner", "RSSI: " + result.getRssi());

            UUID id = UserModel.BytesToId(record.getServiceData(DeviceModel.get().userServiceId));
            DeviceType device = DeviceType.values()[record.getServiceData(DeviceModel.get().typeServiceId)[0]];
            RiskType risk = RiskType.values()[record.getServiceData(DeviceModel.get().riskServiceId)[0]];
            if (!isRunning) return;
            DeviceModel.get().onDeviceScanned(id, device, risk, -result.getRssi());
        }
    };

    // 扫描器定时重启计时器
    private Timer rebootTimer = null;

    // 该定时任务将重启扫描器
    class RebootTask extends TimerTask {
        public void run() {
            stopCore();
            startCore();
            Log.d("Scanner", "Rebooted.");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Controller.getModule(PermissionModule.class).ensurePermission(
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_ADVERTISE
                    }, getString(R.string.request_permission_bluetooth));
        } else {
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

        Log.d("Scanner", "Created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Scanner", "Destroyed.");
    }

    private boolean isRunning = false;

    public void start() {
        if (isRunning) return;

        // 检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(Controller.getContext(),
                    Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                RequestFragment.displayRequest(new RequestFragment.Request(
                        Controller.getContext().getString(R.string.request_permission_title),
                        Controller.getContext().getString(R.string.request_permission_bluetooth),
                        PermissionModule.goToSettings));
                DeviceModel.get().notifyFailure();
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
        scanner = adapter.getBluetoothLeScanner();
        if (!adapter.isEnabled() || scanner == null) {
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
            DeviceModel.get().notifyFailure();
            return;
        }

        if (scanFilters == null) {
            scanFilters = new LinkedList<>();
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceData(DeviceModel.get().userServiceId, new byte[16], new byte[16])
                    .setServiceData(DeviceModel.get().typeServiceId, new byte[1], new byte[1])
                    .setServiceData(DeviceModel.get().riskServiceId, new byte[1], new byte[1])
                    .build();
            scanFilters.add(scanFilter);
        }

        if (!startCore())
            return;

        isRunning = true;

        rebootTimer = new Timer();
        rebootTimer.schedule(new RebootTask(), 10 * 1000, 10 * 1000);

        Log.d("Scanner", "Started.");
    }

    public void stop() {
        if (!isRunning) return;

        // 取消定时重启任务
        if (rebootTimer != null) {
            rebootTimer.cancel();
            rebootTimer = null;
        }

        stopCore();

        isRunning = false;

        Log.d("Scanner", "Stopped.");
    }

    private boolean startCore() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(Controller.getContext(),
                    Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                RequestFragment.displayRequest(new RequestFragment.Request(
                        Controller.getContext().getString(R.string.request_permission_title),
                        Controller.getContext().getString(R.string.request_permission_bluetooth),
                        PermissionModule.goToSettings));
                DeviceModel.get().notifyFailure();
                return false;
            }
        } else if (ActivityCompat.checkSelfPermission(Controller.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(Controller.getContext(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            RequestFragment.displayRequest(new RequestFragment.Request(
                    Controller.getContext().getString(R.string.request_permission_title),
                    Controller.getContext().getString(R.string.request_permission_location),
                    PermissionModule.goToSettings));
            DeviceModel.get().notifyFailure();
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            LocationManager location = (LocationManager) Controller.getContext().
                    getSystemService(Context.LOCATION_SERVICE);
            if (!location.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                RequestFragment.displayRequest(new RequestFragment.Request(
                        Controller.getContext().getString(R.string.request_permission_title),
                        Controller.getContext().getString(R.string.request_permission_location),
                        PermissionModule.goToLocationSettings));
                DeviceModel.get().notifyFailure();
                return false;
            }
        }

        scanner.startScan(scanFilters, scanSettings, scanCallback);

        return true;
    }

    private void stopCore() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        scanner.stopScan(scanCallback);
    }

    public static class ScannerBinder extends Binder {
        private final ScannerService instance;

        public ScannerBinder(ScannerService service){
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
        return new ScannerBinder(this);
    }
}
