package com.nhom12.ourquizlet.data.model;

public class UserAnswer {
    private String IdWord;
    private int numberAnswer;
    private String answer;
    private boolean isCorrect;

    public UserAnswer(String idWord, String answer) {
        IdWord = idWord;
        this.numberAnswer = 0;
        this.answer = answer;
        this.isCorrect = true;
    }

    public String getIdWord() {
        return IdWord;
    }

    public void setIdWord(String idWord) {
        IdWord = idWord;
    }

    public int getNumberAnswer() {
        return numberAnswer;
    }

    public void setNumberAnswer(int numberAnswer) {
        this.numberAnswer = numberAnswer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
