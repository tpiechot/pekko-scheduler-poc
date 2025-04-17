package com.cisco.connector.actor;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.PostStop;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

import com.cisco.connector.message.Command;
import com.cisco.connector.message.request.FileMessage;
import com.cisco.connector.message.response.ParsedSeedfileMessage;
import com.cisco.connector.model.Seedfile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ValidationActor extends AbstractBehavior<Command> {
    private final ActorRef<Command> replyTo;
    private final ObjectMapper objectMapper;
    FileMessage fileMessage;

    public static Behavior<Command> create(ActorRef<Command> replyTo) {
        return Behaviors.setup(context -> new ValidationActor(context, replyTo));
    }

    public ValidationActor(ActorContext<Command> context, ActorRef<Command> replyTo) {
        super(context);
        this.replyTo = replyTo;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(FileMessage.class, this::onFileMessage)
                .build();
    }

    private Behavior<Command> onFileMessage(FileMessage fileMessage) {
        this.fileMessage = fileMessage;
        System.out.println("Received message in ValidationActor: " + getContext().getSelf().path());
        Seedfile seedfile = null;
        try {
            seedfile = objectMapper.readValue(fileMessage.getInput(), Seedfile.class);
        } catch (JsonProcessingException e) {
            replyTo.tell(new ParsedSeedfileMessage(null, fileMessage.getInput()));
        }
        replyTo.tell(new ParsedSeedfileMessage(seedfile, fileMessage.getInput()));
        return Behaviors.stopped();
    }
}
