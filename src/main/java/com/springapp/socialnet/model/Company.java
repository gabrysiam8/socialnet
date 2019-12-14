package com.springapp.socialnet.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    private Integer id;

    private String name;

    private String city;
}
