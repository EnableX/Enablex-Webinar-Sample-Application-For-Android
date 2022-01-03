package com.enablex.webinar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.enablex.webinar.R;
import com.enablex.webinar.model.FloorRequestModel;

import java.util.ArrayList;

public class FloorRequestDialogAdapter extends RecyclerView.Adapter<FloorRequestDialogAdapter.FloorRequestViewHolder> {
    RequestItemClickListener clickListener;
    Context context;
    ArrayList<FloorRequestModel> floorRequestArrayList;

    public FloorRequestDialogAdapter(RequestItemClickListener clickListener, Context context, ArrayList<FloorRequestModel> floorRequestArrayList) {
        this.clickListener = clickListener;
        this.context = context;
        this.floorRequestArrayList = floorRequestArrayList;
    }

    @NonNull
    @Override
    public FloorRequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.floor_request_item, null);
        return new FloorRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorRequestViewHolder floorRequestViewHolder, int i) {
        floorRequestViewHolder.requestTV.setText(checkNullValue(floorRequestArrayList.get(i).getClientName()));
        if (floorRequestArrayList.get(i).isRequestAccepted()) {
            floorRequestViewHolder.acceptTV.setVisibility(View.VISIBLE);
        } else {
            floorRequestViewHolder.acceptTV.setVisibility(View.GONE);
        }
        if (floorRequestArrayList.get(i).isRequestRejected()) {
            floorRequestViewHolder.rejectTV.setVisibility(View.VISIBLE);
        } else {
            floorRequestViewHolder.rejectTV.setVisibility(View.GONE);
        }
        if (!floorRequestArrayList.get(i).isRequestReleased()) {
            floorRequestViewHolder.revokeTV.setVisibility(View.GONE);
        } else {
            floorRequestViewHolder.revokeTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return floorRequestArrayList.size();
    }

    public class FloorRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView requestTV;
        TextView acceptTV;
        TextView rejectTV;
        TextView revokeTV;
        RelativeLayout request_list_item;

        public FloorRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requestTV = (TextView) itemView.findViewById(R.id.requestTV);
            acceptTV = (TextView) itemView.findViewById(R.id.acceptTV);
            rejectTV = (TextView) itemView.findViewById(R.id.rejectTV);
            revokeTV = (TextView) itemView.findViewById(R.id.revokeTV);
            request_list_item = (RelativeLayout) itemView.findViewById(R.id.request_list_item);
            acceptTV.setOnClickListener(this);
            rejectTV.setOnClickListener(this);
            revokeTV.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.acceptTV:
                    clickListener.onFloorRequestAccepted(getLayoutPosition());
                    break;
                case R.id.rejectTV:
                    clickListener.onFloorRequestRejected(getLayoutPosition());
                    break;
                case R.id.revokeTV:
                    clickListener.onFloorRequestRevoked(getLayoutPosition());
                    break;
            }

        }
    }

    private String checkNullValue(String value) {
        if (value != null) {
            return value;
        }
        return "";
    }

    public interface RequestItemClickListener {
        void onFloorRequestAccepted(int position);

        void onFloorRequestRejected(int position);

        void onFloorRequestRevoked(int position);
    }

}
