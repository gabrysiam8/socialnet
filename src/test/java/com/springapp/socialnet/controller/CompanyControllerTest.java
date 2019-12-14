package com.springapp.socialnet.controller;

import java.util.Set;

import com.springapp.socialnet.model.Company;
import com.springapp.socialnet.service.CompanyService;
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
public class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService service;

    private static final Company COMPANY = new Company(1, "Test company", "Test city");

    @Test
    public void shouldReturnCompanyFormWhenAddCompany() throws Exception {
        mockMvc.perform(get("/company"))
            .andExpect(status().isOk())
            .andExpect(content().string(Matchers.containsString("Add company")));
    }

    @Test
    public void shouldReturnCompanyFormWhenEditCompany() throws Exception {
        given(service.getCompanyById(anyInt())).willReturn(COMPANY);

        mockMvc.perform(get("/company")
               .param("id", "1"))
                   .andExpect(status().isOk())
                   .andExpect(content().string(Matchers.containsString("Edit company")));
    }

    @Test
    public void shouldReturnAllCompanyNodesOnGet() throws Exception {
        Company anotherCompany = new Company(2, "Another test company", "Cracow");
        Set<Company> companies = Set.of(COMPANY, anotherCompany);
        given(service.getAllCompanies()).willReturn(companies);

        mockMvc.perform(get("/company/all"))
               .andExpect(status().isOk())
               .andExpect(content().string(Matchers.allOf(
                   Matchers.containsString(COMPANY.getName()),
                   Matchers.containsString(anotherCompany.getName())
               )));
    }

    @Test
    public void shouldAddCompanyWhenCorrectName() throws Exception {
        given(service.addCompany(any(Company.class))).willReturn("");

        mockMvc.perform(post("/company/new")
            .param("name", COMPANY.getName())
            .param("city",COMPANY.getCity())
            .flashAttr("company", new Company()))
               .andExpect(redirectedUrl("/company/all"));

    }

    @Test
    public void shouldReturnErrorMessageOnAddWhenInvalidName() throws Exception {
        String errorMsg = "Company with this name already exists in a database";
        given(service.addCompany(any(Company.class))).willReturn(errorMsg);

        mockMvc.perform(post("/company/new")
            .param("name", COMPANY.getName())
            .param("city",COMPANY.getCity())
            .flashAttr("company", new Company()))
               .andExpect(status().isOk())
               .andExpect(content().string(Matchers.containsString(errorMsg)));

    }

    @Test
    public void shouldEditCompanyWhenCorrectName() throws Exception {
        given(service.editCompany(any(Company.class))).willReturn("");

        mockMvc.perform(post("/company/edit")
            .param("id", String.valueOf(COMPANY.getId()))
            .param("name", "another name")
            .param("city",COMPANY.getCity())
            .flashAttr("company", new Company()))
               .andExpect(redirectedUrl("/company/all"));

    }

    @Test
    public void shouldReturnErrorMessageOnEditWhenInvalidName() throws Exception {
        String errorMsg = "Company with this name already exists in a database";
        given(service.editCompany(any(Company.class))).willReturn(errorMsg);

        mockMvc.perform(post("/company/edit")
            .param("id", String.valueOf(COMPANY.getId()))
            .param("name", "Invalid name")
            .param("city", COMPANY.getCity())
            .flashAttr("company", new Company()))
               .andExpect(status().isOk())
               .andExpect(content().string(Matchers.containsString(errorMsg)));
    }

    @Test
    public void shouldDeleteCompany() throws Exception {
        mockMvc.perform(get("/company/delete/1"))
            .andExpect(redirectedUrl("/company/all"));
    }
}
