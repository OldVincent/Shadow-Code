package edu.upc.shadowcode.presenters;

import android.bluetooth.BluetoothClass;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.Presenter;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.databinding.FragmentScannerBinding;
import edu.upc.shadowcode.databinding.FragmentSettingBinding;
import edu.upc.shadowcode.models.DeviceModel;
import edu.upc.shadowcode.models.DistanceType;
import edu.upc.shadowcode.models.RiskType;
import edu.upc.shadowcode.models.UserModel;
import edu.upc.shadowcode.models.WarnerModel;
import edu.upc.shadowcode.views.LongTextFragment;
import edu.upc.shadowcode.views.MainActivity;
import edu.upc.shadowcode.views.RequestFragment;

public class DescriptionPresenter extends Presenter<FragmentSettingBinding> {
    private static SoftReference<DescriptionPresenter> singletonInstance = null;
    @NotNull
    public static DescriptionPresenter get() {
        if (singletonInstance == null || singletonInstance.get() == null) {
            singletonInstance = new SoftReference<>(new DescriptionPresenter());
        }
        return singletonInstance.get();
    }


    private DescriptionPresenter(){
    }

    public void onAttach(){
        getBinding().setDescriptionPresenter(this);
    }

    public void onDetach(){
    }

    public View.OnClickListener aboutPolicyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = MainActivity.get().getSupportFragmentManager();
            LongTextFragment dialogFragment = new LongTextFragment();
            dialogFragment.setContent(
                    Controller.getContext().getString(R.string.about_title_policy),
                    Controller.getContext().getString(R.string.about_description_policy));
            dialogFragment.show(fragmentManager, "dialog");
        }
    };

    public View.OnClickListener aboutComponentsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = MainActivity.get().getSupportFragmentManager();
            LongTextFragment dialogFragment = new LongTextFragment();
            dialogFragment.setContent(
                    Controller.getContext().getString(R.string.about_title_components),
                    Controller.getContext().getString(R.string.about_description_components));
            dialogFragment.show(fragmentManager, "dialog");
        }
    };
}
