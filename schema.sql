-- =============================================
-- Small-Scale Farmer Crop Planner
-- Run this ONCE in MySQL before starting the app
-- Spring Boot JPA will manage the tables after this
-- =============================================

CREATE DATABASE IF NOT EXISTS crop_planner_db;
USE crop_planner_db;

-- Spring Boot JPA (Hibernate) will auto-create the tables
-- This file only creates the database itself.
-- Crops are seeded automatically by DataInitializer.java on startup.
