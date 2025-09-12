package com.sschoi.notifyninja.feature.notifyninja;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sschoi.notifyninja.R;
import com.sschoi.notifyninja.core.db.LogDBHelper;
import com.sschoi.notifyninja.ui.feature.notifyninja.model.Log;
import com.sschoi.notifyninja.ui.feature.notifyninja.adapter.LogAdapter;

import java.util.List;

public class LogFragment extends Fragment {
    private RecyclerView recyclerLogs;
    private TextView tvEmpty, tvLogGuide;
    private SwipeRefreshLayout swipeRefresh;
    private LogAdapter adapter;
    private LogDBHelper logDb;

    public LogFragment() {
        // 필수 기본 생성자
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notify_log, container, false);

        recyclerLogs = view.findViewById(R.id.recyclerLogs);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvLogGuide = view.findViewById(R.id.tvLogGuide);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        recyclerLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        logDb = new LogDBHelper(requireContext());

        loadLogs();

        swipeRefresh.setOnRefreshListener(() -> {
            loadLogs();
            swipeRefresh.setRefreshing(false);
        });

        return view;
    }

    private void loadLogs() {
        List<Log> logs = logDb.getAllLogs();

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
