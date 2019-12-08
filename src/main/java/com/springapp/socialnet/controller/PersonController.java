package com.springapp.socialnet.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/person")
public class PersonController {

    private final PersonService service;
    private final CompanyService companyService;

    public PersonController(PersonService service, CompanyService companyService) {
        this.service = service;
        this.companyService = companyService;
    }

    @GetMapping
    public String personForm(Model model) {
        List<String> compNames = companyService.getAllCompanies()
                .stream()
                .map(Company::getName)
                .collect(Collectors.toList());
        model.addAttribute("compNames", compNames);
        model.addAttribute("person", new Person());
        model.addAttribute("message", "");
        return "personForm";
    }

    @GetMapping("/all")
    public String getAllPersonNodes(Model model) {
        Set<Person> people = service.getAllPeople();
        model.addAttribute("people", people);
        return "personList";
    }

    @PostMapping
    public String addPersonNode(@ModelAttribute Person person, Model model) {
        String msg = service.addPerson(person);
        if(!msg.isEmpty()) {
            model.addAttribute("message", msg);
            return "personForm";
        }
        return "redirect:/person/all";
    }

    @GetMapping("/delete/{id}")
    public String deletePersonNode(@PathVariable int id) {
        service.deletePerson(id);
        return "redirect:/person/all";
    }
}
