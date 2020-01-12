package com.antitheft.alarm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.antitheft.alarm.R;
import com.inuker.bluetooth.library.search.SearchResult;

import java.util.List;

public class BluetoothDevicesAdapter extends RecyclerView.Adapter<BluetoothDevicesAdapter.DeviceViewHolder> {

    private Context context;
    private List<SearchResult> devices;
    private OnItemViewClickListener itemViewClickListener;

    public BluetoothDevicesAdapter(Context context, List<SearchResult> list) {
        this.context = context;
        devices = list;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bt_device_item_layout, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.btName.setText(devices.get(position).getName());
        holder.btMac.setText(devices.get(position).getAddress());
        if (itemViewClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemViewClickListener.onItemClick(position);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void setOnItemViewClickListener(OnItemViewClickListener itemViewClickListener) {
        this.itemViewClickListener = itemViewClickListener;
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private TextView btName;
        public TextView btMac;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            btName = itemView.findViewById(R.id.bt_name);
            btMac = itemView.findViewById(R.id.bt_mac);
        }
    }

    public interface OnItemViewClickListener {
        void onItemClick(int position);
    }
}
