package com.sschoi.notifyninja.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sschoi.notifyninja.R;
import com.sschoi.notifyninja.core.model.AppModel;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.VH> {

    public interface OnDelete {
        void delete(String packageName);
    }

    private final OnDelete onDelete;
    private final List<AppModel> items = new ArrayList<>();

    public AppAdapter(List<AppModel> initial, OnDelete onDelete) {
        this.onDelete = onDelete;
        setItems(initial);
    }

    public void setItems(List<AppModel> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notify_app, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AppModel m = items.get(pos);
        h.tvAppName.setText(m.getAppName());
        h.tvPhone.setText(m.getPhone());
        h.btnDelete.setOnClickListener(v -> onDelete.delete(m.getPackageName()));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAppName, tvPhone;
        Button btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
