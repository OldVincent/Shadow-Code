package edu.upc.shadowcode.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.upc.shadowcode.databinding.FragmentScannerBinding;
import edu.upc.shadowcode.presenters.ScannerPresenter;

public class ScannerFragment extends Fragment {

    public ScannerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentScannerBinding binding = FragmentScannerBinding.inflate(inflater);

        ScannerPresenter.get().attach(binding);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ScannerPresenter.get().detach();
    }
}