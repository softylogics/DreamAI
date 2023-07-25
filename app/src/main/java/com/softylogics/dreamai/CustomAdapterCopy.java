package com.softylogics.dreamai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softylogics.dreamai.databinding.LayoutHistoryItemBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomAdapterCopy extends RecyclerView.Adapter<CustomAdapterCopy.ViewHolder> {
    private final SetOnClickListener listener;
    private LayoutHistoryItemBinding binding;
    private List<DreamItem> itemList;

    private ArrayList<DreamItem> selectedItems;


    private boolean isSelectionEnable = false;
    private boolean selectAll = false;

    @NonNull
    @Override
    public CustomAdapterCopy.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = LayoutHistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new ViewHolder(binding);
    }

    public CustomAdapterCopy(List<DreamItem> itemList, SetOnClickListener listener) {
        this.listener = listener;
        this.itemList = itemList;
        selectedItems = new ArrayList<>();
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapterCopy.ViewHolder holder, int position) {
        if(isSelectionEnable){
            makeCBVisible(holder, position);
        }
        else{
            hideCB(holder,position);
        }

        if(itemList.get(position).getIsChecked()){
            selectCB(holder);
            changeLayoutColorSelected(holder);
        }
        else{
            unSelectCB(holder);
            changeLayoutColorUnSelected(holder);
        }

        if(itemList.get(position).getFavorite()){
            setFav(holder);
        }
        else{
            setUnFav(holder);
        }


        DreamItem item = itemList.get(position);
        holder.binding.dateTextView.setText(formatMilliseconds(item.getDate()));
        holder.binding.titleTextView.setText(item.getPrompt());
        holder.binding.detailTextView.setText(item.getInterpretation());
        holder.binding.dreamItemHistory.setOnLongClickListener(v->{
            if(!isSelectionEnable) {
                isSelectionEnable = true;
                listener.showDelButton(true);
                itemList.get(position).setChecked(true);
                selectedItems.add(itemList.get(position));
                notifyDataSetChanged();
            }
//            if(!selectedItems.contains(item)){
//                selectItem(position, holder);
//            }
            return true;
        });
        holder.binding.dreamItemHistory.setOnClickListener(v->{
            if(isSelectionEnable){
                if(itemList.get(position).getIsChecked()){
                    itemList.get(position).setChecked(false);
                    selectedItems.remove(itemList.get(position));
                    if(selectedItems.size() == 0){
                        isSelectionEnable = false;
                        listener.showDelButton(false);
                        notifyDataSetChanged();
                    }
                    else{
                        notifyItemChanged(position);
                    }
                }
                else{
                    itemList.get(position).setChecked(true);
                    selectedItems.add(itemList.get(position));
                    notifyItemChanged(position);
                }
            }
            else{
                listener.onItemClick(item, position);
            }
        });
        holder.binding.ivFav.setOnClickListener(v->{
            listener.toggleFavorite(item, position);
        });

        holder.binding.ivShare.setOnClickListener(v->{
            listener.shareDream(item, position);
        });


    }


    private void changeLayoutColorSelected(ViewHolder holder){
        holder.binding.historyItemRootView.setBackground(holder.binding.getRoot().getResources().getDrawable(R.drawable.linearlayoutborder_darker));
    }

    private void changeLayoutColorUnSelected(ViewHolder holder){
        holder.binding.historyItemRootView.setBackground(holder.binding.getRoot().getResources().getDrawable(R.drawable.linearlayoutborder));
    }

    private void selectItem(int position, ViewHolder holder) {
        itemList.get(position).setChecked(true);
        selectedItems.add(itemList.get(position));
        notifyItemChanged(position);
//        holder.binding.checkBox.setChecked(true);
    }

    private void makeCBVisible(ViewHolder holder, int position){
        holder.binding.dot.setVisibility(View.GONE);
        holder.binding.checkBox.setVisibility(View.VISIBLE);
        Animation expandAnimation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.expand);
        expandAnimation.setFillAfter(true);
        holder.binding.checkBox.startAnimation(expandAnimation);
        itemList.get(position).setVisibility(true);
    }


    private void setFav(ViewHolder holder){
        holder.binding.ivFav.setImageDrawable(holder.binding.getRoot().getResources().getDrawable(R.drawable.baseline_favorite_24));
    }
    private void setUnFav(ViewHolder holder) {
        holder.binding.ivFav.setImageDrawable(holder.binding.getRoot().getResources().getDrawable(R.drawable.baseline_favorite_border_24));
    }

    private void selectCB(ViewHolder holder){
        holder.binding.checkBox.setChecked(true);
    }


    private void unSelectCB(ViewHolder holder){

        holder.binding.checkBox.setChecked(false);
    }


    private void hideCB(ViewHolder holder, int position){
        Animation shrinkAnimation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.shrink);
        shrinkAnimation.setFillAfter(true);
        holder.binding.checkBox.startAnimation(shrinkAnimation);
        shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                holder.binding.checkBox.setVisibility(View.GONE);
                holder.binding.dot.setVisibility(View.VISIBLE);
                itemList.get(position).setVisibility(false);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void animateCheckBox(boolean isSelected, ViewHolder holder, int position) {
        if(isSelected){
            holder.binding.dot.setVisibility(View.GONE);
            holder.binding.checkBox.setVisibility(View.VISIBLE);
            Animation expandAnimation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.expand);
            expandAnimation.setFillAfter(true);
            holder.binding.checkBox.startAnimation(expandAnimation);
            itemList.get(position).setVisibility(true);
        }else{
            Animation shrinkAnimation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.shrink);
            shrinkAnimation.setFillAfter(true);
            holder.binding.checkBox.startAnimation(shrinkAnimation);
            holder.binding.checkBox.setVisibility(View.GONE);
            holder.binding.dot.setVisibility(View.VISIBLE);
            itemList.get(position).setVisibility(false);
        }
    }


    public void selectAll(){
        selectAll = true;
        selectedItems.clear();
        for(DreamItem item : itemList){
            item.setChecked(true);
            selectedItems.add(item);
        }
        notifyDataSetChanged();
    }
    public void deSelectAll(){
        selectAll = false;
        for(DreamItem item : itemList){
            item.setChecked(false);
        }
        selectedItems.clear();
        notifyDataSetChanged();
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

    public void handleScrolling(int firstVisibleItemPosition, int lastVisibleItemPosition) {

        for(int i = 0 ; i < itemList.size() ; i++){

            if (i < firstVisibleItemPosition || i > lastVisibleItemPosition) {

                //itemList.get(i).setVisibility(false);

            }
        }
       // notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LayoutHistoryItemBinding binding;

        public ViewHolder(@NonNull LayoutHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void disableSelection(){
        isSelectionEnable = false;

    }

    public boolean getSelectionEnable(){
        return isSelectionEnable;
    }

    public ArrayList<DreamItem> getSelectedItems(){
        return selectedItems;
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
