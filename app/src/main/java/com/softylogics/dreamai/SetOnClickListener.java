package com.softylogics.dreamai;


public interface SetOnClickListener {
    // i1
    void onItemClick(DreamItem item, int position);

    void onLongItemClick(DreamItem item, int position);

    void shareDream(DreamItem item, int position);

    void delDream(DreamItem item, int position);

    void markFav(DreamItem item, int position);

    void unMarkFav(DreamItem item, int adapterPosition);

    void showDelButton(boolean b);

    void toggleFavorite(DreamItem item, int adapterPosition);
}