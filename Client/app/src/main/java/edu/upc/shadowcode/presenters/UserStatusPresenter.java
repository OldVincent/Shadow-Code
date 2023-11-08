package edu.upc.shadowcode.presenters;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.Presenter;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.databinding.FragmentIndexBinding;
import edu.upc.shadowcode.models.UserModel;

public class UserStatusPresenter extends Presenter<FragmentIndexBinding> {
    private static SoftReference<UserStatusPresenter> singletonInstance = null;
    @NotNull
    public static UserStatusPresenter get() {
        if (singletonInstance == null || singletonInstance.get() == null) {
            singletonInstance = new SoftReference<>(new UserStatusPresenter());
        }
        return singletonInstance.get();
    }

    public UserModel user = UserModel.get();

    public ObservableField<String> statusTitle = new ObservableField<>();
    public ObservableField<String> statusDescription = new ObservableField<>();
    public ObservableField<Drawable> statusBackground = new ObservableField<>();

    private final Observable.OnPropertyChangedCallback riskListener = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            updateRiskView();
        }
    };

    private UserStatusPresenter() {
    }

    public void updateRiskView() {
        int titleId = 0;
        int descriptionId = 0;
        int colorId = 0;
        switch (UserModel.get().risk.get()) {
            case Unknown:
                titleId = R.string.risk_unknown;
                descriptionId = R.string.risk_unknown_description;
                colorId = R.color.risk_unknown;
                break;
            case Low:
                titleId = R.string.risk_low;
                descriptionId = R.string.risk_low_description;
                colorId = R.color.risk_low;
                break;
            case Medium:
                titleId = R.string.risk_medium;
                descriptionId = R.string.risk_medium_description;
                colorId = R.color.risk_medium;
                break;
            case High:
                titleId = R.string.risk_high;
                descriptionId = R.string.risk_high_description;
                colorId = R.color.risk_high;
                break;
            case Danger:
                titleId = R.string.risk_danger;
                descriptionId = R.string.risk_danger_description;
                colorId = R.color.risk_danger;
                break;
        }
        statusTitle.set(Controller.getContext().getString(titleId));
        statusDescription.set(Controller.getContext().getString(descriptionId));
        statusBackground.set(new ColorDrawable(Controller.getContext().getColor(colorId)));
    }

    protected void onAttach(){
        getBinding().setUserStatusPresenter(this);
        statusTitle.set(Controller.getContext().getString(R.string.risk_unknown));
        statusDescription.set(Controller.getContext().getString(R.string.risk_unknown_description));
        statusBackground.set(new ColorDrawable(Controller.getContext().getColor(R.color.risk_unknown)));
        UserModel.get().risk.addOnPropertyChangedCallback(riskListener);
        updateRiskView();
    }

    protected void onDetach(){
        UserModel.get().risk.removeOnPropertyChangedCallback(riskListener);
    }
}
