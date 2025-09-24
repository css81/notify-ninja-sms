package com.sschoi.notifyninja.ui;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sschoi.notifyninja.R;
import com.sschoi.notifyninja.db.DBHelper;
import com.sschoi.notifyninja.model.AppModel;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private Spinner spinnerApps;
    private TextView tvSelectedApp;
    private EditText etPhone;
    private EditText etSenderName;
    private EditText etSenderNumber;
    private Button btnSave;

    private final List<AppModel> launchableApps = new ArrayList<>();
    private AppModel selectedApp;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        spinnerApps = findViewById(R.id.spinnerApps);
        tvSelectedApp = findViewById(R.id.tvSelectedApp);
        etPhone = findViewById(R.id.etPhone);
        etSenderName = findViewById(R.id.etSenderName);
        btnSave = findViewById(R.id.btnSave);

        dbHelper = new DBHelper(this);

        loadLaunchableApps();
        setupSpinner();

        btnSave.setOnClickListener(v -> onSave());
    }

    private void loadLaunchableApps() {
        // 홈런처에 노출되는 '실행 가능한' 앱만 로드 → 카톡/은행앱 대부분 포함
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);

        launchableApps.clear();
        for (ResolveInfo info : infos) {
            String pkg = info.activityInfo.packageName;
            String label = info.loadLabel(pm).toString();
            // 필요시 시스템앱 제외 로직 추가 가능
            launchableApps.add(new AppModel(pkg, label, null));
        }
        // 이름순 정렬(선택)
        launchableApps.sort((a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));
    }

    private void setupSpinner() {
        List<String> names = new ArrayList<>();
        for (AppModel m : launchableApps) names.add(m.getAppName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApps.setAdapter(adapter);

        spinnerApps.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedApp = launchableApps.get(position);
                tvSelectedApp.setText("선택 앱: " + selectedApp.getAppName());
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedApp = null;
                tvSelectedApp.setText("선택 앱: 없음");
            }
        });
    }

    private void onSave() {
        if (selectedApp == null) {
            Toast.makeText(this, "앱을 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(this, "받는 번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        String senderName = etSenderName.getText().toString().trim();
        String senderNumber = etSenderNumber.getText().toString().trim();

        boolean ok = dbHelper.insertApp(selectedApp.getPackageName(), selectedApp.getAppName(), phone, senderName.isEmpty() ? null : senderName, senderNumber.isEmpty() ? null : senderNumber);
        if (ok) {
            Toast.makeText(this, "등록 완료", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "이미 등록된 앱입니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
