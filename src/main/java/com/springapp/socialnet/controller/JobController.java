package com.springapp.socialnet.controller;

import com.springapp.socialnet.model.Job;
import com.springapp.socialnet.service.JobService;
import org.neo4j.driver.v1.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
public class JobController {

    private final JobService service;

    public JobController(JobService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String findAllJobs(Model model) {
        Set<Job> jobs = service.getAllJobs();
        model.addAttribute("jobs", jobs);

        return "index";
    }

    @GetMapping("/job/delete")
    public String deleteJobOffer(@RequestParam("compName") String compName, @RequestParam("posName") String posName) {
        service.deleteJobOffer(compName, posName);
        return "redirect:/";
    }
}