package com.springapp.socialnet.service;

import com.springapp.socialnet.model.Person;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class PersonService {

    private final Driver driver;

    public PersonService(Driver driver) {
        this.driver = driver;
    }

    public Set<Person> getAllPeople() {
        Set<Person> people = new HashSet<>();

        try (Session session = driver.session()) {
            StatementResult result = session.run("MATCH(p:Person) OPTIONAL MATCH (p)-[r:WORKS_AT]->(c:Company) RETURN ID(p) AS id, p, c.name");

            for (Record record : result.list()) {
                Person p = createPersonFromRecord(record);
                people.add(p);
            }
        }
        return people;
    }

    public Person getPersonById(int id) {
        try (Session session = driver.session()) {
            StatementResult result = session.run("MATCH(p:Person) WHERE ID(p)={id} "
                    + "OPTIONAL MATCH (p)-[r:WORKS_AT]->(c:Company) RETURN ID(p) AS id, p, c.name",
                Values.parameters("id", id));

            Record record = result.list().get(0);

            return createPersonFromRecord(record);
        }
    }

    public String addPerson(Person person) {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("MATCH(p:Person {email: {email}}) RETURN p.email",
                        Values.parameters("email", person.getEmail()));
                if(!result.list().isEmpty()) {
                    tx.failure();
                    return "Person with this e-mail already exists in a database";
                }
                String statement = "CREATE (p:Person {firstName: {firstName}, lastName: {lastName}, email: {email}}) " +
                        "WITH p " +
                        "MATCH (c:Company {name:{compName}}) "+
                        "MERGE (p)-[r:WORKS_AT]->(c)";
                tx.run(statement, Values.parameters(
                        "firstName", person.getFirstName(),
                        "lastName", person.getLastName(),
                        "email", person.getEmail(),
                        "compName", person.getCompName()));
                tx.success();
            }
        }
        return "";
    }

    public String editPerson(Person person) {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("MATCH(p:Person {email: {email}}) WHERE ID(p)<>{id} RETURN p.email",
                    Values.parameters("email", person.getEmail(),
                        "id", person.getId()));
                if(!result.list().isEmpty()) {
                    tx.failure();
                    return "Person with this e-mail already exists in a database";
                }

                String statement = "MATCH (p:Person) WHERE ID(p)={id} "
                    + "SET p = { firstName: {firstName}, lastName: {lastName}, email: {email} } "
                    + "WITH p "
                    + "OPTIONAL MATCH (p)-[old:WORKS_AT]->(c1:Company) "
                    + "DELETE old "
                    + "WITH p "
                    + "MATCH (c2:Company { name: {compName} }) "
                    + "MERGE (p)-[new:WORKS_AT]->(c2)";

                tx.run(statement, Values.parameters(
                    "id", person.getId(),
                    "firstName", person.getFirstName(),
                    "lastName", person.getLastName(),
                    "email", person.getEmail(),
                    "compName", person.getCompName()));
                tx.success();
            }
        }
        return "";
    }

    public void deletePerson(int id) {
        try (Session session = driver.session()) {
            session.run("MATCH(p:Person) WHERE ID(p)={id} DETACH DELETE p",
                Values.parameters("id", id));
        }
    }

    private Person createPersonFromRecord(Record record) {
        Node person = record.get("p").asNode();
        Person p = new Person();
        p.setId(record.get("id").asInt());
        p.setFirstName(person.get("firstName").asString());
        p.setLastName(person.get("lastName").asString());
        p.setEmail(person.get("email").asString());
        p.setCompName(record.get("c.name").isNull() ? "" : record.get("c.name").asString());
        return p;
    }
}
