package com.sschoi.notifyninja.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.sschoi.notifyninja.R;
import com.sschoi.notifyninja.db.DBHelper;
import com.sschoi.notifyninja.model.AppModel;
import com.sschoi.notifyninja.service.MyNotificationListener;
import com.sschoi.notifyninja.ui.adapter.AppAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private DBHelper dbHelper;

    private MaterialButton btnRegister, btnNotifAccess, btnRefresh, btnLog;

    private final ActivityResultLauncher<String> smsPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this, "문자 권한이 없으면 SMS 전송이 불가합니다.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        // 버튼 초기화
        btnRegister = findViewById(R.id.btnRegister);
        btnNotifAccess = findViewById(R.id.btnNotifAccess);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnLog = findViewById(R.id.btnLog);

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new AppAdapter(dbHelper.getAllApps(), pkg -> {
            dbHelper.deleteApp(pkg);
            loadData();
        });
        recyclerView.setAdapter(adapter);

        // 버튼 클릭 이벤트
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class)));

        btnNotifAccess.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));

        btnLog.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LogActivity.class)));

        btnRefresh.setOnClickListener(v -> loadData());

        // 권한 요청
        requestSmsPermissionIfNeeded();

        // Notification Listener 버튼 초기 상태
        updateNotifAccessButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        // Notification Listener 권한 상태 확인 후 버튼 숨김/표시
        updateNotifAccessButton();
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

    private void updateNotifAccessButton() {
        if (isNotificationServiceEnabled(this)) {
            btnNotifAccess.setVisibility(View.GONE);
        } else {
            btnNotifAccess.setVisibility(View.VISIBLE);
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
