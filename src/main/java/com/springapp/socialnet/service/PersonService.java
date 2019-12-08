package com.springapp.socialnet.service;

import com.springapp.socialnet.model.Person;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
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
            StatementResult result = session.run("MATCH(p:Person) RETURN ID(p) AS id, p");

            for (Record record : result.list()) {
                Node person = record.get("p").asNode();

                Person p = new Person();
                p.setId(record.get("id").asInt());
                p.setFirstName(person.get("firstName").asString());
                p.setLastName(person.get("lastName").asString());
                p.setEmail(person.get("email").asString());
                p.setAge(person.get("age").asInt());
                people.add(p);
            }
        }
        return people;
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
                String statement = "MATCH (c:Company {name:{compName}}) "+
                        "MERGE (p:Person {firstName: {firstName}, lastName: {lastName}, email: {email}, age: {age}}) "+
                        "MERGE (p)-[r:WORKS_AT]->(c)";
                tx.run(statement, Values.parameters(
                        "compName", person.getCompName(),
                        "firstName", person.getFirstName(),
                        "lastName", person.getLastName(),
                        "email", person.getEmail(),
                        "age", person.getAge()));
                tx.success();
            }
        }
        return "";
    }

    public void deletePerson(int id) {
        try (Session session = driver.session()) {
            session.run("MATCH(p:Person) WHERE ID(p)={id} DETACH DELETE p", Values.parameters("id", id));
        }
    }
}
