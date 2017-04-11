package com.dunrite.tallyup;

/**
 * Poll items
 */
public class PollItem {
    private String name;
    private int votes;

    public PollItem(String n, int v) {
        name = n;
        votes = v;
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

    public void addVote() {
        votes++;
    }


}


