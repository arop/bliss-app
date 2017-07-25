package com.arop.bliss_app.apiObjects;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andre on 25/07/2017.
 */

public class Question {
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

    /**
     * Choices inside question, with votes
     */
    public class QuestionChoice {
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
