package com.cisco.connector.actor;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.reactivestreams.Publisher;

import io.micronaut.core.annotation.NonNull;
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

    private Behavior<Command> onDiscoveryCommand(DiscoveryMessage discoveryMessage) throws IOException, InterruptedException {
//        System.out.println("Received message in DiscoveryActor: " + getContext().getSelf().path());
        // Simulate some processing
        String responsePublisher = discoverDeviceAsync();
//        discoverDeviceAsync()
//                .thenApply(result -> {
//                    DiscoveredDeviceMessage discoveredDeviceMessage = new DiscoveredDeviceMessage(discoveryMessage.getIpAddress(), "DeviceName", "DeviceType", "UP");
//                    replyTo.tell(discoveredDeviceMessage);
//                    return null;
//                });
        System.out.println("Thread name: " + Thread.currentThread().getName() + " in discovery actor: " + getContext().getSelf().path());
//        System.out.println("Received IP address: " + discoveryMessage.getIpAddress() + " in actor " + getContext().getSelf().path());

        DiscoveredDeviceMessage discoveredDeviceMessage = new DiscoveredDeviceMessage(discoveryMessage.getIpAddress(), "DeviceName", "DeviceType", "UP");
        replyTo.tell(discoveredDeviceMessage);
        return Behaviors.stopped();
    }

    private String discoverDeviceAsync() {
        HttpClient httpClient = DefaultHttpClient.builder().uri(URI.create("http://127.0.0.1:8888")).build().start();
        Publisher<HttpResponse<String>> exchange = httpClient.exchange("/discover", String.class);
        String response = Mono.from(exchange)
                .map(HttpResponse::getBody)
                .block().get();
        return response;
    }

//    private String discoverDevice() throws IOException, InterruptedException {
//        HttpClient httpClient = HttpClient.newHttpClient();
//        HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder().GET().uri(URI.create("http://127.0.0.1:8888/discover")).build(),
//                HttpResponse.BodyHandlers.ofString());
//        System.out.println("Response from discover endpoint: " + response.body());
//        return response.body();
//    }
}
