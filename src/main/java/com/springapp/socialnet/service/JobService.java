package com.springapp.socialnet.service;

import com.springapp.socialnet.model.Job;
import org.neo4j.driver.v1.*;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class JobService {

    private final Driver driver;

    public JobService(Driver driver) {
        this.driver = driver;
    }

    public Set<Job> getAllJobs() {
        Set<Job> jobs = new HashSet<>();

        try (Session session = driver.session()) {
            StatementResult result = session.run("MATCH (c:Company)-[r:LOOKS_FOR]->(p:Position) RETURN c.name, p.name");

            for (Record record: result.list()) {
                Job job = new Job();
                String compName = record.get("c.name").asString();
                job.setCompName(compName);
                String posName = record.get("p.name").asString();
                job.setPosName(posName);
                jobs.add(job);
            }
        }
        return jobs;
    }

    public void deleteJobOffer(String compName, String posName) {
        try (Session session = driver.session()) {
            session.run("MATCH(c:Company {name: {compName}}) -[r:LOOKS_FOR]->(p:Position {name: {posName}}) DELETE r",
                    Values.parameters("compName", compName, "posName", posName));
        }
    }
}
