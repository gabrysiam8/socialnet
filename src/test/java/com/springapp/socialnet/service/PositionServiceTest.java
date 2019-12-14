package com.springapp.socialnet.service;

import java.util.List;
import java.util.stream.Collectors;

import com.springapp.socialnet.model.Position;
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
public class PositionServiceTest {
    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();

    private GraphDatabaseService graphDb;

    private PositionService service;

    private List<Node> nodes;

    @Before
    public void initNeo4j() {
        ServerControls embeddedDatabaseServer = TestServerBuilders
            .newInProcessBuilder()
            .newServer();
        Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
        service = new PositionService(driver);
        graphDb = embeddedDatabaseServer.graph();

        String cypher = "CREATE (c:Company {name: \"Test company\", city: \"Krakow\"})";
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.execute(cypher);
            nodes = graphDb.getAllNodes().stream().collect(Collectors.toList());
            tx.success();
        }
    }

    @After
    public void closeNeo4j() {
        graphDb.shutdown();
    }

    @Test
    public void shouldAddPositionNodeAndRelationshipWithExistingCompany() {
        int id = (int)(nodes.get(0).getId());
        Position position = new Position();
        position.setName("Java Developer");

        service.addPosition(id, position);

        List<Relationship> relationships;
        try (Transaction tx = graphDb.beginTx()) {
            nodes = graphDb.getAllNodes().stream().collect(Collectors.toList());
            relationships = graphDb.getAllRelationships().stream().collect(Collectors.toList());
            tx.success();
        }
        assertEquals(2, nodes.size());
        assertEquals(1, relationships.size());
    }

    @Test
    public void shouldNotAddPositionNodeAndRelationshipWhenCompanyNotExists() {
        int id = (int)(nodes.get(0).getId())+1;
        Position position = new Position();
        position.setName("Java Developer");

        service.addPosition(id, position);

        List<Relationship> relationships;
        try (Transaction tx = graphDb.beginTx()) {
            nodes = graphDb.getAllNodes().stream().collect(Collectors.toList());
            relationships = graphDb.getAllRelationships().stream().collect(Collectors.toList());
            tx.success();
        }
        assertEquals(1, nodes.size());
        assertEquals(0, relationships.size());
    }
}