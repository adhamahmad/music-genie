package com.example.musicGenie.auth;

import com.example.musicGenie.enums.Role;
import org.springframework.security.oauth2.core.user.OAuth2User;


import com.example.musicGenie.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.*;

@RequiredArgsConstructor
public class OAuth2UserWithRole implements OAuth2User, Serializable {

    private final OAuth2User oauth2User;
    private final Collection<GrantedAuthority> authorities;

    public OAuth2UserWithRole(OAuth2User oauth2User, Role role) {
        this.oauth2User = oauth2User;
        // Spring Security expects "ROLE_" prefix for hasRole() to work
        this.authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public String getName() {
        return oauth2User.getName();
    }

    public Role getRole() {
        // Extract the role from the authorities
        return authorities.stream()
                          .map(GrantedAuthority::getAuthority)
                          .filter(authority -> authority.startsWith("ROLE_"))
                          .map(authority -> authority.substring(5)) // Remove "ROLE_" prefix
                          .map(Role::valueOf)
                          .findFirst()
                          .orElse(Role.USER); // Default role if none found
    }

}