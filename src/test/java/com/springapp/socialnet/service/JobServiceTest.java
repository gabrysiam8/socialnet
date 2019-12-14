package com.springapp.socialnet.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.springapp.socialnet.model.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JobServiceTest {
    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();

    private GraphDatabaseService graphDb;

    private JobService service;

    private static final String COMP_NAME = "Test company";

    private static final String POS_NAME = "Test position";

    @Before
    public void initNeo4j() {
        ServerControls embeddedDatabaseServer = TestServerBuilders
            .newInProcessBuilder()
            .newServer();
        Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
        service = new JobService(driver);
        graphDb = embeddedDatabaseServer.graph();

        String cypher = "CREATE (c:Company {name: {compName}, city: \"Krakow\"}) " +
            "WITH c MERGE (c) -[r:LOOKS_FOR]->(p:Position {name: {posName}})";
        Map<String, Object> params =  Map.of("compName", COMP_NAME, "posName", POS_NAME);
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.execute(cypher, params);
            tx.success();
        }
    }

    @After
    public void closeNeo4j() {
        graphDb.shutdown();
    }

    @Test
    public void shouldGetAllJobs() {
        Set<Job> jobs = service.getAllJobs();

        assertEquals(1, jobs.size());
        Job job = jobs.iterator().next();
        assertEquals(POS_NAME, job.getPosName());
        assertEquals(COMP_NAME, job.getCompName());
    }

    @Test
    public void shouldDeleteJob() {
        service.deleteJobOffer(COMP_NAME, POS_NAME);

        List<Node> nodes;
        List<Relationship> relationships;
        try (Transaction tx = graphDb.beginTx()) {
            nodes = graphDb.getAllNodes().stream().collect(Collectors.toList());
            relationships = graphDb.getAllRelationships().stream().collect(Collectors.toList());
            tx.success();
        }
        assertEquals(2, nodes.size());
        assertEquals(0, relationships.size());
    }
}