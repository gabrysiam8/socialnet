package com.springapp.socialnet.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    private Integer id;

    private String firstName;

    private String lastName;

    private String email;

    private String compName;
}
