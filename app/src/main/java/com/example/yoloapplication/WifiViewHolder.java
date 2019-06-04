package com.example.yoloapplication;

/**
 * Created by Viram on 5/16/2019.
 */
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class WifiViewHolder extends RecyclerView.ViewHolder {

    public TextView tvDetails;

    public WifiViewHolder(@NonNull View itemView) {
        super(itemView);

        tvDetails = itemView.findViewById(R.id.apItem_name);
    }

}
