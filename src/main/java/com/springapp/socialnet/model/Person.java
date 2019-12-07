package com.springapp.socialnet.model;

import lombok.Data;

@Data
public class Person {

    private int id;

    private String firstName;

    private String lastName;

    private String email;

    private int age;
}
