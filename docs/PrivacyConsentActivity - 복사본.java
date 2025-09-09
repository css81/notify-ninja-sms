package com.sschoi.notifyninja.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sschoi.notifyninja.R;

public class PrivacyConsentActivity extends AppCompatActivity {

    private Button btnAgree, btnDisagree;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // 권한 요청 후 다시 확인
                checkPermissions();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_consent);

        TextView tvNotice = findViewById(R.id.tvNotice);
        tvNotice.setText("NotifyNinja는 다음 정보를 사용합니다:\n\n" +
                "- SMS: 알림 메시지 전달\n" +
                "- 연락처: 알림 수신 번호 저장\n\n" +
                "사용자 동의 후 기능이 활성화됩니다.");

        btnAgree = findViewById(R.id.btnAgree);
        btnDisagree = findViewById(R.id.btnDisagree);

        btnAgree.setOnClickListener(v -> requestSmsPermissions());
        btnDisagree.setOnClickListener(v -> {
            Toast.makeText(this, "동의가 필요하여 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 뒤로 돌아왔을 때도 권한 체크
        checkPermissions();
    }

    private void requestSmsPermissions() {
        String[] perms = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
        };

        boolean allGranted = true;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            checkNotificationListenerPermission();
        } else {
            permissionLauncher.launch(perms);
        }
    }

    private void checkPermissions() {
        // SMS 권한 확인
        boolean smsGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;

        if (!smsGranted) return;

        // Notification Listener 권한 확인
        checkNotificationListenerPermission();
    }

    private void checkNotificationListenerPermission() {
        if (!isNotificationListenerEnabled(this)) {
            Toast.makeText(this, "알림 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } else {
            startSplashActivity();
        }
    }

    private void startSplashActivity() {
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    // Notification Listener 권한 체크
    public static boolean isNotificationListenerEnabled(Context context) {
        String pkgName = context.getPackageName();
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(pkgName);
    }
}
