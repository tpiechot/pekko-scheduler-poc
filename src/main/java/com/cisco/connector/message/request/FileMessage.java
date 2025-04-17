package com.cisco.connector.message.request;

import com.cisco.connector.message.Command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileMessage implements Command {
    private String input;
}
