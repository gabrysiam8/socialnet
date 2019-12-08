package com.springapp.socialnet.controller;

import java.util.HashSet;
import java.util.Set;

import com.springapp.socialnet.model.Person;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Node;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/person")
public class PersonController {

    private final Driver driver;

    public PersonController(Driver driver) {
        this.driver = driver;
    }

    @GetMapping
    public String personForm(Model model) {
        model.addAttribute("person", new Person());
        model.addAttribute("message", "");
        return "personForm";
    }

    @GetMapping("/all")
    public String getAllPersonNodes(Model model) {
        Set<Person> people = new HashSet<>();

        try (Session session = driver.session()) {
            StatementResult result = session.run("MATCH(p:Person) RETURN ID(p) AS id, p");

            for (Record record: result.list()) {
                Node person = record.get("p").asNode();

                Person p = new Person();
                p.setId(record.get("id").asInt());
                p.setFirstName(person.get("firstName").asString());
                p.setLastName(person.get("lastName").asString());
                p.setEmail(person.get("email").asString());
                p.setAge(person.get("age").asInt());
                people.add(p);
            }
            model.addAttribute("people", people);
        }
        return "personList";
    }

    @PostMapping
    public String addPersonNode(@ModelAttribute Person person, Model model) {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("MATCH(p:Person {email: {email}}) RETURN p.email",
                    Values.parameters("email", person.getEmail()));
                if(!result.list().isEmpty()) {
                    tx.failure();
                    model.addAttribute("message", "Person with this e-mail already exists in a database");
                    return "personForm";
                }
                String statement = "CREATE (p:Person {firstName: {firstName}, lastName: {lastName}, email: {email}, age: {age}})";
                tx.run(statement, Values.parameters(
                    "firstName", person.getFirstName(),
                    "lastName", person.getLastName(),
                    "email", person.getEmail(),
                    "age", person.getAge()));
                tx.success();
            }
        }
        return "redirect:/person/all";
    }

    @GetMapping("/delete/{id}")
    public String deletePersonNode(@PathVariable int id) {
        try (Session session = driver.session()) {
            session.run("MATCH(p:Person) WHERE ID(p)={id} DELETE p", Values.parameters("id", id));
        }
        return "redirect:/person/all";
    }
}
