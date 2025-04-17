package com.cisco.connector.message.response;

import com.cisco.connector.message.Command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiscoveredDeviceMessage implements Command {
    private String ipAddress;
    private String deviceType;
    private String status;
    private String discoveryTime;
}
