package edu.upc.shadowcode.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableArrayMap;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.upc.shadowcode.R;
import edu.upc.shadowcode.databinding.ExposureElementBinding;
import edu.upc.shadowcode.databinding.ScannerDeviceBinding;
import edu.upc.shadowcode.models.Exposure;

public class RecentRiskExposureAdapter extends RecyclerView.Adapter<RecentRiskExposureViewHolder> {

    private final ObservableArrayList<Exposure> exposures;

    public RecentRiskExposureAdapter(ObservableArrayList<Exposure> boundExposures){
        exposures = boundExposures;
    }

    @NonNull
    @Override
    public RecentRiskExposureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ExposureElementBinding binding =
                ExposureElementBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new RecentRiskExposureViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentRiskExposureViewHolder holder, int position) {
        holder.bindExposure(exposures.get(position));
    }

    @Override
    public int getItemCount() {
        return exposures.size();
    }
}
