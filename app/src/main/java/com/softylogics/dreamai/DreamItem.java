package com.softylogics.dreamai;

import androidx.annotation.Keep;

import com.google.firebase.database.PropertyName;
@Keep
public class DreamItem {

    private String prompt;
    private String interpretation;

    private long date;

    private boolean isFavorite;
    private boolean isChecked;


    private boolean isVisible;


    @PropertyName("date")
    public long getDate() {
        return date;
    }
    @PropertyName("date")
    public void setDate(long date) {
        this.date = date;
    }
    @PropertyName("prompt")
    public String getPrompt() {
        return prompt;
    }
    @PropertyName("prompt")
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    @PropertyName("interpretation")
    public String getInterpretation() {
        return interpretation;
    }
    @PropertyName("interpretation")
    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }
    @PropertyName("isFavorite")
    public boolean getFavorite() {
        return isFavorite;
    }
    @PropertyName("isFavorite")
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean getVisibility() {
        return isVisible;
    }

    public void setVisibility(boolean isvisible) {
        isVisible = isvisible;
    }

    public DreamItem() {
    }

    /**
     * Parameterized constructor for the DreamItem class.
     *
     * @param prompt         The prompt associated with the dream item.
     * @param interpretation The interpretation associated with the dream item.
     * @param date           The date associated with the dream item.
     * @param isFavorite     The favorite status of the dream item.
     * @param isChecked      The checked status of the dream item.
     */
    public DreamItem(String prompt, String interpretation, long date, Boolean isFavorite, boolean isChecked) {
        this.prompt = prompt;
        this.interpretation = interpretation;
        this.date = date;
        this.isFavorite = isFavorite;
        this.isChecked = isChecked;

    }
    @PropertyName("isChecked")
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    @PropertyName("isChecked")
    public boolean getIsChecked(){
        return this.isChecked;
    }

//    public boolean isFavoriteValue() {
//        return this.isFavorite != null && this.isFavorite;
//    }
//
//    public void setBooleanValue(Boolean booleanValue) {
//        this.isFavorite = booleanValue != null && booleanValue;
//    }
}
