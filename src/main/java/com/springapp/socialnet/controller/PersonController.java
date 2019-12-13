package com.springapp.socialnet.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.springapp.socialnet.model.Company;
import com.springapp.socialnet.model.Person;
import com.springapp.socialnet.service.CompanyService;
import com.springapp.socialnet.service.PersonService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PersonController {

    private final PersonService service;
    private final CompanyService companyService;

    public PersonController(PersonService service, CompanyService companyService) {
        this.service = service;
        this.companyService = companyService;
    }

    @GetMapping("/person")
    public String personForm(@RequestParam(name = "id", required = false) Integer id, Model model) {
        List<String> compNames = companyService.getAllCompanies()
                .stream()
                .map(Company::getName)
                .collect(Collectors.toList());
        model.addAttribute("compNames", compNames);
        Optional.ofNullable(id)
                .ifPresentOrElse(personId -> model.addAttribute("person", service.getPersonById(personId)),
                    () -> model.addAttribute("person", new Person()));
        model.addAttribute("message", "");
        return "personForm";
    }

    @GetMapping
    public String getAllPersonNodes(Model model) {
        Set<Person> people = service.getAllPeople();
        model.addAttribute("people", people);
        return "index";
    }

    @PostMapping("/person/new")
    public String addPersonNode(@ModelAttribute Person person, Model model) {
        String msg = service.addPerson(person);
        if(!msg.isEmpty()) {
            model.addAttribute("message", msg);
            return "personForm";
        }
        return "redirect:/";
    }

    @PostMapping("/person/edit")
    public String editPersonNode(@ModelAttribute Person person, Model model) {
        String msg = service.editPerson(person);
        if(!msg.isEmpty()) {
            model.addAttribute("message", msg);
            return "personForm";
        }
        return "redirect:/";
    }

    @GetMapping("/person/delete/{id}")
    public String deletePersonNode(@PathVariable int id) {
        service.deletePerson(id);
        return "redirect:/";
    }
}
