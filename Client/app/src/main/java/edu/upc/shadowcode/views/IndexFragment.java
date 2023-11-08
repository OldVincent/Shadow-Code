package edu.upc.shadowcode.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableList;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.upc.shadowcode.R;
import edu.upc.shadowcode.databinding.FragmentIndexBinding;
import edu.upc.shadowcode.models.DeviceModel;
import edu.upc.shadowcode.presenters.ControlPanelPresenter;
import edu.upc.shadowcode.presenters.UserStatusPresenter;

public class IndexFragment extends Fragment {

    public IndexFragment() {
    }

    private FragmentIndexBinding binding;

    private ObservableList.OnListChangedCallback bindChangingListener(RecentRiskExposureAdapter adapter){
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
    private ObservableList.OnListChangedCallback recentRiskExposuresWatcher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentIndexBinding.inflate(inflater);

        ControlPanelPresenter.get().attach(binding);
        UserStatusPresenter.get().attach(binding);

        RecentRiskExposureAdapter adapter = new RecentRiskExposureAdapter(DeviceModel.get().cachedRiskExposures);
        recentRiskExposuresWatcher = bindChangingListener(adapter);
        binding.listRiskyEncounter.setAdapter(adapter);

        DeviceModel.get().cachedRiskExposures.addOnListChangedCallback(recentRiskExposuresWatcher);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ControlPanelPresenter.get().detach();
        UserStatusPresenter.get().detach();

        if (recentRiskExposuresWatcher != null) {
            DeviceModel.get().cachedRiskExposures.removeOnListChangedCallback(recentRiskExposuresWatcher);
        }
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.listRiskyEncounter.setItemAnimator(new DefaultItemAnimator());
        SwipeRefreshLayout listRiskyEncounterRefresher =
                view.findViewById(R.id.listRiskyEncounterRefresher);
        listRiskyEncounterRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listRiskyEncounterRefresher.setRefreshing(false);
                DeviceModel.get().update();
            }

        });
    }
}