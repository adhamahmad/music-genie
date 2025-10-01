package com.example.musicGenie.controller.user;

import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {

    @GetMapping("/user/login")
    public String testLogin(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "Not logged in";
        return "Logged in as: " + principal.getAttribute("display_name") +
                " | Email: " + principal.getAttribute("email");
    }

    @GetMapping("/admin/login")
    public String adminTestLogin() {
        return "Admin";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

}