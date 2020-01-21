package com.example.androidble.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidble.R;
import com.example.androidble.models.CharacteristicViewModel;
import com.example.androidble.models.ServiceViewModel;
import com.example.androidble.utils.OnItemClick;

import java.util.List;

public class DeviceDataAdapter extends RecyclerView.Adapter<DeviceDataAdapter.ViewHolder> {

    private List<ServiceViewModel> dataSet;
    private static OnItemClick callback;
    public DeviceDataAdapter(List<ServiceViewModel> dataSet, OnItemClick callback) {

        this.dataSet = dataSet;
        setCallback(callback);

    }

    private void setCallback(OnItemClick callback) {

        DeviceDataAdapter.callback = callback;

    }

    @NonNull
    @Override
    public DeviceDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_view_model_row,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceDataAdapter.ViewHolder holder, int position) {

        holder.serviceName.setText(dataSet.get(position).getName());
        holder.serviceUuid.setText(dataSet.get(position).getUuid());

        StringBuilder builder = new StringBuilder();
        for (CharacteristicViewModel model:dataSet.get(position).getCharacteristics()) {

            builder.append(model.getName());
            builder.append("\n");

        }

        holder.serviceCharacteristics.setText(builder.toString());

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout row;
        TextView serviceName;
        TextView serviceUuid;
        TextView serviceCharacteristics;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceName = itemView.findViewById(R.id.service_name);
            serviceUuid = itemView.findViewById(R.id.service_uuid);
            serviceCharacteristics = itemView.findViewById(R.id.service_characteristics);
            row = itemView.findViewById(R.id.service_view_row);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(callback != null){

                        callback.onItemClicked(getAdapterPosition());

                    }
                }
            });

        }
    }

    public void updateDataSet(List<ServiceViewModel>services){
        this.dataSet = services;
        notifyDataSetChanged();
    }
}
