package com.cisco.connector.actor;

import java.io.IOException;
import java.net.URI;

import org.apache.pekko.actor.typed.ActorRef;
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
import com.cisco.connector.message.request.DiscoveryMessage;
import com.cisco.connector.message.response.DiscoveredDeviceMessage;

public class DiscoveryActor extends AbstractBehavior<Command> {
    public final ActorRef<Command> replyTo;

    public DiscoveryActor(ActorContext<Command> context, ActorRef<Command> replyTo) {
        super(context);
        this.replyTo = replyTo;
    }

    public static Behavior<Command> create(ActorRef<Command> replyTo) {
        return Behaviors.setup(context -> new DiscoveryActor(context, replyTo));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(DiscoveryMessage.class, this::onDiscoveryCommand)
                .build();
    }

    private Behavior<Command> onDiscoveryCommand(DiscoveryMessage discoveryMessage) {
        try (HttpClient httpClient = DefaultHttpClient.builder().uri(URI.create("http://127.0.0.1:8888")).build()) {
            Publisher<HttpResponse<String>> exchange = httpClient.exchange("/discover", String.class);
            Mono.from(exchange)
                    .map(HttpResponse::getBody)
                    .subscribe(response -> {
                        System.out.println("Response from /discover: " + response);
                        DiscoveredDeviceMessage discoveredDeviceMessage = new DiscoveredDeviceMessage(discoveryMessage.getIpAddress(), "DeviceName", "DeviceType", "UP");
                        replyTo.tell(discoveredDeviceMessage);
                    });
            return Behaviors.stopped();
        }
    }
}
