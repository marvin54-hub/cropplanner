package com.cropplanner.model;

/**
 * The three user roles the app supports. FARMER is the default for all
 * existing and newly self-registered users — ADMIN and AGRICULTURAL_ADVISOR
 * are elevated roles that, for now, must be granted by directly updating a
 * user's role (e.g. by an existing admin via the role-management endpoint).
 */
public enum Role {
    ADMIN,
    FARMER,
    AGRICULTURAL_ADVISOR
}
