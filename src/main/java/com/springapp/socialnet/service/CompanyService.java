package com.springapp.socialnet.service;

import com.springapp.socialnet.model.Company;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CompanyService {

    private final Driver driver;

    public CompanyService(Driver driver) {
        this.driver = driver;
    }

    public Set<Company> getAllCompanies() {
        Set<Company> companies = new HashSet<>();

        try (Session session = driver.session()) {
            StatementResult result = session.run("MATCH(c:Company) RETURN ID(c) AS id, c");

            for (Record record: result.list()) {
                Node company = record.get("c").asNode();

                Company c = new Company();
                c.setId(record.get("id").asInt());
                c.setName(company.get("name").asString());
                c.setCity(company.get("city").asString());
                companies.add(c);
            }
        }
        return companies;
    }

    public Company getCompanyById(int id) {
        Company c = new Company();
        try (Session session = driver.session()) {
            StatementResult result = session.run("MATCH(c:Company) WHERE ID(c)={id} RETURN ID(c) AS id, c",
                Values.parameters("id", id));

            Record record = result.list().get(0);
            Node person = record.get("c").asNode();

            c.setId(record.get("id").asInt());
            c.setName(person.get("name").asString());
            c.setCity(person.get("city").asString());
        }
        return c;
    }

    public String addCompany(Company company) {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("MATCH(c:Company {name: {name}}) RETURN c.name",
                        Values.parameters("name", company.getName()));
                if(!result.list().isEmpty()) {
                    tx.failure();
                    return "Company with this name already exists in a database";
                }
                String statement = "CREATE (c:Company {name: {name}, city: {city}})";
                tx.run(statement, Values.parameters(
                        "name", company.getName(),
                        "city", company.getCity()));
                tx.success();
            }
        }
        return "";
    }

    public String editCompany(Company company) {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("MATCH(c:Company {name: {name}}) WHERE ID(c)<>{id} RETURN c.name",
                    Values.parameters("name", company.getName(),
                        "id", company.getId()));
                if(!result.list().isEmpty()) {
                    tx.failure();
                    return "Company with this name already exists in a database";
                }
                String statement = "MATCH (c:Company) WHERE ID(c)={id} "
                    + "SET c = { name: {name}, city: {city} } ";
                tx.run(statement, Values.parameters(
                    "id", company.getId(),
                    "name", company.getName(),
                    "city", company.getCity()));
                tx.success();
            }
        }
        return "";
    }

    public void deleteCompany(int id) {
        try (Session session = driver.session()) {
            session.run("MATCH(c:Company) WHERE ID(c)={id} DETACH DELETE c",
                    Values.parameters("id", id));
        }
    }
}
