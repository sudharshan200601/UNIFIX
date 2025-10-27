package com.unifix.utils;

public enum Location {
    // Main Buildings
    UNIVERSITY_BUILDING("University Building"),
    TECHPARK_1("Tech Park 1"),
    TECHPARK_2("Tech Park 2"),
    
    // Hostels
    PAARI_HOSTEL("Hostel - Paari"),
    KAARI_HOSTEL("Hostel - Kaari"),
    OORI_HOSTEL("Hostel - Oori"),
    ADHIYAMAN_HOSTEL("Hostel - Adhiyaman"),
    NELSON_MANDELA_HOSTEL("Hostel - Nelson Mandela"),
    AGASTHIYAR_HOSTEL("Hostel - Agasthiyar"),
    MULLAI_HOSTEL("Hostel - Mullai"),
    MANORANJITHAM_HOSTEL("Hostel - Manoranjitham"),
    AVVAIYAR_HOSTEL("Hostel - Avvaiyar"),
    
    // Food Courts & Common Areas
    VENDHAR_SQUARE("Vendhar Square"),
    JAVA_CANTEEN("Java Canteen"),
    
    // Academic Blocks
    BELL_BLOCK("Bell Block"),
    MBA_BLOCK("MBA Block"),
    BIOTECH_BLOCK("Bio Tech Block"),
    TP_GANESAN_AUDITORIUM("TP Ganesan Auditorium");

    private final String displayName;

    Location(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}