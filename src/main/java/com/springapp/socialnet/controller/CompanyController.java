package com.springapp.socialnet.controller;

import com.springapp.socialnet.model.Company;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/company")
public class CompanyController {

    private final Driver driver;

    public CompanyController(Driver driver) {
        this.driver = driver;
    }

    @GetMapping
    public String companyForm(Model model) {
        model.addAttribute("company", new Company());
        model.addAttribute("message", "");
        return "companyForm";
    }

    @GetMapping("/all")
    public String getAllCompanyNodes(Model model) {
        Set<Company> companies = new HashSet<>();

        try (Session session = driver.session()) {
            StatementResult result = session.run("MATCH(c:Company) RETURN ID(c) AS id, c");

            for (Record record: result.list()) {
                Node company = record.get("c").asNode();

                Company c = new Company();
                c.setId(record.get("id").asInt());
                c.setName(company.get("name").asString());
                c.setSize(company.get("size").asInt());
                companies.add(c);
            }
            model.addAttribute("companies", companies);
        }
        return "companyList";
    }


    @PostMapping
    public String addCompanyNode(@ModelAttribute Company company, Model model) {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("MATCH(c:Company {name: {name}}) RETURN c.name",
                        Values.parameters("name", company.getName()));
                if(!result.list().isEmpty()) {
                    tx.failure();
                    model.addAttribute("message", "Company with this name already exists in a database");
                    return "companyForm";
                }
                String statement = "CREATE (c:Company {name: {name}, size: {size}})";
                tx.run(statement, Values.parameters(
                        "name", company.getName(),
                        "size", company.getSize()));
                tx.success();
            }
        }
        return "redirect:/company/all";
    }
}
