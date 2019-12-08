package com.springapp.socialnet.controller;

import com.springapp.socialnet.model.Company;
import com.springapp.socialnet.model.ExperienceLevel;
import com.springapp.socialnet.model.Position;
import org.neo4j.driver.v1.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/position")
public class PositionController {

    private final Driver driver;

    public PositionController(Driver driver) {
        this.driver = driver;
    }

    @GetMapping
    public String positionForm(@RequestParam("company") int company, Model model) {
        model.addAttribute("levels", ExperienceLevel.values());
        try (Session session = driver.session()) {
            StatementResult result = session.run("MATCH(c:Company) WHERE id(c)={id} RETURN c.name AS name",
                    Values.parameters("id", company));

            Position position = new Position();
            position.setCompName(result.single().get("name").asString());
            model.addAttribute("position", position);
        }

        return "positionForm";
    }

    @PostMapping
    public String addPositionNode(@ModelAttribute Position position, Model model) {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {

                String statement = "MERGE (c:Company {name:{compName}})-[:LOOK_FOR]->(p:Position {name: {posName}, expLevel: {expLevel}})";
                tx.run(statement, Values.parameters(
                        "compName", position.getCompName(),
                        "posName", position.getName(),
                        "expLevel", position.getExpLevel()));
                tx.success();
            }
        }
        return "redirect:/company/all";
    }
}
