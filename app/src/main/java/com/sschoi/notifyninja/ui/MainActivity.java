package com.sschoi.notifyninja.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sschoi.notifyninja.R;
import com.sschoi.notifyninja.ui.feature.callninja.CallNinjaFragment;
import com.sschoi.notifyninja.ui.feature.muntok.MuntokFragment;
import com.sschoi.notifyninja.ui.feature.notifyninja.NotifyNinjaFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // 기본 화면: NotifyNinja
        setFragment(new NotifyNinjaFragment(), "노티닌자");

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_notify_ninja) {
                setFragment(new NotifyNinjaFragment(), "노티닌자");
            } else if (itemId == R.id.nav_call_ninja) {
                setFragment(new CallNinjaFragment(), "콜닌자");
            } else if (itemId == R.id.nav_muntok) {
                setFragment(new MuntokFragment(), "문톡");
            }
            return true;
        });
    }

    /**
     * Fragment 교체 및 액션바 타이틀 변경
     */
    private void setFragment(Fragment fragment, String title) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}
