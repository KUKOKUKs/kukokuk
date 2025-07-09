package com.kukokuk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

  @GetMapping("/")
  public String home() {
    System.out.println("안녕");
    System.out.println("hello");

    return "hello";
  }
}
