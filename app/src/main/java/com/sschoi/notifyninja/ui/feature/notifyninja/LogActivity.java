package com.sschoi.notifyninja.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;  // ✅ 추가!

import com.sschoi.notifyninja.R;
import com.sschoi.notifyninja.core.db.LogDBHelper;
import com.sschoi.notifyninja.core.model.NotifyLog;
import com.sschoi.notifyninja.ui.feature.notifyninja.adapter.LogAdapter;

import java.util.List;

public class LogActivity extends AppCompatActivity {
    private RecyclerView recyclerLogs;
    private TextView tvEmpty, tvLogGuide;
    private SwipeRefreshLayout swipeRefresh;
    private LogAdapter adapter;
    private LogDBHelper logDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_notify_log);

        recyclerLogs = findViewById(R.id.recyclerLogs);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvLogGuide = findViewById(R.id.tvLogGuide);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        recyclerLogs.setLayoutManager(new LinearLayoutManager(this));
        logDb = new LogDBHelper(this);

        loadLogs();

        // ✅ 스와이프 새로고침 동작
        swipeRefresh.setOnRefreshListener(() -> {
            loadLogs();
            swipeRefresh.setRefreshing(false); // 로딩 애니메이션 끄기
        });
    }

    private void loadLogs() {
        List<NotifyLog> logs = logDb.getAllLogs();

        if (adapter == null) {
            adapter = new LogAdapter(logs);
            recyclerLogs.setAdapter(adapter);
        } else {
            adapter.setItems(logs);
        }

        if (logs == null || logs.isEmpty()) {
            recyclerLogs.setVisibility(View.GONE);
            tvLogGuide.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerLogs.setVisibility(View.VISIBLE);
            tvLogGuide.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
