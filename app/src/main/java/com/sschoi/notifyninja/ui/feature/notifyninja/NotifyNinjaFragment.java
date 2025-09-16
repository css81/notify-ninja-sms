package com.sschoi.notifyninja.ui.feature.notifyninja;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.sschoi.notifyninja.R;
import com.sschoi.notifyninja.core.db.DBHelper;
import com.sschoi.notifyninja.core.model.AppModel;
import com.sschoi.notifyninja.ui.feature.notifyninja.RegisterFragment;
import com.sschoi.notifyninja.ui.adapter.AppAdapter;

import java.util.List;

public class NotifyNinjaFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private DBHelper dbHelper;

    private MaterialButton btnRegister, btnNotifAccess, btnRefresh, btnLog;

    private final ActivityResultLauncher<String> smsPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(getContext(), "문자 권한이 없으면 SMS 전송이 불가합니다.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // fragment_notify_ninja.xml 사용
        return inflater.inflate(R.layout.fragment_notify_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DBHelper(getContext());

        // 버튼 초기화
        btnRegister = view.findViewById(R.id.btnRegister);
        btnNotifAccess = view.findViewById(R.id.btnNotifAccess);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        btnLog = view.findViewById(R.id.btnLog);

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        adapter = new AppAdapter(dbHelper.getAllApps(), pkg -> {
            dbHelper.deleteApp(pkg);
            loadData();
        });
        recyclerView.setAdapter(adapter);

        // 버튼 클릭 이벤트
        btnRegister.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new RegisterFragment()) // MainActivity에 있는 FrameLayout id
                    .addToBackStack(null) // 뒤로가기 시 NotifyNinjaFragment로 돌아올 수 있게
                    .commit();
        });


        btnNotifAccess.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));

        btnLog.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new com.sschoi.notifyninja.ui.feature.notifyninja.LogFragment())
                    .addToBackStack(null)
                    .commit();
        });


        btnRefresh.setOnClickListener(v -> loadData());

        // 권한 요청
        requestSmsPermissionIfNeeded();

        // Notification Listener 버튼 초기 상태
        updateNotifAccessButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        updateNotifAccessButton();
    }

    private void loadData() {
        List<AppModel> all = dbHelper.getAllApps();
        adapter.setItems(all);
    }

    private void requestSmsPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            smsPermLauncher.launch(Manifest.permission.SEND_SMS);
        }
    }

    private void updateNotifAccessButton() {
        if (isNotificationServiceEnabled(getContext())) {
            btnNotifAccess.setVisibility(View.GONE);
        } else {
            btnNotifAccess.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNotificationServiceEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(
                context.getContentResolver(),
                "enabled_notification_listeners"
        );
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final android.content.ComponentName cn = android.content.ComponentName.unflattenFromString(name);
                if (cn != null && TextUtils.equals(pkgName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
