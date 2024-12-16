package com.example.crudapp;

public class ReadWriteUserDetails {
    public String  fullName,DOB, gender, mobile;

     //constructor
    public ReadWriteUserDetails(){};

    public ReadWriteUserDetails(String textFullName, String textDOB, String textGender, String textMobile){
        this.fullName = textFullName;
        this.DOB = textDOB;
        this.gender = textGender;
        this.mobile = textMobile;

    }
}
