package edu.upc.shadowcode.models;

import android.bluetooth.BluetoothClass;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.R;

/// 接触
public class Exposure {
    public ObservableField<Date> beginningTime = new ObservableField<Date>(new Date(System.currentTimeMillis()));
    public ObservableField<Date> endingTime = new ObservableField<Date>(new Date(System.currentTimeMillis()));
    public ObservableField<DistanceType> distance = new ObservableField<>(DistanceType.Near);
    public DeviceType device = DeviceType.Person;
    public UUID id;
    public ObservableField<RiskType> risk = new ObservableField<>(RiskType.Unknown);

    public ObservableField<Drawable> image = new ObservableField<>(
            ResourcesCompat.getDrawable(Controller.get().getResources(),
                    R.drawable.ic_person_unknown, null)
    );

    // 信号强度记录队列
    private final LinkedList<Integer> signalStrengthRecords = new LinkedList<>();

    // 记录最新信号强度，并更新距离
    public void updateSignalStrength(int strength) {
        signalStrengthRecords.addLast(strength);
        while (signalStrengthRecords.size() > 3) {
            signalStrengthRecords.removeFirst();
        }
        float averageStrength = getSignalStrength();

        if (averageStrength < 67) {
            distance.set(DistanceType.Near);
        } else if (averageStrength < 80) {
            distance.set(DistanceType.Medium);
        } else distance.set(DistanceType.Far);
    }

    // 获取平均信号强度
    public float getSignalStrength(){
        if (signalStrengthRecords.size() == 0)
            return 0.0f;
        int total_strength = 0;
        for (Integer strength : signalStrengthRecords) {
            total_strength += strength;
        }
        return (float)total_strength / signalStrengthRecords.size();
    }

    public Exposure(){
        risk.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (risk.get() == null)
                    return;
                int imageId = 0;
                if (device == DeviceType.Person){
                    switch (Objects.requireNonNull(risk.get())){
                        case Unknown:
                            imageId = R.drawable.ic_person_unknown;
                            break;
                        case Low:
                            imageId = R.drawable.ic_person_low;
                            break;
                        case Medium:
                            imageId = R.drawable.ic_person_medium;
                            break;
                        case High:
                            imageId = R.drawable.ic_person_high;
                            break;
                        case Danger:
                            imageId = R.drawable.ic_person_danger;
                            break;
                    }
                } else {
                    switch (Objects.requireNonNull(risk.get())){
                        case Unknown:
                            imageId = R.drawable.ic_location_unknown;
                            break;
                        case Low:
                            imageId = R.drawable.ic_location_low;
                            break;
                        case Medium:
                            imageId = R.drawable.ic_location_medium;
                            break;
                        case High:
                            imageId = R.drawable.ic_location_high;
                            break;
                        case Danger:
                            imageId = R.drawable.ic_location_danger;
                            break;
                    }
                }
                image.set(ResourcesCompat.getDrawable(Controller.get().getResources(),
                        imageId, null));
            }
        });
    }
}
