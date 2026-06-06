package com.gym.server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class GymHttpServer {
    public static void start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        ApiHandler apiHandler = new ApiHandler();

        server.createContext("/api/login", apiHandler::handleLogin);
        server.createContext("/api/members", apiHandler::handleMembers);
        server.createContext("/api/plans", apiHandler::handlePlans);
        server.createContext("/api/database", apiHandler::handleDatabaseTables);
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();
    }
}
