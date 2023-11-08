package edu.upc.shadowcode.views;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import edu.upc.shadowcode.Controller;
import edu.upc.shadowcode.R;
import edu.upc.shadowcode.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static MainActivity currentInstance;

    // 获取当前的主视图
    public static MainActivity get(){
        return currentInstance;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        currentInstance = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentInstance = this;
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentContainer);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }
}