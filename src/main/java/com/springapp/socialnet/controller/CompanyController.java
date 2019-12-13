package com.springapp.socialnet.controller;

import com.springapp.socialnet.model.Company;
import com.springapp.socialnet.service.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    public String companyForm(@RequestParam(name = "id", required = false) Integer id, Model model) {
        model.addAttribute("company", new Company());
        Optional.ofNullable(id)
                .ifPresentOrElse(compId -> model.addAttribute("company", service.getCompanyById(compId)),
                    () -> model.addAttribute("company", new Company()));
        model.addAttribute("message", "");
        return "companyForm";
    }

    @GetMapping("/all")
    public String getAllCompanyNodes(Model model) {
        Set<Company> companies = service.getAllCompanies();
        model.addAttribute("companies", companies);
        return "companyList";
    }

    @PostMapping("/new")
    public String addCompanyNode(@ModelAttribute Company company, Model model) {
        String msg = service.addCompany(company);
        if(!msg.isEmpty()) {
            model.addAttribute("message", msg);
            return "companyForm";
        }
        return "redirect:/company/all";
    }

    @PostMapping("/edit")
    public String editCompanyNode(@ModelAttribute Company company, Model model) {
        String msg = service.editCompany(company);
        if(!msg.isEmpty()) {
            model.addAttribute("message", msg);
            return "companyForm";
        }
        return "redirect:/company/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteCompanyNode(@PathVariable int id) {
        service.deleteCompany(id);
        return "redirect:/company/all";
    }
}
