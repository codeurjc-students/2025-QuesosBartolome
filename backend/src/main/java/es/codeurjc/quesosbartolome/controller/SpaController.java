package es.codeurjc.quesosbartolome.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SpaController {

    @GetMapping(value = { "/", "/**/{path:[^\\.]*}" })
    public String forward(@PathVariable(required = false) String path) {
        return "forward:/index.html";
    }
}
