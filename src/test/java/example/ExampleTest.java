package example;

import org.junit.Test;
import ratpack.test.MainClassApplicationUnderTest;
import ratpack.test.http.TestHttpClient;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ExampleTest {

  @Test
  public void canPubSub() throws Exception {
    new MainClassApplicationUnderTest(Example.class).test(http -> {
      // Start the app
      http.getApplicationUnderTest().getAddress();

      int numSubscribers = 3;
      Queue<String> responses = new ConcurrentLinkedQueue<>();
      CountDownLatch responded = new CountDownLatch(numSubscribers);

      for (int i = 0; i < numSubscribers; i++) {
        new Thread() {
          @Override
          public void run() {
            responses.add(TestHttpClient.testHttpClient(http.getApplicationUnderTest()).getText("sub"));
            responded.countDown();
          }
        }.start();
      }

      Thread.sleep(1000); // wait for the subscribers to request

      http.getText("pub/1");
      responded.await(10, TimeUnit.SECONDS);

      assertEquals(numSubscribers, responses.size());

      responses.forEach(response ->
          assertEquals("event: subscribed\n\nevent: received 1\n\n", response)
      );
    });

  }
}
