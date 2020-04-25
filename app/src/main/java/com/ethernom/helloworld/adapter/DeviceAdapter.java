package com.ethernom.helloworld.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ethernom.helloworld.R;
import com.ethernom.helloworld.model.BleClient;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter  {
    private ArrayList<BleClient> bleClient;
    private OnItemCallback callback ;
    public DeviceAdapter(ArrayList<BleClient> bleClient, OnItemCallback callback) {
        this.bleClient = bleClient;
        this.callback = callback;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.btle_device_list_item, parent, false);
        return new MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        TextView dev_name =  holder.itemView.findViewById(R.id.tv_name);
        TextView macAdd =  holder.itemView.findViewById(R.id.tv_uuidscan);
        BleClient ble_client = bleClient.get(position);
        dev_name.setText(ble_client.getDevName());
        macAdd.setText(ble_client.getDeviceSN());
        holder.itemView.setOnClickListener(view -> callback.ItemClickListener(position));
    }
    @Override
    public int getItemCount() {
        return bleClient.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        MyViewHolder(View itemView) {
            super(itemView);
        }
    }
    public interface OnItemCallback  {
        void ItemClickListener(int position);
    }
}

