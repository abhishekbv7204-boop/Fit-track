package com.gym.models;

import java.sql.Date;

public class Member {
    private int id;
    private String name;
    private int age;
    private String gender;
    private String phone;
    private String email;
    private Date joinDate;
    private Integer planId;
    private String planName;

    public Member(int id, String name, int age, String gender, String phone, String email, Date joinDate, Integer planId, String planName) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.joinDate = joinDate;
        this.planId = planId;
        this.planName = planName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public Date getJoinDate() { return joinDate; }
    public Integer getPlanId() { return planId; }
    public String getPlanName() { return planName; }
}

