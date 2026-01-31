package com.Application.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/status")
    public String healthCheck(){
        return "Hospital Management System is Working ✅ ";
    }
}
