package com.springapp.socialnet.controller;

import com.springapp.socialnet.model.Job;
import com.springapp.socialnet.service.JobService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@RequestMapping("/job")
public class JobController {

    private final JobService service;

    public JobController(JobService service) {
        this.service = service;
    }

    @GetMapping
    public String findAllJobs(Model model) {
        Set<Job> jobs = service.getAllJobs();
        model.addAttribute("jobs", jobs);

        return "jobList";
    }

    @GetMapping("/delete")
    public String deleteJobOffer(@RequestParam("compName") String compName, @RequestParam("posName") String posName) {
        service.deleteJobOffer(compName, posName);
        return "redirect:/job";
    }
}