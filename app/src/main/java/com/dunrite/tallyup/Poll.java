package com.dunrite.tallyup;

import java.util.Map;

/**
 * A class that represents a poll. This might not be needed
 */
public class Poll {
    private String question;
    private String type;
    private boolean multiSelect;
    private Map<String, Integer> options; //Integer is number of votes

    public Poll (String q, String t, Boolean m, Map<String, Integer> o) {
        question = q;
        type = t;
        multiSelect = m;
        options = o;
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

    public Map<String, Integer> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Integer> options) {
        this.options = options;
    }
}
