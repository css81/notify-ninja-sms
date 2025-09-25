package com.sschoi.notifyninja.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.sschoi.notifyninja.BuildConfig;
import com.sschoi.notifyninja.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 버전 가져와서 표시
        TextView tvVersion = findViewById(R.id.tvVersion);
        TextView tvDeveloper = findViewById(R.id.tvDeveloper);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;  // AndroidManifest의 versionName
            tvVersion.setText("v" + version);
            // Gradle BuildConfig에서 DEVELOPER_NAME 가져오기
            tvDeveloper.setText("by " + BuildConfig.DEVELOPER_NAME);

        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("v1.0");
            tvDeveloper.setText("by 최석순");
        }

        // 2초 후 MainActivity 실행
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2000);
    }
}
