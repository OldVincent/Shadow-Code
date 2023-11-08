package edu.upc.shadowcode.presenters;

import android.view.View;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.Presenter;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.databinding.FragmentIndexBinding;
import edu.upc.shadowcode.models.DeviceModel;
import edu.upc.shadowcode.models.WarnerModel;

public class ControlPanelPresenter extends Presenter<FragmentIndexBinding> {
    private static SoftReference<ControlPanelPresenter> singletonInstance = null;
    @NotNull
    public static ControlPanelPresenter get() {
        if (singletonInstance == null || singletonInstance.get() == null) {
            singletonInstance = new SoftReference<>(new ControlPanelPresenter());
        }
        return singletonInstance.get();
    }

    public DeviceModel scanner = DeviceModel.get();

    public WarnerModel warner = WarnerModel.get();


    public ObservableField<String> scannerEnableSwitchText = new ObservableField<>();

    public ObservableField<String> warnerEnableSwitchText = new ObservableField<>();

    private final Observable.OnPropertyChangedCallback scannerStatusWatcher = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            updateScannerSwitchText();
        }
    };

    private final Observable.OnPropertyChangedCallback updaterStatusWatcher = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            updateScannerSwitchText();
        }
    };

    private ControlPanelPresenter() {
    }

    protected void onAttach() {
        getBinding().setControlPanelPresenter(this);

        scanner.isEnabled.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {

            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {

            }
        });

        warner.isEnabled.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                updateUpdaterSwitchText();
            }
        });

        updateScannerSwitchText();
        updateUpdaterSwitchText();
    }

    protected void onDetach() {

    }

    @SuppressWarnings("ConstantConditions")
    public void updateScannerSwitchText(){
        if (scanner.isEnabled.get()){
            scannerEnableSwitchText.set(
                    Controller.getContext().getString(R.string.switch_scanner_on));
        } else {
            scannerEnableSwitchText.set(
                    Controller.getContext().getString(R.string.switch_scanner_off));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void updateUpdaterSwitchText(){
        if (warner.isEnabled.get()){
            warnerEnableSwitchText.set(
                    Controller.getContext().getString(R.string.switch_warner_on));
        } else {
            warnerEnableSwitchText.set(
                    Controller.getContext().getString(R.string.switch_warner_off));
        }
    }

    public View.OnClickListener scannerEnableClickListener = view -> {
        SwitchMaterial switchView = (SwitchMaterial) view;
        if (switchView == null)
            return;
        if (switchView.isChecked()) {
            scanner.start();
        } else {
            scanner.stop();
        }
    };

    public View.OnClickListener warnerEnableClickListener = view -> {
        SwitchMaterial switchView = (SwitchMaterial) view;
        if (switchView == null)
            return;
        if (switchView.isChecked()) {
            warner.start();
        } else {
            warner.stop();
        }
    };
}
