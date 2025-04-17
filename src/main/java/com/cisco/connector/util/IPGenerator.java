package com.cisco.connector.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IPGenerator {
    public static List<String> generateRandomIPs(int count) {
        List<String> ipList = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String ip = generateRandomIP(random);
            ipList.add(ip);
        }

        return ipList;
    }

    private static String generateRandomIP(Random random) {
        // Generate each octet of the IP address
        int octet1 = random.nextInt(256);
        int octet2 = random.nextInt(256);
        int octet3 = random.nextInt(256);
        int octet4 = random.nextInt(256);

        // Construct the IP address
        return octet1 + "." + octet2 + "." + octet3 + "." + octet4;
    }
}
