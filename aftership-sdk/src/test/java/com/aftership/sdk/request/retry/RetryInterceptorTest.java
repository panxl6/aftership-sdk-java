package com.aftership.sdk.request.retry;

import com.aftership.sdk.AfterShip;
import com.aftership.sdk.TestUtil;
import com.aftership.sdk.exception.ApiException;
import com.aftership.sdk.exception.RequestException;
import com.aftership.sdk.exception.SdkException;
import com.aftership.sdk.model.AftershipOption;
import com.aftership.sdk.model.tracking.GetTrackingParams;
import com.aftership.sdk.model.tracking.Tracking;
import com.aftership.sdk.utils.JsonUtils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RetryInterceptorTest {

  private MockWebServer mockWebServer;
  private AfterShip aftership;

  @BeforeEach
  public void setup() {
    mockWebServer = new MockWebServer();

    AftershipOption option = new AftershipOption();
    option.setRetryCount(3);
    option.setEndpoint(String.format(TestUtil.ENDPOINT_FORMAT, mockWebServer.getPort()));
    aftership = new AfterShip(TestUtil.YOUR_API_KEY, option);
  }

  @AfterEach
  public void teardown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  public void testSuccessfulRequestWithoutRetries() throws IOException, SdkException, RequestException, ApiException {
    mockWebServer.enqueue(TestUtil.createMockResponse()
      .setBody(TestUtil.getJson("endpoint/tracking/GetTrackingResult.json")).setResponseCode(200));

    String query = TestUtil.getJson("endpoint/tracking/GetTrackingParams.json");
    Tracking tracking = aftership.getTrackingEndpoint()
      .getTracking("100", JsonUtils.getGson().fromJson(query, GetTrackingParams.class));

    assertEquals(tracking.getId(), "5b7658cec7c33c0e007de3c5");
  }

  @Test
  public void testRetryOnServerError() throws IOException, SdkException, RequestException, ApiException {
    // first request fail
    mockWebServer.enqueue(new MockResponse().setResponseCode(500));
    // retry
    mockWebServer.enqueue(new MockResponse().setResponseCode(500));
    mockWebServer.enqueue(new MockResponse().setResponseCode(500));
    mockWebServer.enqueue(TestUtil.createMockResponse()
      .setBody(TestUtil.getJson("endpoint/tracking/GetTrackingResult.json")).setResponseCode(200));

    String query = TestUtil.getJson("endpoint/tracking/GetTrackingParams.json");
    Tracking tracking = aftership.getTrackingEndpoint()
      .getTracking("100", JsonUtils.getGson().fromJson(query, GetTrackingParams.class));

    assertEquals(tracking.getId(), "5b7658cec7c33c0e007de3c5");
  }

}
