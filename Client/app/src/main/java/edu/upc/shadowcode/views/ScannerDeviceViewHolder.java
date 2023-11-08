package edu.upc.shadowcode.views;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.upc.shadowcode.R;
import edu.upc.shadowcode.databinding.ScannerDeviceBinding;

public class ScannerDeviceViewHolder extends RecyclerView.ViewHolder {
    public final ScannerDeviceBinding binding;

    public ScannerDeviceViewHolder(@NonNull ScannerDeviceBinding viewBinding) {
        super(viewBinding.getRoot());
        binding = viewBinding;
    }
}
