package com.sschoi.notifyninja.ui;

import android.Manifest;
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
    private TextView tvNotice;

    private final ActivityResultLauncher<String[]> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    checkNotificationPermission();
                } else {
                    Toast.makeText(this,
                            "필수 권한이 허용되지 않으면 기능을 사용할 수 없습니다.",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_consent);

        tvNotice = findViewById(R.id.tvNotice);
        tvNotice.setText("NotifyNinja는 다음 정보를 사용합니다:\n\n" +
                "- SMS: 알림 메시지 전달\n" +
                "- 연락처: 알림 수신 번호 저장\n" +
                "- 알림 접근: 수신 알림 감지 및 SMS 포워딩\n\n" +
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
        // 뒤로 돌아왔을 때 자동 체크
        checkPermissionsAndProceed();
    }

    private void checkPermissionsAndProceed() {
        if (areSmsPermissionsGranted() && isNotificationServiceEnabled()) {
            // 모든 권한 허용 + Notification Listener 활성화 상태면 바로 다음 화면
            startMainActivity();
        }
        // 안되면 버튼 눌러서 처리
    }

    private void requestSmsPermissions() {
        String[] perms = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
        };

        if (areSmsPermissionsGranted()) {
            checkNotificationPermission();
        } else {
            smsPermissionLauncher.launch(perms);
        }
    }

    private boolean areSmsPermissionsGranted() {
        String[] perms = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
        };

        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkNotificationPermission() {
        if (isNotificationServiceEnabled()) {
            // Notification Listener 권한 활성화면 다음 화면
            startMainActivity();
        } else {
            Toast.makeText(this,
                    "알림 접근 권한이 필요합니다.\n다음 화면에서 설정 후 뒤로 돌아와주세요.",
                    Toast.LENGTH_LONG).show();

            // 설정 화면으로 이동
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        return flat != null && flat.contains(pkgName);
    }

    private void startMainActivity() {
        startActivity(new Intent(this, com.sschoi.notifyninja.ui.SplashActivity.class));
        finish();
    }
}
