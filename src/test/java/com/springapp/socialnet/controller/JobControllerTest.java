package com.springapp.socialnet.controller;

import java.util.Set;

import com.springapp.socialnet.model.Job;
import com.springapp.socialnet.service.JobService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JobControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService service;

    @Test
    public void shouldReturnAllJobOffers() throws Exception {
        Job javaJob = new Job("Test company", "Junior Java Developer");
        Job devOpsJob = new Job("Another company", "DevOps Engineer");
        Set<Job> jobs = Set.of(javaJob, devOpsJob);
        given(service.getAllJobs()).willReturn(jobs);

        mockMvc.perform(get("/job"))
               .andExpect(status().isOk())
               .andExpect(content().string(Matchers.allOf(
                   Matchers.containsString(javaJob.getPosName()),
                   Matchers.containsString(devOpsJob.getPosName())
               )));
    }

    @Test
    public void shouldDeleteJobOffer() throws Exception {

        mockMvc.perform(get("/job/delete")
            .param("compName", "Test company")
            .param("posName", "Junior Java Developer"))
               .andExpect(redirectedUrl("/job"));
    }
}