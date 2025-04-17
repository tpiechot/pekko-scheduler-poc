package com.cisco.connector;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.cisco.connector.model.Seedfile;
import com.cisco.connector.service.DiscoveryService;
import com.cisco.connector.util.IPGenerator;

public class Application {
    public static void main(String[] args) throws IOException {
        Seedfile seedfile = new Seedfile(IPGenerator.generateRandomIPs(100), "SSH");
        ObjectMapper objectMapper = new ObjectMapper();
        String seedfileJson = objectMapper.writeValueAsString(seedfile);
        DiscoveryService discoveryService = new DiscoveryService();
        discoveryService.discoverDevices(seedfileJson);
    }
}
