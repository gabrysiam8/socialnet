package com.springapp.socialnet.controller;

import java.util.Collections;
import java.util.Set;

import com.springapp.socialnet.model.Person;
import com.springapp.socialnet.service.CompanyService;
import com.springapp.socialnet.service.PersonService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService service;

    @MockBean
    private CompanyService companyService;

    private static final Person PERSON = new Person(1, "Anna", "Kot", "test@gmail.com", "");

    @Test
    public void shouldReturnPersonFormWhenAddPerson() throws Exception {
        given(companyService.getAllCompanies()).willReturn(Collections.emptySet());

        mockMvc.perform(get("/person"))
               .andExpect(status().isOk())
               .andExpect(content().string(Matchers.containsString("Add person")));
    }

    @Test
    public void shouldReturnPersonFormWhenEditPerson() throws Exception {
        given(companyService.getAllCompanies()).willReturn(Collections.emptySet());
        given(service.getPersonById(anyInt())).willReturn(PERSON);

        mockMvc.perform(get("/person")
               .param("id", String.valueOf(PERSON.getId())))
               .andExpect(status().isOk())
               .andExpect(content().string(Matchers.containsString("Edit person")));
    }

    @Test
    public void shouldReturnAllCompanyNodesOnGet() throws Exception {
        Person anotherPerson = new Person(2, "Jan", "Kowalski", "kowalski@gmail.com", "Test company");
        Set<Person> people = Set.of(PERSON, anotherPerson);
        given(service.getAllPeople()).willReturn(people);

        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(content().string(Matchers.allOf(
                   Matchers.containsString(PERSON.getEmail()),
                   Matchers.containsString(anotherPerson.getEmail())
               )));
    }

    @Test
    public void shouldAddPersonWhenCorrectEmail() throws Exception {
        given(service.addPerson(any(Person.class))).willReturn("");

        mockMvc.perform(post("/person/new")
            .param("firstName", PERSON.getFirstName())
            .param("lastName", PERSON.getLastName())
            .param("email", PERSON.getEmail())
            .param("compName", PERSON.getCompName())
            .flashAttr("person", new Person()))
               .andExpect(redirectedUrl("/"));
    }

    @Test
    public void shouldReturnErrorMessageOnAddWhenInvalidEmail() throws Exception {
        String errorMsg = "Person with this e-mail already exists in a database";
        given(service.addPerson(any(Person.class))).willReturn(errorMsg);

        mockMvc.perform(post("/person/new")
            .param("firstName", PERSON.getFirstName())
            .param("lastName", PERSON.getLastName())
            .param("email", "invalid@gmail.com")
            .param("compName", PERSON.getCompName())
            .flashAttr("person", new Person()))
               .andExpect(status().isOk())
               .andExpect(content().string(Matchers.containsString(errorMsg)));
    }

    @Test
    public void shouldEditCompanyWhenCorrectName() throws Exception {
        given(service.editPerson(any(Person.class))).willReturn("");

        mockMvc.perform(post("/person/edit")
            .param("id", String.valueOf(PERSON.getId()))
            .param("firstName", PERSON.getFirstName())
            .param("lastName", PERSON.getLastName())
            .param("email", "new@gmail.com")
            .param("compName", PERSON.getCompName())
            .flashAttr("person", new Person()))
               .andExpect(redirectedUrl("/"));
    }

    @Test
    public void shouldReturnErrorMessageOnEditWhenInvalidName() throws Exception {
        String errorMsg = "Person with this e-mail already exists in a database";
        given(service.editPerson(any(Person.class))).willReturn(errorMsg);

        mockMvc.perform(post("/person/edit")
            .param("id", String.valueOf(PERSON.getId()))
            .param("firstName", PERSON.getFirstName())
            .param("lastName", PERSON.getLastName())
            .param("email", "invalid@gmail.com")
            .param("compName", PERSON.getCompName())
            .flashAttr("person", new Person()))
               .andExpect(status().isOk())
               .andExpect(content().string(Matchers.containsString(errorMsg)));
    }

    @Test
    public void shouldDeletePerson() throws Exception {
        mockMvc.perform(get("/person/delete/1"))
               .andExpect(redirectedUrl("/"));
    }
}