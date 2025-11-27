package com.emijor.user_feed.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("login")
    public String login;

    @JsonProperty("permissions")
    public String[] permissions;

    public User() {}

    public User(String id, String name, String login, String[] permissions) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.permissions = permissions;
    }

    public boolean hasPermission(String permission) {
        if (permissions == null) return false;
        for (String p : permissions) {
            if (p.equals(permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin() {
        return hasPermission("admin");
    }

    public boolean isUser() {
        return hasPermission("user");
    }
}
