package ru.tehnobear.essence.util;

public enum Roles {
    ROLE_USER,
    ROLE_ADMIN;

    public static final String ROLE_USER_AUTHORITY = "hasAuthority('ROLE_USER')";
    public static final String ROLE_ADMIN_AUTHORITY = "hasAuthority('ROLE_ADMIN')";
}
