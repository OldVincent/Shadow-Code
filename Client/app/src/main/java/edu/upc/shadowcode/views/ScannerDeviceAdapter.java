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
import edu.upc.shadowcode.databinding.ScannerDeviceBinding;
import edu.upc.shadowcode.models.Exposure;

public class ScannerDeviceAdapter extends RecyclerView.Adapter<ScannerDeviceViewHolder> {
    private final AnimationSet jumpingAnimation = createJumpAnimation();

    private final List<Exposure> exposures;

    public ScannerDeviceAdapter(List<Exposure> boundExposures){
        exposures = boundExposures;
    }

    @NonNull
    @Override
    public ScannerDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ScannerDeviceBinding binding =
                ScannerDeviceBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new ScannerDeviceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScannerDeviceViewHolder holder, int position) {
        holder.binding.setDevice(exposures.get(position));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ScannerDeviceViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.binding.deviceImage.startAnimation(jumpingAnimation);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ScannerDeviceViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.binding.deviceImage.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return exposures.size();
    }

    private static final float JumpingScaleFactor = 1.4f;
    private static final int JumpingTimeDuration = 800;

    private AnimationSet createJumpAnimation() {
        AnimationSet animation = new AnimationSet(true);
        ScaleAnimation expandAnimation = new ScaleAnimation(
                1.0f, JumpingScaleFactor, 1.0f, JumpingScaleFactor,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        expandAnimation.setDuration(JumpingTimeDuration / 2);
        expandAnimation.setRepeatCount(Animation.INFINITE);
        ScaleAnimation collapseAnimation = new ScaleAnimation(
                JumpingScaleFactor, 1.0f, JumpingScaleFactor, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        collapseAnimation.setDuration(JumpingTimeDuration / 2);
        collapseAnimation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(JumpingTimeDuration);
        animation.addAnimation(expandAnimation);
        animation.addAnimation(collapseAnimation);
        return animation;
    }
}
