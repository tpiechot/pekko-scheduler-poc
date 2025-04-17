package com.cisco.connector.actor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

import com.cisco.connector.message.Command;
import com.cisco.connector.message.request.FileMessage;

public class StoreOriginalContentActor extends AbstractBehavior<Command> {

    public static Behavior<Command> create() {
        return Behaviors.setup(StoreOriginalContentActor::new);
    }

    public StoreOriginalContentActor(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(FileMessage.class, this::onFileMessage)
                .build();
    }

    private Behavior<Command> onFileMessage(FileMessage fileMessage) {
        return Behaviors.setup(context -> {
            System.out.println("Received message in StoreOriginalContentActor: " + getContext().getSelf().path());
            String originalContent = fileMessage.getInput();
            System.out.println("Thread name: " + Thread.currentThread().getName());
            HttpClient httpClient = HttpClient.newHttpClient();
            httpClient.sendAsync(HttpRequest.newBuilder().GET().uri(URI.create("http://127.0.0.1:8888/wait")).build(),
                    HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response ->
                        // Print the response body
                        System.out.println("Response from wait endpoint: " + response.body())
                    );
            System.out.println("Saving original content: " + originalContent + ", Actor: " + getContext().getSelf().path());
            return Behaviors.stopped();
        });
    }
}
