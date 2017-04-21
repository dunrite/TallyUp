package com.dunrite.tallyup.pojo;

/**
 * Poll items
 */
public class PollItem {
    private String name;
    private int votes;
    private String type;

    public PollItem(String n, int v) {
        name = n;
        votes = v;
        type = "Plain Text";
    }

    public PollItem(String n, String t, int v) {
        name = n;
        votes = v;
        type = t;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void addVote() {
        votes++;
    }


}


