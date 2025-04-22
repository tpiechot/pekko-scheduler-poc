package com.cisco.connector.actor;

import java.net.URI;
import java.net.http.HttpRequest;

import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.reactivestreams.Publisher;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.netty.DefaultHttpClient;
import reactor.core.publisher.Mono;

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
//            System.out.println("Thread name: " + Thread.currentThread().getName());
            try (HttpClient httpClient = DefaultHttpClient.builder().uri(URI.create("http://127.0.0.1:8888")).build()) {
                Publisher<HttpResponse<String>> exchange = httpClient.exchange("/wait", String.class);

                String response = Mono.from(exchange)
                        .map(HttpResponse::getBody)
                        .block().get();

                System.out.println(response);
                System.out.println("Saving original content: " + originalContent + ", Actor: " + getContext().getSelf().path());
                return Behaviors.stopped();
            }
        });
    }
}
