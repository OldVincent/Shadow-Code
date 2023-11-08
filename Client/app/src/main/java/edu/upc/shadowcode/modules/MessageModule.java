package edu.upc.shadowcode.modules;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.Module;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.models.DistanceType;
import edu.upc.shadowcode.models.RiskType;
import edu.upc.shadowcode.models.UserModel;
import edu.upc.shadowcode.views.MainActivity;

public class MessageModule implements Module {
    private android.app.NotificationManager notification;

    private String channelRecorderStatusId;
    private String channelRiskStatusId;
    private String channelWarningId;

    @Override
    public void install() {
        notification =
                (NotificationManager)Controller.getContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);

        channelRecorderStatusId = "recorder_status";
        NotificationChannel channelRecorderStatus = new NotificationChannel(channelRecorderStatusId,
                Controller.getContext().getString(R.string.notification_channel_recorder),
                NotificationManager.IMPORTANCE_LOW);
        channelRecorderStatus.setDescription(
                Controller.getContext().getString(R.string.notification_channel_recorder_description));
        notification.createNotificationChannel(channelRecorderStatus);

        channelRiskStatusId = "risk_status";
        NotificationChannel channelRiskStatus = new NotificationChannel(channelRiskStatusId,
                Controller.getContext().getString(R.string.notification_channel_risk),
                NotificationManager.IMPORTANCE_HIGH);
        channelRiskStatus.setDescription(
                Controller.getContext().getString(R.string.notification_channel_risk_description));
        channelRiskStatus.enableVibration(true);
        channelRiskStatus.enableLights(true);
        channelRiskStatus.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            channelRiskStatus.setAllowBubbles(true);
        }
        notification.createNotificationChannel(channelRiskStatus);

        channelWarningId = "risk_warning";
        NotificationChannel channelWarning = new NotificationChannel(channelWarningId,
                Controller.getContext().getString(R.string.notification_channel_warning),
                NotificationManager.IMPORTANCE_HIGH);
        channelWarning.setDescription(
                Controller.getContext().getString(R.string.notification_channel_warning_description));
        channelWarning.enableVibration(true);
        channelWarning.enableLights(true);
        channelWarning.setShowBadge(true);
        channelWarning.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notification.createNotificationChannel(channelWarning);
    }

    @Override
    public void uninstall() {
        notification = null;
    }

    public void showToast(CharSequence text){
        MainActivity.get().runOnUiThread(()->{
            Toast.makeText(Controller.getContext(), text, Toast.LENGTH_SHORT).show();
        });
    }

    public void showLongToast(CharSequence text){
        MainActivity.get().runOnUiThread(()->{
            Toast.makeText(Controller.getContext(), text, Toast.LENGTH_LONG).show();
        });
    }

    public void showDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.get());
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setNeutralButton("了解", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        MainActivity.get().runOnUiThread(builder::show);
    }

    public void notifyRecorderStartRunning(){
        Intent intent = new Intent(MainActivity.get(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                Controller.getContext(),
                channelRecorderStatusId);
        builder.setContentTitle(Controller.getContext().getString(R.string.notification_recorder_title))
                .setContentText(Controller.getContext().getString(R.string.notification_recorder_content))
                .setContentIntent(PendingIntent.getActivity(Controller.getContext(),
                        0, intent,
                        PendingIntent.FLAG_IMMUTABLE |PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setSmallIcon(R.drawable.ic_police);
        notification.notify(100, builder.build());
    }

    public void notifyRecorderStopRunning(){
        notification.cancel(100);
    }

    // 提示风险警告
    public void notifyRiskWarning(RiskType risk, DistanceType distance){
        Intent intent = new Intent(MainActivity.get(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(Controller.getContext(),
                0, intent,
                PendingIntent.FLAG_MUTABLE |PendingIntent.FLAG_UPDATE_CURRENT);
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager)Controller.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                Controller.getContext(),
                channelWarningId);
        builder.setContentTitle(Controller.getContext().getString(R.string.notification_warning_title))
                .setContentText(Controller.getContext().getString(R.string.notification_warning_head)
                        + UserModel.translateDistance(distance)
                        + UserModel.translateRisk(risk)
                        + (Controller.getContext().getString(R.string.notification_warning_tail)))
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                .setLargeIcon(BitmapFactory.decodeResource(
                        Controller.getContext().getResources(), R.drawable.ic_coronavirus))
                .setSmallIcon(R.drawable.ic_coronavirus);
        notificationManager.notify(103, builder.build());
    }

    // 提示风险等级提升
    public void notifyRiskIncrement(@NonNull RiskType risk){
        int textId = 0;
        switch (risk){
            case Unknown:
                return;
            case Danger:
                textId = R.string.notification_risk_status_danger;
                break;
            case High:
                textId = R.string.notification_risk_status_high;
                break;
            case Medium:
                textId = R.string.notification_risk_status_medium;
                break;
            case Low:
                textId = R.string.notification_risk_status_low;
                break;
        }
        Intent intent = new Intent(MainActivity.get(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(Controller.getContext(),
                0, intent,
                PendingIntent.FLAG_MUTABLE |PendingIntent.FLAG_UPDATE_CURRENT);
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager)Controller.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Controller.getContext(),
                channelRiskStatusId);
        builder.setContentTitle(Controller.getContext().getString(R.string.notification_risk_status_increase))
                .setContentText(Controller.getContext().getString(textId))
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setSmallIcon(R.drawable.ic_mood_bad);
        notificationManager.notify(101, builder.build());
    }

    // 提示风险等级降低
    public void notifyRiskDecrement(@NonNull RiskType risk){
        int textId = 0;
        switch (risk){
            case Unknown:
                return;
            case Danger:
                textId = R.string.notification_risk_status_danger;
                break;
            case High:
                textId = R.string.notification_risk_status_high;
                break;
            case Medium:
                textId = R.string.notification_risk_status_medium;
                break;
            case Low:
                textId = R.string.notification_risk_status_low;
                break;
        }
        Intent intent = new Intent(MainActivity.get(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(Controller.getContext(),
                0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager)Controller.getContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Controller.getContext(),
                channelRiskStatusId);
        builder.setContentTitle(Controller.getContext().getString(R.string.notification_risk_status_increase))
                .setContentText(Controller.getContext().getString(textId))
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSmallIcon(R.drawable.ic_mood_good);
        notificationManager.notify(102, builder.build());
    }
}
