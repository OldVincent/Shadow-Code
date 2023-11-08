package edu.upc.shadowcode.presenters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.databinding.Observable;
import androidx.databinding.adapters.TextViewBindingAdapter;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.Objects;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.Presenter;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.models.UserModel;
import edu.upc.shadowcode.databinding.FragmentSettingBinding;
import edu.upc.shadowcode.modules.MessageModule;

public class UserInformationPresenter extends Presenter<FragmentSettingBinding> {
    private static SoftReference<UserInformationPresenter> singletonInstance = null;
    @NotNull
    public static UserInformationPresenter get() {
        if (singletonInstance == null || singletonInstance.get() == null) {
            singletonInstance = new SoftReference<>(new UserInformationPresenter());
        }
        return singletonInstance.get();
    }

    public UserModel user = UserModel.get();

    private UserInformationPresenter(){
    }

    protected void onAttach(){
        getBinding().setUserInformationPresenter(this);

        user.isLoggedIn.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                updateUserStatusVisibility();
            }
        });

        updateUserStatusVisibility();
    }

    protected void onDetach(){

    }

    @SuppressWarnings("ConstantConditions")
    private void updateUserStatusVisibility() {
        if (!user.isLoggedIn.get()) {
            getBinding().cardUserInformation.setVisibility(View.GONE);
            getBinding().cardUserLogin.setVisibility(View.VISIBLE);
            getBinding().textLayoutIdentity.setVisibility(View.GONE);
        } else {
            getBinding().cardUserInformation.setVisibility(View.VISIBLE);
            getBinding().cardUserLogin.setVisibility(View.GONE);
        }
    }

    public View.OnClickListener logoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            UserModel.LogoutResult result = user.logout();
            if (result == UserModel.LogoutResult.Success)
                return;
            Controller.getModule(MessageModule.class).showToast(
                    Controller.getContext().getString(R.string.failure_invalid_internet));
        }
    };

    public View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean ready = true;
            boolean register = getBinding().checkBoxRegister.isChecked();
            if (Objects.requireNonNull(getBinding().textFieldUser.getText()).toString().isEmpty()){
                ready = false;
                getBinding().textLayoutUserName.setError(
                        getBinding().getRoot().getContext().getString(R.string.text_requires_user_name));
            } else getBinding().textLayoutUserName.setErrorEnabled(false);
            if (Objects.requireNonNull(getBinding().textFieldPassword.getText()).toString().isEmpty()){
                ready = false;
                getBinding().textLayoutPassword.setError(
                        getBinding().getRoot().getContext().getString(R.string.text_requires_password));
            } else getBinding().textLayoutPassword.setErrorEnabled(false);
            if (register && Objects.requireNonNull(getBinding().textFieldIdentity.getText()).toString().isEmpty()){
                ready = false;
                getBinding().textLayoutIdentity.setError(
                        getBinding().getRoot().getContext().getString(R.string.text_requires_identity));
            } else getBinding().textLayoutIdentity.setErrorEnabled(false);
            if (!ready)
                return;

            if (!register) {
                // 进行用户登录
                UserModel.LoginResult result =
                        user.login(getBinding().textFieldUser.getText().toString(),
                        getBinding().textFieldPassword.getText().toString());
                if (result == UserModel.LoginResult.Success) {
                    getBinding().textFieldUser.setText("");
                    getBinding().textFieldPassword.setText("");
                    getBinding().textFieldIdentity.setText("");
                    getBinding().textFieldIdentity.setText("");
                } else if (result == UserModel.LoginResult.WrongUserName) {
                    getBinding().textLayoutUserName.setError(Controller.getContext().getString(R.string.failure_wrong_user_name));
                } else if (result == UserModel.LoginResult.WrongPassword) {
                    getBinding().textLayoutPassword.setError(Controller.getContext().getString(R.string.failure_wrong_user_password));
                } else {
                    Controller.getModule(MessageModule.class).showToast(
                            Controller.getContext().getString(R.string.failure_invalid_internet));
                }
            } else {
                // 进行用户注册
                UserModel.RegisterResult result = user.register(getBinding().textFieldUser.getText().toString(),
                        getBinding().textFieldPassword.getText().toString(),
                        getBinding().textFieldIdentity.getText().toString());
                if (result == UserModel.RegisterResult.InnerError) {
                    Controller.getModule(MessageModule.class).showToast(
                            Controller.getContext().getString(R.string.failure_invalid_internet));
                } else if (result == UserModel.RegisterResult.Conflict) {
                    getBinding().textLayoutUserName.setError(Controller.getContext().getString(R.string.failure_name_or_identity_conflict));
                    getBinding().textLayoutIdentity.setError(Controller.getContext().getString(R.string.failure_name_or_identity_conflict));
                }
            }
        }
    };

    public View.OnClickListener registerCheckBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckBox box = (CheckBox) view;
            if (box == null)
                return;
            if (box.isChecked()) {
                getBinding().buttonLogin.setText(
                        getBinding().getRoot().getContext().getString(R.string.button_submit));
                getBinding().textLayoutIdentity.setVisibility(View.VISIBLE);
            } else {
                getBinding().buttonLogin.setText(
                        getBinding().getRoot().getContext().getString(R.string.button_login));
                getBinding().textLayoutIdentity.setVisibility(View.GONE);
            }
        }
    };

}
