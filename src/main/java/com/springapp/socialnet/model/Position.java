package com.springapp.socialnet.model;

import lombok.Data;

@Data
public class Position {

    private int id;

    private String name;

    private ExperienceLevel expLevel;

    private String compName;
}