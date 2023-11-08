package edu.upc.shadowcode.models;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.util.Log;

import androidx.databinding.ObservableField;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.UUID;

import edu.upc.shadowcode.Client;
import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.modules.MessageModule;
import edu.upc.shadowcode.services.UpdaterService;
import edu.upc.shadowcode.views.MainActivity;

public class UserModel {
    private static final UserModel singletonInstance = new UserModel();
    @NotNull
    public static UserModel get() {
        return singletonInstance;
    }

    // 用户是否已经登录
    public ObservableField<Boolean> isLoggedIn = new ObservableField<>(false);
    // 用户名
    public ObservableField<String> name = new ObservableField<>("");
    // 显示的带有隐藏内容的证件号码
    public ObservableField<String> displayingIdentity = new ObservableField<>("");
    // 风险状态
    public ObservableField<RiskType> risk = new ObservableField<>(RiskType.Unknown);

    public ObservableField<DeviceType> deviceType = new ObservableField<>(DeviceType.Person);

    // 完整证件号码
    private String identity = "";
    private String getIdentity() {
        return identity;
    }


    // 设备唯一标识码
    public final UUID deviceId;

    public final byte[] deviceIdBytes;

    public static byte[] IdToBytes(UUID uuid){
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    public static UUID BytesToId(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long high = buffer.getLong();
        long low = buffer.getLong();
        return new UUID(high, low);
    }

    // 加密过的混淆用户名
    private String fusedUserName = null;
    // 加密过的混淆密码
    private String fusedUserPassword = null;

    private int userId = -1;

    public UserModel() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences preferences = Controller.get().getPreferences();
        if (preferences.contains("DeviceId")){
            deviceId = java.util.UUID.fromString(preferences.getString("DeviceId", ""));
            Log.d("User", "Loaded ID: " + deviceId.toString());
        } else {
            deviceId = java.util.UUID.randomUUID();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("DeviceId", deviceId.toString());
            editor.apply();
            Log.d("User", "Generated ID: " + deviceId.toString());
        }
        deviceIdBytes = IdToBytes(deviceId);

        if (preferences.contains("UserName")){
            fusedUserName = preferences.getString("UserName", "");
            name.set(decryptString(fusedUserName));
        }
        if (preferences.contains("UserPassword")){
            fusedUserPassword = preferences.getString("UserPassword", "");
        }
        if (preferences.contains("UserIdentity")){
            displayingIdentity.set(preferences.getString("UserIdentity", ""));
        }
        if (preferences.contains("UserId")){
            userId = preferences.getInt("UserId", -1);
            isLoggedIn.set(true);
        }
        if (preferences.contains("UserRisk")){
            risk.set(RiskType.values()[preferences.getInt("UserRisk", RiskType.Unknown.ordinal())]);
        } else if (isLoggedIn.get()){
            updateRisk(false);
        } else risk.set(RiskType.Unknown);


    }

    public void updateRisk(boolean display_error) {
        ConnectivityManager connectivityManager = (ConnectivityManager) MainActivity.get()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = connectivityManager.getActiveNetworkInfo();
        if (network == null || !network.isAvailable()) {
            if (!display_error) return;
            Controller.getModule(MessageModule.class).showToast(
                    Controller.getContext().getString(R.string.failure_invalid_internet));
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("id", userId);
            request.put("risk", risk.get().ordinal());
            JSONObject response = Client.request("update_risk", request);
            if (response == null) {
                return;
            }
            int code = response.getInt("Code");
            if (code == -1)
                return;
            int risk = response.getInt("Risk");
            changeRisk(RiskType.values()[risk]);
        } catch (JSONException e) {
            Log.d("UserModel", e.toString());
        }
    }

    public enum LogoutResult {
        Success,
        InnerError
    }

    public LogoutResult logout() {
        try {
            JSONObject request = new JSONObject();
            request.put("id", userId);
            request.put("device", deviceId.toString());

            JSONObject response = Client.request("user_logout", request);
            if (response == null) {
                return LogoutResult.InnerError;
            }

            int code = response.getInt("Code");
            if (code == -1)
                return LogoutResult.InnerError;
        } catch (JSONException e) {
            Log.d("UserModel", e.toString());
            return LogoutResult.InnerError;
        }

        isLoggedIn.set(false);

        SharedPreferences preferences = Controller.get().getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("UserName");
        editor.remove("UserId");
        editor.remove("UserPassword");
        editor.remove("UserIdentity");
        editor.remove("UserRisk");
        editor.apply();
        userId = -1;

        fusedUserName = null;
        fusedUserPassword = null;

        return LogoutResult.Success;
    }

    public enum LoginResult {
        Success,
        WrongUserName,
        WrongPassword,
        InnerError
    }

    public LoginResult login(String userName, String password) {
        return loginWithFusion(encryptString(userName), encryptString(password));
    }

