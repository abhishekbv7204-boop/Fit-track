package com.gym.models;

import java.math.BigDecimal;

public class Plan {
    private int id;
    private String name;
    private int durationMonths;
    private BigDecimal price;

    public Plan(int id, String name, int durationMonths, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.durationMonths = durationMonths;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getDurationMonths() { return durationMonths; }
    public BigDecimal getPrice() { return price; }
}

