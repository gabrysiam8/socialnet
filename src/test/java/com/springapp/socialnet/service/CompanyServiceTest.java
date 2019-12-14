package com.springapp.socialnet.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.springapp.socialnet.model.Company;
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
public class CompanyServiceTest {

    private static final Config driverConfig = Config.build().withoutEncryption().toConfig();

    private GraphDatabaseService graphDb;

    private CompanyService service;

    private List<Node> nodes;

    @Before
    public void initNeo4j() {
        ServerControls embeddedDatabaseServer = TestServerBuilders
            .newInProcessBuilder()
            .newServer();
        Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
        service = new CompanyService(driver);
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
    public void shouldGetAllCompanies() {
        Set<Company> companies = service.getAllCompanies();

        assertEquals(1, companies.size());
    }

    @Test
    public void shouldGetPersonById() {
        int id = (int)(nodes.get(0).getId());

        Company company = service.getCompanyById(id);

        assertEquals("Test company", company.getName());
    }

    @Test
    public void shouldAddCompany() {
        Company company = new Company();
        company.setName("Another name");
        company.setCity("Krakow");

        String msg = service.addCompany(company);

        try (Transaction tx = graphDb.beginTx()) {
            nodes = graphDb.getAllNodes().stream().collect(Collectors.toList());
            tx.success();
        }
        assertEquals(2, nodes.size());
        assertEquals("", msg);
    }

    @Test
    public void shouldNotAddCompanyWhenNameAlreadyExists() {
        Company company = new Company();
        company.setName("Test company");
        company.setCity("Krakow");

        String msg = service.addCompany(company);

        try (Transaction tx = graphDb.beginTx()) {
            nodes = graphDb.getAllNodes().stream().collect(Collectors.toList());
            tx.success();
        }
        assertEquals(1, nodes.size());
        assertEquals("Company with this name already exists in a database", msg);
    }

    @Test
    public void shouldEditCompany() {
        int id = (int)(nodes.get(0).getId());
        Company company = new Company(id, "Another name", "Krakow");

        String msg = service.editCompany(company);

        assertEquals("", msg);
    }

    @Test
    public void shouldNotEditCompanyWhenNameAlreadyExists() {
        String name = "Another name";
        Company company = new Company();
        company.setName(name);
        company.setCity("Krakow");
        service.addCompany(company);

        int id = (int)(nodes.get(0).getId());
        Company edited = new Company(id, name, "Krakow");

        String msg = service.editCompany(edited);

        assertEquals("Company with this name already exists in a database", msg);
    }

    @Test
    public void shouldDeleteCompany() {
        int id = (int)(nodes.get(0).getId());

        service.deleteCompany(id);

        try (Transaction tx = graphDb.beginTx()) {
            nodes = graphDb.getAllNodes().stream().collect(Collectors.toList());
            tx.success();
        }
        assertEquals(0, nodes.size());
    }
}