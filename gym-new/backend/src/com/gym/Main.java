package com.gym;

import com.gym.server.GymHttpServer;
import com.gym.utils.DatabaseInitializer;

public class Main {
    public static void main(String[] args) {
        int port = 5000;

        // First, check environment variable
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid PORT environment variable. Using default port 5000.");
            }
        }

        // Then, check command-line argument (overrides environment variable)
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid port argument. Using current port " + port + ".");
            }
        }

        try {
            DatabaseInitializer.initialize();
            GymHttpServer.start(port);
            System.out.println("Gym Management System is running at http://localhost:" + port);
        } catch (Exception e) {
            System.err.println("Server could not start.");
            e.printStackTrace();
        }
    }
}
