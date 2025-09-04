package com.sschoi.notifyninja;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private DBHelper dbHelper;

    private Button btnRegister, btnNotifAccess, btnRefresh;

    private final ActivityResultLauncher<String> smsPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this, "문자 권한이 없으면 SMS 전송이 불가합니다.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        btnRegister = findViewById(R.id.btnRegister);
        btnNotifAccess = findViewById(R.id.btnNotifAccess);
        btnRefresh = findViewById(R.id.btnRefresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppAdapter(dbHelper.getAllApps(), pkg -> {
            dbHelper.deleteApp(pkg);
            loadData();
        });
        recyclerView.setAdapter(adapter);

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class)));

        btnNotifAccess.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));

        btnRefresh.setOnClickListener(v -> loadData());

        requestSmsPermissionIfNeeded();

        Button btnNotifAccess = findViewById(R.id.btnNotifAccess);

        if (isNotificationServiceEnabled(this)) {
            // 이미 권한 허용 → 버튼 숨기기
            btnNotifAccess.setVisibility(View.GONE);
        } else {
            // 권한이 없으면 버튼 클릭 시 설정 화면으로 이동
            btnNotifAccess.setOnClickListener(v -> {
                startActivity(new Intent(
                        Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                ));
            });
        }
        ComponentName cn = new ComponentName(this, MyNotificationListener.class);
        if (NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {
            Log.d("Main", "Notification Listener 권한 허용됨");
        } else {
            Log.d("Main", "Notification Listener 권한 없음");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<AppModel> all = dbHelper.getAllApps();
        adapter.setItems(all);
    }

    private void requestSmsPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            smsPermLauncher.launch(Manifest.permission.SEND_SMS);
        }
    }

    public boolean isNotificationServiceEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(
                context.getContentResolver(),
                "enabled_notification_listeners"
        );
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && TextUtils.equals(pkgName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
