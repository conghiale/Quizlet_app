package com.nhom12.ourquizlet.ui.viewModel;

import androidx.lifecycle.ViewModel;

public class StudyFCViewModel extends ViewModel {
    private boolean isShuffle = false;
    private boolean isMakeSound = false;
    private boolean isUpdated = false;
    private String frontSide = "TERM";

    public StudyFCViewModel() {
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public boolean isMakeSound() {
        return isMakeSound;
    }

    public void setMakeSound(boolean makeSound) {
        isMakeSound = makeSound;
    }

    public String getFrontSide() {
        return frontSide;
    }

    public void setFrontSide(String frontSide) {
        this.frontSide = frontSide;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean updated) {
        isUpdated = updated;
    }
}
