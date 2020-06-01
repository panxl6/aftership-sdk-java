package com.aftership.sdk.endpoint.notification;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import com.aftership.sdk.AfterShip;
import com.aftership.sdk.TestUtil;
import com.aftership.sdk.exception.AftershipException;
import com.aftership.sdk.model.notification.Notification;
import com.aftership.sdk.model.notification.NotificationWrapper;
import com.aftership.sdk.model.tracking.SlugTrackingNumber;
import com.aftership.sdk.utils.JsonUtils;
import com.aftership.sdk.utils.UrlUtils;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class AddNotificationBySlugTest {
  public static MockWebServer server;

  @BeforeAll
  static void setUp() throws IOException {
    server = new MockWebServer();
    server.enqueue(
        TestUtil.createMockResponse()
            .setBody(TestUtil.getJson("endpoint/notification/AddNotificationResult.json")));
    server.start();
  }

  @AfterAll
  static void tearDown() throws IOException {
    server.shutdown();
  }

  @BeforeEach
  void initialize() throws IOException {
    System.out.println("....initialize....");
  }

  @Test
  public void testAddNotification()
      throws IOException, InterruptedException, AftershipException, URISyntaxException {
    AfterShip afterShip = TestUtil.createAfterShip(server);

    System.out.println(">>>>> addNotification(SlugTrackingNumber identifier, Notification notification)");
    SlugTrackingNumber identifier = new SlugTrackingNumber("fedex", "111111111111");
    String requestBody = TestUtil.getJson("endpoint/notification/AddNotificationRequest.json");
    NotificationWrapper wrapper =
        JsonUtils.create().fromJson(requestBody, NotificationWrapper.class);
    Notification notification =
        afterShip.getNotificationEndpoint().addNotification(identifier, wrapper.getNotification());

    Assertions.assertNotNull(notification);
    Assertions.assertEquals(2, notification.getEmails().length, "emails size mismatch.");

    RecordedRequest recordedRequest = server.takeRequest();
    Assertions.assertEquals("POST", recordedRequest.getMethod(), "Method mismatch.");
    Assertions.assertEquals(
        MessageFormat.format(
            "/v4/notifications/{0}/{1}/add", identifier.getSlug(), identifier.getTrackingNumber()),
        new URI(UrlUtils.decode(recordedRequest.getPath())).getPath(),
        "path mismatch.");

    TestUtil.printResponse(afterShip, notification);
    TestUtil.printRequest(recordedRequest);
  }
}
