package com.arop.bliss_app.apiObjects;

import android.widget.TextView;

import com.arop.bliss_app.R;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by andre on 25/07/2017.
 */

public class Question implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("image_url")
    private String image_url;
    @SerializedName("thumb_url")
    private String thumb_url;
    @SerializedName("question")
    private String question;
    @SerializedName("published_at")
    private String date;
    @SerializedName("choices")
    private List<QuestionChoice> choices;

    public Question(int id, String img, String thumb, String qt, String d) {
        this.id = id;
        image_url = img;
        thumb_url = thumb;
        question = qt;
        date = d;
        choices = new ArrayList<>();
    }

    public void addQuestionChoice(QuestionChoice qc) {
        choices.add(qc);
    }

    public void addQuestionChoice(String choice, int votes) {
        choices.add(new QuestionChoice(choice,votes));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public String getQuestion() {
        return question;
    }

    public String getDate() {
        return date;
    }

    public List<QuestionChoice> getChoices() {
        return choices;
    }

    public String getFormattedDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        DateFormat df1 = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.SHORT, Locale.getDefault());
        Date date;
        try {
            date = df.parse(this.date);
            return df1.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Choices inside question, with votes
     */
    public class QuestionChoice implements Serializable {
        @SerializedName("choice")
        private String name;
        @SerializedName("votes")
        private int votes;

        QuestionChoice(String n, int v) {
            this.name = n;
            this.votes = v;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getVotes() {
            return votes;
        }

        public void setVotes(int votes) {
            this.votes = votes;
        }
    }
}
