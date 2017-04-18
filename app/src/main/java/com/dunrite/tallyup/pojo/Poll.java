package com.dunrite.tallyup.pojo;

import com.dunrite.tallyup.utility.Utils;

import java.util.ArrayList;

/**
 * A class that represents a poll. This might not be needed
 */
public class Poll {
    private String id;
    private String question;
    private String type;
    private String expireTime;
    private ArrayList<PollItem> items;
    private int voteCount;

    public Poll (String i, String q, String t, String et, ArrayList<PollItem> pi) {
        question = q;
        type = t;
        items = pi;
        id = i;
        expireTime = et;
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

    public int getVoteCount() {
        int tot=0;
        for (PollItem item: items) {
            tot += item.getVotes();
        }
        return tot;
    }

    public String getLeadingAnswers() {
        int topNum = 0;
        int numOfLeaders = 0;
        String leader= "";
        for(PollItem item : items) {
            if (item.getVotes() > topNum)  {
                topNum = item.getVotes();
                leader = item.getName();
                numOfLeaders = 1;
            } else if (item.getVotes() == topNum) {
                leader += ", " + item.getName();
                numOfLeaders++;
            }
        }
        if (numOfLeaders > 1 && numOfLeaders != items.size()) {
            leader = Utils.replaceLast(leader, ",", " and") +" are tied for the lead";
        } else if (numOfLeaders == items.size()){
            leader = "Nothing is leading";
        } else if (numOfLeaders == 1){
            leader += " is in the lead";
        }
        return leader;
    }


    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }
}
