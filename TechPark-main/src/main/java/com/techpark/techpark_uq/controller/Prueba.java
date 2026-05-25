package com.techpark.techpark_uq.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Prueba {

    @GetMapping("/vista")
    public String getMethodName(@RequestParam(name="param", required=false) String param) {
        System.out.println("Parametro: " + param);
        return "vista"; 
    }
}