    private LoginResult loginWithFusion(String userName, String userPassword) {
        try {
            JSONObject request = new JSONObject();
            request.put("name", userName);
            request.put("password", userPassword);
            request.put("device", deviceId.toString());

            JSONObject response = Client.request("user_login", request);
            if (response == null) {
                return LoginResult.InnerError;
            }

            int code = response.getInt("Code");
            if (code == 0) {
                userId = response.getInt("Id");
                onLoginSucceed(encryptString(userName), userPassword, response.getString("Identity"));
            } else if (code == -1)
                return LoginResult.WrongUserName;
            else if (code == -2)
                return LoginResult.WrongPassword;
            else return LoginResult.InnerError;
        } catch (JSONException e) {
            Log.d("UserModel", e.toString());
            return LoginResult.InnerError;
        }

        return LoginResult.Success;

    }

    private void onLoginSucceed(String encryptedName, String encryptedPassword, String identity){
        name.set(decryptString(encryptedName));

        displayingIdentity.set(identity);

        // 存储使用的用户名和密码
        fusedUserName = encryptedName;
        fusedUserPassword = encryptedPassword;
        SharedPreferences preferences = Controller.get().getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("UserName", fusedUserName);
        editor.putString("UserPassword", fusedUserPassword);
        editor.putString("UserIdentity", identity);
        editor.putInt("UserId", userId);
        editor.apply();

        isLoggedIn.set(true);
    }

    public void setIdentity(String userIdentity) {
        identity = userIdentity;
        StringBuilder maskedIdentity = new StringBuilder();
        if (userIdentity.length() >= 8){
            maskedIdentity.append(userIdentity.substring(0, 3));
            for (int index = 0; index < userIdentity.length() - 7; ++index){
                maskedIdentity.append('*');
            }
            maskedIdentity.append(userIdentity.substring(userIdentity.length() - 4));
        } else {
            maskedIdentity.append(userIdentity.charAt(0));
            for (int index = 0; index < userIdentity.length() - 2; ++index){
                maskedIdentity.append('*');
            }
            maskedIdentity.append(userIdentity.substring(userIdentity.length() - 1));
        }
        String maskedIdentityText = maskedIdentity.toString();
        displayingIdentity.set(maskedIdentityText);

        SharedPreferences preferences = Controller.get().getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("UserIdentity", maskedIdentityText);
        editor.apply();
    }

    public enum RegisterResult {
        Success,
        Conflict,
        InnerError
    }

    public RegisterResult register(String userName, String password, String userIdentity) {
        try {
            JSONObject request = new JSONObject();
            request.put("name", userName);
            request.put("password", password);
            request.put("identity", userIdentity);

            JSONObject response = Client.request("user_register", request);
            if (response == null) {
                return RegisterResult.InnerError;
            }

            int code = response.getInt("Code");
            if (code == 0) {
                if (login(userName, password) == LoginResult.Success)
                    return RegisterResult.Success;
                return RegisterResult.InnerError;
            } else if (code == -1)
                return RegisterResult.Conflict;
            else return RegisterResult.InnerError;
        } catch (JSONException e) {
            Log.d("UserModel", e.toString());
            return RegisterResult.InnerError;
        }
    }

    public String encryptString(String raw) {
        return raw;
    }

    public String decryptString(String raw) {
        return raw;
    }

    public void changeRisk(RiskType targetRisk) {
        if (targetRisk.ordinal() == risk.get().ordinal())
            return;

        if (targetRisk.ordinal() > risk.get().ordinal()) {
            Controller.getModule(MessageModule.class).notifyRiskIncrement(targetRisk);
        } else
            Controller.getModule(MessageModule.class).notifyRiskDecrement(targetRisk);
        risk.set(targetRisk);

        SharedPreferences preferences = Controller.get().getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("UserRisk", targetRisk.ordinal());
        editor.apply();
    }

    public void increaseRisk(RiskType targetRisk) {
        if (targetRisk.ordinal() <= risk.get().ordinal())
            return;

        Controller.getModule(MessageModule.class).notifyRiskIncrement(targetRisk);
        risk.set(targetRisk);

        SharedPreferences preferences = Controller.get().getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("UserRisk", targetRisk.ordinal());
        editor.apply();
    }

    public void decreaseRisk(RiskType targetRisk) {
        if (targetRisk.ordinal() >= risk.get().ordinal())
            return;

        Controller.getModule(MessageModule.class).notifyRiskDecrement(targetRisk);
        risk.set(targetRisk);

        SharedPreferences preferences = Controller.get().getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("UserRisk", targetRisk.ordinal());
        editor.apply();
    }

    public static String translateRisk(RiskType risk) {
        switch (risk) {
            case Unknown:
                return Controller.getContext().getString(R.string.risk_unknown);
            case Low:
                return Controller.getContext().getString(R.string.risk_low);
            case Medium:
                return Controller.getContext().getString(R.string.risk_medium);
            case High:
                return Controller.getContext().getString(R.string.risk_high);
            case Danger:
                return Controller.getContext().getString(R.string.risk_danger);
        }
        return Controller.getContext().getString(R.string.risk_unknown);
    }

    public static String translateDistance(DistanceType distance) {
        switch (distance) {
            case Near:
                return Controller.getContext().getString(R.string.distance_type_near);
            case Medium:
                return Controller.getContext().getString(R.string.distance_type_medium);
            case Far:
                return Controller.getContext().getString(R.string.distance_type_far);
        }
        return Controller.getContext().getString(R.string.distance_type_medium);
    }
}
