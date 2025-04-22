package com.cisco.connector.service;

import java.util.concurrent.TimeUnit;

import org.apache.pekko.actor.Actor;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;

import scala.concurrent.duration.Duration;

import com.cisco.connector.actor.MainDiscoveryActor;
import com.cisco.connector.message.Command;

public class DiscoveryService {

    public void discoverDevices(String seedfile) {

        ActorSystem<Command> system = ActorSystem.create(MainDiscoveryActor.create(), "MainActor");
        ActorRef<Command> mainDiscoveryActor = system;

        system.scheduler().scheduleAtFixedRate(
                Duration.Zero(),
                Duration.create(10, TimeUnit.SECONDS),
                () -> mainDiscoveryActor.tell(new MainDiscoveryActor.Trigger(seedfile)),
                system.executionContext()
        );

        Runtime.getRuntime().addShutdownHook(new Thread(system::terminate));
    }
}
