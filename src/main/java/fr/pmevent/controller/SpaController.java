package fr.pmevent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping("/{path:[^\\.]*}")
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}