package example;

import org.junit.Test;
import ratpack.test.MainClassApplicationUnderTest;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ExampleTest {

  @Test
  public void canPubSub() throws Exception {
    MainClassApplicationUnderTest app = new MainClassApplicationUnderTest(Example.class);
    app.test(http -> {
      // Start the app
      http.getApplicationUnderTest().getAddress();

      int numSubscribers = 3;
      Queue<String> responses = new ConcurrentLinkedQueue<>();
      CountDownLatch responded = new CountDownLatch(numSubscribers);

      for (int i = 0; i < numSubscribers; i++) {
        new Thread() {
          @Override
          public void run() {
            responses.add(app.getHttpClient().getText("sub"));
            responded.countDown();
          }
        }.start();
      }

      Thread.sleep(2000); // wait for the subscribers to request

      http.getText("pub/1");
      responded.await(10, TimeUnit.SECONDS);

      assertEquals(numSubscribers, responses.size());

      responses.forEach(response ->
          assertEquals("event: subscribed\n\nevent: received 1\n\n", response)
      );
    });

  }
}
