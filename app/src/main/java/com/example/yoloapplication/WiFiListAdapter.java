package com.example.yoloapplication;

/**
 * Created by Viram on 5/16/2019.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class WiFiListAdapter extends RecyclerView.Adapter<WifiViewHolder> {

    private Context context;
    private List<ScanResult> wiFiListScanResult;

    private OnItemClicked onClick;

    public String SSIDname;
    public String newSSID;

    public WiFiListAdapter(Context context, List<ScanResult> wiFiListScanResult) {
        this.context = context;
        this.wiFiListScanResult = wiFiListScanResult;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemCount() {
        return wiFiListScanResult.size();
    }

    @NonNull
    @Override
    public WifiViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.accesspoint_item, viewGroup, false);

        return new WifiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WifiViewHolder wifiViewHolder, @SuppressLint("RecyclerView") final int position) {

        wifiViewHolder.tvDetails.setText(wiFiListScanResult.get(position).SSID);

        wifiViewHolder.tvDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemClick(position);
            }
        });

        SSIDname = wiFiListScanResult.get(position).SSID;

        String[] s0 = SSIDname.split(",");
        Log.d("S0",""+s0[0]);

        newSSID = s0[0];
    }

    public interface OnItemClicked {
        void onItemClick(int position);
    }

    void setOnClick(OnItemClicked onClick) {
        this.onClick = onClick;
    }



//    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//
//            int itemPosition = mRecyclerView.getChildLayoutPosition(view);
//            String item = mList.get(itemPosition);
//            Toast.makeText(mContext, item, Toast.LENGTH_LONG).show();
//
//        }
//    };
}
