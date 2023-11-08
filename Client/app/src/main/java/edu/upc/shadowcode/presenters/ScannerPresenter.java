package edu.upc.shadowcode.presenters;

import android.bluetooth.BluetoothClass;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;

import androidx.databinding.Observable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.Random;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.Presenter;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.databinding.FragmentScannerBinding;
import edu.upc.shadowcode.models.DeviceModel;
import edu.upc.shadowcode.models.DeviceType;
import edu.upc.shadowcode.models.Exposure;
import edu.upc.shadowcode.models.UserModel;
import edu.upc.shadowcode.views.CircleLayoutManager;
import edu.upc.shadowcode.views.ScannerDeviceAdapter;
import edu.upc.shadowcode.views.ScannerDeviceAnimator;

public class ScannerPresenter extends Presenter<FragmentScannerBinding> {
    private static SoftReference<ScannerPresenter> singletonInstance = null;
    @NotNull
    public static ScannerPresenter get() {
        if (singletonInstance == null || singletonInstance.get() == null) {
            singletonInstance = new SoftReference<>(new ScannerPresenter());
        }
        return singletonInstance.get();
    }

    public DeviceModel device = DeviceModel.get();

    private final AnimationSet scannerCircleMediumAnimation = createMediumCircleAnimation();
    private final AnimationSet scannerCircleOuterAnimation = createOuterCircleAnimation();

    private final ScannerDeviceAdapter nearDevicesAdapter =
            new ScannerDeviceAdapter(DeviceModel.get().currentNearExposures);
    private final ScannerDeviceAdapter mediumDevicesAdapter =
            new ScannerDeviceAdapter(DeviceModel.get().currentMediumExposures);
    private final ScannerDeviceAdapter farDevicesAdapter =
            new ScannerDeviceAdapter(DeviceModel.get().currentFarExposures);

