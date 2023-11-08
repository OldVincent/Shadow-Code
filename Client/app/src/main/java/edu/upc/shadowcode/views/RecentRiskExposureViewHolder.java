package edu.upc.shadowcode.views;

import android.content.res.ColorStateList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.databinding.ExposureElementBinding;
import edu.upc.shadowcode.models.DistanceType;
import edu.upc.shadowcode.models.Exposure;
import edu.upc.shadowcode.models.RiskType;
import edu.upc.shadowcode.models.UserModel;

public class RecentRiskExposureViewHolder extends RecyclerView.ViewHolder {
    public final ExposureElementBinding binding;

    public static ColorStateList createColorList(RiskType risk) {
        int[][] states = new int[1][];
        states[0] = new int[] {};
        int[] colors = new int[1];
        int colorId = R.color.risk_unknown;
        switch (risk) {
            case Unknown:
                break;
            case Low:
                colorId = R.color.risk_low;
                break;
            case Medium:
                colorId = R.color.risk_medium;
                break;
            case High:
                colorId = R.color.risk_high;
                break;
            case Danger:
                colorId = R.color.risk_danger;
                break;
        }
        colors[0] = Controller.getContext().getColor(colorId);
        return new ColorStateList(states, colors);
    }



    public RecentRiskExposureViewHolder(@NonNull ExposureElementBinding viewBinding) {
        super(viewBinding.getRoot());
        binding = viewBinding;
    }

    public void bindExposure(Exposure exposure) {
        binding.setExposure(exposure);
        binding.exposureRecordChip.setText(
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(exposure.beginningTime.get())
                        + " " + UserModel.translateDistance(exposure.distance.get())
                        + " " + UserModel.translateRisk(exposure.risk.get()));
        binding.exposureRecordChip.setChipBackgroundColor(createColorList(exposure.risk.get()));
    }

}
