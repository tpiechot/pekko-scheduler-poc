package com.cisco.connector.service;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import com.cisco.connector.actor.MainDiscoveryActor;
import com.cisco.connector.message.Command;

public class DiscoveryService {

    public void discoverDevices(String seedfile) {
        ActorRef<Command> deviceActor = ActorSystem.create(MainDiscoveryActor.create(), "MainActor");
        deviceActor.tell(new MainDiscoveryActor.Trigger(seedfile));
    }
}
