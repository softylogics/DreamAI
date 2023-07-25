package com.softylogics.dreamai;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.softylogics.dreamai.databinding.LayoutHistoryItemBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomFavoriteAdapter extends RecyclerView.Adapter<CustomFavoriteAdapter.ViewHolder> {
    //todo: add long click listener in next update

    private final SetOnClickListener listener;
    private LayoutHistoryItemBinding binding;
    private List<DreamItem> itemList;



    @NonNull
    @Override
    public CustomFavoriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = LayoutHistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new ViewHolder(binding);
    }

    public CustomFavoriteAdapter(List<DreamItem> itemList, SetOnClickListener listener) {
        this.listener = listener;
        this.itemList = itemList;

    }

    @Override
    public void onBindViewHolder(@NonNull CustomFavoriteAdapter.ViewHolder holder, int position) {
        DreamItem item = itemList.get(position);
        holder.binding.dateTextView.setText(formatMilliseconds(item.getDate()));
        holder.binding.titleTextView.setText(item.getPrompt());
        holder.binding.detailTextView.setText(item.getInterpretation());

        holder.binding.dreamItemHistory.setOnClickListener(v->{
            listener.onItemClick(item, holder.getAdapterPosition());
        });

        holder.binding.ivShare.setOnClickListener(v->{
             listener.shareDream(item, holder.getAdapterPosition());
        });
        holder.binding.ivFav.setOnClickListener(v->{
             listener.toggleFavorite(item, holder.getAdapterPosition());
        });
        if(item.getFavorite()) {
            holder.binding.ivFav.setImageDrawable(holder.binding.getRoot().getResources().getDrawable(R.drawable.baseline_favorite_24));
        }

    }
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setList(List<DreamItem> itemList) {
        this.itemList = itemList;
    }

    public String formatMilliseconds(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault());
        String formattedDateTime = dateFormat.format(new Date(milliseconds));
        return formattedDateTime;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LayoutHistoryItemBinding binding;

        public ViewHolder(@NonNull LayoutHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
