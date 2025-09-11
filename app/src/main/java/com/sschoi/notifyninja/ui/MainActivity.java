package com.sschoi.notifyninja.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sschoi.notifyninja.R;
import com.sschoi.notifyninja.ui.fragment.CallNinjaFragment;
import com.sschoi.notifyninja.ui.fragment.MuntokFragment;
import com.sschoi.notifyninja.ui.fragment.NotifyNinjaFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // 기본 화면: NotifyNinja
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new NotifyNinjaFragment())
                .commit();

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_notify_ninja) {
                selectedFragment = new NotifyNinjaFragment();
            } else if (itemId == R.id.nav_call_ninja) {
                selectedFragment = new CallNinjaFragment();
            } else if (itemId == R.id.nav_muntok) {
                selectedFragment = new MuntokFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }

            return true;
        });

    }
}
