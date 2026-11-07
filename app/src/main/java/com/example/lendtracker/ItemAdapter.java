package com.example.lendtracker;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView Adapter + ViewHolder: shows each Item as a card in the list.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    /** MainActivity implements this to handle the buttons on each card */
    public interface OnItemActionListener {
        void onEdit(Item item);
        void onDelete(Item item);
        void onCall(Item item);
        void onShare(Item item);
    }

    private final List<Item> items;
    private final OnItemActionListener listener;

    public ItemAdapter(List<Item> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);

        holder.tvItemName.setText(item.getItemName());
        holder.tvFriend.setText(holder.itemView.getContext()
                .getString(R.string.lent_to, item.getFriendName()));
        holder.tvDate.setText(holder.itemView.getContext()
                .getString(R.string.lent_on, item.getLentDate()));

        Bitmap photo = ImageUtil.base64ToBitmap(item.getImageBase64());
        if (photo != null) {
            holder.ivPhoto.setImageBitmap(photo);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
        holder.btnCall.setOnClickListener(v -> listener.onCall(item));
        holder.btnShare.setOnClickListener(v -> listener.onShare(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvItemName, tvFriend, tvDate;
        ImageButton btnCall, btnShare, btnDelete;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvFriend = itemView.findViewById(R.id.tvFriend);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
