package com.unifix.main;
import com.unifix.auth.LoginPage;

public class UniFix {
    public static void main(String[] args) {
        if (FirstTimeSetup.isFirstRun()) {
            new FirstTimeSetup();
        } else {
            new LoginPage();
        }
    }
}