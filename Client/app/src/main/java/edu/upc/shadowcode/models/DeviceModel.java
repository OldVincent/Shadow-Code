package edu.upc.shadowcode.models;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.util.Log;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableArrayMap;
import androidx.databinding.ObservableField;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.modules.PermissionModule;
import edu.upc.shadowcode.services.AdvertiserService;
import edu.upc.shadowcode.services.RecorderService;
import edu.upc.shadowcode.services.ScannerService;


public class DeviceModel {

    private static DeviceModel singletonInstance;

    @NotNull
    public static DeviceModel get() {
        if (singletonInstance == null)
            singletonInstance = new DeviceModel();
        return singletonInstance;
    }

    // 避免从Model外部直接修改该变量的值
    public ObservableField<Boolean> isEnabled = new ObservableField<>(false);

    public AdvertiserService.AdvertiserBinder advertiser;
    public ScannerService.ScannerBinder scanner;
    public RecorderService.RecorderBinder recorder;

    private boolean advertiserRunning = false;
    private boolean scannerRunning = false;
    private boolean recorderRunning = false;

    private void startAdvertiser() {
        // 启动广播器
        if (advertiser == null) {
            Intent intent = new Intent(Controller.getContext(), AdvertiserService.class);
            Controller.getContext().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    advertiser = (AdvertiserService.AdvertiserBinder) binder;
                    if (advertiserRunning)
                        advertiser.start();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    advertiser = null;
                }
            }, BIND_AUTO_CREATE);
        } else advertiser.start();
        advertiserRunning = true;
    }

    private void stopAdvertiser() {
        // 停止广播器
        if (advertiser != null)
            advertiser.stop();
    }

    private void startScanner() {
        if (scanner == null) {
            Intent intent = new Intent(Controller.getContext(), ScannerService.class);
            Controller.getContext().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    scanner = (ScannerService.ScannerBinder) binder;
                    if (scannerRunning)
                        scanner.start();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    scanner = null;
                }
            }, BIND_AUTO_CREATE);
        } else scanner.start();
        scannerRunning = true;
    }

    private void stopScanner() {
        if (scanner != null)
            scanner.stop();
    }

    private void startRecorder() {
        if (recorder == null) {
            Intent intent = new Intent(Controller.getContext(), RecorderService.class);
            Controller.getContext().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    recorder = (RecorderService.RecorderBinder) binder;
                    if (recorderRunning)
                        recorder.start();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    recorder = null;
                }
            }, BIND_AUTO_CREATE);
        } else recorder.start();
        recorderRunning = true;
    }

    private void stopRecorder() {
        if (recorder != null)
            recorder.stop();
    }

    private PowerManager.WakeLock wakeLock;

    public void start() {
        if (wakeLock == null){
            Controller.getModule(PermissionModule.class).ensureBackgroundPermission();

            PowerManager powerManager =
                    (PowerManager) Controller.getContext().getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "shadow_code:advertiser_lock");
        }
        wakeLock.acquire();

        isEnabled.set(true);

        startAdvertiser();
        startScanner();
        startRecorder();
    }

    public void stop() {
        stopAdvertiser();
        stopScanner();
        stopRecorder();

        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }

        isEnabled.set(false);
    }

    public void update() {

    }

    // 通知扫描器模型，某个或某些服务启动失败
    public void notifyFailure(){
        stop();
        isEnabled.set(false);
    }

    // 本地的尚未判断风险状态的接触记录
    public ObservableArrayList<Exposure> cachedUnknownExposures = new ObservableArrayList<>();
    // 本地的中高风险的接触记录
    public ObservableArrayList<Exposure> cachedRiskExposures = new ObservableArrayList<>();
    // 本地的低风险的接触记录
    public ObservableArrayList<Exposure> cachedSafeExposures = new ObservableArrayList<>();

    public ObservableArrayList<Exposure> currentNearExposures = new ObservableArrayList<>();

    public ObservableArrayList<Exposure> currentMediumExposures = new ObservableArrayList<>();

    public ObservableArrayList<Exposure> currentFarExposures = new ObservableArrayList<>();

    public ObservableArrayMap<UUID, Exposure> currentExposures = new ObservableArrayMap<>();

    public final ParcelUuid userServiceId =
            ParcelUuid.fromString("0000cb2b-0000-1000-8000-00805f9b34fb");
    public final ParcelUuid riskServiceId =
            ParcelUuid.fromString("0000cb2c-0000-1000-8000-00805f9b34fb");
    public final ParcelUuid typeServiceId =
            ParcelUuid.fromString("0000cb2d-0000-1000-8000-00805f9b34fb");

    public DeviceModel() {
    }

    private ObservableArrayList<Exposure> getCachedExposures(RiskType risk) {
        switch (risk) {
            case Low:
                return cachedSafeExposures;
            case Medium:
            case High:
            case Danger:
                return cachedRiskExposures;
            case Unknown:
                return cachedUnknownExposures;
        }
        return null;
    }

    private ObservableArrayList<Exposure> getCurrentExposures(DistanceType distance) {
        switch (distance) {
            case Near:
                return currentNearExposures;
            case Medium:
                return currentMediumExposures;
            case Far:
                return currentFarExposures;
        }
        return null;
    }

    private final Random random = new Random();

    // 添加一个暴露数据到对应的距离列表中
    private void addDevice(Exposure exposure) {
        ObservableArrayList<Exposure> list = getCurrentExposures(exposure.distance.get());
        list.add(random.nextInt(list.size() + 1), exposure);
        currentExposures.put(exposure.id, exposure);
    }

    // 设备过期，即一段时间内未能扫描到该设备
    public void onDeviceExpired(Exposure exposure) {
        // 从当前接触设备中移除
        getCurrentExposures(exposure.distance.get()).remove(exposure);
        // 添加到缓存列表中
        recordDevice(exposure);
        // 从设备查询表中移除，表明已脱离接触
        currentExposures.remove(exposure.id);
    }

    public void recordDevice(Exposure exposure) {
        if (!getCachedExposures(exposure.risk.get()).contains(exposure)) {
            getCachedExposures(exposure.risk.get()).add(exposure);
        }
    }

    // 扫描到设备
    public void onDeviceScanned(UUID id, DeviceType device, RiskType risk, int strength) {
        Log.d("Scanner", "Scanned: " + id.toString());

        Exposure exposure = currentExposures.get(id);

        // 目标设备已经处于暴露状态
        if (exposure != null) {
            // 更新最近接触时间
            exposure.endingTime.set(new Date(System.currentTimeMillis()));

            boolean riskChanging = false;

            // 更新风险状态
            if (exposure.risk.get() != risk) {
                exposure.risk.set(risk);
                riskChanging = true;
            }

            // 迁移距离分组
            DistanceType previousDistance = exposure.distance.get();
            exposure.updateSignalStrength(strength);
            if (exposure.distance.get() != previousDistance){
                getCurrentExposures(previousDistance).remove(exposure);
                addDevice(exposure);
            }
            RiskType exposureRisk = exposure.risk.get();
            DistanceType exposureDistance = exposure.distance.get();

            if (riskChanging && (risk == RiskType.Danger || risk == RiskType.High || risk == RiskType.Medium)){
                WarnerModel.get().warn(risk, exposureDistance);
            }

            // 更新本机风险
            switch (exposureRisk){
                case Unknown:
                case Low:
                    break;
                case Medium:
                    if (exposureDistance == DistanceType.Near) {
                        UserModel.get().increaseRisk(RiskType.Medium);
                    }
                    break;
                case High:
                case Danger:
                    if (exposureDistance == DistanceType.Near)
                        UserModel.get().increaseRisk(RiskType.High);
                    else if (exposureDistance == DistanceType.Medium)
                        UserModel.get().increaseRisk(RiskType.Medium);
                    break;
            }
            return;
        }

        // 尚未解除目标设备
        exposure = new Exposure();
        exposure.updateSignalStrength(strength);
        exposure.id = id;
        exposure.device = device;
        exposure.beginningTime.set(new Date(System.currentTimeMillis()));
        exposure.endingTime.set(new Date(System.currentTimeMillis()));

        addDevice(exposure);

        RiskType exposureRisk = exposure.risk.get();
        DistanceType exposureDistance = exposure.distance.get();
        switch (exposureRisk){
            case Unknown:
            case Low:
                break;
            case Medium:
                WarnerModel.get().warn(exposureRisk, exposureDistance);
                break;
            case High:
            case Danger:
                WarnerModel.get().warn(exposureRisk, exposureDistance);
                break;
        }

        // 更新本机风险
        switch (exposureRisk){
            case Unknown:
            case Low:
                break;
            case Medium:
                if (exposureDistance == DistanceType.Near) {
                    UserModel.get().increaseRisk(RiskType.Medium);
                }
                break;
            case High:
            case Danger:
                if (exposureDistance == DistanceType.Near)
                    UserModel.get().increaseRisk(RiskType.High);
                else if (exposureDistance == DistanceType.Medium)
                    UserModel.get().increaseRisk(RiskType.Medium);
                break;
        }
    }
}
