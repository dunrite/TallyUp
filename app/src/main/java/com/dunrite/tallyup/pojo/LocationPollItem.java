package com.dunrite.tallyup.pojo;

public class LocationPollItem extends PollItem {
    private String address;

    public LocationPollItem(String n, int v, String a) {
        super(n, v);
        address = a;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
