package com.sschoi.notifyninja.ui.feature.notifyninja;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sschoi.notifyninja.R;
import com.sschoi.notifyninja.core.db.DBHelper;
import com.sschoi.notifyninja.core.model.AppModel;

import java.util.ArrayList;
import java.util.List;

public class RegisterFragment extends Fragment {

    private Spinner spinnerApps;
    private TextView tvSelectedApp;
    private EditText etPhone;
    private Button btnSave;

    private final List<AppModel> launchableApps = new ArrayList<>();
    private AppModel selectedApp;
    private DBHelper dbHelper;

    public RegisterFragment() {
        // 빈 생성자 (Fragment 필수)
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notify_register, container, false);

        spinnerApps = view.findViewById(R.id.spinnerApps);
        tvSelectedApp = view.findViewById(R.id.tvSelectedApp);
        etPhone = view.findViewById(R.id.etPhone);
        btnSave = view.findViewById(R.id.btnSave);

        dbHelper = new DBHelper(requireContext());

        loadLaunchableApps();
        setupSpinner();

        btnSave.setOnClickListener(v -> onSave());

        return view;
    }

    private void loadLaunchableApps() {
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = requireContext().getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);

        launchableApps.clear();
        for (ResolveInfo info : infos) {
            String pkg = info.activityInfo.packageName;
            String label = info.loadLabel(pm).toString();
            launchableApps.add(new AppModel(pkg, label, null));
        }
        launchableApps.sort((a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));
    }

    private void setupSpinner() {
        List<String> names = new ArrayList<>();
        for (AppModel m : launchableApps) names.add(m.getAppName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                names
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApps.setAdapter(adapter);

        spinnerApps.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent,
                                       android.view.View view,
                                       int position,
                                       long id) {
                selectedApp = launchableApps.get(position);
                tvSelectedApp.setText("선택 앱: " + selectedApp.getAppName());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedApp = null;
                tvSelectedApp.setText("선택 앱: 없음");
            }
        });
    }

    private void onSave() {
        if (selectedApp == null) {
            Toast.makeText(requireContext(), "앱을 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), "받는 번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean ok = dbHelper.insertApp(
                selectedApp.getPackageName(),
                selectedApp.getAppName(),
                phone
        );
        if (ok) {
            Toast.makeText(requireContext(), "등록 완료", Toast.LENGTH_SHORT).show();
            // Fragment 닫기 → 뒤로가기
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(requireContext(), "이미 등록된 앱입니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
