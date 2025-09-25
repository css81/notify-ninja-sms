package com.sschoi.notifyninja.ui;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private LinearLayout layoutSenderNumber;
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
        etSenderNumber = findViewById(R.id.etSenderNumber);
        layoutSenderNumber = findViewById(R.id.layoutSenderNumber);
        btnSave = findViewById(R.id.btnSave);

        dbHelper = new DBHelper(this);

        loadLaunchableApps();
        setupSpinner();

        btnSave.setOnClickListener(v -> onSave());
    }

    private void loadLaunchableApps() {
        try {
            // 홈런처에 노출되는 '실행 가능한' 앱만 로드 → 카톡/은행앱 대부분 포함
            Intent main = new Intent(Intent.ACTION_MAIN, null);
            main.addCategory(Intent.CATEGORY_LAUNCHER);
            PackageManager pm = getPackageManager();
            List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);

            launchableApps.clear();
            for (ResolveInfo info : infos) {
                try {
                    String pkg = info.activityInfo.packageName;
                    String label = info.loadLabel(pm).toString();
                    // 필요시 시스템앱 제외 로직 추가 가능
                    launchableApps.add(new AppModel(pkg, label, null));
                } catch (Exception e) {
                    // 개별 앱 정보 로딩 실패 시 무시하고 계속 진행
                    continue;
                }
            }
            // 이름순 정렬(선택)
            launchableApps.sort((a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));
        } catch (Exception e) {
            // 앱 로딩 실패 시 빈 리스트로 초기화
            launchableApps.clear();
            Toast.makeText(this, "앱 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
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
                if (position >= 0 && position < launchableApps.size()) {
                    selectedApp = launchableApps.get(position);
                    tvSelectedApp.setText("선택 앱: " + selectedApp.getAppName());
                    
                    // SMS/메시지 앱인지 확인하여 번호 필터 레이아웃 표시/숨김
                    if (isSMSApp(selectedApp.getPackageName())) {
                        layoutSenderNumber.setVisibility(android.view.View.VISIBLE);
                    } else {
                        layoutSenderNumber.setVisibility(android.view.View.GONE);
                        etSenderNumber.setText(""); // 숨길 때 입력값 초기화
                    }
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedApp = null;
                tvSelectedApp.setText("선택 앱: 없음");
                layoutSenderNumber.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void onSave() {
        try {
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
                // 등록 성공 후 액티비티 종료
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "이미 등록된 앱입니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * SMS/메시지 앱인지 확인
     */
    private boolean isSMSApp(String packageName) {
        // 주요 SMS/메시지 앱 패키지명들
        return "com.android.mms".equals(packageName) ||
               "com.samsung.android.messaging".equals(packageName) ||
               "com.google.android.apps.messaging".equals(packageName) ||
               "com.android.messaging".equals(packageName) ||
               "com.samsung.android.messaging".equals(packageName) ||
               packageName.toLowerCase().contains("sms") ||
               packageName.toLowerCase().contains("message");
    }
}
