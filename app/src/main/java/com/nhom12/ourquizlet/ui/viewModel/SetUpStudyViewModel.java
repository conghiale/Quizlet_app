package com.nhom12.ourquizlet.ui.viewModel;

import androidx.lifecycle.ViewModel;

public class SetUpStudyViewModel extends ViewModel {
    private boolean isShuffle = false;
    private boolean isStar = false;
    private String languageDefine;
    private int numberWords;
    private String idTopic;
    private String idCategory;

    public SetUpStudyViewModel() {
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public boolean isStar() {
        return isStar;
    }

    public void setStar(boolean star) {
        isStar = star;
    }

    public String getLanguageDefine() {
        return languageDefine;
    }

    public void setLanguageDefine(String languageDefine) {
        this.languageDefine = languageDefine;
    }

    public int getNumberWords() {
        return numberWords;
    }

    public void setNumberWords(int numberWords) {
        this.numberWords = numberWords;
    }

    public String getIdTopic() {
        return idTopic;
    }

    public void setIdTopic(String idTopic) {
        this.idTopic = idTopic;
    }

    public String getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(String idCategory) {
        this.idCategory = idCategory;
    }
}
