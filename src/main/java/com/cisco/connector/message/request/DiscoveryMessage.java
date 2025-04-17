package com.cisco.connector.message.request;

import com.cisco.connector.message.Command;

import lombok.Getter;

@Getter
public class DiscoveryMessage implements Command {

    private final String ipAddress;

    public DiscoveryMessage(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
