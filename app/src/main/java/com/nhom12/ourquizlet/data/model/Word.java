package com.nhom12.ourquizlet.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Word implements Parcelable, Cloneable {
    private String id;
    private String idTopic;
    private String term, define;
    private String answer1;
    private String answer2;
    private String answer3;
    private String answer4;
    private String status = "Don't Know";
    private boolean star = false;
    private boolean isFlipped = false;

    public Word() {
    }


    public Word(String term, String define) {
        this.term = term;
        this.define = define;
    }

    protected Word(Parcel in) {
        id = in.readString ();
        idTopic = in.readString ();
        term = in.readString ();
        define = in.readString ();
        answer1 = in.readString ();
        answer2 = in.readString ();
        answer3 = in.readString ();
        answer4 = in.readString ();
        status = in.readString ();
        star = in.readByte () != 0;
        isFlipped = in.readByte () != 0;
    }

    public static final Creator<Word> CREATOR = new Creator<Word> () {
        @Override
        public Word createFromParcel(Parcel in) {
            return new Word (in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdTopic() {
        return idTopic;
    }

    public void setIdTopic(String idTopic) {
        this.idTopic = idTopic;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefine() {
        return define;
    }

    public void setDefine(String define) {
        this.define = define;
    }

    public String getAnswer1() {
        return answer1;
    }

    public void setAnswer1(String answer1) {
        this.answer1 = answer1;
    }

    public String getAnswer2() {
        return answer2;
    }

    public void setAnswer2(String answer2) {
        this.answer2 = answer2;
    }

    public String getAnswer3() {
        return answer3;
    }

    public void setAnswer3(String answer3) {
        this.answer3 = answer3;
    }

    public String getAnswer4() {
        return answer4;
    }

    public void setAnswer4(String answer4) {
        this.answer4 = answer4;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    public void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }


    @NonNull
    @Override
    public Word clone() {
        try {
            Word clone = (Word) super.clone ();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError ();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString (id);
        dest.writeString (idTopic);
        dest.writeString (term);
        dest.writeString (define);
        dest.writeString (answer1);
        dest.writeString (answer2);
        dest.writeString (answer3);
        dest.writeString (answer4);
        dest.writeString (status);
        dest.writeByte ((byte) (star ? 1 : 0));
        dest.writeByte ((byte) (isFlipped ? 1 : 0));
    }
}
