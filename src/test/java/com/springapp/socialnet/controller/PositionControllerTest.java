package com.springapp.socialnet.controller;

import com.springapp.socialnet.model.Position;
import com.springapp.socialnet.service.PositionService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PositionService service;

    @Test
    public void shouldReturnCompanyFormWhenAddCompany() throws Exception {
        mockMvc.perform(get("/position")
            .param("compId", "123"))
               .andExpect(status().isOk())
               .andExpect(content().string(Matchers.containsString("Add position")));
    }

    @Test
    public void shouldAddPositionNode() throws Exception {
        mockMvc.perform(post("/position")
            .param("compId", "123")
            .param("name", "Junior Java Developer")
            .flashAttr("position", new Position()))
               .andExpect(redirectedUrl("/company/all"));
    }
}