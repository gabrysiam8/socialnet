package com.springapp.socialnet.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.springapp.socialnet.model.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonServiceTest {

    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();

    private GraphDatabaseService graphDb;

    private PersonService service;

    private List<Node> nodes;

    @Before
    public void initNeo4j() {
        ServerControls embeddedDatabaseServer = TestServerBuilders
            .newInProcessBuilder()
            .newServer();
        Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
        service = new PersonService(driver);
        graphDb = embeddedDatabaseServer.graph();

        String cypher = "CREATE (p:Person {firstName: \"Gaba\", lastName: \"Testowa\", email: \"test@gmail.com\"})";
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
    public void shouldGetAllPeople() {
        Set<Person> people = service.getAllPeople();

        assertEquals(1, people.size());
    }

    @Test
    public void shouldGetPersonById() {
        int id = (int)(nodes.get(0).getId());

        Person person = service.getPersonById(id);

        assertEquals("Gaba", person.getFirstName());
    }

    @Test
    public void shouldAddPerson() {
        Person person = new Person();
        person.setFirstName("Anna");
        person.setLastName("Kot");
        person.setEmail("anna@gmail.com");

        String msg = service.addPerson(person);

        try (Transaction tx = graphDb.beginTx()) {
            nodes = graphDb.getAllNodes().stream().collect(Collectors.toList());
            tx.success();
        }
        assertEquals(2, nodes.size());
        assertEquals("", msg);
    }

    @Test
    public void shouldNotAddPersonWhenEmailAlreadyExists() {
        Person person = new Person();
        person.setFirstName("Anna");
        person.setLastName("Kot");
        person.setEmail("test@gmail.com");

        String msg = service.addPerson(person);

        try (Transaction tx = graphDb.beginTx()) {
            nodes = graphDb.getAllNodes().stream().collect(Collectors.toList());
            tx.success();
        }
        assertEquals(1, nodes.size());
        assertEquals("Person with this e-mail already exists in a database", msg);
    }

    @Test
    public void shouldEditPerson() {
        int id = (int)(nodes.get(0).getId());
        Person person = new Person(id, "Anna", "Testowa", "test@gmail.com","");

        String msg = service.editPerson(person);

        assertEquals("", msg);
    }

    @Test
    public void shouldNotEditPersonWhenEmailAlreadyExists() {
        String email = "invalid@gmail.com";
        Person person = new Person();
        person.setFirstName("Anna");
        person.setLastName("Kot");
        person.setEmail(email);
        service.addPerson(person);

        int id = (int)(nodes.get(0).getId());
        Person edited = new Person(id, "Gaba", "Testowa", email,"");

        String msg = service.editPerson(edited);

        assertEquals("Person with this e-mail already exists in a database", msg);
    }

    @Test
    public void shouldDeletePerson() {
        int id = (int)(nodes.get(0).getId());

        service.deletePerson(id);

        try (Transaction tx = graphDb.beginTx()) {
            nodes = graphDb.getAllNodes().stream().collect(Collectors.toList());
            tx.success();
        }
        assertEquals(0, nodes.size());
    }
}