package com.dunrite.tallyup;

import java.util.ArrayList;

/**
 * A class that represents a poll. This might not be needed
 */
public class Poll {
    private String id;
    private String question;
    private String type;
    private boolean multiSelect;
    private String expireTime;
    private ArrayList<PollItem> items;

    public Poll (String i, String q, String t, Boolean m, ArrayList<PollItem> pi) {
        question = q;
        type = t;
        multiSelect = m;
        items = pi;
        id = i;
    }

    public Poll (String i, String q) {
        id = i;
        question = q;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<PollItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<PollItem> items) {
        this.items = items;
    }
}
