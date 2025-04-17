package com.cisco.connector.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.SupervisorStrategy;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

import com.cisco.connector.message.Command;
import com.cisco.connector.message.request.DiscoveryMessage;
import com.cisco.connector.message.request.FileMessage;
import com.cisco.connector.message.response.DiscoveredDeviceMessage;
import com.cisco.connector.message.response.ParsedSeedfileMessage;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MainDiscoveryActor extends AbstractBehavior<Command> {

    private int totalDevices;
    private int receivedResponses;
    private List<DiscoveredDeviceMessage> discoveredDevices = new ArrayList<>();

    public static class Trigger implements Command {
        private final String message;

        public Trigger(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private List<String> ipAddresses;

    public MainDiscoveryActor(ActorContext<Command> context) {
        super(context);
        this.receivedResponses = 0;
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(MainDiscoveryActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        System.out.println("Received message in MainActor: " + getContext().getSelf().path());
        return newReceiveBuilder()
                .onMessage(Trigger.class, this::onTrigger)
                .onMessage(ParsedSeedfileMessage.class, this::onValidationComplete)
                .onMessage(DiscoveredDeviceMessage.class, this::onDiscoveryCompleted)
                .build();
    }

    private Behavior<Command> onTrigger(Trigger trigger) {
        System.out.println("Received trigger: " + trigger.getMessage());
        // Start the validation process
        ActorRef<Command> validationActor = getContext().spawn(ValidationActor.create(getContext().getSelf()), "validationActor");
        validationActor.tell(new FileMessage(trigger.getMessage()));
        return this;
    }

    private Behavior<Command> onValidationComplete(ParsedSeedfileMessage message) {
        if (Objects.isNull(message.getSeedfile())) {
            System.out.println("Validation failed.");
            return Behaviors.stopped();
        }

        getContext().spawn(StoreOriginalContentActor.create(), "storeOriginalContentActor")
                .tell(new FileMessage(message.getOriginalContent()));

        this.ipAddresses = message.getSeedfile().getIpAddresses();
        this.totalDevices = ipAddresses.size();

        for (String ipAddress : ipAddresses) {
            // Create a new DiscoveryActor for each IP address
            Behavior<Command> behavior = DiscoveryActor.create(getContext().getSelf());
            Behaviors.supervise(behavior)
                    .onFailure(JsonProcessingException.class, SupervisorStrategy.stop());
            ActorRef<Command> discoveryActor = getContext().spawn(behavior, "discoveryActor:" + ipAddress.replace(".", "_"));
            discoveryActor.tell(new DiscoveryMessage(ipAddress));
        }
        return this;
    }

    private Behavior<Command> onDiscoveryCompleted(DiscoveredDeviceMessage message) throws InterruptedException {
        System.out.println("Received DiscoveredDeviceMessage: " + message.getIpAddress());
        discoveredDevices.add(message);
        receivedResponses++;
        if (receivedResponses == totalDevices) {
            System.out.println("All devices discovered: " + discoveredDevices);
            System.out.println("Tree: " + getContext().getSystem().printTree());
            Thread.sleep(5000);
            return Behaviors.stopped();
        }
        System.out.println("Tree: " + getContext().getSystem().printTree());
        return this;
    }
}
