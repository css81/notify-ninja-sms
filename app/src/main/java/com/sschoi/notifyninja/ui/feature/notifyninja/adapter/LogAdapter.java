package com.sschoi.notifyninja.ui.feature.notifyninja.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sschoi.notifyninja.R;
import com.sschoi.notifyninja.core.model.NotifyLog;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private List<NotifyLog> logList;

    public LogAdapter(List<NotifyLog> logList) {
        this.logList = logList;
    }

    public void setItems(List<NotifyLog> logs) {
        this.logList = logs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notify_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        NotifyLog log = logList.get(position);

        holder.tvTime.setText(log.getTime());
        holder.tvTarget.setText(log.getTarget());
        holder.tvTitle.setText(log.getTitle());

        // 상태 색상 표시
        if ("SENT".equalsIgnoreCase(log.getStatus())) {
            holder.tvStatus.setText("성공");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // 초록
        } else {
            holder.tvStatus.setText("실패");
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // 빨강
        }
    }

    @Override
    public int getItemCount() {
        return logList != null ? logList.size() : 0;
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTarget, tvTitle, tvStatus;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTarget = itemView.findViewById(R.id.tvTarget);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
