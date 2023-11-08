package edu.upc.shadowcode;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Hashtable;

import edu.upc.shadowcode.modules.PermissionModule;
import edu.upc.shadowcode.modules.MessageModule;
import edu.upc.shadowcode.views.MainActivity;

public class Controller extends Application {
    // 单例控制器实例
    private static Controller currentInstance;
    // 获取单例的控制器实例
    public static Controller get(){
        return currentInstance;
    }

    public static Context getContext(){
        return currentInstance.getApplicationContext();
    }

    private static final Hashtable<Class<? extends Module>, Module> _modules = new Hashtable<>();

    public static void addModule(Module manager){
        _modules.put(manager.getClass(), manager);
    }

    public static void removeModule(Class<? extends Module> type){
        _modules.remove(type);
    }

    @SuppressWarnings("unchecked")
    public static <TManager extends Module> TManager getModule(Class<TManager> type) {
        return (TManager) _modules.get(type);
    }

    public Controller() {
        addModule(new MessageModule());
        addModule(new PermissionModule());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        currentInstance = this;

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                if (activity instanceof MainActivity)
                    install();
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (activity instanceof MainActivity)
                    uninstall();
            }
        });
    }

    // 当前程序是否已经初始化
    private boolean initialized = false;
    // 初始化控制器
    private void install() {
        if (initialized)
            return;

        for (Module module: _modules.values()) {
            module.install();
        }

        initialized = true;
    }

    private void uninstall(){
        if (!initialized)
            return;
        for (Module module: _modules.values()) {
            module.uninstall();
        }
        initialized = false;
    }

    public SharedPreferences getPreferences(){
        return getContext().getSharedPreferences("data", MODE_PRIVATE);
    }
}
