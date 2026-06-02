package com.facoffee.financeService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hi")
    public String dizerOla() {
        return "Hi!";
    }

}
