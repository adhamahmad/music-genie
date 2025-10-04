package com.example.musicGenie.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {

    @GetMapping("/user/login")
    @Operation(
            summary = "Login as a MusicGenie user",
            description = "Requires authentication using Spotify. Make sure you are logged in with a the musicGenie Spotify account found in the readme file."
    )
    public String testLogin(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "Not logged in";
        return "Logged in as: " + principal.getAttribute("display_name") +
                " | Email: " + principal.getAttribute("email");
    }

    @GetMapping("/admin/login")
    @Operation(
            summary = "Admin login check",
            description = "Requires Spotify authentication and only works with accounts that have ADMIN privileges."
    )
    public String adminTestLogin() {
        return "Admin";
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Public endpoint. Does not require authentication."
    )
    public String health() {
        return "OK";
    }

}