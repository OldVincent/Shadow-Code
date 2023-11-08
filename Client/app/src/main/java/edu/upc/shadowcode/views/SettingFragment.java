package edu.upc.shadowcode.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.upc.shadowcode.databinding.FragmentSettingBinding;
import edu.upc.shadowcode.models.DeviceModel;
import edu.upc.shadowcode.presenters.DescriptionPresenter;
import edu.upc.shadowcode.presenters.UserInformationPresenter;

public class SettingFragment extends Fragment {

    public SettingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSettingBinding binding = FragmentSettingBinding.inflate(inflater);

        UserInformationPresenter.get().attach(binding);
        DescriptionPresenter.get().attach(binding);
        binding.setDevice(DeviceModel.get());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        UserInformationPresenter.get().detach();
        DescriptionPresenter.get().detach();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}