    private ObservableList.OnListChangedCallback bindChangingListener(ScannerDeviceAdapter adapter){
        return new ObservableList.OnListChangedCallback(){
            @Override
            public void onChanged(ObservableList sender) {
                return;
            }

            @Override
            public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
                adapter.notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
                adapter.notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
                return;
            }

            @Override
            public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
                adapter.notifyItemRangeRemoved(positionStart, itemCount);
            }
        };
    }
    private ObservableList.OnListChangedCallback currentNearExposuresListener = null;
    private ObservableList.OnListChangedCallback currentMediumExposuresListener = null;
    private ObservableList.OnListChangedCallback currentFarExposuresListener = null;
    private Observable.OnPropertyChangedCallback deviceEnableListener = new Observable.OnPropertyChangedCallback() {
        @SuppressWarnings("unchecked")
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            ObservableField<Boolean> value = (ObservableField<Boolean>) sender;
            if (value.get()) {
                getBinding().scannerCircle2.startAnimation(scannerCircleMediumAnimation);
                getBinding().scannerCircle3.startAnimation(scannerCircleOuterAnimation);
            } else {
                getBinding().scannerCircle2.clearAnimation();
                getBinding().scannerCircle3.clearAnimation();
            }
        }
    };

    private ScannerPresenter() {
    }

    private final Observable.OnPropertyChangedCallback riskListener = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            updateSelfImage();
        }
    };

    public void updateSelfImage() {

        int circleId = 0;
        switch (UserModel.get().risk.get()) {
            case Unknown:
                circleId = R.drawable.shape_circle_unknown;
                break;
            case Low:
                circleId = R.drawable.shape_circle_low;
                break;
            case Medium:
                circleId = R.drawable.shape_circle_medium;
                break;
            case High:
                circleId =R.drawable.shape_circle_high;
                break;
            case Danger:
                circleId =R.drawable.shape_circle_danger;
                break;
        }

        int imageId = 0;
        if (UserModel.get().deviceType.get() == DeviceType.Location) {
            switch (UserModel.get().risk.get()) {
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
        } else {
            switch (UserModel.get().risk.get()) {
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
        }

        getBinding().imageSelf.setImageDrawable(Controller.getContext().getDrawable(imageId));
        getBinding().scannerCircle1.setImageDrawable(Controller.getContext().getDrawable(circleId));
        getBinding().scannerCircle2.setImageDrawable(Controller.getContext().getDrawable(circleId));
        getBinding().scannerCircle3.setImageDrawable(Controller.getContext().getDrawable(circleId));
    }

    public void onAttach() {
        getBinding().setScannerPresenter(this);

        float density = getBinding().getRoot().getResources().getDisplayMetrics().density;
        int scannerOffset = 0;

        getBinding().listNearExposures.setLayoutManager(new CircleLayoutManager((int)(90*density), scannerOffset));
        LayoutAnimationController deviceAppearingAnimation = createDeviceInitializingAnimation();
        getBinding().listNearExposures.setLayoutAnimation(deviceAppearingAnimation);
        getBinding().listNearExposures.setItemAnimator(new ScannerDeviceAnimator());
        getBinding().listNearExposures.setAdapter(nearDevicesAdapter);

        getBinding().listMediumExposures.setLayoutManager(new CircleLayoutManager((int)(150*density), scannerOffset));
        getBinding().listMediumExposures.setLayoutAnimation(deviceAppearingAnimation);
        getBinding().listMediumExposures.setItemAnimator(new ScannerDeviceAnimator());
        getBinding().listMediumExposures.setAdapter(mediumDevicesAdapter);

        FlexboxLayoutManager flexboxLayout = new FlexboxLayoutManager(getBinding().getRoot().getContext());
        flexboxLayout.setFlexDirection(FlexDirection.ROW);
        flexboxLayout.setAlignItems(AlignItems.CENTER);
        flexboxLayout.setJustifyContent(JustifyContent.CENTER);

        getBinding().listFarExposures.setLayoutManager(flexboxLayout);
        getBinding().listFarExposures.setLayoutAnimation(deviceAppearingAnimation);
        getBinding().listFarExposures.setItemAnimator(new ScannerDeviceAnimator());
        getBinding().listFarExposures.setAdapter(farDevicesAdapter);


        DeviceModel.get().isEnabled.addOnPropertyChangedCallback(deviceEnableListener);

        //noinspection ConstantConditions
        if (DeviceModel.get().isEnabled.get()) {
            getBinding().scannerCircle2.startAnimation(scannerCircleMediumAnimation);
            getBinding().scannerCircle3.startAnimation(scannerCircleOuterAnimation);
        }

        if (currentNearExposuresListener == null)
            currentNearExposuresListener = bindChangingListener(nearDevicesAdapter);
        if (currentMediumExposuresListener == null)
            currentMediumExposuresListener = bindChangingListener(mediumDevicesAdapter);
        if (currentFarExposuresListener == null)
            currentFarExposuresListener = bindChangingListener(farDevicesAdapter);
        DeviceModel.get().currentNearExposures.addOnListChangedCallback(
                currentNearExposuresListener);
        DeviceModel.get().currentMediumExposures.addOnListChangedCallback(
                currentMediumExposuresListener);
        DeviceModel.get().currentFarExposures.addOnListChangedCallback(
                currentFarExposuresListener);

        UserModel.get().risk.addOnPropertyChangedCallback(riskListener);

        updateSelfImage();
    }

    public void onDetach() {
        DeviceModel.get().isEnabled.removeOnPropertyChangedCallback(deviceEnableListener);
        DeviceModel.get().currentNearExposures.removeOnListChangedCallback(
                currentNearExposuresListener);
        DeviceModel.get().currentMediumExposures.removeOnListChangedCallback(
                currentMediumExposuresListener);
        DeviceModel.get().currentFarExposures.removeOnListChangedCallback(
                currentFarExposuresListener);

        UserModel.get().risk.removeOnPropertyChangedCallback(riskListener);
    }

    public View.OnClickListener enableClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Boolean enabled = device.isEnabled.get();
            if (enabled == null || !enabled)
                device.start();
            else device.stop();
        }
    };

    private LayoutAnimationController createDeviceInitializingAnimation() {
        Animation animation = AnimationUtils.loadAnimation(
                Controller.getContext(), R.anim.device_initializing);
        LayoutAnimationController layoutAnimationController = new LayoutAnimationController(animation);
        layoutAnimationController.setOrder(LayoutAnimationController.ORDER_RANDOM);
        layoutAnimationController.setDelay(0.2f);
        return layoutAnimationController;
    }

    // 中环波纹动画
    private AnimationSet createMediumCircleAnimation() {
        AnimationSet animation = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.3f, 1.0f, 1.3f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.addAnimation(scaleAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.5f);
        scaleAnimation.setDuration(800);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(800);
        animation.addAnimation(alphaAnimation);
        return animation;
    }

    // 外环波纹动画
    private AnimationSet createOuterCircleAnimation() {
        AnimationSet animation = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.3f, 1.6f, 1.3f, 1.6f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(800);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        animation.addAnimation(scaleAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 0.1f);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(800);
        animation.addAnimation(alphaAnimation);
        return animation;
    }
}
