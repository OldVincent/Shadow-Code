package edu.upc.shadowcode.models;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.databinding.ObservableField;

import org.jetbrains.annotations.NotNull;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.modules.MessageModule;
import edu.upc.shadowcode.services.UpdaterService;

public class WarnerModel {
    private static final WarnerModel singletonInstance = new WarnerModel();
    @NotNull
    public static WarnerModel get() {
        return singletonInstance;
    }

    public ObservableField<Boolean> isEnabled = new ObservableField<>(false);

    public WarnerModel() {
        SharedPreferences preferences = Controller.get().getPreferences();
        if (preferences.contains("WarnerEnable")) {
            if (preferences.getBoolean("WarnerEnable", false)) {
                start();
            }
        }
    }

    public UpdaterService.UpdaterBinder updater;
    private boolean updaterRunning = false;

    private void startUpdater() {
        if (updater == null) {
            Intent intent = new Intent(Controller.getContext(), UpdaterService.class);
            Controller.getContext().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    updater = (UpdaterService.UpdaterBinder) binder;
                    if (updaterRunning)
                        updater.start();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    updater = null;
                }
            }, BIND_AUTO_CREATE);
        } else updater.start();
        updaterRunning = true;
    }

    private void stopUpdater() {
        if (updater != null)
            updater.stop();
    }

    public void start() {
        startUpdater();

        isEnabled.set(true);

        SharedPreferences preferences = Controller.get().getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("WarnerEnable", true);
        editor.apply();
    }

    public void stop() {
        stopUpdater();

        isEnabled.set(false);

        SharedPreferences preferences = Controller.get().getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("WarnerEnable", false);
        editor.apply();
    }

    public void warn(RiskType risk, DistanceType distance) {
        if (!isEnabled.get()) return;
        Controller.getModule(MessageModule.class).notifyRiskWarning(risk, distance);
    }
}
