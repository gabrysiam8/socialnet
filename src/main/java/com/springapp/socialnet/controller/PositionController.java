package com.springapp.socialnet.controller;

import com.springapp.socialnet.model.Position;
import com.springapp.socialnet.service.PositionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/position")
public class PositionController {

    private final PositionService service;

    public PositionController(PositionService service) {
        this.service = service;
    }

    @GetMapping
    public String positionForm(@RequestParam("compId") int compId, Model model) {
        model.addAttribute("compId", compId);
        model.addAttribute("position", new Position());
        return "positionForm";
    }

    @PostMapping
    public String addPositionNode(@RequestParam("compId") int compId, @ModelAttribute Position position, Model model) {
        service.addPosition(compId, position);
        return "redirect:/company/all";
    }
}
