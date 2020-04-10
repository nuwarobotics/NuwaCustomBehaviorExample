package com.nuwarobotics.service.nuwaextservice.contact;

public class ContactData {
    public long faceid;
    public String nickname;
    public String organization;
    public String title;

    public ContactData() {
    }

    public ContactData(long faceid, String nickname, String organization, String title) {
        this.faceid = faceid;
        this.nickname = nickname;
        this.organization = organization;
        this.title = title;
    }
}