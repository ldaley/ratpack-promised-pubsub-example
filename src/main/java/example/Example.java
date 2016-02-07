package example;

import ratpack.exec.Promise;
import ratpack.exec.util.Promised;
import ratpack.server.RatpackServer;
import ratpack.stream.Streams;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;
import static ratpack.func.Function.identity;
import static ratpack.sse.ServerSentEvents.serverSentEvents;

public class Example {
  public static void main(String[] args) throws Exception {
    AtomicReference<Promised<String>> ref = new AtomicReference<>(new Promised<>());
    RatpackServer.start(s -> s
        .handlers(c -> c
            .get("pub/:val", ctx -> {
              String val = ctx.getPathTokens().get("val");
              ref.getAndSet(new Promised<>()).success(val);
              ctx.render("published: " + val);
            })
            .get("sub", ctx -> {
              List<Promise<String>> promises = asList(
                  Promise.value("subscribed"),
                  ref.get().promise().map("received "::concat)
              );
              ctx.render(serverSentEvents(
                  Streams.publish(promises).flatMap(identity()),
                  event -> event.event(identity())
              ));
            })
        )
    );
  }
}
