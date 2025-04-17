package com.cisco.connector.message.response;

import com.cisco.connector.message.Command;
import com.cisco.connector.model.Seedfile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParsedSeedfileMessage implements Command {
    Seedfile seedfile;
    String originalContent;
}
