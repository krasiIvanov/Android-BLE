package com.example.androidble.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidble.R;

import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {

    private List<BluetoothDevice> mDataSet;

    private static ItemClickListener callback;

    public DevicesAdapter(List<BluetoothDevice> dataSet, ItemClickListener callback) {

        mDataSet = dataSet;
        setCallback(callback);

    }

    private void setCallback(ItemClickListener callback) {
        DevicesAdapter.callback = callback;
    }

    @NonNull
    @Override
    public DevicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.devices_list_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesAdapter.ViewHolder holder, int position) {

        BluetoothDevice device = mDataSet.get(position);

        if(device.getName() == null){
            holder.name.setText("Unknown");
        }else{
            holder.name.setText(device.getName());
        }
        holder.address.setText(device.getAddress());
        int servicesCount = 0;
        if(device.getUuids() != null){
            servicesCount = device.getUuids().length;
        }

        holder.services.setText("Services : " + servicesCount);

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout row;
        TextView name;
        TextView address;
        TextView services;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            row = itemView.findViewById(R.id.device_row);
            name = itemView.findViewById(R.id.device_name);
            address = itemView.findViewById(R.id.device_address);
            services = itemView.findViewById(R.id.device_services);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(callback != null){
                        callback.onItemClick(getAdapterPosition());
                    }
                }
            });

        }
    }

    public void updateDataSet(List<BluetoothDevice> devices){
        this.mDataSet = devices;
        notifyDataSetChanged();
    }

    public interface ItemClickListener{

        void onItemClick(int position);

    }

}
