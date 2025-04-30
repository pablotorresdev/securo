package com.mb.conitrack.controller.maestro;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/user1")
    public String user1Page() {
        return "user1"; //.html
    }

    @GetMapping("/user2")
    public String user2Page() {
        return "user2"; //.html
    }

}

